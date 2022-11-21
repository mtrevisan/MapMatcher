package io.github.mtrevisan.mapmatcher.pathfinding;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.weight.LogMapEdgeWeightCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class ViterbiPathfinderTest{

	@Test
	void should_return_the_shortest_path1(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.euclideanDistanceTestGraphConnected();
		for(final Edge edge : testGraph.getGraph().edges())
			edge.setWeight(-StrictMath.log(edge.getWeight() / 100.));
		PathfindingStrategy pathfinder = new ViterbiPathfinder(new LogMapEdgeWeightCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

		Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
	}

	@Test
	void should_return_the_shortest_path2(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.euclideanDistanceTestGraphDisconnected();
		for(final Edge edge : testGraph.getGraph().edges())
			edge.setWeight(-StrictMath.log(edge.getWeight() / 100.));
		PathfindingStrategy pathfinder = new ViterbiPathfinder(new LogMapEdgeWeightCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

		Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
	}

	@Test
	void should_return_the_shortest_path3(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.vertexCountTestGraphConnected();
		for(final Edge edge : testGraph.getGraph().edges())
			edge.setWeight(-StrictMath.log(edge.getWeight() / 100.));
		PathfindingStrategy pathfinder = new ViterbiPathfinder(new LogMapEdgeWeightCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

		Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
	}

	@Test
	void should_return_the_shortest_path4(){
		ShortestPathPathfindingTestGraphs.TestGraphSummary testGraph = ShortestPathPathfindingTestGraphs.vertexCountTestGraphDisconnected();
		for(final Edge edge : testGraph.getGraph().edges())
			edge.setWeight(-StrictMath.log(edge.getWeight() / 100.));
		PathfindingStrategy pathfinder = new ViterbiPathfinder(new LogMapEdgeWeightCalculator());

		PathSummary path = pathfinder.findPath(testGraph.getStart(), testGraph.getEnd(), testGraph.getGraph());

		Assertions.assertEquals(testGraph.getShortestPath(), path.simplePath());
	}

}
