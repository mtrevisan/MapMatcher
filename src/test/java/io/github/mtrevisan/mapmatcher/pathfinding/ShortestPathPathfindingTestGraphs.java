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

import io.github.mtrevisan.mapmatcher.distances.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.NearLineMergeGraph;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.helpers.spatial.Coordinate;
import io.github.mtrevisan.mapmatcher.helpers.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.EdgeWeightCalculator;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.NodeCountCalculator;

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
		final Node nodeA = new Node("0", Coordinate.of(0., 0.));
		final Node nodeB = new Node("1", Coordinate.of(1., 1.));
		final Node nodeC = new Node("2", Coordinate.of(3., 1.));
		final Node nodeD = new Node("3", Coordinate.of(2., 0.));
		final Node nodeE = new Node("4", Coordinate.of(2., -1.));
		final Node nodeF = new Node("5", Coordinate.of(6., -1.));
		final Node nodeG = new Node("6", Coordinate.of(3., 1.));
		final Node nodeH = new Node("7", Coordinate.of(4., 1.));
		final Node nodeI = new Node("8", Coordinate.of(4., 3.));
		final Node nodeJ = new Node("9", Coordinate.of(4., 2.));
		final Node nodeK = new Node("10", Coordinate.of(6., 2.));
		final Node nodeM = new Node("11", Coordinate.of(6., 3.));

		final NearLineMergeGraph gb = new NearLineMergeGraph(0.5, new EuclideanCalculator());
		gb.addApproximateDirectEdge(Polyline.of(nodeA.getCoordinate(), nodeD.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeA.getCoordinate(), nodeC.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeA.getCoordinate(), nodeB.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeB.getCoordinate(), nodeG.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeB.getCoordinate(), nodeI.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeG.getCoordinate(), nodeH.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeH.getCoordinate(), nodeI.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeI.getCoordinate(), nodeM.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeD.getCoordinate(), nodeJ.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeC.getCoordinate(), nodeD.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeC.getCoordinate(), nodeE.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeE.getCoordinate(), nodeF.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeF.getCoordinate(), nodeK.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeK.getCoordinate(), nodeM.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeJ.getCoordinate(), nodeK.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeJ.getCoordinate(), nodeI.getCoordinate()));

		final Node startNode = new ArrayList<>(gb.getNodesNear(nodeA.getCoordinate())).get(0);
		final Node endNode = new ArrayList<>(gb.getNodesNear(nodeM.getCoordinate())).get(0);
		return new TestGraphSummary(gb, startNode, endNode, new ArrayList<>(Arrays.asList(nodeA, nodeB, nodeI, nodeM)),
			new EuclideanDistanceTestEdgeWeightCalculator());
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
		final Node nodeA = new Node("0", Coordinate.of(0., 0.));
		final Node nodeB = new Node("1", Coordinate.of(1., 1.));
		final Node nodeC = new Node("2", Coordinate.of(3., 1.));
		final Node nodeD = new Node("3", Coordinate.of(2., 0.));
		final Node nodeE = new Node("4", Coordinate.of(2., -1.));
		final Node nodeF = new Node("5", Coordinate.of(6., -1.));
		final Node nodeG = new Node("6", Coordinate.of(3., 1.));
		final Node nodeH = new Node("7", Coordinate.of(4., 1.));
		final Node nodeI = new Node("8", Coordinate.of(4., 3.));
		final Node nodeJ = new Node("9", Coordinate.of(4., 2.));
		final Node nodeK = new Node("10", Coordinate.of(6., 2.));
		final Node nodeM = new Node("11", Coordinate.of(6., 3.));

		final NearLineMergeGraph gb = new NearLineMergeGraph(0.5, new EuclideanCalculator());
		gb.addApproximateDirectEdge(Polyline.of(nodeA.getCoordinate(), nodeD.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeA.getCoordinate(), nodeC.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeA.getCoordinate(), nodeB.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeB.getCoordinate(), nodeG.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeG.getCoordinate(), nodeH.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeI.getCoordinate(), nodeM.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeC.getCoordinate(), nodeD.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeC.getCoordinate(), nodeE.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeF.getCoordinate(), nodeK.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeK.getCoordinate(), nodeM.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeJ.getCoordinate(), nodeK.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeJ.getCoordinate(), nodeI.getCoordinate()));

		final Node startNode = new ArrayList<>(gb.getNodesNear(nodeA.getCoordinate())).get(0);
		final Node endNode = new ArrayList<>(gb.getNodesNear(nodeM.getCoordinate())).get(0);
		return new TestGraphSummary(gb, startNode, endNode, new ArrayList<>(), new EuclideanDistanceTestEdgeWeightCalculator());
	}

	static TestGraphSummary nodeCountTestGraphConnected(){
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
		final Node nodeA = new Node("0", Coordinate.of(0., 0.));
		final Node nodeB = new Node("1", Coordinate.of(1., 1.));
		final Node nodeC = new Node("2", Coordinate.of(3., 1.));
		final Node nodeD = new Node("3", Coordinate.of(2., 0.));
		final Node nodeE = new Node("4", Coordinate.of(2., -1.));
		final Node nodeF = new Node("5", Coordinate.of(6., -1.));
		final Node nodeG = new Node("6", Coordinate.of(3., 1.));
		final Node nodeH = new Node("7", Coordinate.of(4., 1.));
		final Node nodeI = new Node("8", Coordinate.of(4., 3.));
		final Node nodeJ = new Node("9", Coordinate.of(4., 2.));
		final Node nodeK = new Node("10", Coordinate.of(6., 2.));
		final Node nodeM = new Node("11", Coordinate.of(6., 3.));

		final NearLineMergeGraph gb = new NearLineMergeGraph(0.5, new EuclideanCalculator());
		gb.addApproximateDirectEdge(Polyline.of(nodeA.getCoordinate(), nodeD.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeA.getCoordinate(), nodeC.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeA.getCoordinate(), nodeB.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeB.getCoordinate(), nodeG.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeB.getCoordinate(), nodeI.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeG.getCoordinate(), nodeH.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeH.getCoordinate(), nodeI.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeI.getCoordinate(), nodeM.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeD.getCoordinate(), nodeJ.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeC.getCoordinate(), nodeD.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeC.getCoordinate(), nodeE.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeC.getCoordinate(), nodeF.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeE.getCoordinate(), nodeF.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeF.getCoordinate(), nodeK.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeK.getCoordinate(), nodeM.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeJ.getCoordinate(), nodeK.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeJ.getCoordinate(), nodeI.getCoordinate()));

		final Node startNode = new ArrayList<>(gb.getNodesNear(nodeA.getCoordinate())).get(0);
		final Node endNode = new ArrayList<>(gb.getNodesNear(nodeM.getCoordinate())).get(0);
		return new TestGraphSummary(gb, startNode, endNode, new ArrayList<>(Arrays.asList(nodeA, nodeB, nodeI, nodeM)),
			new NodeCountCalculator());
	}

	static TestGraphSummary nodeCountTestGraphDisconnected(){
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
		final Node nodeA = new Node("0", Coordinate.of(0., 0.));
		final Node nodeB = new Node("1", Coordinate.of(1., 1.));
		final Node nodeC = new Node("2", Coordinate.of(3., 1.));
		final Node nodeD = new Node("3", Coordinate.of(2., 0.));
		final Node nodeE = new Node("4", Coordinate.of(2., -1.));
		final Node nodeF = new Node("5", Coordinate.of(6., -1.));
		final Node nodeG = new Node("6", Coordinate.of(3., 1.));
		final Node nodeH = new Node("7", Coordinate.of(4., 1.));
		final Node nodeI = new Node("8", Coordinate.of(4., 3.));
		final Node nodeJ = new Node("9", Coordinate.of(4., 2.));
		final Node nodeK = new Node("10", Coordinate.of(6., 2.));
		final Node nodeM = new Node("11", Coordinate.of(6., 3.));

		final NearLineMergeGraph gb = new NearLineMergeGraph(0.5, new EuclideanCalculator());
		gb.addApproximateDirectEdge(Polyline.of(nodeA.getCoordinate(), nodeD.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeA.getCoordinate(), nodeC.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeA.getCoordinate(), nodeB.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeB.getCoordinate(), nodeG.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeG.getCoordinate(), nodeH.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeI.getCoordinate(), nodeM.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeC.getCoordinate(), nodeD.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeC.getCoordinate(), nodeE.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeF.getCoordinate(), nodeK.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeK.getCoordinate(), nodeM.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeJ.getCoordinate(), nodeK.getCoordinate()));
		gb.addApproximateDirectEdge(Polyline.of(nodeJ.getCoordinate(), nodeI.getCoordinate()));

		final Node startNode = new ArrayList<>(gb.getNodesNear(nodeA.getCoordinate())).get(0);
		final Node endNode = new ArrayList<>(gb.getNodesNear(nodeM.getCoordinate())).get(0);
		return new TestGraphSummary(gb, startNode, endNode, new ArrayList<>(), new NodeCountCalculator());
	}


	static class TestGraphSummary{

		private final Graph graph;
		private final Node start;
		private final Node end;
		private final List<Node> shortestPath;
		private final EdgeWeightCalculator calculator;


		TestGraphSummary(final Graph graph, final Node start, final Node end, final List<Node> shortestPath,
				final EdgeWeightCalculator calculator){
			this.graph = graph;
			this.start = start;
			this.end = end;
			this.shortestPath = shortestPath;
			this.calculator = calculator;
		}

		final Graph getGraph(){
			return graph;
		}

		final Node getStart(){
			return start;
		}

		final Node getEnd(){
			return end;
		}

		final List<Node> getShortestPath(){
			return shortestPath;
		}

		final EdgeWeightCalculator getCalculator(){
			return calculator;
		}

	}

}
