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
		Vertex A = new Vertex(1, new Coordinates(0, 0));
		Vertex B = new Vertex(2, new Coordinates(1, 1));
		Vertex C = new Vertex(3, new Coordinates(1, 3));
		Vertex D = new Vertex(4, new Coordinates(0, 2));
		Vertex E = new Vertex(5, new Coordinates(- 1, 2));
		Vertex F = new Vertex(6, new Coordinates(- 1, 6));
		Vertex G = new Vertex(7, new Coordinates(1, 3));
		Vertex H = new Vertex(8, new Coordinates(1, 4));
		Vertex I = new Vertex(9, new Coordinates(3, 4));
		Vertex J = new Vertex(10, new Coordinates(2, 4));
		Vertex K = new Vertex(11, new Coordinates(2, 6));
		Vertex M = new Vertex(12, new Coordinates(3, 6));

		GraphBuilder gb = new GraphBuilder();
		gb.addVertex(A).addVertex(B).addVertex(C).addVertex(D).addVertex(E).addVertex(F).addVertex(G).addVertex(H).addVertex(I).addVertex(J).addVertex(K).addVertex(M).connect(A, D, 50).connect(A, C, 50).connect(A, B, 50).connect(B, G, 50).connect(B, I, 50).connect(G, H, 50).connect(H, I, 50).connect(I, M, 50).connect(D, J, 50).connect(C, D, 50).connect(C, E, 50).connect(E, F, 50).connect(F, K, 50).connect(K, M, 50).connect(J, K, 50).connect(J, I, 50);

		return new TestGraphSummary(gb.asGraph(), A, M, new ArrayList<Vertex>(Arrays.asList(A, B, I, M)), new EuclideanDistanceTestEdgeWeightCalculator());
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
		Vertex A = new Vertex(1, new Coordinates(0, 0));
		Vertex B = new Vertex(2, new Coordinates(1, 1));
		Vertex C = new Vertex(3, new Coordinates(1, 3));
		Vertex D = new Vertex(4, new Coordinates(0, 2));
		Vertex E = new Vertex(5, new Coordinates(- 1, 2));
		Vertex F = new Vertex(6, new Coordinates(- 1, 6));
		Vertex G = new Vertex(7, new Coordinates(1, 3));
		Vertex H = new Vertex(8, new Coordinates(1, 4));
		Vertex I = new Vertex(9, new Coordinates(3, 4));
		Vertex J = new Vertex(10, new Coordinates(2, 4));
		Vertex K = new Vertex(11, new Coordinates(2, 6));
		Vertex M = new Vertex(12, new Coordinates(3, 6));

		GraphBuilder gb = new GraphBuilder();
		gb.addVertex(A).addVertex(B).addVertex(C).addVertex(D).addVertex(E).addVertex(F).addVertex(G).addVertex(H).addVertex(I).addVertex(J).addVertex(K).addVertex(M).connect(A, D, 50).connect(A, C, 50).connect(A, B, 50).connect(B, G, 50).connect(G, H, 50).connect(I, M, 50).connect(C, D, 50).connect(C, E, 50).connect(F, K, 50).connect(K, M, 50).connect(J, K, 50).connect(J, I, 50);

		return new TestGraphSummary(gb.asGraph(), A, M, new ArrayList<Vertex>(), new EuclideanDistanceTestEdgeWeightCalculator());
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
		Vertex A = new Vertex(1, new Coordinates(0, 0));
		Vertex B = new Vertex(2, new Coordinates(1, 1));
		Vertex C = new Vertex(3, new Coordinates(1, 3));
		Vertex D = new Vertex(4, new Coordinates(0, 2));
		Vertex E = new Vertex(5, new Coordinates(- 1, 2));
		Vertex F = new Vertex(6, new Coordinates(- 1, 6));
		Vertex G = new Vertex(7, new Coordinates(1, 3));
		Vertex H = new Vertex(8, new Coordinates(1, 4));
		Vertex I = new Vertex(9, new Coordinates(3, 4));
		Vertex J = new Vertex(10, new Coordinates(2, 4));
		Vertex K = new Vertex(11, new Coordinates(2, 6));
		Vertex M = new Vertex(12, new Coordinates(3, 6));

		GraphBuilder gb = new GraphBuilder();
		gb.addVertex(A).addVertex(B).addVertex(C).addVertex(D).addVertex(E).addVertex(F).addVertex(G).addVertex(H).addVertex(I).addVertex(J).addVertex(K).addVertex(M).connect(A, D, 50).connect(A, C, 50).connect(A, B, 50).connect(B, G, 50).connect(B, I, 50).connect(G, H, 50).connect(H, I, 50).connect(I, M, 50).connect(D, J, 50).connect(C, D, 50).connect(C, E, 50).connect(C, F, 50).connect(E, F, 50).connect(F, K, 50).connect(K, M, 50).connect(J, K, 50).connect(J, I, 50);

		return new TestGraphSummary(gb.asGraph(), A, M, new ArrayList<Vertex>(Arrays.asList(A, B, I, M)), new VertexCountEdgeWeightCalculator());
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
		Vertex A = new Vertex(1, new Coordinates(0, 0));
		Vertex B = new Vertex(2, new Coordinates(1, 1));
		Vertex C = new Vertex(3, new Coordinates(1, 3));
		Vertex D = new Vertex(4, new Coordinates(0, 2));
		Vertex E = new Vertex(5, new Coordinates(- 1, 2));
		Vertex F = new Vertex(6, new Coordinates(- 1, 6));
		Vertex G = new Vertex(7, new Coordinates(1, 3));
		Vertex H = new Vertex(8, new Coordinates(1, 4));
		Vertex I = new Vertex(9, new Coordinates(3, 4));
		Vertex J = new Vertex(10, new Coordinates(2, 4));
		Vertex K = new Vertex(11, new Coordinates(2, 6));
		Vertex M = new Vertex(12, new Coordinates(3, 6));

		GraphBuilder gb = new GraphBuilder();
		gb.addVertex(A).addVertex(B).addVertex(C).addVertex(D).addVertex(E).addVertex(F).addVertex(G).addVertex(H).addVertex(I).addVertex(J).addVertex(K).addVertex(M).connect(A, D, 50).connect(A, C, 50).connect(A, B, 50).connect(B, G, 50).connect(G, H, 50).connect(I, M, 50).connect(C, D, 50).connect(C, E, 50).connect(F, K, 50).connect(K, M, 50).connect(J, K, 50).connect(J, I, 50);

		return new TestGraphSummary(gb.asGraph(), A, M, new ArrayList<Vertex>(), new VertexCountEdgeWeightCalculator());
	}


	static class TestGraphSummary{

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

		private final Graph graph;
		private final Vertex start;
		private final Vertex end;
		private final List<Vertex> shortestPath;
		private final EdgeWeightCalculator calculator;
	}

}
