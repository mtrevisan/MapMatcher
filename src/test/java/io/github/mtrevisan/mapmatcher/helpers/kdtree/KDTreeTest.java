/**
 * Copyright (c) 2023 Mauro Trevisan
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.mtrevisan.mapmatcher.helpers.SpatialNode;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.QuadTree;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.QuadTreeOptions;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;
import io.github.mtrevisan.mapmatcher.helpers.rtree.RTree;
import io.github.mtrevisan.mapmatcher.helpers.rtree.RTreeOptions;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.topologies.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


class KDTreeTest{

	private static final GeometryFactory FACTORY = new GeometryFactory(new GeoidalCalculator());
	private static final GeometryFactory FACTORY_EUCLIDEAN = new GeometryFactory(new EuclideanCalculator());

	private static final String FILENAME_TOLL_BOOTHS_RAW = "src/test/resources/it.tollBooths.wkt";


	private static final String OVERPASS_API_URI = "https://overpass-api.de/api/interpreter";

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	private static final String FILE_BUILDINGS_DATA = "src/test/resources/buildings/real_data.dat";

	private static final Random RANDOM = new Random(System.currentTimeMillis());


	@SuppressWarnings("CallToPrintStackTrace")
	private static Set<Point> extractPoints(final File file){
		final List<Point> lines = new ArrayList<>();
		try(final BufferedReader br = new BufferedReader(new FileReader(file))){
			String readLine;
			while((readLine = br.readLine()) != null){
				if(!readLine.isEmpty())
					lines.add(parsePoint(readLine));
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return new HashSet<>(lines);
	}

	private static Point parsePoint(final String line){
		int startIndex = 0;
		int separatorIndex = line.indexOf(" ", startIndex + 1);
		int endIndex = line.length();
		return FACTORY.createPoint(
			Double.parseDouble(line.substring(startIndex, separatorIndex)),
			Double.parseDouble(line.substring(separatorIndex + 1, endIndex))
		);
	}


	@Test
	void contains_all(){
		KDTree tree = KDTree.ofDimensions(2);
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		Set<Point> tollBooths = extractPoints(tollBoothsFile);
		for(Point tollBooth : tollBooths)
			tree.insert(tollBooth);

		for(Point tollBooth : tollBooths)
			Assertions.assertTrue(tree.contains(tollBooth));
		Assertions.assertFalse(tree.contains(FACTORY.createPoint(0., 0.)));
	}

	@Test
	void neighbor(){
		KDTree tree = KDTree.ofDimensions(2);
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		Set<Point> tollBooths = extractPoints(tollBoothsFile);
		for(Point tollBooth : tollBooths)
			tree.insert(tollBooth);

		for(Point tollBooth : tollBooths)
			Assertions.assertEquals(tollBooth, tree.nearestNeighbor(tollBooth));
		for(Point tollBooth : tollBooths)
			Assertions.assertEquals(tollBooth, tree.nearestNeighbor(
				tollBooth.getFactory().createPoint(tollBooth.getX() + 1.e-7, tollBooth.getY() + 1.e-7)));
		Assertions.assertEquals(FACTORY.createPoint(7.5925975, 43.8008445),
			tree.nearestNeighbor(FACTORY.createPoint(0., 0.)));
	}

	@Test
	void neighbor_euclidean(){
		KDTree tree = KDTree.ofDimensions(2);
		tree.insert(FACTORY_EUCLIDEAN.createPoint(6., 4.));
		tree.insert(FACTORY_EUCLIDEAN.createPoint(5., 2.));
		tree.insert(FACTORY_EUCLIDEAN.createPoint(8., 6.));
		tree.insert(FACTORY_EUCLIDEAN.createPoint(2., 1.));
		tree.insert(FACTORY_EUCLIDEAN.createPoint(4., 7.));
		tree.insert(FACTORY_EUCLIDEAN.createPoint(9., 3.));
		tree.insert(FACTORY_EUCLIDEAN.createPoint(2., 8.));

		Assertions.assertEquals(FACTORY_EUCLIDEAN.createPoint(8., 6.),
			tree.nearestNeighbor(FACTORY_EUCLIDEAN.createPoint(9., 8.)));
	}

	@Test
	void points_in_range1(){
		KDTree tree = KDTree.ofDimensions(2);
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		Set<Point> tollBooths = extractPoints(tollBoothsFile);
		for(Point tollBooth : tollBooths)
			tree.insert(tollBooth);

		Collection<Point> points = tree.query(FACTORY.createPoint(12.1, 45.5),
			FACTORY.createPoint(12.5, 45.9));
		Assertions.assertEquals(37, points.size());

		points = tree.query(FACTORY.createPoint(7.5925975, 43.8008445),
			FACTORY.createPoint(7.5925975, 43.8008445));
		Assertions.assertEquals(1, points.size());
	}

	@Test
	void points_in_range2(){
		KDTree tree = KDTree.ofDimensions(2);
		tree.insert(FACTORY_EUCLIDEAN.createPoint(6., 4.));
		tree.insert(FACTORY_EUCLIDEAN.createPoint(5., 2.));
		tree.insert(FACTORY_EUCLIDEAN.createPoint(8., 6.));
		tree.insert(FACTORY_EUCLIDEAN.createPoint(2., 1.));
		tree.insert(FACTORY_EUCLIDEAN.createPoint(4., 7.));
		tree.insert(FACTORY_EUCLIDEAN.createPoint(9., 3.));
		tree.insert(FACTORY_EUCLIDEAN.createPoint(2., 8.));

		Collection<Point> points = tree.query(FACTORY_EUCLIDEAN.createPoint(1., 5.),
			FACTORY_EUCLIDEAN.createPoint(5., 9.));
		Assertions.assertEquals(new HashSet<>(Arrays.asList(FACTORY_EUCLIDEAN.createPoint(2., 8.),
				FACTORY_EUCLIDEAN.createPoint(4., 7.))),
			new HashSet<>(points));
	}


	@Test
	void bulk_contains_all(){
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		Set<Point> tollBooths = extractPoints(tollBoothsFile);
		KDTree tree = KDTree.ofPoints(tollBooths);

		for(Point tollBooth : tollBooths)
			Assertions.assertTrue(tree.contains(tollBooth));
		Assertions.assertFalse(tree.contains(FACTORY.createPoint(0., 0.)));
	}

	@Test
	void bulk_neighbor(){
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		Set<Point> tollBooths = extractPoints(tollBoothsFile);
		KDTree tree = KDTree.ofPoints(tollBooths);

		for(Point tollBooth : tollBooths)
			Assertions.assertEquals(tollBooth, tree.nearestNeighbor(tollBooth));
		for(Point tollBooth : tollBooths)
			Assertions.assertEquals(tollBooth, tree.nearestNeighbor(
				tollBooth.getFactory().createPoint(tollBooth.getX() - 1.e-7, tollBooth.getY() + 1.e-7)));
		Assertions.assertEquals(FACTORY.createPoint(7.5946376, 43.8000279),
			tree.nearestNeighbor(FACTORY.createPoint(0., 0.)));
	}

	@Test
	void bulk_neighbor_euclidean(){
		KDTree tree = KDTree.ofPoints(new HashSet<>(Arrays.asList(
			FACTORY_EUCLIDEAN.createPoint(6., 4.),
			FACTORY_EUCLIDEAN.createPoint(5., 2.),
			FACTORY_EUCLIDEAN.createPoint(8., 6.),
			FACTORY_EUCLIDEAN.createPoint(2., 1.),
			FACTORY_EUCLIDEAN.createPoint(4., 7.),
			FACTORY_EUCLIDEAN.createPoint(9., 3.),
			FACTORY_EUCLIDEAN.createPoint(2., 8.)
		)));

		Assertions.assertEquals(FACTORY.createPoint(8., 6.),
			tree.nearestNeighbor(FACTORY.createPoint(9., 8.)));
	}

	@Test
	void bulk_points_in_range1(){
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		Set<Point> tollBooths = extractPoints(tollBoothsFile);
		KDTree tree = KDTree.ofPoints(tollBooths);

		Collection<Point> points = tree.query(FACTORY.createPoint(7.5925975, 43.8008445),
			FACTORY.createPoint(8.5925975, 44.8008445));
		Assertions.assertEquals(52, points.size());

		points = tree.query(FACTORY.createPoint(7.5925975, 43.8008445),
			FACTORY.createPoint(7.5925975, 43.8008445));
		Assertions.assertEquals(1, points.size());
	}

	@Test
	void bulk_points_in_range2(){
		KDTree tree = KDTree.ofPoints(new HashSet<>(Arrays.asList(
			FACTORY_EUCLIDEAN.createPoint(6., 4.),
			FACTORY_EUCLIDEAN.createPoint(5., 2.),
			FACTORY_EUCLIDEAN.createPoint(8., 6.),
			FACTORY_EUCLIDEAN.createPoint(2., 1.),
			FACTORY_EUCLIDEAN.createPoint(4., 7.),
			FACTORY_EUCLIDEAN.createPoint(9., 3.),
			FACTORY_EUCLIDEAN.createPoint(2., 8.)
		)));

		Collection<Point> points = tree.query(FACTORY_EUCLIDEAN.createPoint(1., 5.),
			FACTORY_EUCLIDEAN.createPoint(5., 9.));
		Assertions.assertEquals(new HashSet<>(Arrays.asList(FACTORY_EUCLIDEAN.createPoint(2., 8.),
				FACTORY_EUCLIDEAN.createPoint(4., 7.))),
			new HashSet<>(points));
	}

	@Test
	void stress(){
		KDTree tree = KDTree.ofDimensions(2);
		//create unbalanced tree
		int size = 50_000;
		for(int i = 0; i < size; i ++){
			Point pt = FACTORY_EUCLIDEAN.createPoint(i, 0.);
			tree.insert(pt);
		}

		for(int i = 0; i < size - 10; i ++){
			Point min = FACTORY_EUCLIDEAN.createPoint(i, 0.);
			Point max = FACTORY_EUCLIDEAN.createPoint(i + 10., 1.);
			Assertions.assertEquals(11, tree.query(min, max).size());
		}
		for(int i = size - 10; i < size; i ++){
			Point min = FACTORY_EUCLIDEAN.createPoint(i, 0.);
			Point max = FACTORY_EUCLIDEAN.createPoint(i + 10., 1.);
			Assertions.assertEquals(size - i, tree.query(min, max).size());
		}
	}


	void create_real_data() throws IOException{
		//read buildings
		final BuildingData[] buildings = extractBuildingData("Miane");

		//write buildings to file
		try(final BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_BUILDINGS_DATA, StandardCharsets.UTF_8, true))){
			for(final BuildingData building : buildings){
				final String line = JSON_MAPPER.writeValueAsString(building);
				writer.write(line);
				writer.newLine();
			}
		}
	}

	@Test
	void real_data_r_tree() throws IOException{
		//world boundary:
		final double minLongitude = 12.0383688;
		final double minLatitude = 45.9045485;
		final double maxLongitude = 12.1368368;
		final double maxLatitude = 45.9823375;

		//read buildings from file and store in a tree
		final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		final KDTree tree = KDTree.ofDimensions(2);
		try(final BufferedReader br = Files.newBufferedReader(Paths.get(FILE_BUILDINGS_DATA), StandardCharsets.UTF_8)){
			String line;
			while((line = br.readLine()) != null){
				final BuildingData building = JSON_MAPPER.readValue(line, BuildingData.class);
				tree.insert(factory.createPoint(building.longitude, building.latitude));
			}
		}

		//query tree
		//via Zanchet 19, Miane, Treviso, Italia
		Point neighbor = tree.nearestNeighbor(factory.createPoint(12.09257, 45.94489));
		Assertions.assertEquals(factory.createPoint(12.0925717, 45.9449099), neighbor);

		//query tree
		final int counter = 1_000_000;
		final long start = System.currentTimeMillis();
		for(int i = 0; i < counter; i ++){
			final double longitude = generateRandomCoordinate(minLongitude, maxLongitude);
			final double latitude = generateRandomCoordinate(minLatitude, maxLatitude);
			tree.nearestNeighbor(factory.createPoint(longitude, latitude));
		}
		final long stop = System.currentTimeMillis();
		final int speed = Math.round((float)counter / (stop - start));
		//268 op/ms
		System.out.println("speed: " + speed + " op/ms");
	}

	@Test
	void real_data_r_kd_tree() throws IOException{
		//world boundary:
		final double minLongitude = 12.0383688;
		final double minLatitude = 45.9045485;
		final double maxLongitude = 12.1368368;
		final double maxLatitude = 45.9823375;

		//parameters:
		final int regionDivision = 6;
		final double pointRegionExtentFactor = 2.4;
		final int rTreeMinObjects = 1;
		final int rTreeMaxObjects = 3;

		final double deltaLongitude = (maxLongitude - minLongitude) / regionDivision;
		final double deltaLatitude = (maxLatitude - minLatitude) / regionDivision;
		final RTreeOptions options = new RTreeOptions()
			.withMinObjects(rTreeMinObjects)
			.withMaxObjects(rTreeMaxObjects);
		HybridKDTree<RTreeOptions> tree = HybridKDTree.create(RTree.create(), options);
		double minX = minLongitude;
		double minY = minLatitude;
		for(int i = 0; i < regionDivision; i ++){
			final double prevMinY = minY;
			for(int j = 0; j < regionDivision; j ++){
				tree.insert(Region.of(minX, minY, minX + deltaLongitude, minY + deltaLatitude)
					.withID("slice " + i + "+" + j));

				minY += deltaLatitude;
			}
			minX += deltaLongitude;
			minY = prevMinY;
		}


		//read buildings from file and store in a tree
		final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		final double regionExtentLongitude = deltaLongitude / pointRegionExtentFactor;
		final double regionExtentLatitude = deltaLatitude / pointRegionExtentFactor;
		final Map<Region, SpatialNode> nodes = new HashMap<>();
		try(final BufferedReader br = Files.newBufferedReader(Paths.get(FILE_BUILDINGS_DATA), StandardCharsets.UTF_8)){
			String line;
			while((line = br.readLine()) != null){
				final BuildingData building = JSON_MAPPER.readValue(line, BuildingData.class);
				final Region region = Region.of(building.longitude - regionExtentLongitude, building.latitude - regionExtentLatitude,
					building.longitude + regionExtentLongitude, building.latitude + regionExtentLatitude);
				tree.insert(nodes, region, factory.createPoint(building.longitude, building.latitude));
			}
		}

		//via Zanchet 19, Miane, Treviso, Italia
		final Region region2 = Region.of(12.09257 - regionExtentLongitude, 45.94489 - regionExtentLatitude,
			12.09257 + regionExtentLongitude, 45.94489 + regionExtentLatitude);
		final Point neighbor = tree.nearestNeighbor(nodes, region2, factory.createPoint(12.09257, 45.94489));
		Assertions.assertEquals(factory.createPoint(12.0925717, 45.9449099), neighbor);

		//query tree
		final int counter = 1_000_000;
		final long start = System.currentTimeMillis();
		for(int i = 0; i < counter; i ++){
			final double longitude = generateRandomCoordinate(minLongitude, maxLongitude);
			final double latitude = generateRandomCoordinate(minLatitude, maxLatitude);
			final Region region = Region.of(longitude - regionExtentLongitude, latitude - regionExtentLatitude,
				longitude + regionExtentLongitude, latitude + regionExtentLatitude);
			tree.nearestNeighbor(nodes, region, factory.createPoint(longitude, latitude));
		}
		final long stop = System.currentTimeMillis();
		final int speed = Math.round((float)counter / (stop - start));
		//926 op/ms (3.5x)
		System.out.println("speed: " + speed + " op/ms");
	}

	@Test
	void real_data_quad_kd_tree() throws IOException{
		//world boundary:
		final double minLongitude = 12.0383688;
		final double minLatitude = 45.9045485;
		final double maxLongitude = 12.1368368;
		final double maxLatitude = 45.9823375;

		//parameters:
		final int regionDivision = 6;
		final double pointRegionExtentFactor = 2.4;
		final int quadTreeMaxRegionsPerNode = 3;
		final int quadTreeMaxLevels = 1;

		final double deltaLongitude = (maxLongitude - minLongitude) / regionDivision;
		final double deltaLatitude = (maxLatitude - minLatitude) / regionDivision;
		final QuadTreeOptions options = new QuadTreeOptions()
			.withMaxRegionsPerNode(quadTreeMaxRegionsPerNode)
			.withMaxLevels(quadTreeMaxLevels);
		final QuadTree quadTree = QuadTree.create(Region.of(minLongitude, minLatitude, maxLongitude, maxLatitude));
		HybridKDTree<QuadTreeOptions> tree = HybridKDTree.create(quadTree, options);
		double minX = minLongitude;
		double minY = minLatitude;
		for(int i = 0; i < regionDivision; i ++){
			final double prevMinY = minY;
			for(int j = 0; j < regionDivision; j ++){
				tree.insert(Region.of(minX, minY, minX + deltaLongitude, minY + deltaLatitude)
					.withID("slice " + i + "+" + j));

				minY += deltaLatitude;
			}
			minX += deltaLongitude;
			minY = prevMinY;
		}


		//read buildings from file and store in a tree
		final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		final double regionExtentLongitude = deltaLongitude / pointRegionExtentFactor;
		final double regionExtentLatitude = deltaLatitude / pointRegionExtentFactor;
		final Map<Region, SpatialNode> nodes = new HashMap<>();
		try(final BufferedReader br = Files.newBufferedReader(Paths.get(FILE_BUILDINGS_DATA), StandardCharsets.UTF_8)){
			String line;
			while((line = br.readLine()) != null){
				final BuildingData building = JSON_MAPPER.readValue(line, BuildingData.class);
				final Region region = Region.of(building.longitude - regionExtentLongitude, building.latitude - regionExtentLatitude,
					building.longitude + regionExtentLongitude, building.latitude + regionExtentLatitude);
				tree.insert(nodes, region, factory.createPoint(building.longitude, building.latitude));
			}
		}

		//via Zanchet 19, Miane, Treviso, Italia
		final Region region2 = Region.of(12.09257 - regionExtentLongitude, 45.94489 - regionExtentLatitude,
			12.09257 + regionExtentLongitude, 45.94489 + regionExtentLatitude);
		Point neighbor = tree.nearestNeighbor(nodes, region2, factory.createPoint(12.09257, 45.94489));
		Assertions.assertEquals(factory.createPoint(12.0925717, 45.9449099), neighbor);

		//query tree
		final int counter = 1_000_000;
		final long start = System.currentTimeMillis();
		for(int i = 0; i < counter; i ++){
			final double longitude = generateRandomCoordinate(minLongitude, maxLongitude);
			final double latitude = generateRandomCoordinate(minLatitude, maxLatitude);
			final Region region = Region.of(longitude - regionExtentLongitude, latitude - regionExtentLatitude,
				longitude + regionExtentLongitude, latitude + regionExtentLatitude);
			tree.nearestNeighbor(nodes, region, factory.createPoint(longitude, latitude));
		}
		final long stop = System.currentTimeMillis();
		final int speed = Math.round((float)counter / (stop - start));
		//330 op/ms (1.2x)
		System.out.println("speed: " + speed + " op/ms");
	}

	private static double generateRandomCoordinate(final double min, final double max){
		return min + (max - min) * RANDOM.nextDouble();
	}

	private static BuildingData[] extractBuildingData(final String municipalityName) throws IOException{
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

		final List<BuildingData> cleanedList = new ArrayList<>();
		if(buildingData != null)
			for(final BuildingData buildingDatum : buildingData)
				if(buildingDatum != null)
					cleanedList.add(buildingDatum);
		return cleanedList.toArray(BuildingData[]::new);
	}

	private static DataResponse post(final String uri, final String requestBody) throws IOException{
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

	private static final class BuildingData{
		@JsonProperty
		private double longitude;
		@JsonProperty
		private double latitude;
		@JsonProperty
		private String city;
		@JsonProperty
		private String municipality;
		@JsonProperty
		private String street;
		@JsonProperty
		private String houseNumber;

		@JsonProperty
		private String name;
	}

}
