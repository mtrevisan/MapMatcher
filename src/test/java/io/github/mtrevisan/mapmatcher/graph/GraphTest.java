package io.github.mtrevisan.mapmatcher.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


class GraphTest{

	private static final PrecisionModel PRECISION_MODEL = new PrecisionModel(PrecisionModel.FLOATING);
	private static final int SRID_WGS84 = 4326;
	private static final GeometryFactory FACTORY = new GeometryFactory(PRECISION_MODEL, SRID_WGS84);


	@Test
	void should_return_the_edges_of_an_vertex(){
		Vertex vertex = new Vertex("1", FACTORY.createPoint(new Coordinate(1., 1.)));
		Vertex firstNeighbor = new Vertex("2", FACTORY.createPoint(new Coordinate(1., 1.)));
		Vertex secondNeighbor = new Vertex("3", FACTORY.createPoint(new Coordinate(1., 1.)));
		Graph graph = new GraphBuilder()
			.addVertex(vertex)
			.addVertex(firstNeighbor)
			.addVertex(secondNeighbor)
			.connectByIds("1", "2", 50.)
			.connectByIds("1", "3", 50.)
			.asGraph();

		Collection<Edge> result = graph.getVertexEdges(vertex);

		Collection<Edge> expected = new ArrayList<>(Arrays.asList(
			new Edge(vertex, firstNeighbor, 50.),
			new Edge(vertex, secondNeighbor, 50.)
		));
		Assertions.assertEquals(expected, result);
	}

	@Test
	void graph_shouldn_t_reflect_updates_made_to_the_builder_after_it_s_creation(){
		GraphBuilder graphBuilder = new GraphBuilder();
		Vertex vertex1 = new Vertex("1", FACTORY.createPoint(new Coordinate(22.22, 33.33)));
		graphBuilder.addVertex(vertex1);
		Graph result = graphBuilder.asGraph();
		Vertex vertex2 = new Vertex("2", FACTORY.createPoint(new Coordinate(22.22, 33.33)));
		graphBuilder.addVertex(vertex2);

		Assertions.assertArrayEquals(new Vertex[]{vertex1}, result.vertices().toArray());
	}

	@Test
	void should_return_reversed_graph(){
		final Vertex vertex = new Vertex("1", FACTORY.createPoint(new Coordinate(1., 1.)));
		final Vertex firstNeighbor = new Vertex("2", FACTORY.createPoint(new Coordinate(1., 1.)));
		final Vertex secondNeighbor = new Vertex("3", FACTORY.createPoint(new Coordinate(1., 1.)));
		Graph graph = new GraphBuilder()
			.addVertex(vertex)
			.addVertex(firstNeighbor)
			.addVertex(secondNeighbor)
			.connectByIds("1", "2", 50.)
			.connectByIds("1", "3", 50.)
			.asGraph();

		final Graph result = graph.reversed();

		Collection<Edge> expectedFirst = List.of(new Edge(firstNeighbor, vertex, 50.));
		Assertions.assertEquals(expectedFirst, result.getVertexEdges(firstNeighbor));
		Collection<Edge> expectedSecond = List.of(new Edge(secondNeighbor, vertex, 50.));
		Assertions.assertEquals(expectedSecond, result.getVertexEdges(secondNeighbor));
	}

	@Test
	void should_return_graph_vertices(){
		Vertex first = new Vertex("1", FACTORY.createPoint(new Coordinate(1., 1.)));
		Vertex second = new Vertex("2", FACTORY.createPoint(new Coordinate(1., 1.)));
		Vertex third = new Vertex("3", FACTORY.createPoint(new Coordinate(1., 1.)));
		Graph graph = new GraphBuilder()
			.addVertex(first)
			.addVertex(second)
			.addVertex(third)
			.asGraph();

		Collection<Vertex> result = graph.vertices();

		Assertions.assertArrayEquals(new Vertex[]{first, second, third}, result.toArray());
	}

	@Test
	void should_return_graph_edges(){
		Vertex vertex = new Vertex("1", FACTORY.createPoint(new Coordinate(1., 1.)));
		Vertex firstNeighbor = new Vertex("2", FACTORY.createPoint(new Coordinate(1., 1.)));
		Vertex secondNeighbor = new Vertex("3", FACTORY.createPoint(new Coordinate(1., 1.)));
		Graph graph = new GraphBuilder()
			.addVertex(vertex)
			.addVertex(firstNeighbor)
			.addVertex(secondNeighbor)
			.connectByIds("1", "2", 50.)
			.connectByIds("1", "3", 50.)
			.asGraph();

		Collection<Edge> result = graph.edges();

		Collection<Edge> expected = List.of(new Edge(vertex, firstNeighbor, 50.), new Edge(vertex, secondNeighbor, 50.));
		Assertions.assertEquals(expected, result);
	}

}
