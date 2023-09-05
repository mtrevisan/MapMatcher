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


== list of buildings ==
[out:json];
area[name="Lozzo di Cadore"]->.searchArea;
(
  nwr["building"](area.searchArea);
  nwr["addr:housenumber"](area.searchArea);
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
//		final StateData stateData = collectData(stateCode);

		extractMunicipalityData(46539);

		//TODO
	}

	private static StateData collectData(final String stateCode){
		final StateData stateData = extractStateData(stateCode);
		for(final RegionData region : stateData.regions){
			//collect province IDs
			final int[] provinceIDs = listProvinceIDs(region.regionCode);

			//extract data of each province
			final ProvinceData[] provinceData = new ProvinceData[provinceIDs.length];
			for(int index = 0; index < provinceIDs.length; index ++)
				provinceData[index] = extractProvinceData(provinceIDs[index]);
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
					minLongitude = region.getX();
					minLatitude = region.getY();
					maxLongitude = region.getX() + region.getWidth();
					maxLatitude = region.getY() + region.getHeight();
				}
				else{
					if(region.getX() < minLongitude)
						minLongitude = region.getX();
					if(region.getY() < minLatitude)
						minLatitude = region.getY();
					if(region.getX() + region.getWidth() > maxLongitude)
						maxLongitude = region.getX() + region.getWidth();
					if(region.getY() + region.getHeight() > maxLatitude)
						maxLatitude = region.getY() + region.getHeight();
				}
			}
		}
		final Region stateBoundary = Region.of(minLongitude, minLatitude, maxLongitude - minLongitude, maxLatitude - minLatitude);

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
		final String requestBody = "[out:csv(::id)];\n"
			+ "area[\"ISO3166-1\"=\"" + stateCode + "\"];\n"
			+ "rel(area)[\"ISO3166-2\"~\"^" + stateCode + "\"][\"admin_level\"=\"4\"];\n"
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
		final String requestBody = "[out:json];\n"
			+ "rel(id:" + id + ");\n"
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
//				dataRegion.elements = new DataElement[elementSize];
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

					final ArrayNode members = (ArrayNode)element.path("members");
					final int memberSize = members.size();

//					final DataElement dataElement = new DataElement();
//					dataElement.region = Region.of(minLongitude, minLatitude, maxLongitude, maxLatitude);
//					dataElement.members = new DataMember[memberSize];
//
//					for(int memberIndex = 0; memberIndex < memberSize; memberIndex ++){
//						final JsonNode member = members.get(memberIndex);
//
//						if(!member.has("geometry"))
//							continue;
//
//						final ArrayNode geometry = (ArrayNode)member.path("geometry");
//						final String rawRole = member.path("role").asText("").toUpperCase(Locale.ROOT);
//						final DataMember.MemberRole role = DataMember.MemberRole.valueOf(rawRole);
//						final StringJoiner sj = new StringJoiner(",", "LINESTRING(", ")");
//						for(final JsonNode point : geometry){
//							final double longitude = point.path("lon").asDouble(Double.NaN);
//							final double latitude = point.path("lat").asDouble(Double.NaN);
//							sj.add(longitude + " " + latitude);
//						}
//
//						final DataMember dataMember = new DataMember();
//						dataMember.role = role;
//						dataMember.polyline = factory.createPolyline(sj.toString());
//						dataElement.members[memberIndex] = dataMember;
//					}

					regionData.regionCode = regionCode;
					regionData.regionName = regionName;
//					dataRegion.elements[elementIndex] = dataElement;
					regionData.boundaries[elementIndex] = Region.of(minLongitude, minLatitude,
						maxLongitude - minLongitude, maxLatitude - minLatitude);
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
		final String requestBody = "[out:csv(::id)];\n"
			+ "area[\"ISO3166-2\"=\"" + regionCode + "\"];\n"
			+ "rel(area)[\"admin_level\"=\"6\"];\n"
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
		final String requestBody = "[out:json];\n"
			+ "rel(id:" + id + ");\n"
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

				provinceData = new ProvinceData();
//				provinceData.elements = new DataElement[elementSize];
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

					final ArrayNode members = (ArrayNode)element.path("members");
					final int memberSize = members.size();

