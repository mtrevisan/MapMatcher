package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import io.github.mtrevisan.mapmatcher.spatial.Envelope;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class KDTreeTest{

	private static final GeometryFactory FACTORY = new GeometryFactory(new GeoidalCalculator());

	private static final String FILENAME_TOLL_BOOTHS_RAW = "src/test/resources/it.tollBooths.wkt";


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
	void query_tree_contains_all(){
		KDTree tree = KDTree.ofEmpty();

		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		Set<Point> tollBooths = extractPoints(tollBoothsFile);
		for(Point tollBooth : tollBooths)
			tree.insert(tollBooth);

		for(Point tollBooth : tollBooths)
			Assertions.assertTrue(tree.contains(tollBooth));
		Assertions.assertFalse(tree.contains(FACTORY.createPoint(0., 0.)));
	}

	@Test
	void query_tree_neighbour(){
		KDTree tree = KDTree.ofEmpty();

		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		Set<Point> tollBooths = extractPoints(tollBoothsFile);
		for(Point tollBooth : tollBooths)
			tree.insert(tollBooth);

		for(Point tollBooth : tollBooths)
			Assertions.assertEquals(tollBooth, tree.nearestNeighbour(tollBooth));
		Assertions.assertEquals(FACTORY.createPoint(7.5925975, 43.8008445),
			tree.nearestNeighbour(FACTORY.createPoint(0., 0.)));
	}

	@Test
	void query_tree_points_in_range(){
		KDTree tree = KDTree.ofEmpty();

		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		Set<Point> tollBooths = extractPoints(tollBoothsFile);
		for(Point tollBooth : tollBooths)
			tree.insert(tollBooth);

		Collection<Point> points = tree.pointsInRange(Envelope.of(FACTORY.createPoint(7.5925975, 43.8008445),
			FACTORY.createPoint(8.5925975, 44.8008445)));
		Assertions.assertEquals(52, points.size());

		points = tree.pointsInRange(Envelope.of(FACTORY.createPoint(7.5925975, 43.8008445),
			FACTORY.createPoint(7.5925975, 43.8008445)));
		Assertions.assertEquals(1, points.size());
	}


	@Test
	void query_bulk_tree_contains_all(){
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		Set<Point> tollBooths = extractPoints(tollBoothsFile);
		KDTree tree = KDTree.of(tollBooths);

		for(Point tollBooth : tollBooths)
			Assertions.assertTrue(tree.contains(tollBooth));
		Assertions.assertFalse(tree.contains(FACTORY.createPoint(0., 0.)));
	}

	@Test
	void query_bulk_tree_neighbour(){
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		Set<Point> tollBooths = extractPoints(tollBoothsFile);
		KDTree tree = KDTree.of(tollBooths);

		for(Point tollBooth : tollBooths)
			Assertions.assertEquals(tollBooth, tree.nearestNeighbour(tollBooth));
		Assertions.assertEquals(FACTORY.createPoint(7.5946376, 43.8000279),
			tree.nearestNeighbour(FACTORY.createPoint(0., 0.)));
	}

	@Test
	void query_bulk_tree_points_in_range(){
		File tollBoothsFile = new File(FILENAME_TOLL_BOOTHS_RAW);
		Set<Point> tollBooths = extractPoints(tollBoothsFile);
		KDTree tree = KDTree.of(tollBooths);

		Collection<Point> points = tree.pointsInRange(Envelope.of(FACTORY.createPoint(7.5925975, 43.8008445),
			FACTORY.createPoint(8.5925975, 44.8008445)));
		Assertions.assertEquals(52, points.size());

		points = tree.pointsInRange(Envelope.of(FACTORY.createPoint(7.5925975, 43.8008445),
			FACTORY.createPoint(7.5925975, 43.8008445)));
		Assertions.assertEquals(1, points.size());
	}




//	@Test
//	void empty_tree_using_list_query(){
//		KDTree tree = new KDTree();
//
//		List<Object> list = tree.query(Envelope.of(0., 1., 0., 1.));
//
//		Assertions.assertTrue(list.isEmpty());
//	}
//
//	@Test
//	void disallowed_inserts(){
//		KDTree t = new KDTree(3);
//		t.insert(Envelope.of(0., 0., 0., 0.), new Object());
//		t.insert(Envelope.of(0., 0., 0., 0.), new Object());
//		t.query(Envelope.ofEmpty());
//		try{
//			t.insert(Envelope.of(0., 0., 0., 0.), new Object());
//			Assertions.fail();
//		}
//		catch(IllegalStateException e){
//			Assertions.assertTrue(true);
//		}
//	}
//
//	@Test
//	void rTree(){
//		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
//		List<Polyline> geometries = new ArrayList<>();
//		geometries.add(factory.createPolyline(factory.createPoint(5., 5.), factory.createPoint(15., 15.)));
//		geometries.add(factory.createPolyline(factory.createPoint(25., 25.), factory.createPoint(35., 35.)));
//		geometries.add(factory.createPolyline(factory.createPoint(5., 5.), factory.createPoint(17., 15.)));
//		geometries.add(factory.createPolyline(factory.createPoint(25., 25.), factory.createPoint(35., 35.)));
//		geometries.add(factory.createPolyline(factory.createPoint(5., 25.), factory.createPoint(25., 35.)));
//		geometries.add(factory.createPolyline(factory.createPoint(25., 5.), factory.createPoint(35., 15.)));
//		geometries.add(factory.createPolyline(factory.createPoint(2., 2.), factory.createPoint(4., 4.)));
//		KDTree t = new KDTree(7);
//		for(int i = 0; i < geometries.size(); i ++)
//			t.insert(geometries.get(i).getBoundingBox(), String.valueOf(i + 1));
//
//		Assertions.assertArrayEquals(new String[]{"1", "3", "7"}, t.query(Envelope.of(3., 3., 6., 6.)).toArray());
//	}
//
//	@Test
//	void query(){
//		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
//		List<Polyline> geometries = new ArrayList<>();
//		geometries.add(factory.createPolyline(factory.createPoint(0., 0.), factory.createPoint(10., 10.)));
//		geometries.add(factory.createPolyline(factory.createPoint(20., 20.), factory.createPoint(30., 30.)));
//		geometries.add(factory.createPolyline(factory.createPoint(20., 20.), factory.createPoint(30., 30.)));
//		KDTree t = new KDTree(3);
//		for(Polyline g : geometries)
//			t.insert(g.getBoundingBox(), new Object());
//
//		t.query(Envelope.of(5., 5., 6., 6.));
//
//		Assertions.assertEquals(1, t.query(Envelope.of(5., 5., 6., 6.)).size());
//		Assertions.assertEquals(0, t.query(Envelope.of(20., 0., 30., 10.)).size());
//		Assertions.assertEquals(2, t.query(Envelope.of(25., 25., 26., 26.)).size());
//		Assertions.assertEquals(3, t.query(Envelope.of(0., 0., 100., 100.)).size());
//	}
//
//	@Test
//	void query3(){
//		KDTree t = new KDTree();
//		for(int i = 0; i < 3; i ++)
//			t.insert(Envelope.of(i, i, i + 1, i + 1), i);
//
//		t.query(Envelope.of(0., 0., 1., 1.));
//
//		Assertions.assertEquals(3, t.query(Envelope.of(1., 1., 2., 2.)).size());
//		Assertions.assertEquals(0, t.query(Envelope.of(9., 9., 10., 10.)).size());
//	}
//
//	@Test
//	void query10(){
//		KDTree t = new KDTree();
//		for(int i = 0; i < 10; i ++)
//			t.insert(Envelope.of(i, i, i + 1, i + 1), i);
//
//		t.query(Envelope.of(0, 0, 1, 1));
//
//		Assertions.assertEquals(3, t.query(Envelope.of(5, 5, 6, 6)).size());
//		Assertions.assertEquals(2, t.query(Envelope.of(9, 9, 10, 10)).size());
//		Assertions.assertEquals(0, t.query(Envelope.of(25, 25, 26, 26)).size());
//		Assertions.assertEquals(10, t.query(Envelope.of(0, 0, 10, 10)).size());
//	}
//
//	@Test
//	void query100(){
//		queryGrid(100, new KDTree());
//	}
//
//	@Test
//	void query100_cap8(){
//		queryGrid(100, new KDTree());
//	}
//
//	@Test
//	void query100_cap2(){
//		queryGrid(100, new KDTree());
//	}
//
//
//	private void queryGrid(int size, KDTree tree){
//		for(int i = 0; i < size; i ++)
//			tree.insert(Envelope.of(i, i, i + 1, i + 1), i);
//
//		tree.query(Envelope.of(0, 0, 1, 1));
//
//		Assertions.assertEquals(3, tree.query(Envelope.of(5, 5, 6, 6)).size());
//		Assertions.assertEquals(3, tree.query(Envelope.of(9, 9, 10, 10)).size());
//		Assertions.assertEquals(3, tree.query(Envelope.of(25, 25, 26, 26)).size());
//		Assertions.assertEquals(11, tree.query(Envelope.of(0, 0, 10, 10)).size());
//	}

}
