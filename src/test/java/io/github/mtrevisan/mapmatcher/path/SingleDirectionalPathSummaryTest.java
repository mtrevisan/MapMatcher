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


class SingleDirectionalPathSummaryTest{

	@Test
	void should_return_path_consisting_of_vertices(){
		Vertex first = new Vertex("1", Coordinates.of(1., 2.));
		Vertex second = new Vertex("2", Coordinates.of(2., 2.));
		List<Edge> path = new ArrayList<>(List.of(new Edge(first, second, 50.)));
		SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		List<Vertex> result = pathSummary.simplePath();

		Assertions.assertEquals(new ArrayList<>(Arrays.asList(first, second)), result);
	}

	@Test
	void should_return_empty_list_when_path_is_empty(){
		List<Edge> path = new ArrayList<>();
		SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		List<Vertex> result = pathSummary.simplePath();

		Assertions.assertEquals(new ArrayList<>(), result);
	}

	@Test
	void should_return_the_number_of_vertices_in_path(){
		List<Edge> path = new ArrayList<>(List.of(
			new Edge(new Vertex("1", Coordinates.of(1., 2.)),
				new Vertex("2", Coordinates.of(2., 2.)), 50.)));
		SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		int result = pathSummary.numberOfVertices();

		Assertions.assertEquals(2, result);
	}

	@Test
	void should_return_0_when_path_is_empty(){
		List<Edge> path = new ArrayList<>();
		SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		int result = pathSummary.numberOfVertices();

		Assertions.assertEquals(0, result);
	}

	@Test
	void should_return_the_number_of_visited_vertices(){
		Set<Vertex> visitedVertices = new HashSet<>(Arrays.asList(
			new Vertex("1", Coordinates.of(1., 1.)),
			new Vertex("2", Coordinates.of(2., 2.))));
		SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(new ArrayList<Edge>(), visitedVertices);

		int result = pathSummary.totalVisitedVertices();

		Assertions.assertEquals(2, result);
	}

	@Test
	void should_return_path_distance(){
		List<Edge> path = new ArrayList<>(List.of(
			new Edge(new Vertex("1", Coordinates.of(14.552797, 121.058805)),
				new Vertex("2", Coordinates.of(14.593999, 120.994260)), 50.)));
		SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		double result = pathSummary.totalDistance();

		Assertions.assertEquals(8_316.3, result, 0.05);
	}

	@Test
	void should_return_path_duration(){
		List<Edge> path = new ArrayList<>(List.of(
			new Edge(new Vertex("1", Coordinates.of(14.552797, 121.058805)),
				new Vertex("2", Coordinates.of(14.593999, 120.994260)), 50.)));
		SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		double result = pathSummary.totalDuration();

		Assertions.assertEquals(9_979.5, result, 0.05);
	}

	@Test
	void should_return_path_search_boundaries(){
		final Vertex firstVertex = new Vertex("1", Coordinates.of(1, 1));
		final Vertex secondVertex = new Vertex("2", Coordinates.of(3, 3));
		final Vertex thirdVertex = new Vertex("3", Coordinates.of(1, 5));
		SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(new ArrayList<>(),
			new HashSet<>(Arrays.asList(firstVertex, secondVertex, thirdVertex)));

		Collection<List<Vertex>> result = pathSummary.searchBoundaries();

		Assertions.assertEquals(1, result.size());
		Assertions.assertEquals(Arrays.asList(secondVertex, firstVertex, thirdVertex), result.toArray()[0]);
	}

	@Test
	void should_return_whether_the_path_is_empty(){
		List<Edge> path = new ArrayList<>();
		SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		Assertions.assertFalse(pathSummary.isFound());


		path = new ArrayList<>(List.of(
			new Edge(new Vertex("1", Coordinates.of(14.552797, 121.058805)),
				new Vertex("2", Coordinates.of(14.593999, 120.994260)), 50.)));
		pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		Assertions.assertTrue(pathSummary.isFound());
	}

}
