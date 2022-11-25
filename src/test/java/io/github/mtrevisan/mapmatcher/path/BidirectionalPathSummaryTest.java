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


class BidirectionalPathSummaryTest{

	private static final GeometryFactory FACTORY = new GeometryFactory();


	@Test
	void should_return_path_consisting_of_vertices(){
		Node first = new Node("0", new Coordinate(1., 2.));
		Node second = new Node("1", new Coordinate(2., 2.));
		List<Edge> path = new ArrayList<>(List.of(Edge.createBidirectionalEdge(first, second,
			FACTORY.createLineString(new Coordinate[]{first.getCoordinate(), second.getCoordinate()}))));
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(path, new HashSet<>(), new HashSet<>());

		List<Node> result = pathSummary.simplePath();

		Assertions.assertEquals(Arrays.asList(first, second), result);
	}

	@Test
	void should_return_empty_list_when_path_is_empty(){
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(new ArrayList<>(), new HashSet<>(), new HashSet<>());

		List<Node> result = pathSummary.simplePath();

		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	void should_return_the_number_of_vertices_in_path(){
		final Node first = new Node("0", new Coordinate(1., 2.));
		final Node second = new Node("1", new Coordinate(2., 2.));
		List<Edge> path = new ArrayList<>(List.of(Edge.createBidirectionalEdge(first, second,
			FACTORY.createLineString(new Coordinate[]{first.getCoordinate(), second.getCoordinate()}))));
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
		Set<Node> visitedVertices = new HashSet<>(Arrays.asList(
			new Node("0", new Coordinate(1., 1.)),
			new Node("1", new Coordinate(2., 2.))
		));
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(new ArrayList<>(), visitedVertices, visitedVertices);

		int result = pathSummary.totalVisitedVertices();

		Assertions.assertEquals(4, result);
	}

	@Test
	void should_return_path_distance(){
		final Node first = new Node("0", new Coordinate(14.552797, 121.058805));
		final Node second = new Node("1", new Coordinate(14.593999, 120.994260));
		ArrayList<Edge> path = new ArrayList<>(List.of(Edge.createBidirectionalEdge(first, second,
			WGS84GeometryHelper.createLineString(new Coordinate[]{first.getCoordinate(), second.getCoordinate()}))));
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(path, new HashSet<>(), new HashSet<>());

		double result = pathSummary.totalDistance();

		Assertions.assertEquals(0.076_6, result, 0.000_05);
	}

	@Test
	void should_return_path_duration(){
		final Node first = new Node("0", new Coordinate(14.552797, 121.058805));
		final Node second = new Node("1", new Coordinate(14.593999, 120.994260));
		final Edge edge = Edge.createBidirectionalEdge(first, second,
			WGS84GeometryHelper.createLineString(new Coordinate[]{first.getCoordinate(), second.getCoordinate()}));
		edge.setWeight(50.);
		ArrayList<Edge> path = new ArrayList<>(List.of(edge));
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(path, new HashSet<>(), new HashSet<>());

		double result = pathSummary.totalDuration();

		Assertions.assertEquals(0.091_9, result, 0.000_05);
	}

	@Test
	void should_return_whether_the_path_is_empty(){
		List<Edge> path = new ArrayList<>();
		BidirectionalPathSummary pathSummary = new BidirectionalPathSummary(path, new HashSet<>(), new HashSet<>());

		Assertions.assertTrue(pathSummary.simplePath().isEmpty());
		Assertions.assertFalse(pathSummary.isFound());


		Node node1 = new Node("0", new Coordinate(14.552797, 121.058805));
		Node node2 = new Node("1", new Coordinate(14.593999, 120.994260));
		path = new ArrayList<>(List.of(Edge.createBidirectionalEdge(node1, node2, FACTORY.createLineString(new Coordinate[]{node1.getCoordinate(), node2.getCoordinate()}))));
		pathSummary = new BidirectionalPathSummary(path, new HashSet<>(), new HashSet<>());

		Assertions.assertEquals(Arrays.asList(node1, node2), pathSummary.simplePath());
		Assertions.assertTrue(pathSummary.isFound());
	}

}
