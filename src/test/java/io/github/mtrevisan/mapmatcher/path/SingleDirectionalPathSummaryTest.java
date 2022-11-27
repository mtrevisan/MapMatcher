package io.github.mtrevisan.mapmatcher.path;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.helpers.WGS84GeometryHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class SingleDirectionalPathSummaryTest{

	private static final GeometryFactory FACTORY = new GeometryFactory();


	@Test
	void should_return_path_consisting_of_vertices(){
		final Node first = new Node("0", new Coordinate(1., 2.));
		final Node second = new Node("1", new Coordinate(2., 2.));
		final List<Edge> path = new ArrayList<>(List.of(Edge.createDirectEdge(first, second,
			FACTORY.createLineString(new Coordinate[]{first.getCoordinate(), second.getCoordinate()}))));
		final SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		final List<Node> result = pathSummary.simplePath();

		Assertions.assertEquals(new ArrayList<>(Arrays.asList(first, second)), result);
	}

	@Test
	void should_return_empty_list_when_path_is_empty(){
		final List<Edge> path = new ArrayList<>();
		final SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		final List<Node> result = pathSummary.simplePath();

		Assertions.assertEquals(new ArrayList<>(), result);
	}

	@Test
	void should_return_the_number_of_vertices_in_path(){
		final Node first = new Node("0", new Coordinate(1., 2.));
		final Node second = new Node("1", new Coordinate(2., 2.));
		final List<Edge> path = new ArrayList<>(List.of(Edge.createDirectEdge(first, second,
			FACTORY.createLineString(new Coordinate[]{first.getCoordinate(), second.getCoordinate()}))));
		final SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		final int result = pathSummary.numberOfVertices();

		Assertions.assertEquals(2, result);
	}

	@Test
	void should_return_0_when_path_is_empty(){
		final List<Edge> path = new ArrayList<>();
		final SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		final int result = pathSummary.numberOfVertices();

		Assertions.assertEquals(0, result);
	}

	@Test
	void should_return_the_number_of_visited_vertices(){
		final Node first = new Node("0", new Coordinate(1., 1.));
		final Node second = new Node("1", new Coordinate(2., 2.));
		final Set<Node> visitedVertices = new HashSet<>(Arrays.asList(first, second));
		final SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(new ArrayList<>(), visitedVertices);

		final int result = pathSummary.totalVisitedVertices();

		Assertions.assertEquals(2, result);
	}

	@Test
	void should_return_path_distance(){
		final Node first = new Node("0", new Coordinate(14.552797, 121.058805));
		final Node second = new Node("1", new Coordinate(14.593999, 120.994260));
		final List<Edge> path = new ArrayList<>(List.of(Edge.createDirectEdge(first, second,
			WGS84GeometryHelper.createLineString(new Coordinate[]{first.getCoordinate(), second.getCoordinate()}))));
		final SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		final double result = pathSummary.totalDistance();

		Assertions.assertEquals(0.076_6, result, 0.000_05);
	}

	@Test
	void should_return_path_duration(){
		final Node first = new Node("0", new Coordinate(14.552797, 121.058805));
		final Node second = new Node("1", new Coordinate(14.593999, 120.994260));
		final Edge edge = Edge.createDirectEdge(first, second,
			WGS84GeometryHelper.createLineString(new Coordinate[]{first.getCoordinate(), second.getCoordinate()}));
		edge.setWeight(50.);
		final List<Edge> path = new ArrayList<>(List.of(edge));
		final SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		final double result = pathSummary.totalDuration();

		Assertions.assertEquals(0.091_9, result, 0.000_05);
	}

	@Test
	void should_return_whether_the_path_is_empty(){
		List<Edge> path = new ArrayList<>();
		SingleDirectionalPathSummary pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		Assertions.assertFalse(pathSummary.isFound());


		final Node first = new Node("0", new Coordinate(14.552797, 121.058805));
		final Node second = new Node("1", new Coordinate(14.593999, 120.994260));
		path = new ArrayList<>(List.of(Edge.createDirectEdge(first, second,
			WGS84GeometryHelper.createLineString(new Coordinate[]{first.getCoordinate(), second.getCoordinate()}))));
		pathSummary = new SingleDirectionalPathSummary(path, new HashSet<>());

		Assertions.assertTrue(pathSummary.isFound());
	}

}
