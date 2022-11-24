package io.github.mtrevisan.mapmatcher.graph;

import io.github.mtrevisan.mapmatcher.distances.EarthEllipsoidalCalculator;
import io.github.mtrevisan.mapmatcher.distances.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.helpers.WGS84GeometryHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class GraphTest{

	private static final GeometryFactory FACTORY = new GeometryFactory();


	@Test
	void should_connect_two_vertices_in_graph(){
		final NearLineMergeGraph graph = new NearLineMergeGraph(5., new EarthEllipsoidalCalculator());
		final Node from = new Node("0", new Coordinate(22.22, 33.33));
		final Node to = new Node("1", new Coordinate(33.22, 44.33));

		final LineString lineString = WGS84GeometryHelper.createLineString(new Coordinate[]{from.getCoordinate(), to.getCoordinate()});
		final Set<Edge> addedEdges = (Set<Edge>)graph.addApproximateEdge(lineString);

		final List<Node> fromNodes = new ArrayList<>(graph.getNodesNear(from.getCoordinate()));
		Assertions.assertEquals(1, fromNodes.size());
		final Collection<Edge> edges = fromNodes.get(0).geOutEdges();
		Assertions.assertEquals(addedEdges, edges);
	}

	@Test
	void should_return_the_edges_of_an_node(){
		final Node node = new Node("0", new Coordinate(1., 1.));
		final Node firstNeighbor = new Node("1", new Coordinate(1., 2.));
		final Node secondNeighbor = new Node("2", new Coordinate(1., 3.));
		final NearLineMergeGraph graph = new NearLineMergeGraph(1., new EuclideanCalculator());
		final LineString lineString12 = FACTORY.createLineString(new Coordinate[]{node.getCoordinate(), firstNeighbor.getCoordinate()});
		graph.addApproximateEdge(lineString12);
		final LineString lineString13 = FACTORY.createLineString(new Coordinate[]{node.getCoordinate(), secondNeighbor.getCoordinate()});
		graph.addApproximateEdge(lineString13);

		final List<Node> fromNodes = new ArrayList<>(graph.getNodesNear(node.getCoordinate()));
		Assertions.assertEquals(1, fromNodes.size());
		final Set<Edge> result = (Set<Edge>)fromNodes.get(0).geOutEdges();

		final Set<Edge> expected = new HashSet<>(Arrays.asList(
			new Edge(node, firstNeighbor, lineString12),
			new Edge(node, secondNeighbor, lineString13)
		));
		Assertions.assertEquals(expected, result);
	}

	@Test
	void should_return_graph_vertices(){
		final Node node = new Node("0", new Coordinate(1., 1.));
		final Node firstNeighbor = new Node("1", new Coordinate(1., 2.));
		final Node secondNeighbor = new Node("2", new Coordinate(1., 3.));
		final NearLineMergeGraph graph = new NearLineMergeGraph(1., new EuclideanCalculator());
		final LineString lineString12 = FACTORY.createLineString(new Coordinate[]{node.getCoordinate(), firstNeighbor.getCoordinate()});
		graph.addApproximateEdge(lineString12);
		final LineString lineString13 = FACTORY.createLineString(new Coordinate[]{node.getCoordinate(), secondNeighbor.getCoordinate()});
		graph.addApproximateEdge(lineString13);

		final Collection<Node> result = graph.nodes();

		Assertions.assertArrayEquals(new Node[]{node, firstNeighbor, secondNeighbor}, result.toArray());
	}

	@Test
	void should_return_graph_edges(){
		final Node node = new Node("0", new Coordinate(1., 1.));
		final Node firstNeighbor = new Node("1", new Coordinate(1., 2.));
		final Node secondNeighbor = new Node("2", new Coordinate(1., 3.));
		final NearLineMergeGraph graph = new NearLineMergeGraph(1., new EuclideanCalculator());
		final LineString lineString12 = FACTORY.createLineString(new Coordinate[]{node.getCoordinate(), firstNeighbor.getCoordinate()});
		graph.addApproximateEdge(lineString12);
		final LineString lineString13 = FACTORY.createLineString(new Coordinate[]{node.getCoordinate(), secondNeighbor.getCoordinate()});
		graph.addApproximateEdge(lineString13);

		final Set<Edge> result = (Set<Edge>)graph.edges();

		final Set<Edge> expected = new HashSet<>(List.of(
			new Edge(node, firstNeighbor, lineString12),
			new Edge(node, secondNeighbor, lineString13)
		));
		Assertions.assertEquals(expected, result);
	}

}
