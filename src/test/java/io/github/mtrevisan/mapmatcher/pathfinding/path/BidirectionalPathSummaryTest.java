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
package io.github.mtrevisan.mapmatcher.pathfinding.path;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.topologies.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class BidirectionalPathSummaryTest{

	@Test
	void should_return_path_consisting_of_vertices(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Node first = new Node("0", factory.createPoint(1., 2.));
		Node second = new Node("1", factory.createPoint(2., 2.));
		List<Edge> path = new ArrayList<>(List.of(
			Edge.createDirectEdge(first, second, factory.createPolyline(first.getPoint(), second.getPoint()))
		));
		BidirectionalPathSummary pathSummary = BidirectionalPathSummary.ofPath(path, new HashSet<>(), new HashSet<>());

		List<Node> result = pathSummary.simplePath();

		Assertions.assertEquals(Arrays.asList(first, second), result);
	}

	@Test
	void should_return_empty_list_when_path_is_empty(){
		BidirectionalPathSummary pathSummary = BidirectionalPathSummary.ofPath(new ArrayList<>(), new HashSet<>(), new HashSet<>());

		List<Node> result = pathSummary.simplePath();

		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	void should_return_the_number_of_vertices_in_path(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		final Node first = new Node("0", factory.createPoint(1., 2.));
		final Node second = new Node("1", factory.createPoint(2., 2.));
		List<Edge> path = new ArrayList<>(List.of(
			Edge.createDirectEdge(first, second, factory.createPolyline(first.getPoint(), second.getPoint()))
		));
		BidirectionalPathSummary pathSummary = BidirectionalPathSummary.ofPath(path, new HashSet<>(), new HashSet<>());

		int result = pathSummary.numberOfVertices();

		Assertions.assertEquals(2, result);
	}

	@Test
	void should_return_0_when_path_is_empty(){
		List<Edge> path = new ArrayList<>();
		BidirectionalPathSummary pathSummary = BidirectionalPathSummary.ofPath(path, new HashSet<>(), new HashSet<>());

		int result = pathSummary.numberOfVertices();

		Assertions.assertEquals(0, result);
	}

	@Test
	void should_return_the_number_of_visited_vertices(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Set<Node> visitedVertices = new HashSet<>(Arrays.asList(
			new Node("0", factory.createPoint(1., 1.)),
			new Node("1", factory.createPoint(2., 2.))
		));
		BidirectionalPathSummary pathSummary = BidirectionalPathSummary.ofPath(new ArrayList<>(), visitedVertices, visitedVertices);

		int result = pathSummary.totalVisitedVertices();

		Assertions.assertEquals(4, result);
	}

	@Test
	void should_return_path_distance(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		final Node first = new Node("0", factory.createPoint(121.058805, 14.552797));
		final Node second = new Node("1", factory.createPoint(120.994260, 14.593999));
		ArrayList<Edge> path = new ArrayList<>(List.of(
			Edge.createDirectEdge(first, second, factory.createPolyline(first.getPoint(), second.getPoint()))
		));
		BidirectionalPathSummary pathSummary = BidirectionalPathSummary.ofPath(path, new HashSet<>(), new HashSet<>());

		double result = pathSummary.totalDistance();

		Assertions.assertEquals(8_316.3, result, 0.05);
	}

	@Test
	void should_return_path_duration(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		final Node first = new Node("0", factory.createPoint(121.058805, 14.552797));
		final Node second = new Node("1", factory.createPoint(120.994260, 14.593999));
		final Edge edge = Edge.createDirectEdge(first, second, factory.createPolyline(first.getPoint(), second.getPoint()));
		edge.setWeight(50.);
		ArrayList<Edge> path = new ArrayList<>(List.of(edge));
		BidirectionalPathSummary pathSummary = BidirectionalPathSummary.ofPath(path, new HashSet<>(), new HashSet<>());

		double result = pathSummary.totalDuration();

		Assertions.assertEquals(10.0, result, 0.05);
	}

	@Test
	void should_return_whether_the_path_is_empty(){
		List<Edge> path = new ArrayList<>();
		BidirectionalPathSummary pathSummary = BidirectionalPathSummary.ofPath(path, new HashSet<>(), new HashSet<>());

		Assertions.assertTrue(pathSummary.simplePath().isEmpty());
		Assertions.assertFalse(pathSummary.isFound());


		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Node node1 = new Node("0", factory.createPoint(14.552797, 121.058805));
		Node node2 = new Node("1", factory.createPoint(14.593999, 120.994260));
		path = new ArrayList<>(List.of(
			Edge.createDirectEdge(node1, node2, factory.createPolyline(node1.getPoint(), node2.getPoint()))
		));
		pathSummary = BidirectionalPathSummary.ofPath(path, new HashSet<>(), new HashSet<>());

		Assertions.assertEquals(Arrays.asList(node1, node2), pathSummary.simplePath());
		Assertions.assertTrue(pathSummary.isFound());
	}

}
