package io.github.mtrevisan.mapmatcher.pathfinding;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.GraphBuilder;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.weight.EdgeWeightCalculator;
import io.github.mtrevisan.mapmatcher.weight.VertexCountEdgeWeightCalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class ShortestPathPathfindingTestGraphs{

	static TestGraphSummary euclideanDistanceTestGraphConnected(){
        /*
         Test graph structure
              H(1,4) ---- I(3,4)  --- M(3,6)
                |      /     |        |
             G(1,3)  /      |         |
               |   /      J(2,4) --- K(2,6)
             B(1,1)      /            |
             |         /              |
         A(0,0) --- D (0,2)           |
             \    /                   |
           C(-1,1) ---- E(-1,2) --- F(-1,6)
          legend:
          * start - A
          * end - M
        */
		Vertex vertexA = new Vertex(1, new Coordinates(0, 0));
		Vertex vertexB = new Vertex(2, new Coordinates(1, 1));
		Vertex vertexC = new Vertex(3, new Coordinates(1, 3));
		Vertex vertexD = new Vertex(4, new Coordinates(0, 2));
		Vertex vertexE = new Vertex(5, new Coordinates(- 1, 2));
		Vertex vertexF = new Vertex(6, new Coordinates(- 1, 6));
		Vertex vertexG = new Vertex(7, new Coordinates(1, 3));
		Vertex vertexH = new Vertex(8, new Coordinates(1, 4));
		Vertex vertexI = new Vertex(9, new Coordinates(3, 4));
		Vertex vertexJ = new Vertex(10, new Coordinates(2, 4));
		Vertex vertexK = new Vertex(11, new Coordinates(2, 6));
		Vertex vertexM = new Vertex(12, new Coordinates(3, 6));

		GraphBuilder gb = new GraphBuilder();
		gb.addVertex(vertexA)
			.addVertex(vertexB)
			.addVertex(vertexC)
			.addVertex(vertexD)
			.addVertex(vertexE)
			.addVertex(vertexF)
			.addVertex(vertexG)
			.addVertex(vertexH)
			.addVertex(vertexI)
			.addVertex(vertexJ)
			.addVertex(vertexK)
			.addVertex(vertexM)
			.connect(vertexA, vertexD, 50.)
			.connect(vertexA, vertexC, 50.)
			.connect(vertexA, vertexB, 50.)
			.connect(vertexB, vertexG, 50.)
			.connect(vertexB, vertexI, 50.)
			.connect(vertexG, vertexH, 50.)
			.connect(vertexH, vertexI, 50.)
			.connect(vertexI, vertexM, 50.)
			.connect(vertexD, vertexJ, 50.)
			.connect(vertexC, vertexD, 50.)
			.connect(vertexC, vertexE, 50.)
			.connect(vertexE, vertexF, 50.)
			.connect(vertexF, vertexK, 50.)
			.connect(vertexK, vertexM, 50.)
			.connect(vertexJ, vertexK, 50.)
			.connect(vertexJ, vertexI, 50.);

		return new TestGraphSummary(gb.asGraph(), vertexA, vertexM, new ArrayList<Vertex>(Arrays.asList(vertexA, vertexB, vertexI, vertexM)), new EuclideanDistanceTestEdgeWeightCalculator());
	}

	static TestGraphSummary euclideanDistanceTestGraphDisconnected(){
        /*
         Test graph structure
              H(1,4)      I(3,4)  --- M(3,6)
                |           |        |
             G(1,3)        |         |
               |          J(2,4) --- K(2,6)
             B(1,1)                   |
             |                        |
         A(0,0) --- D (0,2)           |
             \    /                   |
           C(-1,1) ---- E(-1,2)  F(-1,6)
          legend:
          * start - A
          * end - M
        */
		Vertex vertexA = new Vertex(1, new Coordinates(0, 0));
		Vertex vertexB = new Vertex(2, new Coordinates(1, 1));
		Vertex vertexC = new Vertex(3, new Coordinates(1, 3));
		Vertex vertexD = new Vertex(4, new Coordinates(0, 2));
		Vertex vertexE = new Vertex(5, new Coordinates(- 1, 2));
		Vertex vertexF = new Vertex(6, new Coordinates(- 1, 6));
		Vertex vertexG = new Vertex(7, new Coordinates(1, 3));
		Vertex vertexH = new Vertex(8, new Coordinates(1, 4));
		Vertex vertexI = new Vertex(9, new Coordinates(3, 4));
		Vertex vertexJ = new Vertex(10, new Coordinates(2, 4));
		Vertex vertexK = new Vertex(11, new Coordinates(2, 6));
		Vertex vertexM = new Vertex(12, new Coordinates(3, 6));

		GraphBuilder gb = new GraphBuilder();
		gb.addVertex(vertexA)
			.addVertex(vertexB)
			.addVertex(vertexC)
			.addVertex(vertexD)
			.addVertex(vertexE)
			.addVertex(vertexF)
			.addVertex(vertexG)
			.addVertex(vertexH)
			.addVertex(vertexI)
			.addVertex(vertexJ)
			.addVertex(vertexK)
			.addVertex(vertexM)
			.connect(vertexA, vertexD, 50.)
			.connect(vertexA, vertexC, 50.)
			.connect(vertexA, vertexB, 50.)
			.connect(vertexB, vertexG, 50.)
			.connect(vertexG, vertexH, 50.)
			.connect(vertexI, vertexM, 50.)
			.connect(vertexC, vertexD, 50.)
			.connect(vertexC, vertexE, 50.)
			.connect(vertexF, vertexK, 50.)
			.connect(vertexK, vertexM, 50.)
			.connect(vertexJ, vertexK, 50.)
			.connect(vertexJ, vertexI, 50.);

		return new TestGraphSummary(gb.asGraph(), vertexA, vertexM, new ArrayList<Vertex>(), new EuclideanDistanceTestEdgeWeightCalculator());
	}

	static TestGraphSummary vertexCountTestGraphConnected(){
        /*
         Test graph structure
              H(1,4) ---- I(3,4)  --- M(3,6)
                |      /     |        |
             G(1,3)  /      |         |
               |   /      J(2,4) --- K(2,6)
             B(1,1)      /            |
             |         /              |
         A(0,0) --- D (0,2)           |
             \    /                   |
           C(-1,1) ---- E(-1,2) --- F(-1,6)
          legend:
          * start - A
          * end - M
        */
		Vertex vertexA = new Vertex(1, new Coordinates(0, 0));
		Vertex vertexB = new Vertex(2, new Coordinates(1, 1));
		Vertex vertexC = new Vertex(3, new Coordinates(1, 3));
		Vertex vertexD = new Vertex(4, new Coordinates(0, 2));
		Vertex vertexE = new Vertex(5, new Coordinates(- 1, 2));
		Vertex vertexF = new Vertex(6, new Coordinates(- 1, 6));
		Vertex vertexG = new Vertex(7, new Coordinates(1, 3));
		Vertex vertexH = new Vertex(8, new Coordinates(1, 4));
		Vertex vertexI = new Vertex(9, new Coordinates(3, 4));
		Vertex vertexJ = new Vertex(10, new Coordinates(2, 4));
		Vertex vertexK = new Vertex(11, new Coordinates(2, 6));
		Vertex vertexM = new Vertex(12, new Coordinates(3, 6));

		GraphBuilder gb = new GraphBuilder();
		gb.addVertex(vertexA)
			.addVertex(vertexB)
			.addVertex(vertexC)
			.addVertex(vertexD)
			.addVertex(vertexE)
			.addVertex(vertexF)
			.addVertex(vertexG)
			.addVertex(vertexH)
			.addVertex(vertexI)
			.addVertex(vertexJ)
			.addVertex(vertexK)
			.addVertex(vertexM)
			.connect(vertexA, vertexD, 50.)
			.connect(vertexA, vertexC, 50.)
			.connect(vertexA, vertexB, 50.)
			.connect(vertexB, vertexG, 50.)
			.connect(vertexB, vertexI, 50.)
			.connect(vertexG, vertexH, 50.)
			.connect(vertexH, vertexI, 50.)
			.connect(vertexI, vertexM, 50.)
			.connect(vertexD, vertexJ, 50.)
			.connect(vertexC, vertexD, 50.)
			.connect(vertexC, vertexE, 50.)
			.connect(vertexC, vertexF, 50.)
			.connect(vertexE, vertexF, 50.)
			.connect(vertexF, vertexK, 50.)
			.connect(vertexK, vertexM, 50.)
			.connect(vertexJ, vertexK, 50.)
			.connect(vertexJ, vertexI, 50.);

		return new TestGraphSummary(gb.asGraph(), vertexA, vertexM, new ArrayList<Vertex>(Arrays.asList(vertexA, vertexB, vertexI, vertexM)), new VertexCountEdgeWeightCalculator());
	}

	static TestGraphSummary vertexCountTestGraphDisconnected(){
        /*
         Test graph structure
              H(1,4)      I(3,4)  --- M(3,6)
                |           |        |
             G(1,3)        |         |
               |          J(2,4) --- K(2,6)
             B(1,1)                   |
             |                        |
         A(0,0) --- D (0,2)           |
             \    /                   |
           C(-1,1) ---- E(-1,2)  F(-1,6)
          legend:
          * start - A
          * end - M
        */
		Vertex vertexA = new Vertex(1, new Coordinates(0, 0));
		Vertex vertexB = new Vertex(2, new Coordinates(1, 1));
		Vertex vertexC = new Vertex(3, new Coordinates(1, 3));
		Vertex vertexD = new Vertex(4, new Coordinates(0, 2));
		Vertex vertexE = new Vertex(5, new Coordinates(- 1, 2));
		Vertex vertexF = new Vertex(6, new Coordinates(- 1, 6));
		Vertex vertexG = new Vertex(7, new Coordinates(1, 3));
		Vertex vertexH = new Vertex(8, new Coordinates(1, 4));
		Vertex vertexI = new Vertex(9, new Coordinates(3, 4));
		Vertex vertexJ = new Vertex(10, new Coordinates(2, 4));
		Vertex vertexK = new Vertex(11, new Coordinates(2, 6));
		Vertex vertexM = new Vertex(12, new Coordinates(3, 6));

		GraphBuilder gb = new GraphBuilder();
		gb.addVertex(vertexA)
			.addVertex(vertexB)
			.addVertex(vertexC)
			.addVertex(vertexD)
			.addVertex(vertexE)
			.addVertex(vertexF)
			.addVertex(vertexG)
			.addVertex(vertexH)
			.addVertex(vertexI)
			.addVertex(vertexJ)
			.addVertex(vertexK)
			.addVertex(vertexM)
			.connect(vertexA, vertexD, 50.)
			.connect(vertexA, vertexC, 50.)
			.connect(vertexA, vertexB, 50.)
			.connect(vertexB, vertexG, 50.)
			.connect(vertexG, vertexH, 50.)
			.connect(vertexI, vertexM, 50.)
			.connect(vertexC, vertexD, 50.)
			.connect(vertexC, vertexE, 50.)
			.connect(vertexF, vertexK, 50.)
			.connect(vertexK, vertexM, 50.)
			.connect(vertexJ, vertexK, 50.)
			.connect(vertexJ, vertexI, 50.);

		return new TestGraphSummary(gb.asGraph(), vertexA, vertexM, new ArrayList<Vertex>(), new VertexCountEdgeWeightCalculator());
	}


	static class TestGraphSummary{

		private final Graph graph;
		private final Vertex start;
		private final Vertex end;
		private final List<Vertex> shortestPath;
		private final EdgeWeightCalculator calculator;


		TestGraphSummary(Graph graph, Vertex start, Vertex end, List<Vertex> shortestPath, EdgeWeightCalculator calculator){
			this.graph = graph;
			this.start = start;
			this.end = end;
			this.shortestPath = shortestPath;
			this.calculator = calculator;
		}

		public final Graph getGraph(){
			return graph;
		}

		public final Vertex getStart(){
			return start;
		}

		public final Vertex getEnd(){
			return end;
		}

		public final List<Vertex> getShortestPath(){
			return shortestPath;
		}

		public final EdgeWeightCalculator getCalculator(){
			return calculator;
		}

	}

}