//					final DataElement dataElement = new DataElement();
//					dataElement.region = Region.of(minLongitude, minLatitude, maxLongitude, maxLatitude);
//					dataElement.members = new DataMember[memberSize];
//
//					for(int memberIndex = 0; memberIndex < memberSize; memberIndex ++){
//						final JsonNode member = members.get(memberIndex);
//
//						if(!member.has("geometry"))
//							continue;
//
//						final ArrayNode geometry = (ArrayNode)member.path("geometry");
//						final String rawRole = member.path("role").asText("").toUpperCase(Locale.ROOT);
//						final DataMember.MemberRole role = DataMember.MemberRole.valueOf(rawRole);
//						final StringJoiner sj = new StringJoiner(",", "LINESTRING(", ")");
//						for(final JsonNode point : geometry){
//							final double longitude = point.path("lon").asDouble(Double.NaN);
//							final double latitude = point.path("lat").asDouble(Double.NaN);
//							sj.add(longitude + " " + latitude);
//						}
//
//						final DataMember dataMember = new DataMember();
//						dataMember.role = role;
//						dataMember.polyline = factory.createPolyline(sj.toString());
//						dataElement.members[memberIndex] = dataMember;
//					}

					provinceData.provinceCode = provinceCode;
					provinceData.provinceName = provinceName;
//					provinceData.elements[elementIndex] = dataElement;
					provinceData.boundaries[elementIndex] = Region.of(minLongitude, minLatitude,
						maxLongitude - minLongitude, maxLatitude - minLatitude);
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
		final String requestBody = "[out:csv(::id)];\n"
			+ "area[\"ISO3166-2\"=\"" + provinceCode + "\"];\n"
			+ "rel(area)[\"admin_level\"=\"8\"];\n"
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
		final String requestBody = "[out:json];\n"
			+ "rel(id:" + id + ");\n"
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
//				municipalityData.elements = new DataElement[elementSize];
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

					final ArrayNode members = (ArrayNode)element.path("members");
					final int memberSize = members.size();

//					final DataElement dataElement = new DataElement();
//					dataElement.region = Region.of(minLongitude, minLatitude, maxLongitude, maxLatitude);
//					dataElement.members = new DataMember[memberSize];
//
//					for(int memberIndex = 0; memberIndex < memberSize; memberIndex ++){
//						final JsonNode member = members.get(memberIndex);
//
//						if(!member.has("geometry"))
//							continue;
//
//						final ArrayNode geometry = (ArrayNode)member.path("geometry");
//						final String rawRole = member.path("role").asText("").toUpperCase(Locale.ROOT);
//						final DataMember.MemberRole role = DataMember.MemberRole.valueOf(rawRole);
//						final StringJoiner sj = new StringJoiner(",", "LINESTRING(", ")");
//						for(final JsonNode point : geometry){
//							final double longitude = point.path("lon").asDouble(Double.NaN);
//							final double latitude = point.path("lat").asDouble(Double.NaN);
//							sj.add(longitude + " " + latitude);
//						}
//
//						final DataMember dataMember = new DataMember();
//						dataMember.role = role;
//						dataMember.polyline = factory.createPolyline(sj.toString());
//						dataElement.members[memberIndex] = dataMember;
//					}

					municipalityData.municipalityName = municipalityName;
//					municipalityData.elements[elementIndex] = dataElement;
					municipalityData.boundaries[elementIndex] = Region.of(minLongitude, minLatitude,
						maxLongitude - minLongitude, maxLatitude - minLatitude);
				}
			}
			catch(final JsonProcessingException jpe){
				LOGGER.error(jpe.getMessage(), jpe);
			}
		}
		return municipalityData;
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
//		private MunicipalityData[] municipalities;
		private Region[] boundaries;
	}

	private static final class MunicipalityData{
		private String municipalityName;
		private Region[] boundaries;
	}

//	private static final class DataElement{
//		private Region region;
//		private DataElementMember[] members;
//	}
//
//	private static final class DataElementMember{
//		private MemberRole role;
//		private Polyline polyline;
//
//		private enum MemberRole{OUTER, INNER}
//	}


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
