package io.github.mtrevisan.mapmatcher.convexhull;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


class GrahamScanConvexHullCalculatorTest{

	@Test
	void should_return_all_vertices_when_2_or_less_vertices_were_given(){
		ArrayList<Vertex> vertices = new ArrayList<>(Arrays.asList(
			new Vertex("1", Coordinates.of(1., 1.)),
			new Vertex("2", Coordinates.of(2., 1.))
		));

		GrahamScanConvexHullCalculator convexHulkCalculator = new GrahamScanConvexHullCalculator();
		List<Vertex> actualResult = convexHulkCalculator.calculateConvexHull(vertices);

		Assertions.assertEquals(vertices, actualResult);
	}

	@Test
	void should_remove_excess_vertices_and_determine_a_valid_convex_hull_when_given_7_points(){
		ArrayList<Vertex> vertices = new ArrayList<>(Arrays.asList(
			new Vertex("1", Coordinates.of(0., 3.)),
			new Vertex("2", Coordinates.of(2., 4.)),
			new Vertex("3", Coordinates.of(1., 1.)),
			new Vertex("4", Coordinates.of(2., 1.)),
			new Vertex("5", Coordinates.of(3., 0.)),
			new Vertex("6", Coordinates.of(0., 0.)),
			new Vertex("7", Coordinates.of(3., 3.))
		));

		GrahamScanConvexHullCalculator convexHulkCalculator = new GrahamScanConvexHullCalculator();
		List<Vertex> actualResult = convexHulkCalculator.calculateConvexHull(vertices);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList("6", "2", "7", "5", "1")),
			actualResult.stream().map(Vertex::getId).collect(Collectors.toList()));
	}

	@Test
	void should_remove_excess_vertices_and_determine_a_valid_convex_hull_when_given_an_additional_point(){
		ArrayList<Vertex> vertices = new ArrayList<>(Arrays.asList(
			new Vertex("1", Coordinates.of(0., 3.)),
			new Vertex("2", Coordinates.of(1., 1.)),
			new Vertex("3", Coordinates.of(2., 2.)),
			new Vertex("4", Coordinates.of(4., 4.)),
			new Vertex("5", Coordinates.of(0., 0.)),
			new Vertex("6", Coordinates.of(1., 5.)),
			new Vertex("7", Coordinates.of(3., 1.)),
			new Vertex("8", Coordinates.of(3., 3.))
		));

		GrahamScanConvexHullCalculator convexHulkCalculator = new GrahamScanConvexHullCalculator();
		List<Vertex> actualResult = convexHulkCalculator.calculateConvexHull(vertices);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList("5", "6", "4", "7", "1")),
			actualResult.stream().map(Vertex::getId).collect(Collectors.toList()));
	}

}
