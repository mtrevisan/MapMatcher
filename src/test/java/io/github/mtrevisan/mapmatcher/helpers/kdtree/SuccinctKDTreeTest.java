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

import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.topologies.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;


class SuccinctKDTreeTest{

	private static final GeometryFactory FACTORY = new GeometryFactory(new GeoidalCalculator());
	private static final GeometryFactory FACTORY_EUCLIDEAN = new GeometryFactory(new EuclideanCalculator());

	private static final String FILENAME_TOLL_BOOTHS_RAW = "src/test/resources/it.tollBooths.wkt";


	@SuppressWarnings("CallToPrintStackTrace")
	private static List<Point> extractPoints(final File file){
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
		return lines;
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
//		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
//		List<Point> tollBooths = extractPoints(tollBoothsFile);
//		SuccinctKDTree tree = SuccinctKDTree.ofEmpty(2, tollBooths.size());
//		for(Point tollBooth : tollBooths)
//			tree.insert(tollBooth);
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		List<Point> tollBooths = extractPoints(tollBoothsFile);
		SuccinctKDTree tree = SuccinctKDTree.ofPoints(tollBooths);

		for(Point tollBooth : tollBooths)
			Assertions.assertTrue(tree.contains(tollBooth));
		Assertions.assertFalse(tree.contains(FACTORY.createPoint(0., 0.)));
	}

	@Test
	void neighbor(){
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		List<Point> tollBooths = extractPoints(tollBoothsFile);
		SuccinctKDTree tree = SuccinctKDTree.ofPoints(tollBooths);

		for(Point tollBooth : tollBooths)
			Assertions.assertEquals(tollBooth, tree.nearestNeighbor(tollBooth));
		for(Point tollBooth : tollBooths)
			Assertions.assertEquals(tollBooth, tree.nearestNeighbor(
				tollBooth.getFactory().createPoint(tollBooth.getX() + 1.e-7, tollBooth.getY() + 1.e-7)));
		Assertions.assertEquals(FACTORY.createPoint(7.5946376, 43.8000279),
			tree.nearestNeighbor(FACTORY.createPoint(0., 0.)));
	}

	@Test
	void neighbor_euclidean(){
		SuccinctKDTree tree = SuccinctKDTree.ofDimensions(2);
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
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		List<Point> tollBooths = extractPoints(tollBoothsFile);
		SuccinctKDTree tree = SuccinctKDTree.ofPoints(tollBooths);

		Collection<Point> points = tree.query(FACTORY.createPoint(12.1, 45.5),
			FACTORY.createPoint(12.5, 45.9));
		Assertions.assertEquals(34, points.size());

		points = tree.query(FACTORY.createPoint(7.5925975, 43.8008445),
			FACTORY.createPoint(7.5925975, 43.8008445));
		Assertions.assertEquals(1, points.size());
	}

	@Test
	void points_in_range2(){
		SuccinctKDTree tree = SuccinctKDTree.ofDimensions(2);
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
		List<Point> tollBooths = extractPoints(tollBoothsFile);
		SuccinctKDTree tree = SuccinctKDTree.ofPoints(tollBooths);

		for(Point tollBooth : tollBooths)
			Assertions.assertTrue(tree.contains(tollBooth));
		Assertions.assertFalse(tree.contains(FACTORY.createPoint(0., 0.)));
	}

	@Test
	void bulk_neighbor(){
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		List<Point> tollBooths = extractPoints(tollBoothsFile);
		SuccinctKDTree tree = SuccinctKDTree.ofPoints(tollBooths);

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
		SuccinctKDTree tree = SuccinctKDTree.ofPoints(Arrays.asList(
			FACTORY_EUCLIDEAN.createPoint(6., 4.),
			FACTORY_EUCLIDEAN.createPoint(5., 2.),
			FACTORY_EUCLIDEAN.createPoint(8., 6.),
			FACTORY_EUCLIDEAN.createPoint(2., 1.),
			FACTORY_EUCLIDEAN.createPoint(4., 7.),
			FACTORY_EUCLIDEAN.createPoint(9., 3.),
			FACTORY_EUCLIDEAN.createPoint(2., 8.)
		));

		Assertions.assertEquals(FACTORY.createPoint(8., 6.),
			tree.nearestNeighbor(FACTORY.createPoint(9., 8.)));
	}

	@Test
	void bulk_points_in_range1(){
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		List<Point> tollBooths = extractPoints(tollBoothsFile);
		SuccinctKDTree tree = SuccinctKDTree.ofPoints(tollBooths);

		Collection<Point> points = tree.query(FACTORY.createPoint(7.5925975, 43.8008445),
			FACTORY.createPoint(8.5925975, 44.8008445));
		Assertions.assertEquals(52, points.size());

		points = tree.query(FACTORY.createPoint(7.5925975, 43.8008445),
			FACTORY.createPoint(7.5925975, 43.8008445));
		Assertions.assertEquals(1, points.size());
	}

	@Test
	void bulk_points_in_range2(){
		SuccinctKDTree tree = SuccinctKDTree.ofPoints(Arrays.asList(
			FACTORY_EUCLIDEAN.createPoint(6., 4.),
			FACTORY_EUCLIDEAN.createPoint(5., 2.),
			FACTORY_EUCLIDEAN.createPoint(8., 6.),
			FACTORY_EUCLIDEAN.createPoint(2., 1.),
			FACTORY_EUCLIDEAN.createPoint(4., 7.),
			FACTORY_EUCLIDEAN.createPoint(9., 3.),
			FACTORY_EUCLIDEAN.createPoint(2., 8.)
		));

		Collection<Point> points = tree.query(FACTORY_EUCLIDEAN.createPoint(1., 5.),
			FACTORY_EUCLIDEAN.createPoint(5., 9.));
		Assertions.assertEquals(new HashSet<>(Arrays.asList(FACTORY_EUCLIDEAN.createPoint(2., 8.),
				FACTORY_EUCLIDEAN.createPoint(4., 7.))),
			new HashSet<>(points));
	}

}
