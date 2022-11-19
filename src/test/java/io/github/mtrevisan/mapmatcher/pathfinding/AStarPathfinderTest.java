package io.github.mtrevisan.mapmatcher.pathfinding;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;


class AStarPathfinderTest{

	@Test
	void should_return_the_shortest_path(){
		List<ShortestPathPathfindingTestGraphs.TestGraphSummary> testGraphs = Arrays.asList(
			ShortestPathPathfindingTestGraphs.euclideanDistanceTestGraphConnected(),
			ShortestPathPathfindingTestGraphs.euclideanDistanceTestGraphDisconnected(),
			ShortestPathPathfindingTestGraphs.vertexCountTestGraphConnected(),
			ShortestPathPathfindingTestGraphs.vertexCountTestGraphDisconnected()
		);
		for(ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph : testGraphs){
			AStarPathfinder pathfinder = new AStarPathfinder(testGraph.getCalculator());

			PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

			Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
		}
	}

}
