package io.github.mtrevisan.mapmatcher.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


class GraphTest{

	@Test
	void should_return_the_edges_of_an_vertex(){
		Vertex vertex = new Vertex("1", Coordinates.of(1, 1));
		Vertex firstNeighbour = new Vertex("2", Coordinates.of(1, 1));
		Vertex secondNeighbour = new Vertex("3", Coordinates.of(1, 1));
		Graph graph = new GraphBuilder()
			.addVertex(vertex)
			.addVertex(firstNeighbour)
			.addVertex(secondNeighbour)
			.connectByIds("1", "2", 50.)
			.connectByIds("1", "3", 50.)
			.asGraph();

		Collection<Edge> result = graph.getVertexEdges(vertex);

		Collection<Edge> expected = new ArrayList<>(Arrays.asList(
			new Edge(vertex, firstNeighbour, 50.),
			new Edge(vertex, secondNeighbour, 50.)
		));
		Assertions.assertEquals(expected, result);
	}

	@Test
	void graph_shouldn_t_reflect_updates_made_to_the_builder_after_it_s_creation(){
		GraphBuilder graphBuilder = new GraphBuilder();
		Vertex vertex1 = new Vertex("1", Coordinates.of(22.22, 33.33));
		graphBuilder.addVertex(vertex1);
		Graph result = graphBuilder.asGraph();
		Vertex vertex2 = new Vertex("2", Coordinates.of(22.22, 33.33));
		graphBuilder.addVertex(vertex2);

		Assertions.assertArrayEquals(new Vertex[]{vertex1}, result.vertices().toArray());
	}

	@Test
	void should_return_reversed_graph(){
		final Vertex vertex = new Vertex("1", Coordinates.of(1, 1));
		final Vertex firstNeighbour = new Vertex("2", Coordinates.of(1, 1));
		final Vertex secondNeighbour = new Vertex("3", Coordinates.of(1, 1));
		Graph graph = new GraphBuilder()
			.addVertex(vertex)
			.addVertex(firstNeighbour)
			.addVertex(secondNeighbour)
			.connectByIds("1", "2", 50.)
			.connectByIds("1", "3", 50.)
			.asGraph();

		final Graph result = graph.reversed();

		Collection<Edge> expectedFirst = List.of(new Edge(firstNeighbour, vertex, 50.));
		Assertions.assertEquals(expectedFirst, result.getVertexEdges(firstNeighbour));
		Collection<Edge> expectedSecond = List.of(new Edge(secondNeighbour, vertex, 50.));
		Assertions.assertEquals(expectedSecond, result.getVertexEdges(secondNeighbour));
	}

	@Test
	void should_return_graph_vertices(){
		Vertex first = new Vertex("1", Coordinates.of(1, 1));
		Vertex second = new Vertex("2", Coordinates.of(1, 1));
		Vertex third = new Vertex("3", Coordinates.of(1, 1));
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
		Vertex vertex = new Vertex("1", Coordinates.of(1, 1));
		Vertex firstNeighbour = new Vertex("2", Coordinates.of(1, 1));
		Vertex secondNeighbour = new Vertex("3", Coordinates.of(1, 1));
		Graph graph = new GraphBuilder()
			.addVertex(vertex)
			.addVertex(firstNeighbour)
			.addVertex(secondNeighbour)
			.connectByIds("1", "2", 50.)
			.connectByIds("1", "3", 50.)
			.asGraph();

		Collection<Edge> result = graph.edges();

		Collection<Edge> expected = List.of(new Edge(vertex, firstNeighbour, 50.), new Edge(vertex, secondNeighbour, 50.));
		Assertions.assertEquals(expected, result);
	}

}
