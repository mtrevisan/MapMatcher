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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;


class PathSummaryCreatorTest{

	@Test
	void should_return_path_between_start_and_end_node_present_in_predecessor_tree(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Node start = Node.of("0", factory.createPoint(1., 1.));
		Node middle = Node.of("1", factory.createPoint(1., 2.));
		Node end = Node.of("2", factory.createPoint(1., 3.));
		Map<Node, Edge> predecessorTree = new LinkedHashMap<>(2);
		predecessorTree.put(middle, Edge.createDirectEdge(start, middle));
		predecessorTree.put(end, Edge.createDirectEdge(middle, end));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createUnidirectionalPath(start, end, predecessorTree);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList(start, middle, end)), result.simplePath());
	}

	@Test
	void should_return_an_empty_path_when_start_node_and_end_node_are_not_connected_in_predecessor_tree(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Node start = Node.of("0", factory.createPoint(1., 1.));
		Node randomNode = Node.of("1", factory.createPoint(1., 1.));
		Node middle = Node.of("2", factory.createPoint(1., 1.));
		Node end = Node.of("3", factory.createPoint(1., 1.));
		Map<Node, Edge> predecessorTree = new LinkedHashMap<>(2);
		predecessorTree.put(middle, Edge.createDirectEdge(randomNode, middle));
		predecessorTree.put(end, Edge.createDirectEdge(middle, end));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createUnidirectionalPath(start, end, predecessorTree);

		Assertions.assertFalse(result.isFound());
		Assertions.assertTrue(result.simplePath().isEmpty());
	}

	@Test
	void should_return_an_empty_path_when_start_node_and_mid_node_are_not_connected_in_predecessor_tree(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Node start = Node.of("0", factory.createPoint(1., 1.));
		Node randomNode = Node.of("1", factory.createPoint(1., 2.));
		Node middle = Node.of("2", factory.createPoint(1., 3.));
		Node end = Node.of("3", factory.createPoint(1., 4.));
		Map<Node, Edge> predecessorTreeStart = new LinkedHashMap<>(1);
		predecessorTreeStart.put(middle, Edge.createDirectEdge(randomNode, middle));
		Map<Node, Edge> predecessorTreeEnd = new LinkedHashMap<>(1);
		predecessorTreeEnd.put(middle, Edge.createDirectEdge(end, middle));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createBidirectionalPath(start, middle, end, predecessorTreeStart, predecessorTreeEnd);

		Assertions.assertFalse(result.isFound());
		Assertions.assertTrue(result.simplePath().isEmpty());
	}

	@Test
	void should_return_an_empty_path_when_mid_node_and_end_node_are_not_connected_in_predecessor_tree(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Node start = Node.of("0", factory.createPoint(1., 1.));
		Node randomNode = Node.of("1", factory.createPoint(1., 2.));
		Node middle = Node.of("2", factory.createPoint(1., 3.));
		Node end = Node.of("3", factory.createPoint(1., 4.));
		Map<Node, Edge> predecessorTreeStart = new LinkedHashMap<>(1);
		predecessorTreeStart.put(middle, Edge.createDirectEdge(start, middle));
		Map<Node, Edge> predecessorTreeEnd = new LinkedHashMap<>(1);
		predecessorTreeEnd.put(middle, Edge.createDirectEdge(randomNode, middle));

		PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
		PathSummary result = pathSummaryCreator.createBidirectionalPath(start, middle, end, predecessorTreeStart, predecessorTreeEnd);

		Assertions.assertFalse(result.isFound());
		Assertions.assertTrue(result.simplePath().isEmpty());
	}

}
