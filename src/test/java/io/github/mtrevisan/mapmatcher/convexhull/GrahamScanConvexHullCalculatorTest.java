package io.github.mtrevisan.mapmatcher.convexhull;

import io.github.mtrevisan.mapmatcher.graph.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class GrahamScanConvexHullCalculatorTest{

	@Test
	void should_return_all_vertices_when_2_or_less_vertices_were_given(){
		ArrayList<Node> vertices = new ArrayList<>(Arrays.asList(
			new Node("0", new Coordinate(1., 1.)),
			new Node("1", new Coordinate(2., 1.))
		));

		GrahamScanConvexHullCalculator convexHulkCalculator = new GrahamScanConvexHullCalculator();
		List<Node> actualResult = convexHulkCalculator.calculateConvexHull(vertices);

		Assertions.assertEquals(vertices, actualResult);
	}

	@Test
	void should_remove_excess_vertices_and_determine_a_valid_convex_hull_when_given_7_points(){
		ArrayList<Node> vertices = new ArrayList<>(Arrays.asList(
			new Node("0", new Coordinate(3., 0.)),
			new Node("1", new Coordinate(4., 2.)),
			new Node("2", new Coordinate(1., 1.)),
			new Node("3", new Coordinate(1., 2.)),
			new Node("4", new Coordinate(0., 3.)),
			new Node("5", new Coordinate(0., 0.)),
			new Node("6", new Coordinate(3., 3.))
		));

		GrahamScanConvexHullCalculator convexHulkCalculator = new GrahamScanConvexHullCalculator();
		List<Node> actualResult = convexHulkCalculator.calculateConvexHull(vertices);

		final Set<Node> expected = new HashSet<>(Arrays.asList(vertices.get(5), vertices.get(1), vertices.get(6),
			vertices.get(4), vertices.get(0)));
		Assertions.assertEquals(expected, new HashSet<>(actualResult));
	}

	@Test
	void should_remove_excess_vertices_and_determine_a_valid_convex_hull_when_given_an_additional_point(){
		ArrayList<Node> vertices = new ArrayList<>(Arrays.asList(
			new Node("0", new Coordinate(3., 0.)),
			new Node("1", new Coordinate(1., 1.)),
			new Node("2", new Coordinate(2., 2.)),
			new Node("3", new Coordinate(4., 4.)),
			new Node("4", new Coordinate(0., 0.)),
			new Node("5", new Coordinate(5., 1.)),
			new Node("6", new Coordinate(1., 3.)),
			new Node("7", new Coordinate(3., 3.))
		));

		GrahamScanConvexHullCalculator convexHulkCalculator = new GrahamScanConvexHullCalculator();
		List<Node> actualResult = convexHulkCalculator.calculateConvexHull(vertices);

		final Set<Node> expected = new HashSet<>(Arrays.asList(vertices.get(4), vertices.get(5), vertices.get(3),
			vertices.get(6), vertices.get(0)));
		Assertions.assertEquals(expected, new HashSet<>(actualResult));
	}

}
