package io.github.mtrevisan.mapmatcher.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;


/*
	https://clydedacruz.github.io/openstreetmap-wkt-playground/

	https://overpass-turbo.eu/
[out:json];
area["ISO3166-1"="IT"]->.it;
(
  way["highway"="motorway"](area.it);
  way["highway"="motorway_link"](area.it);
  way["barrier"="toll_booth"](area.it);
);
out geom;
//out body;
//>;
//out skel qt;


== list of buildings (elements.type=node) ==
[out:json];
area[name="Miane"];
//area(id:46539);
(
  nwr(area)["building"];
  nwr(area)["addr:housenumber"];
);
out center;
out tags;
out qt;

== list of streets ==
[out:json];
area["ISO3166-2"="IT-34"]->.region;
area[name="Lozzo di Cadore"]->.municipality;
way(area);
//area(id:46539);
way(area.region)(area.municipality)["highway"~"^(motorway|trunk|primary|secondary|tertiary|unclassified|residential|service|motorway_link|trunk_link|primary_link|secondary_link|tertiary_link)$"];
(._;>;);
out qt;


https://openaddresses.io/

	== con outline ==
	out body;
	>;
	out skel qt;
*/
public class GeographicalDataExtractor{

	private static final Logger LOGGER = LoggerFactory.getLogger(GeographicalDataExtractor.class);

	private static final String OVERPASS_API_URI = "https://overpass-api.de/api/interpreter";

