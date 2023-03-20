/**
 * Copyright (c) 2022 Mauro Trevisan
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mtrevisan.mapmatcher.graph;

import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.topologies.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class GraphTest{

	@Test
	void should_connect_two_vertices_in_graph(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		final NearNodeMergeGraph graph = new NearNodeMergeGraph(500.);
		final Node from = Node.of("0", factory.createPoint(22.22, 33.33));
		final Node to = Node.of("1", factory.createPoint(33.22, 44.33));

		final Set<Edge> addedEdges = (Set<Edge>)graph.addApproximateDirectEdge(from.getPoint(), to.getPoint());

		final List<Node> fromNodes = new ArrayList<>(graph.getNodesNear(from.getPoint()));
		Assertions.assertEquals(1, fromNodes.size());
		final Collection<Edge> edges = fromNodes.get(0).getOutEdges();
		Assertions.assertEquals(addedEdges, edges);
	}

	@Test
	void should_return_the_edges_of_an_node(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		final Node node = Node.of("0", factory.createPoint(1., 1.));
		final Node firstNeighbor = Node.of("1", factory.createPoint(1., 2.));
		final Node secondNeighbor = Node.of("2", factory.createPoint(1., 3.));
		final NearNodeMergeGraph graph = new NearNodeMergeGraph(0.5);
		graph.addApproximateDirectEdge("0-1", node.getPoint(), firstNeighbor.getPoint());
		graph.addApproximateDirectEdge("0-2", node.getPoint(), secondNeighbor.getPoint());

		final List<Node> fromNodes = new ArrayList<>(graph.getNodesNear(node.getPoint()));
		Assertions.assertEquals(1, fromNodes.size());
		final Set<Edge> result = new HashSet<>(fromNodes.get(0).getOutEdges());

		final Set<Edge> expected = new HashSet<>(Arrays.asList(
			Edge.createDirectEdge(node, firstNeighbor),
			Edge.createDirectEdge(node, secondNeighbor)
		));
		Assertions.assertEquals(expected, result);
	}

	@Test
	void should_return_graph_vertices(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		final Node node = Node.of("0", factory.createPoint(1., 1.));
		final Node firstNeighbor = Node.of("1", factory.createPoint(1., 2.));
		final Node secondNeighbor = Node.of("2", factory.createPoint(1., 3.));
		final NearNodeMergeGraph graph = new NearNodeMergeGraph(0.5);
		graph.addApproximateDirectEdge("01", node.getPoint(), firstNeighbor.getPoint());
		graph.addApproximateDirectEdge("03", node.getPoint(), secondNeighbor.getPoint());

		final Set<Node> result = new HashSet<>(graph.nodes());

		final Set<Node> expected = new HashSet<>(List.of(node, firstNeighbor, secondNeighbor));
		Assertions.assertEquals(expected, result);
	}

	@Test
	void should_return_graph_edges(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		final Node node = Node.of("0-1/from,0-2/from", factory.createPoint(1., 1.));
		final Node firstNeighbor = Node.of("0-1/to", factory.createPoint(1., 2.));
		final Node secondNeighbor = Node.of("0-2/to", factory.createPoint(1., 3.));
		final NearNodeMergeGraph graph = new NearNodeMergeGraph(0.5);
		graph.addApproximateDirectEdge("0-1", node.getPoint(), firstNeighbor.getPoint());
		graph.addApproximateDirectEdge("0-2", node.getPoint(), secondNeighbor.getPoint());

		final Collection<Edge> result = new HashSet<>(graph.edges());

		final Set<Edge> expected = new HashSet<>(List.of(
			Edge.createDirectEdge(Node.of("0", factory.createPoint(1., 1.)), Node.of("1", factory.createPoint(1., 2.))),
			Edge.createDirectEdge(Node.of("0", factory.createPoint(1., 1.)), Node.of("2", factory.createPoint(1., 3.)))
		));
		Assertions.assertEquals(expected, result);
	}

}
