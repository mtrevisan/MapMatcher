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

import io.github.mtrevisan.mapmatcher.distances.GeodeticCalculator;
import io.github.mtrevisan.mapmatcher.helpers.spatial.Coordinate;
import io.github.mtrevisan.mapmatcher.helpers.spatial.Envelope;
import io.github.mtrevisan.mapmatcher.helpers.spatial.Polyline;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


class HPRTreeTest{

	private static final GeodeticCalculator DISTANCE_CALCULATOR = new GeodeticCalculator();


	@Test
	void test(){
		HPRtree<Polyline> tree = new HPRtree<>();
		List<Polyline> polylines = readWKTFile("src/test/resources/it.highways.wkt");
		for(Polyline polyline : polylines){
			Envelope geoBoundingBox = polyline.getBoundingBox();
			tree.insert(geoBoundingBox, polyline);
		}

		List<Polyline> roads = tree.query(Envelope.of(Coordinate.of(9.01670, 45.60973), Coordinate.of(9.40355, 45.33115)));

		Assertions.assertEquals(931, roads.size());
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
		List<Polyline> geometries = new ArrayList<>();
		geometries.add(Polyline.of(Coordinate.of(0., 0.), Coordinate.of(10., 10.)));
		geometries.add(Polyline.of(Coordinate.of(20., 20.), Coordinate.of(30., 30.)));
		geometries.add(Polyline.of(Coordinate.of(20., 20.), Coordinate.of(30., 30.)));
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

	private void queryGrid(int size, HPRtree<Integer> t){
		for(int i = 0; i < size; i ++)
			t.insert(Envelope.of(i, i + 1, i, i + 1), i);

		t.query(Envelope.of(0, 1, 0, 1));

		Assertions.assertEquals(3, t.query(Envelope.of(5, 6, 5, 6)).size());
		Assertions.assertEquals(3, t.query(Envelope.of(9, 10, 9, 10)).size());
		Assertions.assertEquals(3, t.query(Envelope.of(25, 26, 25, 26)).size());
		Assertions.assertEquals(11, t.query(Envelope.of(0, 10, 0, 10)).size());
	}


	private static List<Polyline> readWKTFile(final String filename){
		final List<Polyline> lines = new ArrayList<>();
		final File f = new File(filename);
		try(final BufferedReader br = new BufferedReader(new FileReader(f))){
			String readLine;
			while((readLine = br.readLine()) != null)
				if(!readLine.isEmpty())
					lines.add(parseLineString(readLine));
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return lines;
	}

	private static Polyline parseLineString(final String line){
		if(!line.startsWith("LINESTRING (") && !line.endsWith(")"))
			throw new IllegalArgumentException("Unrecognized element, cannot parse line: " + line);

		List<Coordinate> coordinates = new ArrayList<>(0);
		int startIndex = line.indexOf('(') + 1;
		while(true){
			int lonIndex = line.indexOf(" ", startIndex + 1);
			if(lonIndex < 0)
				break;

			int endIndex = line.indexOf(", ", lonIndex + 1);
			if(endIndex < 0)
				endIndex = line.indexOf(')', lonIndex + 1);
			coordinates.add(Coordinate.of(
				Double.parseDouble(line.substring(startIndex, lonIndex)),
				Double.parseDouble(line.substring(lonIndex + 1, endIndex))
			));
			startIndex = endIndex + 2;
		}
		return Polyline.ofSimplified(DISTANCE_CALCULATOR, 5., coordinates.toArray(Coordinate[]::new));
	}

}
