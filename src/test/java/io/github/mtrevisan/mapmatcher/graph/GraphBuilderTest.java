package io.github.mtrevisan.mapmatcher.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


class GraphBuilderTest{

	@Test
	void should_add_an_vertex_to_graph(){
		GraphBuilder graphBuilder = new GraphBuilder();
		Vertex insertedVertex = new Vertex(1, new Coordinates(22.22, 33.33));

		graphBuilder.addVertex(insertedVertex);

		Assertions.assertTrue(graphBuilder.asGraph().vertices().contains(insertedVertex));
	}

	@Test
	void should_connect_two_vertices_in_graph(){
		GraphBuilder graphBuilder = new GraphBuilder();
		Vertex from = new Vertex(1, new Coordinates(22.22, 33.33));
		final Vertex to = new Vertex(2, new Coordinates(33.22, 44.33));
		graphBuilder.addVertex(from);
		graphBuilder.addVertex(to);

		graphBuilder.connect(from, to, 50);

		Collection<Edge> edges = graphBuilder.asGraph().getVertexEdges(from);
		Collection<Edge> expected = new ArrayList<>(Arrays.asList(
			new Edge(from, to, 50)
		));
		Assertions.assertEquals(expected, edges);
	}

	@Test
	void should_throw_an_exception_when_from_vertex_is_not_present_in_graph(){
		GraphBuilder graphBuilder = new GraphBuilder();
		Vertex from = new Vertex(1, new Coordinates(22.22, 33.33));
		Vertex to = new Vertex(2, new Coordinates(33.22, 44.33));
		graphBuilder.addVertex(to);

		Executable when = () -> graphBuilder.connect(from, to, 50);

		Assertions.assertThrows(VertexNotPresentException.class, when);
	}

	@Test
	void should_throw_an_exception_when_to_vertex_is_not_present_in_graph(){
		GraphBuilder graphBuilder = new GraphBuilder();
		Vertex from = new Vertex(1, new Coordinates(22.22, 33.33));
		Vertex to = new Vertex(2, new Coordinates(33.22, 44.33));
		graphBuilder.addVertex(from);

		Executable when = () -> graphBuilder.connect(from, to, 50);

		Assertions.assertThrows(VertexNotPresentException.class, when);
	}

	@Test
	void should_throw_an_exception_when_from_vertex_is_not_present(){
		GraphBuilder graphBuilder = new GraphBuilder();
		Vertex from = new Vertex(1, new Coordinates(22.22, 33.33));
		Vertex to = new Vertex(2, new Coordinates(33.22, 44.33));
		graphBuilder.addVertex(to);

		Executable when = () -> graphBuilder.connectByIds(from.getId(), to.getId(), 50);

		Assertions.assertThrows(VertexNotPresentException.class, when);
	}

	@Test
	public void should_throw_an_exception_when_to_vertex_is_not_present(){
		GraphBuilder graphBuilder = new GraphBuilder();
		Vertex from = new Vertex(1, new Coordinates(22.22, 33.33));
		Vertex to = new Vertex(2, new Coordinates(33.22, 44.33));
		graphBuilder.addVertex(from);

		Executable when = () -> graphBuilder.connectByIds(from.getId(), to.getId(), 50);

		Assertions.assertThrows(VertexNotPresentException.class, when);
	}

}
