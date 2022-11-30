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

import io.github.mtrevisan.mapmatcher.distances.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.distances.GeodeticCalculator;
import io.github.mtrevisan.mapmatcher.helpers.JTSGeometryHelper;
import org.apache.commons.lang3.StringUtils;
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
		final NearLineMergeGraph graph = new NearLineMergeGraph(500., new GeodeticCalculator());
		final Node from = new Node("0", new Coordinate(22.22, 33.33));
		final Node to = new Node("1", new Coordinate(33.22, 44.33));

		final LineString lineString = JTSGeometryHelper.createLineString(new Coordinate[]{from.getCoordinate(), to.getCoordinate()});
		final Set<Edge> addedEdges = (Set<Edge>)graph.addApproximateDirectEdge(lineString);

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
		final NearLineMergeGraph graph = new NearLineMergeGraph(0.5, new EuclideanCalculator());
		final LineString lineString12 = FACTORY.createLineString(new Coordinate[]{node.getCoordinate(), firstNeighbor.getCoordinate()});
		graph.addApproximateDirectEdge(lineString12);
		final LineString lineString13 = FACTORY.createLineString(new Coordinate[]{node.getCoordinate(), secondNeighbor.getCoordinate()});
		graph.addApproximateDirectEdge(lineString13);

		final List<Node> fromNodes = new ArrayList<>(graph.getNodesNear(node.getCoordinate()));
		Assertions.assertEquals(1, fromNodes.size());
		final Set<Edge> result = new HashSet<>(fromNodes.get(0).geOutEdges());

		final Set<Edge> expected = new HashSet<>(Arrays.asList(
			Edge.createDirectEdge(node, firstNeighbor, lineString12),
			Edge.createDirectEdge(node, secondNeighbor, lineString13)
		));
		Assertions.assertEquals(expected, result);
	}

	@Test
	void should_return_graph_vertices(){
		final Node node = new Node("0", new Coordinate(1., 1.));
		final Node firstNeighbor = new Node("1", new Coordinate(1., 2.));
		final Node secondNeighbor = new Node("2", new Coordinate(1., 3.));
		final NearLineMergeGraph graph = new NearLineMergeGraph(0.5, new EuclideanCalculator());
		final LineString lineString02 = FACTORY.createLineString(new Coordinate[]{node.getCoordinate(), firstNeighbor.getCoordinate()});
		graph.addApproximateDirectEdge("01", lineString02);
		final LineString lineString03 = FACTORY.createLineString(new Coordinate[]{node.getCoordinate(), secondNeighbor.getCoordinate()});
		graph.addApproximateDirectEdge("03", lineString03);

		final Set<Node> result = new HashSet<>(graph.nodes());

		final Set<Node> expected = new HashSet<>(List.of(node, firstNeighbor, secondNeighbor));
		Assertions.assertEquals(expected, result);
	}

	@Test
	void should_return_graph_edges(){
		final Node node = new Node("0", new Coordinate(1., 1.));
		final Node firstNeighbor = new Node("1", new Coordinate(1., 2.));
		final Node secondNeighbor = new Node("2", new Coordinate(1., 3.));
		final NearLineMergeGraph graph = new NearLineMergeGraph(1., new EuclideanCalculator());
		final LineString lineString02 = FACTORY.createLineString(new Coordinate[]{node.getCoordinate(), firstNeighbor.getCoordinate()});
		graph.addApproximateDirectEdge("01", lineString02);
		final LineString lineString03 = FACTORY.createLineString(new Coordinate[]{node.getCoordinate(), secondNeighbor.getCoordinate()});
		graph.addApproximateDirectEdge("02", lineString03);

		final Collection<Edge> result = new HashSet<>(graph.edges());

		final Set<Edge> expected = new HashSet<>(List.of(
			Edge.createDirectEdge(new Node("0", new Coordinate(1., 1.25)), new Node("1", new Coordinate(1., 1.25)), lineString02),
			Edge.createDirectEdge(new Node("0", new Coordinate(1., 1.25)), new Node("2", new Coordinate(1., 3.)), lineString03)
		));
		Assertions.assertEquals(expected, result);
	}

}
