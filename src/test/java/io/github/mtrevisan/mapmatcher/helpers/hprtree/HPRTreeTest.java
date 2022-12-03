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

import io.github.mtrevisan.mapmatcher.helpers.Coordinate;
import io.github.mtrevisan.mapmatcher.helpers.Polyline;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


//https://github.com/locationtech/jts/blob/master/modules/core/src/main/java/org/locationtech/jts/index/hprtree/HPRtree.java
class HPRTreeTest{

	@Test
	void test(){
//		HPRTree<Polyline> tree = new HPRTree<>(10, 2, 2);
//
//		List<String> lines = readFile("src/test/resources/it.highways.txt");
//		for(final String line : lines){
//			Polyline polyline = parseLineString(line);
//			double[] geoBoundingBox = polyline.getBoundingBox();
//			double[] coordinates = new double[]{geoBoundingBox[0], geoBoundingBox[1]};
//			double[] dimensions = new double[]{geoBoundingBox[2] - geoBoundingBox[0], geoBoundingBox[3] - geoBoundingBox[1]};
//			tree.insert(coordinates, dimensions, polyline);
//		}
		//TODO
	}


	private static List<String> readFile(final String filename){
		final List<String> lines = new ArrayList<>();
		final File f = new File(filename);
		try(final BufferedReader br = new BufferedReader(new FileReader(f))){
			String readLine;
			while((readLine = br.readLine()) != null)
				if(!readLine.isEmpty())
					lines.add(readLine);
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
		String coordinatesLine = line.substring(line.indexOf('(') + 1, line.lastIndexOf(')'));
		String[] coordinatePairs = coordinatesLine.split(", ");
		for(String coordinatePair : coordinatePairs){
			String[] longitudeLatitude = coordinatePair.split(" ");
			coordinates.add(Coordinate.of(Double.parseDouble(longitudeLatitude[0]), Double.parseDouble(longitudeLatitude[1])));
		}
		return Polyline.of(coordinates.toArray(Coordinate[]::new));
	}

}
