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
package io.github.mtrevisan.mapmatcher.pathfinding;

import io.github.mtrevisan.mapmatcher.pathfinding.path.PathSummary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class AStarPathFinderTest{

	@Test
	void should_return_void_path(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.euclideanDistanceTestGraphConnected();
		PathFindingStrategy pathfinder = new AStarPathFinder(testGraph.getCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getStart(), testGraph.getGraph());

		Assertions.assertEquals(1, path.simplePath().size());
	}

	@Test
	void should_return_the_shortest_path1(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.euclideanDistanceTestGraphConnected();
		PathFindingStrategy pathfinder = new AStarPathFinder(testGraph.getCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

		Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
	}

	@Test
	void should_return_the_shortest_path2(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.euclideanDistanceTestGraphDisconnected();
		PathFindingStrategy pathfinder = new AStarPathFinder(testGraph.getCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

		Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
	}

	@Test
	void should_return_the_shortest_path3(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.nodeCountTestGraphConnected();
		PathFindingStrategy pathfinder = new AStarPathFinder(testGraph.getCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

		Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
	}

	@Test
	void should_return_the_shortest_path4(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.nodeCountTestGraphDisconnected();
		PathFindingStrategy pathfinder = new AStarPathFinder(testGraph.getCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

		Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
	}

}
