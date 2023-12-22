/**
 * Copyright (c) 2022 Mauro Trevisan
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
package io.github.mtrevisan.mapmatcher.helpers.hilbertrtree;

import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.simplification.RamerDouglasPeuckerSimplifier;
import io.github.mtrevisan.mapmatcher.spatial.topologies.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


class HilbertRTreeTest{

	private static final GeometryFactory FACTORY = new GeometryFactory(new GeoidalCalculator());

	private static final String FILENAME_ROADS_RAW = "src/test/resources/it.highways.zip";
	private static final String FILENAME_TOLL_BOOTHS_RAW = "src/test/resources/it.tollBooths.wkt";
	private static final String FILENAME_ROADS_SIMPLIFIED = "src/test/resources/it.highways.simplified.5.wkt";
	private static final String FILENAME_TOLL_BOOTHS_SIMPLIFIED = "src/test/resources/it.tollBooths.simplified.wkt";


	/*
	https://clydedacruz.github.io/openstreetmap-wkt-playground/

	https://overpass-turbo.eu/

https://openaddresses.io/

	== con outline ==
	out body;
	>;
	out skel qt;


	*/
	//simplify and create roads and toll-booths files
	public void main(String[] args) throws IOException{
//	public static void main(String[] args) throws IOException{
		//extract highways
		String output;
		try(ZipFile zipFile = new ZipFile(FILENAME_ROADS_RAW)){
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			ZipEntry entry = entries.nextElement();
			InputStream stream = zipFile.getInputStream(entry);
			output = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}

		JSONObject payload = new JSONObject(output);
		Collection<Polyline> roads = parseOverpassAPIResponse(payload);

		//extract toll booths
		final File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		final Set<Point> tollBooths = extractPoints(tollBoothsFile);

		//filter only toll booths on highways
		filterPointsAlongPolylines(tollBooths, roads);

		final File outputTollBoothsFile = new File(FILENAME_TOLL_BOOTHS_SIMPLIFIED);
		writePoints(tollBooths, outputTollBoothsFile);

		//preserve connection point on highways coming from connection links
		final Collection<Polyline> reducedRoads = simplifyPolylines(roads, 5.);

		final File outputRoadsFile = new File(FILENAME_ROADS_SIMPLIFIED);
		writePolylines(reducedRoads, outputRoadsFile);
	}

	public static List<Polyline> parseOverpassAPIResponse(final JSONObject payload){
		final List<Polyline> polylines = new ArrayList<>();
		final Set<Polyline> twoWayPolylines = new HashSet<>();

		final JSONArray features = payload.getJSONArray("features");
		for(int i = 0; i < features.length(); i ++){
			final JSONObject feature = features.getJSONObject(i);
			final JSONArray geometry = feature.getJSONObject("geometry")
				.getJSONArray("coordinates");
			if(feature.has("type") && geometry != null && !geometry.isEmpty()){
				final int size = geometry.length();
				final Point[] points = new Point[size];
				for(int j = 0; j < size; j ++){
					final JSONArray coordinates = (JSONArray)geometry.get(j);
					final double x = coordinates.getDouble(0);
					final double y = coordinates.getDouble(1);
					points[j] = Point.of(FACTORY, x, y);
				}
				if(size > 0){
					final Polyline polyline = Polyline.of(FACTORY, points);
					polylines.add(polyline);

					//register and remember which roads are two-way (to be split AFTER #connectDirectPolylines)
					//add reverse if two-way road
					if("no".equals(feature.getJSONObject("properties").optString("oneway", null)))
						twoWayPolylines.add(polyline);
				}
			}
		}

		connectDirectPolylines(polylines, twoWayPolylines);

		return polylines;
	}

	private static Set<Polyline> extractPolylines(final File file) throws IOException{
		//read segments:
		final Set<Polyline> polylines = new HashSet<>();
		try(final BufferedReader br = new BufferedReader(new FileReader(file))){
			String readLine;
			while((readLine = br.readLine()) != null)
				if(!readLine.isEmpty())
					polylines.add(parsePolyline(readLine));
		}
		return polylines;
	}

	//FIXME highly inefficient
	private static Collection<Polyline> connectDirectPolylines(final Collection<Polyline> polylines, final Set<Polyline> twoWayPolylines){
		//all the segments must be connected, that is each node must be attached to more than one edge, or no edges at all:
		final Map<Point, List<Polyline>> uselessNodes = new HashMap<>(polylines.size() * 2);
		while(true){
			//read all useless nodes:
			uselessNodes.clear();
			for(final Polyline polyline : polylines){
				//store each terminal node of each segment
				uselessNodes.computeIfAbsent(polyline.getStartPoint(), k -> new ArrayList<>(1))
					.add(polyline);
				uselessNodes.computeIfAbsent(polyline.getEndPoint(), k -> new ArrayList<>(1))
					.add(polyline);
			}
			uselessNodes.entrySet()
				.removeIf(entry -> {
					final List<Polyline> list = entry.getValue();
					return (list.size() != 2
						|| list.get(0).getStartPoint().equals(list.get(1).getStartPoint())
						|| list.get(0).getEndPoint().equals(list.get(1).getEndPoint()));
				});
			if(uselessNodes.isEmpty())
				break;

			//connect all edges that point to a useless node:
			for(final Map.Entry<Point, List<Polyline>> entry : uselessNodes.entrySet()){
				final Point entryPoint = entry.getKey();
				final List<Polyline> entryPolylines = entry.getValue();

				final Polyline polyline1 = entryPolylines.get(0);
				final Polyline polyline2 = entryPolylines.get(1);
				if(polylines.contains(polyline1) && polylines.contains(polyline2)){
					polylines.remove(polyline1);
					polylines.remove(polyline2);

					final Polyline mergedPolyline = (polyline1.getStartPoint().equals(entryPoint) && polyline2.getEndPoint().equals(entryPoint)
						? polyline2.append(polyline1)
						: polyline1.append(polyline2));
					polylines.add(mergedPolyline);

					if(twoWayPolylines.remove(polyline1) && twoWayPolylines.remove(polyline2))
						twoWayPolylines.add(mergedPolyline);
				}
			}
		}

		for(final Polyline reversedPolyline : twoWayPolylines)
			polylines.add(reversedPolyline.reverse());
		return polylines;
	}

	private static Polyline parsePolyline(final String line){
		List<Point> points = new ArrayList<>(0);
		int startIndex = 0;
		while(true){
			int separatorIndex = line.indexOf(" ", startIndex + 1);
			if(separatorIndex < 0)
				break;

			int endIndex = line.indexOf(", ", separatorIndex + 1);
			if(endIndex < 0)
				endIndex = line.length();
			points.add(FACTORY.createPoint(
				Double.parseDouble(line.substring(startIndex, separatorIndex)),
				Double.parseDouble(line.substring(separatorIndex + 1, endIndex))
			));
			startIndex = endIndex + 2;
		}

		return FACTORY.createPolyline(points.toArray(Point[]::new));
	}

	private static Set<Point> extractPoints(final File file){
		final List<Point> lines = new ArrayList<>();
		try(final BufferedReader br = new BufferedReader(new FileReader(file))){
			String readLine;
			while((readLine = br.readLine()) != null)
				if(!readLine.isEmpty())
					lines.add(parsePoint(readLine));
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

	private static Set<Point> filterPointsAlongPolylines(final Set<Point> points, final Collection<Polyline> polylines){
		final Iterator<Point> itr = points.iterator();
		while(itr.hasNext()){
			final Point point = itr.next();
			boolean found = false;
			for(final Polyline polyline : polylines)
				if(polyline.contains(point)){
					found = true;
					break;
				}
			if(!found)
				itr.remove();
		}
		return points;
	}

	private static Collection<Polyline> simplifyPolylines(final Collection<Polyline> polylines, final double tolerance){
		if(polylines.isEmpty())
			return polylines;

		//collect intersection points
		final Set<Point> intersectionPoints = collectIntersectionPoints(polylines);

		//split polylines
		final List<Polyline> splitPolylines = new ArrayList<>(polylines.size());
		for(final Polyline polyline : polylines){
			final Queue<Point> cutpoints = extractCutpoints(polyline, intersectionPoints);

			//split
			splitPolyline(polyline, cutpoints, splitPolylines);
		}


		final RamerDouglasPeuckerSimplifier simplifier = new RamerDouglasPeuckerSimplifier();
		simplifier.setDistanceTolerance(tolerance);
		final List<Polyline> reducedPolylines = new ArrayList<>(splitPolylines.size());
		for(final Polyline polyline : splitPolylines){
			final Point[] originalPoints = polyline.getPoints();
			final Point[] reducedPoints = simplifier.simplify(originalPoints);
			reducedPolylines.add(reducedPoints.length != originalPoints.length
				? FACTORY.createPolyline(reducedPoints)
				: polyline);
		}
		return reducedPolylines;
	}

	private static Set<Point> collectIntersectionPoints(Collection<Polyline> polylines){
		final Map<Point, Polyline> points = new HashMap<>(polylines.size());
		final Set<Point> intersectionPoints = new HashSet<>();
		for(final Polyline polyline : polylines)
			for(final Point point : polyline.getPoints()){
				final Polyline previousPolyline = points.put(point, polyline);
				//NOTE: if an intersection point belongs to the very same edge, `other that` reverted, then ignore
				if(previousPolyline != null && !previousPolyline.equals(polyline.reverse()))
					intersectionPoints.add(point);
			}
		return intersectionPoints;
	}

	private static Queue<Point> extractCutpoints(final Polyline polyline, final Set<Point> intersectionPoints){
		final Queue<Point> cutpoints = new PriorityQueue<>(Comparator.comparingDouble(polyline::alongTrackDistance));
		final Point[] points = polyline.getPoints();
		for(int i = 1; i < points.length - 1; i ++){
			final Point point = points[i];
			if(intersectionPoints.contains(point))
				cutpoints.add(point);
		}
		return cutpoints;
	}

	private static void splitPolyline(Polyline polyline, final Queue<Point> cutpoints, final List<Polyline> splitPolylines){
		if(cutpoints.isEmpty())
			splitPolylines.add(polyline);
		else{
			for(final Point cutpoint : cutpoints){
				final Point[][] pls = polyline.cutOnNode(cutpoint);
				if(pls[0].length > 1){
					splitPolylines.add(FACTORY.createPolyline(pls[0]));
					polyline = FACTORY.createPolyline(pls[1]);
				}
			}
			if(polyline.getPoints().length > 1)
				splitPolylines.add(polyline);
		}
	}

	private static void writePoints(final Collection<Point> points, final File outputFile) throws IOException{
		try(final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
			for(final Point point : points)
				writer.write(point.toSimpleString() + "\r\n");
		}
	}

	private static void writePolylines(final Collection<Polyline> polylines, final File outputFile) throws IOException{
		try(final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
			for(final Polyline polyline : polylines)
				writer.write(polyline.toSimpleString() + "\r\n");
		}
	}


	@Test
	void query_tree() throws IOException{
		HilbertRTree tree = HilbertRTree.create();
		Set<Polyline> highways = extractPolylines(new File(FILENAME_ROADS_SIMPLIFIED));
		for(Polyline polyline : highways){
			Region region = Region.of(polyline.getBoundingBox());
			tree.insert(region);
		}

		Collection<Region> regions = tree.query(Region.of(
			FACTORY.createPoint(9.01670, 45.33115),
			FACTORY.createPoint(9.40355, 45.60973)
		));

		Assertions.assertEquals(1178, regions.size());
	}

	@Test
	void empty_tree_using_list_query(){
		HilbertRTree tree = HilbertRTree.create();

		Collection<Region> list = tree.query(Region.of(0., 1., 0., 1.));

		Assertions.assertTrue(list.isEmpty());
	}

	@Test
	void disallowed_inserts(){
		HilbertRTree tree = HilbertRTree.create(3);
		tree.insert(Region.of(0., 0., 0., 0.));
		tree.insert(Region.of(0., 0., 0., 0.));
		tree.query(Region.ofEmpty());
		try{
			tree.insert(Region.of(0., 0., 0., 0.));
			Assertions.fail();
		}
		catch(IllegalStateException e){
			Assertions.assertTrue(true);
		}
	}

	@Test
	void rTree(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		List<Polyline> geometries = new ArrayList<>();
		geometries.add(factory.createPolyline(factory.createPoint(5., 5.), factory.createPoint(15., 15.)));
		geometries.add(factory.createPolyline(factory.createPoint(25., 25.), factory.createPoint(35., 35.)));
		geometries.add(factory.createPolyline(factory.createPoint(5., 5.), factory.createPoint(17., 15.)));
		geometries.add(factory.createPolyline(factory.createPoint(25., 25.), factory.createPoint(35., 35.)));
		geometries.add(factory.createPolyline(factory.createPoint(5., 25.), factory.createPoint(25., 35.)));
		geometries.add(factory.createPolyline(factory.createPoint(25., 5.), factory.createPoint(35., 15.)));
		geometries.add(factory.createPolyline(factory.createPoint(2., 2.), factory.createPoint(4., 4.)));
		HilbertRTree tree = HilbertRTree.create(7);
		for(int i = 0; i < geometries.size(); i ++){
			Region region = Region.of(geometries.get(i).getBoundingBox())
				.withID(String.valueOf(i));
			tree.insert(region);
		}

		final Collection<Region> regions = tree.query(Region.of(3., 3., 6., 6.));
		final String[] ids = new String[regions.size()];
		int i = 0;
		for(final Region region : regions)
			ids[i ++] = region.getID();
		Assertions.assertArrayEquals(new String[]{"0", "2", "6"}, ids);
	}

	@Test
	void query(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		List<Polyline> geometries = new ArrayList<>();
		geometries.add(factory.createPolyline(factory.createPoint(0., 0.), factory.createPoint(10., 10.)));
		geometries.add(factory.createPolyline(factory.createPoint(20., 20.), factory.createPoint(30., 30.)));
		geometries.add(factory.createPolyline(factory.createPoint(20., 20.), factory.createPoint(30., 30.)));
		HilbertRTree tree = HilbertRTree.create(3);
		for(Polyline g : geometries){
			Region region = Region.of(g.getBoundingBox());
			tree.insert(region);
		}

		tree.query(Region.of(5., 5., 6., 6.));

		Assertions.assertEquals(1, tree.query(Region.of(5., 5., 6., 6.)).size());
		Assertions.assertEquals(0, tree.query(Region.of(20., 0., 30., 10.)).size());
		Assertions.assertEquals(2, tree.query(Region.of(25., 25., 26., 26.)).size());
		Assertions.assertEquals(3, tree.query(Region.of(0., 0., 100., 100.)).size());
	}

	@Test
	void query3(){
		HilbertRTree tree = HilbertRTree.create();
		for(int i = 0; i < 3; i ++)
			tree.insert(Region.of(i, i, i + 1, i + 1));

		tree.query(Region.of(0., 0., 1., 1.));

		Assertions.assertEquals(3, tree.query(Region.of(1., 1., 2., 2.)).size());
		Assertions.assertEquals(0, tree.query(Region.of(9., 9., 10., 10.)).size());
	}

	@Test
	void query10(){
		HilbertRTree tree = HilbertRTree.create();
		for(int i = 0; i < 10; i ++)
			tree.insert(Region.of(i, i, i + 1, i + 1));

		tree.query(Region.of(0, 0, 1, 1));

		Assertions.assertEquals(3, tree.query(Region.of(5., 5., 6., 6.)).size());
		Assertions.assertEquals(2, tree.query(Region.of(9., 9., 10., 10.)).size());
		Assertions.assertEquals(0, tree.query(Region.of(25., 25., 26., 26.)).size());
		Assertions.assertEquals(10, tree.query(Region.of(0., 0., 10., 10.)).size());
	}

	@Test
	void query_100_times(){
		queryGrid(100, HilbertRTree.create());
	}

	@Test
	void query100_with_node_capacity_8(){
		queryGrid(100, HilbertRTree.create(8));
	}

	@Test
	void query100_with_node_capacity_2(){
		queryGrid(100, HilbertRTree.create(2));
	}


	private void queryGrid(int size, HilbertRTree tree){
		for(int i = 0; i < size; i ++)
			tree.insert(Region.of(i, i, i + 1, i + 1));

		tree.query(Region.of(0, 0, 1, 1));

		Assertions.assertEquals(3, tree.query(Region.of(5., 5., 6., 6.)).size());
		Assertions.assertEquals(3, tree.query(Region.of(9., 9., 10., 10.)).size());
		Assertions.assertEquals(3, tree.query(Region.of(25., 25., 26., 26.)).size());
		Assertions.assertEquals(11, tree.query(Region.of(0., 0., 10., 10.)).size());
	}

}