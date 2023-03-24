/**
 * Copyright (c) 2021 Mauro Trevisan
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
package io.github.mtrevisan.mapmatcher.helpers.hprtree;

import io.github.mtrevisan.mapmatcher.spatial.Envelope;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.simplification.RamerDouglasPeuckerSimplifier;
import io.github.mtrevisan.mapmatcher.spatial.topologies.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;


class HPRTreeTest{

	private static final GeometryFactory FACTORY = new GeometryFactory(new GeoidalCalculator());

	private static final String FILENAME_ROADS_RAW = "src/test/resources/it.highways.wkt";
	private static final String FILENAME_TOLL_BOOTHS_RAW = "src/test/resources/it.tollBooths.wkt";
	private static final String FILENAME_ROADS_SIMPLIFIED = "src/test/resources/it.highways.simplified.5.wkt";
	private static final String FILENAME_TOLL_BOOTHS_SIMPLIFIED = "src/test/resources/it.tollBooths.simplified.wkt";


	/*
	https://clydedacruz.github.io/openstreetmap-wkt-playground/

	https://overpass-turbo.eu/
[out:json][timeout:125];
area["name:en"="Italy"]->.it;
(
  way["highway"="motorway"](area.it);
  way["highway"="motorway_link"](area.it);
  way["barrier"="toll_booth"](area.it);
);
out body;
>;
out skel qt;
	*/
	//simplify and create roads and toll-booths files
	public static void main(String[] args) throws IOException{
		//extract highways
		final File roadsFile = new File(FILENAME_ROADS_RAW);
		final List<Polyline> roads = extractPolylines(roadsFile);

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

	private static List<Polyline> extractPolylines(final File file) throws IOException{
		final List<Polyline> polylines = new ArrayList<>();
		try(final BufferedReader br = new BufferedReader(new FileReader(file))){
			String readLine;
			while((readLine = br.readLine()) != null)
				if(!readLine.isEmpty())
					polylines.add(parsePolyline(readLine));
		}
		return polylines;
	}

	private static Polyline parsePolyline(final String line){
		if(!(line.startsWith("LINESTRING (") || line.startsWith("LINESTRING(")) && !line.endsWith(")"))
			throw new IllegalArgumentException("Unrecognized element, cannot parse line: " + line);

		List<Point> points = new ArrayList<>(0);
		int startIndex = line.indexOf('(') + 1;
		while(true){
			int separatorIndex = line.indexOf(" ", startIndex + 1);
			if(separatorIndex < 0)
				break;

			int endIndex = line.indexOf(", ", separatorIndex + 1);
			if(endIndex < 0)
				endIndex = line.indexOf(')', separatorIndex + 1);
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
		if(!(line.startsWith("POINT (") || line.startsWith("POINT(")) && !line.endsWith(")"))
			throw new IllegalArgumentException("Unrecognized element, cannot parse line: " + line);

		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		int startIndex = line.indexOf('(') + 1;
		int separatorIndex = line.indexOf(" ", startIndex + 1);
		int endIndex = line.indexOf(')', separatorIndex + 1);
		return factory.createPoint(
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
				if(polyline.getStartPoint().equals(point) || polyline.getEndPoint().equals(point)){
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
		final Set<Point> points = new HashSet<>(polylines.size() * 2);
		final Set<Point> intersectionPoints = new HashSet<>();
		for(final Polyline polyline : polylines)
			for(final Point point : polyline.getPoints())
				if(!points.add(point))
					intersectionPoints.add(point);

		//split polylines
		final List<Polyline> splitPolylines = new ArrayList<>(polylines.size());
		for(Polyline polyline : polylines){
			final Queue<Point> cutpoints = new PriorityQueue<>(Comparator.comparingDouble(polyline::alongTrackDistance));
			for(final Point point : polyline.getPoints())
				if(intersectionPoints.contains(point))
					cutpoints.add(point);

			if(cutpoints.isEmpty())
				splitPolylines.add(polyline);
			else{
				//split
				for(final Point cutpoint : cutpoints){
					final Point[][] pls = polyline.cut(cutpoint);
					if(pls[0].length > 1){
						splitPolylines.add(FACTORY.createPolyline(pls[0]));
						polyline = FACTORY.createPolyline(pls[1]);
					}
				}
				if(polyline.getPoints().length > 1)
					splitPolylines.add(polyline);
			}
		}


		final RamerDouglasPeuckerSimplifier simplifier = new RamerDouglasPeuckerSimplifier();
		simplifier.setDistanceTolerance(tolerance);
		final List<Polyline> reducedPolylines = new ArrayList<>(splitPolylines.size());
		for(final Polyline polyline : splitPolylines){
			final Point[] reducedPoints = simplifier.simplify(polyline.getPoints());
			reducedPolylines.add(FACTORY.createPolyline(reducedPoints));
		}
		return reducedPolylines;
	}

	private static void writePoints(final Collection<Point> points, final File outputFile) throws IOException{
		try(final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
			for(final Point point : points)
				writer.write(point.toString() + "\r\n");
		}
	}

	private static void writePolylines(final Collection<Polyline> polylines, final File outputFile) throws IOException{
		try(final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
			for(final Polyline polyline : polylines)
				writer.write(polyline.toString() + "\r\n");
		}
	}


	@Test
	void query_tree() throws IOException{
		HPRtree<Polyline> tree = new HPRtree<>();
		List<Polyline> highways = extractPolylines(new File(FILENAME_ROADS_SIMPLIFIED));
		for(Polyline polyline : highways){
			Envelope geoBoundingBox = polyline.getBoundingBox();
			tree.insert(geoBoundingBox, polyline);
		}

		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		List<Polyline> roads = tree.query(Envelope.of(
			factory.createPoint(9.01670, 45.60973),
			factory.createPoint(9.40355, 45.33115)
		));

		Assertions.assertEquals(2391, roads.size());
	}

	@Test
	void empty_tree_using_list_query(){
		HPRtree<Object> tree = new HPRtree<>();

		List<Object> list = tree.query(Envelope.of(0., 0., 1., 1.));

		Assertions.assertTrue(list.isEmpty());
	}

	@Test
	void disallowed_inserts(){
		HPRtree<Object> t = new HPRtree<>(3);
		t.insert(Envelope.of(0., 0., 0., 0.), new Object());
		t.insert(Envelope.of(0., 0., 0., 0.), new Object());
		t.query(Envelope.ofEmpty());
		try{
			t.insert(Envelope.of(0., 0., 0., 0.), new Object());
			Assertions.fail();
		}
		catch(IllegalStateException e){
			Assertions.assertTrue(true);
		}
	}

	@Test
	void query(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		List<Polyline> geometries = new ArrayList<>();
		geometries.add(factory.createPolyline(factory.createPoint(0., 0.), factory.createPoint(10., 10.)));
		geometries.add(factory.createPolyline(factory.createPoint(20., 20.), factory.createPoint(30., 30.)));
		geometries.add(factory.createPolyline(factory.createPoint(20., 20.), factory.createPoint(30., 30.)));
		HPRtree<Object> t = new HPRtree<>(3);
		for(Polyline g : geometries)
			t.insert(g.getBoundingBox(), new Object());

		t.query(Envelope.of(5., 6., 5., 6.));

		Assertions.assertEquals(1, t.query(Envelope.of(5., 6., 5., 6.)).size());
		Assertions.assertEquals(0, t.query(Envelope.of(20., 30., 0., 10.)).size());
		Assertions.assertEquals(2, t.query(Envelope.of(25., 26., 25., 26.)).size());
		Assertions.assertEquals(3, t.query(Envelope.of(0., 100., 0., 100.)).size());
	}

	@Test
	void query3(){
		HPRtree<Integer> t = new HPRtree<>();
		for(int i = 0; i < 3; i ++)
			t.insert(Envelope.of(i, i + 1, i, i + 1), i);

		t.query(Envelope.of(0., 1., 0., 1.));

		Assertions.assertEquals(3, t.query(Envelope.of(1., 2., 1., 2.)).size());
		Assertions.assertEquals(0, t.query(Envelope.of(9., 10., 9., 10.)).size());
	}

	@Test
	void query10(){
		HPRtree<Integer> t = new HPRtree<>();
		for(int i = 0; i < 10; i ++)
			t.insert(Envelope.of(i, i + 1, i, i + 1), i);

		t.query(Envelope.of(0, 1, 0, 1));

		Assertions.assertEquals(3, t.query(Envelope.of(5, 6, 5, 6)).size());
		Assertions.assertEquals(2, t.query(Envelope.of(9, 10, 9, 10)).size());
		Assertions.assertEquals(0, t.query(Envelope.of(25, 26, 25, 26)).size());
		Assertions.assertEquals(10, t.query(Envelope.of(0, 10, 0, 10)).size());
	}

	@Test
	void query100(){
		queryGrid(100, new HPRtree<>());
	}

	@Test
	void query100_cap8(){
		queryGrid(100, new HPRtree<>(8));
	}

	@Test
	void query100_cap2(){
		queryGrid(100, new HPRtree<>(2));
	}


	private void queryGrid(int size, HPRtree<Integer> tree){
		for(int i = 0; i < size; i ++)
			tree.insert(Envelope.of(i, i + 1, i, i + 1), i);

		tree.query(Envelope.of(0, 1, 0, 1));

		Assertions.assertEquals(3, tree.query(Envelope.of(5, 6, 5, 6)).size());
		Assertions.assertEquals(3, tree.query(Envelope.of(9, 10, 9, 10)).size());
		Assertions.assertEquals(3, tree.query(Envelope.of(25, 26, 25, 26)).size());
		Assertions.assertEquals(11, tree.query(Envelope.of(0, 10, 0, 10)).size());
	}

}
