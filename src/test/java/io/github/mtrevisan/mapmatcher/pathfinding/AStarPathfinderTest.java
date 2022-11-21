package io.github.mtrevisan.mapmatcher.pathfinding;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class AStarPathfinderTest{

	@Test
	void should_return_the_shortest_path1(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.euclideanDistanceTestGraphConnected();
		PathfindingStrategy pathfinder = new AStarPathfinder(testGraph.getCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

		Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
	}

	@Test
	void should_return_the_shortest_path2(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.euclideanDistanceTestGraphDisconnected();
		PathfindingStrategy pathfinder = new AStarPathfinder(testGraph.getCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

		Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
	}

	@Test
	void should_return_the_shortest_path3(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.vertexCountTestGraphConnected();
		PathfindingStrategy pathfinder = new AStarPathfinder(testGraph.getCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

		Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
	}

	@Test
	void should_return_the_shortest_path4(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.vertexCountTestGraphDisconnected();
		PathfindingStrategy pathfinder = new AStarPathfinder(testGraph.getCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

		Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
	}

}
