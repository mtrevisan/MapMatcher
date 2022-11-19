package io.github.mtrevisan.mapmatcher.convexhull;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


class AndrewMonotoneChainConvexHullCalculatorTest{

	@Test
	void should_return_all_vertices_when_2_or_less_vertices_were_given(){
		List<Vertex> vertices = new ArrayList<>(Arrays.asList(
			new Vertex(1, new Coordinates(1, 1)),
			new Vertex(2, new Coordinates(2, 1))
		));

		AndrewMonotoneChainConvexHullCalculator convexHulkCalculator = new AndrewMonotoneChainConvexHullCalculator();
		List<Vertex> actualResult = convexHulkCalculator.calculateConvexHull(vertices);

		Assertions.assertEquals(vertices, actualResult);
	}

	@Test
	void should_remove_excess_vertices_and_determine_a_valid_convex_hull_when_given_7_points(){
		ArrayList<Vertex> vertices = new ArrayList<>(Arrays.asList(
			new Vertex(1, new Coordinates(0, 3)),
			new Vertex(2, new Coordinates(2, 4)),
			new Vertex(3, new Coordinates(1, 1)),
			new Vertex(4, new Coordinates(2, 1)),
			new Vertex(5, new Coordinates(3, 0)),
			new Vertex(6, new Coordinates(0, 0)),
			new Vertex(7, new Coordinates(3, 3))
		));

		AndrewMonotoneChainConvexHullCalculator convexHulkCalculator = new AndrewMonotoneChainConvexHullCalculator();
		List<Vertex> actualResult = convexHulkCalculator.calculateConvexHull(vertices);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList(7L, 5L, 6L, 1L, 2L)),
			actualResult.stream().map(Vertex::getId).collect(Collectors.toList()));
	}

	@Test
	void should_remove_excess_vertices_and_determine_a_valid_convex_hull_when_given_an_additional_point(){
		ArrayList<Vertex> vertices = new ArrayList<>(Arrays.asList(
			new Vertex(1, new Coordinates(0, 3)),
			new Vertex(2, new Coordinates(1, 1)),
			new Vertex(3, new Coordinates(2, 2)),
			new Vertex(4, new Coordinates(4, 4)),
			new Vertex(5, new Coordinates(0, 0)),
			new Vertex(6, new Coordinates(1, 5)),
			new Vertex(7, new Coordinates(3, 1)),
			new Vertex(8, new Coordinates(3, 3))
		));

		AndrewMonotoneChainConvexHullCalculator convexHulkCalculator = new AndrewMonotoneChainConvexHullCalculator();
		List<Vertex> actualResult = convexHulkCalculator.calculateConvexHull(vertices);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList(4L, 7L, 5L, 1L, 6L)),
			actualResult.stream().map(Vertex::getId).collect(Collectors.toList()));
	}

}
