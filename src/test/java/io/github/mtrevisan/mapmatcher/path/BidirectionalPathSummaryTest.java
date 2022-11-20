package io.github.mtrevisan.mapmatcher.path;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class BidirectionalPathSummaryTest{

	@Test
	void should_return_path_consisting_of_vertices(){
		Vertex first = new Vertex(1, new Coordinates(1., 2.));
		Vertex second = new Vertex(2, new Coordinates(2., 2.));
		List<Edge> path = new ArrayList<>(List.of(new Edge(first, second, 50.)));
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(path, new HashSet<>(), new HashSet<>());

		List<Vertex> result = pathSummary.simplePath();

		Assertions.assertEquals(Arrays.asList(first, second), result);
	}

	@Test
	void should_return_empty_list_when_path_is_empty(){
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(new ArrayList<>(), new HashSet<>(), new HashSet<>());

		List<Vertex> result = pathSummary.simplePath();

		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	void should_return_the_number_of_vertices_in_path(){
		List<Edge> path = new ArrayList<>(List.of(
			new Edge(new Vertex(1, new Coordinates(1., 2.)),
				new Vertex(2, new Coordinates(2., 2.)), 50.)));
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(path, new HashSet<>(), new HashSet<>());

		int result = pathSummary.numberOfVertices();

		Assertions.assertEquals(2, result);
	}

	@Test
	void should_return_0_when_path_is_empty(){
		List<Edge> path = new ArrayList<>();
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(path, new HashSet<>(), new HashSet<>());

		int result = pathSummary.numberOfVertices();

		Assertions.assertEquals(0, result);
	}

	@Test
	void should_return_the_number_of_visited_vertices(){
		Set<Vertex> visitedVertices = new HashSet<>(Arrays.asList(
			new Vertex(1, new Coordinates(1., 1.)),
			new Vertex(2, new Coordinates(2., 2.))
		));
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(new ArrayList<>(), visitedVertices, visitedVertices);

		int result = pathSummary.totalVisitedVertices();

		Assertions.assertEquals(4, result);
	}

	@Test
	void should_return_path_distance(){
		ArrayList<Edge> path = new ArrayList<>(List.of(
			new Edge(new Vertex(1, new Coordinates(14.552797, 121.058805)),
				new Vertex(2, new Coordinates(14.593999, 120.994260)), 50.)));
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(path, new HashSet<>(), new HashSet<>());

		double result = pathSummary.totalDistance();

		Assertions.assertEquals(8.321, result, 0.00005);
	}

	@Test
	void should_return_path_duration(){
		ArrayList<Edge> path = new ArrayList<>(List.of(
			new Edge(new Vertex(1, new Coordinates(14.552797, 121.058805)),
				new Vertex(2, new Coordinates(14.593999, 120.994260)), 50.)));
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(path, new HashSet<>(), new HashSet<>());

		double result = pathSummary.totalDuration();

		Assertions.assertEquals(9.98519, result, 0.00005);
	}

	@Test
	void should_return_path_search_boundaries(){
		final Vertex firstVertex = new Vertex(1, new Coordinates(1., 1.));
		final Vertex secondVertex = new Vertex(2, new Coordinates(3., 3.));
		final Vertex thirdVertex = new Vertex(3, new Coordinates(1., 5.));
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(new ArrayList<>(),
			new HashSet<>(Arrays.asList(firstVertex, secondVertex, thirdVertex)),
			new HashSet<>(Arrays.asList(thirdVertex, firstVertex, secondVertex)));

		Collection<List<Vertex>> result = pathSummary.searchBoundaries();

		Assertions.assertEquals(2, result.size());
		Assertions.assertEquals(Arrays.asList(secondVertex, firstVertex, thirdVertex), result.toArray()[0]);
		Assertions.assertEquals(Arrays.asList(secondVertex, firstVertex, thirdVertex), result.toArray()[1]);
	}

	@Test
	void should_return_whether_the_path_is_empty(){
		List<Edge> path = new ArrayList<>();
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(path, new HashSet<>(), new HashSet<>());

		Assertions.assertTrue(pathSummary.simplePath().isEmpty());
		Assertions.assertFalse(pathSummary.isFound());


		Vertex vertex1 = new Vertex(1, new Coordinates(14.552797, 121.058805));
		Vertex vertex2 = new Vertex(2, new Coordinates(14.593999, 120.994260));
		path = new ArrayList<>(List.of(new Edge(vertex1, vertex2, 50.)));
		pathSummary = new BidirectionalPathSummary(path, new HashSet<>(), new HashSet<>());

		Assertions.assertEquals(Arrays.asList(vertex1, vertex2), pathSummary.simplePath());
		Assertions.assertTrue(pathSummary.isFound());
	}

}