	public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	static{
		JSON_MAPPER.registerModule(new JavaTimeModule());
		JSON_MAPPER.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));

		JSON_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		JSON_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		JSON_MAPPER.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);

		JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		JSON_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}


	public static void main(final String[] args) throws IOException{
		final String stateCode = "IT";
		final StateData stateData = collectData(stateCode);

//		extractBuildingData("Miane");

		//TODO
		System.out.println();
	}

	private static StateData collectData(final String stateCode){
		final StateData stateData = extractStateData(stateCode);
		for(final RegionData region : stateData.regions){
			//collect province IDs
			final int[] provinceIDs = listProvinceIDs(region.regionCode);

			//extract data of each province
			final ProvinceData[] provinceData = new ProvinceData[provinceIDs.length];
			for(int i = 0; i < provinceIDs.length; i ++){
				final int provinceID = provinceIDs[i];
				provinceData[i] = extractProvinceData(provinceID);

				final int[] municipalityIDs = listMunicipalityIDs(provinceData[i].provinceCode);
				final MunicipalityData[] municipalityData = new MunicipalityData[municipalityIDs.length];
				for(int j = 0; j < municipalityIDs.length; j ++){
					municipalityData[j] = extractMunicipalityData(municipalityIDs[j]);

					municipalityData[j].buildings = extractBuildingData(municipalityData[j].municipalityName);
				}

				provinceData[i].municipalities = municipalityData;
			}
			region.provinces = provinceData;
		}
		return stateData;
	}

	private static StateData extractStateData(final String stateCode){
		double minLongitude = Double.NaN;
		double minLatitude = Double.NaN;
		double maxLongitude = Double.NaN;
		double maxLatitude = Double.NaN;

		//collect region IDs
		final int[] regionIDs = listRegionIDs(stateCode);
		//collect boundary of each region
		final RegionData[] regionData = new RegionData[regionIDs.length];
		for(int index = 0; index < regionIDs.length; index ++){
			regionData[index] = extractRegionData(regionIDs[index]);

			//incrementally calculate region of whole state
			for(final Region region : regionData[index].boundaries){
				if(Double.isNaN(minLatitude)){
					minLongitude = region.getMinX();
					minLatitude = region.getMinY();
					maxLongitude = region.getMaxX();
					maxLatitude = region.getMaxY();
				}
				else{
					if(region.getMinX() < minLongitude)
						minLongitude = region.getMinX();
					if(region.getMinY() < minLatitude)
						minLatitude = region.getMinY();
					if(region.getMaxX() > maxLongitude)
						maxLongitude = region.getMaxX();
					if(region.getMaxY() > maxLatitude)
						maxLatitude = region.getMaxY();
				}
			}
		}
		final Region stateBoundary = Region.of(minLongitude, minLatitude, maxLongitude, maxLatitude);

		StateData stateData = new StateData();
		stateData.stateCode = stateCode;
		//TODO
//		dataState.stateName = ;
		stateData.regions = regionData;
		stateData.boundary = stateBoundary;
		return stateData;
	}


	/**
	 *
	 * @param stateCode	ISO3166-1 state code.
	 * @return	The list of ID of regions.
	 */
	private static int[] listRegionIDs(final String stateCode){
		int[] ids = null;
		final String requestBody = "[out:csv(::id)];"
			+ "area[\"ISO3166-1\"=\"" + stateCode + "\"];"
			+ "rel(area)[\"ISO3166-2\"~\"^" + stateCode + "\"][admin_level=4];"
			+ "out body;";
		final DataResponse response = post(OVERPASS_API_URI, requestBody);
		if(response.statusCode == HttpStatus.SC_OK){
			//interpret as CSV
			final int size = countMatches(response.body, '\n') - 1;
			ids = new int[size];
			final StringTokenizer tokenizer = new StringTokenizer(response.body);
			int i = 0;
			//skip "@id" header
			tokenizer.nextToken();
			while(tokenizer.hasMoreTokens())
				ids[i ++] = Integer.parseInt(tokenizer.nextToken());
		}
		return ids;
	}

	private static int countMatches(final String str, final char chr){
		int count = 0;
		for(int i = 0; i < str.length(); i ++)
			if(str.charAt(i) == chr)
				count ++;
		return count;
	}

	private static RegionData extractRegionData(final int id){
		RegionData regionData = null;
		final String requestBody = "[out:json];"
			+ "rel(id:" + id + ");"
			+ "out geom;";
		final DataResponse response = post(OVERPASS_API_URI, requestBody);
		if(response.statusCode == HttpStatus.SC_OK){
			try{
//				final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());

				//interpret as JSON
				final JsonNode data = JSON_MAPPER.readTree(response.body);

				final ArrayNode elements = (ArrayNode)data.path("elements");
				final int elementSize = elements.size();
				if(elementSize != 1)
					throw new UnsupportedOperationException("There are more than one element for id " + id + ", what should be done?");

				regionData = new RegionData();
				regionData.boundaries = new Region[elementSize];

				for(int elementIndex = 0; elementIndex < elementSize; elementIndex ++){
					final JsonNode element = elements.get(elementIndex);

					final JsonNode tags = element.path("tags");
					final String regionCode = tags.path("ISO3166-2").asText(null);
					final String regionName = tags.path("name").asText(null);
					final JsonNode bounds = element.path("bounds");
					final double minLongitude = bounds.path("minlon").asDouble(Double.NaN);
					final double minLatitude = bounds.path("minlat").asDouble(Double.NaN);
					final double maxLongitude = bounds.path("maxlon").asDouble(Double.NaN);
					final double maxLatitude = bounds.path("maxlat").asDouble(Double.NaN);

					regionData.regionCode = regionCode;
					regionData.regionName = regionName;
					regionData.boundaries[elementIndex] = Region.of(minLongitude, minLatitude, maxLongitude, maxLatitude);
				}
			}
			catch(final JsonProcessingException jpe){
				LOGGER.error(jpe.getMessage(), jpe);
			}
		}
		return regionData;
	}


	/**
	 *
	 * @param regionCode	ISO3166-2 region code.
	 * @return	The list of ID of provinces.
	 */
	private static int[] listProvinceIDs(final String regionCode){
		int[] ids = null;
		final String requestBody = "[out:csv(::id)];"
			+ "area[\"ISO3166-2\"=\"" + regionCode + "\"];"
			+ "rel(area)[admin_level=6];"
			+ "out body;";
		final DataResponse response = post(OVERPASS_API_URI, requestBody);
		if(response.statusCode == HttpStatus.SC_OK){
			//interpret as CSV
			final int size = countMatches(response.body, '\n') - 1;
			ids = new int[size];
			final StringTokenizer tokenizer = new StringTokenizer(response.body);
			int i = 0;
			//skip "@id" header
			tokenizer.nextToken();
			while(tokenizer.hasMoreTokens())
				ids[i ++] = Integer.parseInt(tokenizer.nextToken());
		}
		return ids;
	}

	private static ProvinceData extractProvinceData(final int id){
		ProvinceData provinceData = null;
		final String requestBody = "[out:json];"
			+ "rel(id:" + id + ");"
			+ "out geom;";
		final DataResponse response = post(OVERPASS_API_URI, requestBody);
		if(response.statusCode == HttpStatus.SC_OK){
			try{
				//interpret as JSON
				final JsonNode data = JSON_MAPPER.readTree(response.body);

				final ArrayNode elements = (ArrayNode)data.path("elements");
				final int elementSize = elements.size();
				if(elementSize != 1)
					throw new UnsupportedOperationException("There are more than one element for id " + id + ", what should be done?");

				provinceData = new ProvinceData();
				provinceData.boundaries = new Region[elementSize];

				for(int elementIndex = 0; elementIndex < elementSize; elementIndex ++){
					final JsonNode element = elements.get(elementIndex);

					final JsonNode tags = element.path("tags");
					final String provinceCode = tags.path("ISO3166-2").asText(null);
					final String provinceName = tags.path("name").asText(null);
					final JsonNode bounds = element.path("bounds");
					final double minLongitude = bounds.path("minlon").asDouble(Double.NaN);
					final double minLatitude = bounds.path("minlat").asDouble(Double.NaN);
					final double maxLongitude = bounds.path("maxlon").asDouble(Double.NaN);
					final double maxLatitude = bounds.path("maxlat").asDouble(Double.NaN);

					provinceData.provinceCode = provinceCode;
					provinceData.provinceName = provinceName;
					provinceData.boundaries[elementIndex] = Region.of(minLongitude, minLatitude, maxLongitude, maxLatitude);
				}
			}
			catch(final JsonProcessingException jpe){
				LOGGER.error(jpe.getMessage(), jpe);
			}
		}
		return provinceData;
	}


	/**
	 *
	 * @param provinceCode	ISO3166-2 province code.
	 * @return	The list of ID of municipalities.
	 */
	private static int[] listMunicipalityIDs(final String provinceCode){
		int[] ids = null;
		final String requestBody = "[out:csv(::id)];"
			+ "area[\"ISO3166-2\"=\"" + provinceCode + "\"];"
			+ "rel(area)[admin_level=8];"
			+ "out body;";
		final DataResponse response = post(OVERPASS_API_URI, requestBody);
		if(response.statusCode == HttpStatus.SC_OK){
			//interpret as CSV
			final int size = countMatches(response.body, '\n') - 1;
			ids = new int[size];
			final StringTokenizer tokenizer = new StringTokenizer(response.body);
			int i = 0;
			//skip "@id" header
			tokenizer.nextToken();
			while(tokenizer.hasMoreTokens())
				ids[i ++] = Integer.parseInt(tokenizer.nextToken());
		}
		return ids;
	}

	private static MunicipalityData extractMunicipalityData(final int id){
		MunicipalityData municipalityData = null;
		final String requestBody = "[out:json];"
			+ "rel(id:" + id + ");"
			+ "out geom;";
		final DataResponse response = post(OVERPASS_API_URI, requestBody);
		if(response.statusCode == HttpStatus.SC_OK){
			try{
//				final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());

				//interpret as JSON
				final JsonNode data = JSON_MAPPER.readTree(response.body);

				final ArrayNode elements = (ArrayNode)data.path("elements");
				final int elementSize = elements.size();
				if(elementSize != 1)
					throw new UnsupportedOperationException("There are more than one element for id " + id + ", what should be done?");

				municipalityData = new MunicipalityData();
				municipalityData.boundaries = new Region[elementSize];

				for(int elementIndex = 0; elementIndex < elementSize; elementIndex ++){
					final JsonNode element = elements.get(elementIndex);

					final JsonNode tags = element.path("tags");
					final String municipalityName = tags.path("name").asText(null);
					final JsonNode bounds = element.path("bounds");
					final double minLongitude = bounds.path("minlon").asDouble(Double.NaN);
					final double minLatitude = bounds.path("minlat").asDouble(Double.NaN);
					final double maxLongitude = bounds.path("maxlon").asDouble(Double.NaN);
					final double maxLatitude = bounds.path("maxlat").asDouble(Double.NaN);

					municipalityData.municipalityName = municipalityName;
					municipalityData.boundaries[elementIndex] = Region.of(minLongitude, minLatitude, maxLongitude, maxLatitude);
				}
			}
			catch(final JsonProcessingException jpe){
				LOGGER.error(jpe.getMessage(), jpe);
			}
		}
		return municipalityData;
	}

	private static BuildingData[] extractBuildingData(final String municipalityName){
		BuildingData[] buildingData = null;
		final String requestBody = "[out:json];"
			+ "area[name=\"" + municipalityName + "\"];"
			+ "("
			+ "nwr(area)[\"building\"];"
			+ "nwr(area)[\"addr:housenumber\"];"
			+ ");"
			+ "out center;"
			+ "out tags;"
			+ "out qt;";
		final DataResponse response = post(OVERPASS_API_URI, requestBody);
		if(response.statusCode == HttpStatus.SC_OK){
			try{
//				final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());

				//interpret as JSON
				final JsonNode data = JSON_MAPPER.readTree(response.body);

				final ArrayNode elements = (ArrayNode)data.path("elements");
				final int elementSize = elements.size();
				buildingData = new BuildingData[elementSize];

				for(int elementIndex = 0; elementIndex < elementSize; elementIndex ++){
					final JsonNode element = elements.get(elementIndex);

					final JsonNode center = element.path("center");
					final double longitude = (center.isEmpty()? element: center).path("lon").asDouble(Double.NaN);
					final double latitude = (center.isEmpty()? element: center).path("lat").asDouble(Double.NaN);
					if(Double.isNaN(longitude) || Double.isNaN(latitude))
						continue;

					final JsonNode tags = element.path("tags");
					final String city = tags.path("addr:city").asText(null);
					final String street = tags.path("addr:street").asText(null);
					final String houseNumber = tags.path("addr:housenumber").asText(null);
					final String name = tags.path("name").asText(null);

					final BuildingData building = new BuildingData();
					building.longitude = longitude;
					building.latitude = latitude;
					building.city = city;
					building.municipality = municipalityName;
					building.street = street;
					building.houseNumber = houseNumber;
					building.name = name;
					buildingData[elementIndex] = building;
				}
			}
			catch(final JsonProcessingException jpe){
				LOGGER.error(jpe.getMessage(), jpe);
			}
		}

		final List<BuildingData> cleanedList = new ArrayList<>();
		if(buildingData != null)
			for(final BuildingData buildingDatum : buildingData)
				if(buildingDatum != null)
					cleanedList.add(buildingDatum);
		return cleanedList.toArray(BuildingData[]::new);
	}


	private static final class StateData{
		/** ISO3166-1 */
		private String stateCode;
		private String stateName;
		private RegionData[] regions;
		private Region boundary;
	}

	private static final class RegionData{
		/** ISO3166-2 */
		private String regionCode;
		private String regionName;
		private ProvinceData[] provinces;
		private Region[] boundaries;
	}

	private static final class ProvinceData{
		private String provinceCode;
		private String provinceName;
		private MunicipalityData[] municipalities;
		private Region[] boundaries;
	}

	private static final class MunicipalityData{
		private String municipalityName;
		private Region[] boundaries;
		private BuildingData[] buildings;
	}

	private static final class BuildingData{
		private double longitude;
		private double latitude;
		private String city;
		private String municipality;
		private String street;
		private String houseNumber;

		private String name;
	}


	private static DataResponse post(final String uri, final String requestBody){
		DataResponse dataResponse = null;
		//create HTTP client
		try(final CloseableHttpClient httpClient = HttpClients.createDefault()){
			//create HTTP method
			final HttpPost request = new HttpPost(uri);
			//set payload and content-type
			request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.DEFAULT_TEXT);
			final HttpEntity requestBodyEntity = new StringEntity(requestBody, ContentType.TEXT_PLAIN);
			request.setEntity(requestBodyEntity);

			//execute the method through the HTTP client
			final HttpClientResponseHandler<DataResponse> responseHandler = response -> {
				//read response
				final int statusCode = response.getCode();
				final String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
				return new DataResponse(statusCode, responseBody);
			};
			dataResponse = httpClient.execute(request, responseHandler);

			LOGGER.info("Status code: " + dataResponse.statusCode);
			LOGGER.info("Response body: " + dataResponse.body);
		}
		catch(final IOException ioe){
			LOGGER.error(ioe.getMessage(), ioe);
		}
		return dataResponse;
	}

	private static final class DataResponse{
		private final int statusCode;
		private final String body;

		DataResponse(final int statusCode, final String body){
			this.statusCode = statusCode;
			this.body = body;
		}
	}

}
