package io.github.mtrevisan.mapmatcher.path;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.pathfinding.PathSummary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;


class PathSummaryCreatorTest{

	private static final GeometryFactory FACTORY = new GeometryFactory();

	@Test
	void should_return_path_between_start_and_end_vertex_present_in_predecessor_tree(){
		Node start = new Node(new Coordinate(1., 1.));
		Node middle = new Node(new Coordinate(1., 2.));
		Node end = new Node(new Coordinate(1., 3.));
		Map<Node, Edge> predecessorTree = new LinkedHashMap<>(2);
		predecessorTree.put(middle, new Edge(start, middle,
			FACTORY.createLineString(new Coordinate[]{start.getCoordinate(), middle.getCoordinate()})));
		predecessorTree.put(end, new Edge(middle, end,
			FACTORY.createLineString(new Coordinate[]{middle.getCoordinate(), end.getCoordinate()})));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createUnidirectionalPath(start, end, predecessorTree);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList(start, middle, end)), result.simplePath());
	}

	@Test
	void should_return_an_empty_path_when_start_vertex_and_end_vertex_are_not_connected_in_predecessor_tree(){
		Node start = new Node(new Coordinate(1., 1.));
		Node randomNode = new Node(new Coordinate(1., 1.));
		Node middle = new Node(new Coordinate(1., 1.));
		Node end = new Node(new Coordinate(1., 1.));
		Map<Node, Edge> predecessorTree = new LinkedHashMap<>(2);
		predecessorTree.put(middle, new Edge(randomNode, middle,
			FACTORY.createLineString(new Coordinate[]{randomNode.getCoordinate(), middle.getCoordinate()})));
		predecessorTree.put(end, new Edge(middle, end,
			FACTORY.createLineString(new Coordinate[]{middle.getCoordinate(), end.getCoordinate()})));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createUnidirectionalPath(start, end, predecessorTree);

		Assertions.assertFalse(result.isFound());
		Assertions.assertTrue(result.simplePath().isEmpty());
	}

	@Test
	void should_return_path_between_start_and_end_when_start_and_mid_are_equal_and_start_and_end_are_connected(){
		Node start = new Node(new Coordinate(1., 1.));
		Node middle = start;
		Node end = new Node(new Coordinate(1., 2.));
		Map<Node, Edge> predecessorTree = new LinkedHashMap<>(1);
		predecessorTree.put(end, new Edge(middle, end,
			FACTORY.createLineString(new Coordinate[]{middle.getCoordinate(), end.getCoordinate()})));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createUnidirectionalPath(start, end, predecessorTree);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList(start, end)), result.simplePath());
	}

	@Test
	void should_return_path_between_start_and_end_when_mid_and_end_are_equal_and_end_and_start_are_connected(){
		Node start = new Node(new Coordinate(1., 1.));
		Node end = new Node(new Coordinate(1., 2.));
		Node middle = end;
		Map<Node, Edge> predecessorTree = new LinkedHashMap<>(1);
		predecessorTree.put(middle, new Edge(start, middle,
			FACTORY.createLineString(new Coordinate[]{start.getCoordinate(), middle.getCoordinate()})));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createUnidirectionalPath(start, end, predecessorTree);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList(start, end)), result.simplePath());
	}

	@Test
	void should_return_an_empty_path_when_start_vertex_and_mid_vertex_are_not_connected_in_predecessor_tree(){
		Node start = new Node(new Coordinate(1., 1.));
		Node randomNode = new Node(new Coordinate(1., 2.));
		Node middle = new Node(new Coordinate(1., 3.));
		Node end = new Node(new Coordinate(1., 4.));
		Map<Node, Edge> predecessorTreeStart = new LinkedHashMap<>(1);
		predecessorTreeStart.put(middle, new Edge(randomNode, middle,
			FACTORY.createLineString(new Coordinate[]{randomNode.getCoordinate(), middle.getCoordinate()})));
		Map<Node, Edge> predecessorTreeEnd = new LinkedHashMap<>(1);
		predecessorTreeEnd.put(middle, new Edge(end, middle,
			FACTORY.createLineString(new Coordinate[]{end.getCoordinate(), middle.getCoordinate()})));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createBidirectionalPath(start, middle, end, predecessorTreeStart, predecessorTreeEnd);

		Assertions.assertFalse(result.isFound());
		Assertions.assertTrue(result.simplePath().isEmpty());
	}

	@Test
	void should_return_an_empty_path_when_mid_vertex_and_end_vertex_are_not_connected_in_predecessor_tree(){
		Node start = new Node(new Coordinate(1., 1.));
		Node randomNode = new Node(new Coordinate(1., 2.));
		Node middle = new Node(new Coordinate(1., 3.));
		Node end = new Node(new Coordinate(1., 4.));
		Map<Node, Edge> predecessorTreeStart = new LinkedHashMap<>(1);
		predecessorTreeStart.put(middle, new Edge(start, middle,
			FACTORY.createLineString(new Coordinate[]{start.getCoordinate(), middle.getCoordinate()})));
		Map<Node, Edge> predecessorTreeEnd = new LinkedHashMap<>(1);
		predecessorTreeEnd.put(middle, new Edge(randomNode, middle,
			FACTORY.createLineString(new Coordinate[]{randomNode.getCoordinate(), middle.getCoordinate()})));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createBidirectionalPath(start, middle, end, predecessorTreeStart, predecessorTreeEnd);

		Assertions.assertFalse(result.isFound());
		Assertions.assertTrue(result.simplePath().isEmpty());
	}

}
