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
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.distances.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.spatial.distances.GeodeticCalculator;
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
		GeometryFactory factory = new GeometryFactory(new GeodeticCalculator());
		final NearLineMergeGraph graph = new NearLineMergeGraph(500.);
		final Node from = new Node("0", factory.createPoint(22.22, 33.33));
		final Node to = new Node("1", factory.createPoint(33.22, 44.33));

		final Polyline polyline = factory.createPolyline(from.getPoint(), to.getPoint());
		final Set<Edge> addedEdges = (Set<Edge>)graph.addApproximateDirectEdge(polyline);

		final List<Node> fromNodes = new ArrayList<>(graph.getNodesNear(from.getPoint()));
		Assertions.assertEquals(1, fromNodes.size());
		final Collection<Edge> edges = fromNodes.get(0).getOutEdges();
		Assertions.assertEquals(addedEdges, edges);
	}

	@Test
	void should_return_the_edges_of_an_node(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		final Node node = new Node("0", factory.createPoint(1., 1.));
		final Node firstNeighbor = new Node("1", factory.createPoint(1., 2.));
		final Node secondNeighbor = new Node("2", factory.createPoint(1., 3.));
		final NearLineMergeGraph graph = new NearLineMergeGraph(0.5);
		final Polyline polyline12 = factory.createPolyline(node.getPoint(), firstNeighbor.getPoint());
		graph.addApproximateDirectEdge(polyline12);
		final Polyline polyline13 = factory.createPolyline(node.getPoint(), secondNeighbor.getPoint());
		graph.addApproximateDirectEdge(polyline13);

		final List<Node> fromNodes = new ArrayList<>(graph.getNodesNear(node.getPoint()));
		Assertions.assertEquals(1, fromNodes.size());
		final Set<Edge> result = new HashSet<>(fromNodes.get(0).getOutEdges());

		final Set<Edge> expected = new HashSet<>(Arrays.asList(
			Edge.createDirectEdge(node, firstNeighbor, polyline12),
			Edge.createDirectEdge(node, secondNeighbor, polyline13)
		));
		Assertions.assertEquals(expected, result);
	}

	@Test
	void should_return_graph_vertices(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		final Node node = new Node("0", factory.createPoint(1., 1.));
		final Node firstNeighbor = new Node("1", factory.createPoint(1., 2.));
		final Node secondNeighbor = new Node("2", factory.createPoint(1., 3.));
		final NearLineMergeGraph graph = new NearLineMergeGraph(0.5);
		final Polyline polyline02 = factory.createPolyline(node.getPoint(), firstNeighbor.getPoint());
		graph.addApproximateDirectEdge("01", polyline02);
		final Polyline polyline03 = factory.createPolyline(node.getPoint(), secondNeighbor.getPoint());
		graph.addApproximateDirectEdge("03", polyline03);

		final Set<Node> result = new HashSet<>(graph.nodes());

		final Set<Node> expected = new HashSet<>(List.of(node, firstNeighbor, secondNeighbor));
		Assertions.assertEquals(expected, result);
	}

	@Test
	void should_return_graph_edges(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		final Node node = new Node("0", factory.createPoint(1., 1.));
		final Node firstNeighbor = new Node("1", factory.createPoint(1., 2.));
		final Node secondNeighbor = new Node("2", factory.createPoint(1., 3.));
		final NearLineMergeGraph graph = new NearLineMergeGraph(1.);
		final Polyline polyline02 = factory.createPolyline(node.getPoint(), firstNeighbor.getPoint());
		graph.addApproximateDirectEdge("01", polyline02);
		final Polyline polyline03 = factory.createPolyline(node.getPoint(), secondNeighbor.getPoint());
		graph.addApproximateDirectEdge("02", polyline03);

		final Collection<Edge> result = new HashSet<>(graph.edges());

		final Set<Edge> expected = new HashSet<>(List.of(
			Edge.createDirectEdge(new Node("0", factory.createPoint(1., 1.25)), new Node("1", factory.createPoint(1., 1.25)), polyline02),
			Edge.createDirectEdge(new Node("0", factory.createPoint(1., 1.25)), new Node("2", factory.createPoint(1., 3.)), polyline03)
		));
		Assertions.assertEquals(expected, result);
	}

}
