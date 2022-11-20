package io.github.mtrevisan.mapmatcher.path;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.pathfinding.PathSummary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;


class PathSummaryCreatorTest{

	@Test
	void should_return_path_between_start_and_end_vertex_present_in_predecessor_tree(){
		Vertex start = new Vertex("1", Coordinates.of(1., 1.));
		Vertex mid = new Vertex("2", Coordinates.of(1., 1.));
		Vertex end = new Vertex("3", Coordinates.of(1., 1.));
		Map<Vertex, Edge> predecessorTree = new LinkedHashMap<>(2);
		predecessorTree.put(mid, new Edge(start, mid, 50.));
		predecessorTree.put(end, new Edge(mid, end, 50.));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createUnidirectionalPath(start, end, predecessorTree);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList(start, mid, end)), result.simplePath());
	}

	@Test
	void should_return_an_empty_path_when_start_vertex_and_end_vertex_are_not_connected_in_predecessor_tree(){
		Vertex start = new Vertex("1", Coordinates.of(1., 1.));
		Vertex randomVertex = new Vertex("4", Coordinates.of(1., 1.));
		Vertex middle = new Vertex("2", Coordinates.of(1., 1.));
		Vertex end = new Vertex("3", Coordinates.of(1., 1.));
		Map<Vertex, Edge> predecessorTree = new LinkedHashMap<>(2);
		predecessorTree.put(middle, new Edge(randomVertex, middle, 50.));
		predecessorTree.put(end, new Edge(middle, end, 50.));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createUnidirectionalPath(start, end, predecessorTree);

		Assertions.assertFalse(result.isFound());
		Assertions.assertTrue(result.simplePath().isEmpty());
	}

	@Test
	void should_return_path_between_start_and_end_when_start_and_mid_are_equal_and_start_and_end_are_connected(){
		Vertex start = new Vertex("1", Coordinates.of(1., 1.));
		Vertex middle = start;
		Vertex end = new Vertex("3", Coordinates.of(1., 1.));
		Map<Vertex, Edge> predecessorTree = new LinkedHashMap<>(1);
		predecessorTree.put(end, new Edge(middle, end, 50.));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createUnidirectionalPath(start, end, predecessorTree);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList(start, end)), result.simplePath());
	}

	@Test
	void should_return_path_between_start_and_end_when_mid_and_end_are_equal_and_end_and_start_are_connected(){
		Vertex start = new Vertex("1", Coordinates.of(1., 1.));
		Vertex end = new Vertex("3", Coordinates.of(1., 1.));
		Vertex middle = end;
		Map<Vertex, Edge> predecessorTree = new LinkedHashMap<>(1);
		predecessorTree.put(middle, new Edge(start, middle, 50.));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createUnidirectionalPath(start, end, predecessorTree);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList(start, end)), result.simplePath());
	}

	@Test
	void should_return_an_empty_path_when_start_vertex_and_mid_vertex_are_not_connected_in_predecessor_tree(){
		Vertex start = new Vertex("1", Coordinates.of(1., 1.));
		Vertex randomVertex = new Vertex("4", Coordinates.of(1., 1.));
		Vertex mid = new Vertex("2", Coordinates.of(1., 1.));
		Vertex end = new Vertex("3", Coordinates.of(1., 1.));
		Map<Vertex, Edge> predecessorTreeStart = new LinkedHashMap<>(1);
		predecessorTreeStart.put(mid, new Edge(randomVertex, mid, 50.));
		Map<Vertex, Edge> predecessorTreeEnd = new LinkedHashMap<>(1);
		predecessorTreeEnd.put(mid, new Edge(end, mid, 50.));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createBidirectionalPath(start, mid, end, predecessorTreeStart, predecessorTreeEnd);

		Assertions.assertFalse(result.isFound());
		Assertions.assertTrue(result.simplePath().isEmpty());
	}

	@Test
	void should_return_an_empty_path_when_mid_vertex_and_end_vertex_are_not_connected_in_predecessor_tree(){
		Vertex start = new Vertex("1", Coordinates.of(1., 1.));
		Vertex randomVertex = new Vertex("4", Coordinates.of(1., 1.));
		Vertex mid = new Vertex("2", Coordinates.of(1., 1.));
		Vertex end = new Vertex("3", Coordinates.of(1., 1.));
		Map<Vertex, Edge> predecessorTreeStart = new LinkedHashMap<>(1);
		predecessorTreeStart.put(mid, new Edge(start, mid, 50.));
		Map<Vertex, Edge> predecessorTreeEnd = new LinkedHashMap<>(1);
		predecessorTreeEnd.put(mid, new Edge(randomVertex, mid, 50.));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createBidirectionalPath(start, mid, end, predecessorTreeStart, predecessorTreeEnd);

		Assertions.assertFalse(result.isFound());
		Assertions.assertTrue(result.simplePath().isEmpty());
	}

}
