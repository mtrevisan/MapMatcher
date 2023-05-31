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

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.NearNodeMergeGraph;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.EdgeWeightCalculator;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.NodeCountCalculator;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.topologies.EuclideanCalculator;

import java.util.ArrayList;
import java.util.Collection;


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
           - start: A
           - end: M
        */
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		final Point pointA = factory.createPoint(0., 0.);
		final Point pointB = factory.createPoint(1., 1.);
		final Point pointC = factory.createPoint(1., -1.);
		final Point pointD = factory.createPoint(2., 0.);
		final Point pointE = factory.createPoint(2., -1.);
		final Point pointF = factory.createPoint(6., -1.);
		final Point pointG = factory.createPoint(3., 1.);
		final Point pointH = factory.createPoint(4., 1.);
		final Point pointI = factory.createPoint(4., 3.);
		final Point pointJ = factory.createPoint(4., 2.);
		final Point pointK = factory.createPoint(6., 2.);
		final Point pointM = factory.createPoint(6., 3.);

		final NearNodeMergeGraph gb = new NearNodeMergeGraph(0.5);
		gb.addApproximateEdge("A-D", pointA, pointD);
		gb.addApproximateEdge("A-C", pointA, pointC);
		Collection<Edge> edgeAB = gb.addApproximateEdge("A-B", pointA, pointB);
		gb.addApproximateEdge("B-G", pointB, pointG);
		Collection<Edge> edgeBI = gb.addApproximateEdge("B-I", pointB, pointI);
		gb.addApproximateEdge("G-H", pointG, pointH);
		gb.addApproximateEdge("H-I", pointH, pointI);
		Collection<Edge> edgeIM = gb.addApproximateEdge("I-M", pointI, pointM);
		gb.addApproximateEdge("D-J", pointD, pointJ);
		gb.addApproximateEdge("C-D", pointC, pointD);
		gb.addApproximateEdge("C-E", pointC, pointE);
		gb.addApproximateEdge("E-F", pointE, pointF);
		gb.addApproximateEdge("F-K", pointF, pointK);
		gb.addApproximateEdge("K-M", pointK, pointM);
		gb.addApproximateEdge("J-K", pointJ, pointK);
		gb.addApproximateEdge("J-I", pointJ, pointI);

		final Node startNode = new ArrayList<>(gb.getNodesNear(pointA)).get(0);
		final Node endNode = new ArrayList<>(gb.getNodesNear(pointM)).get(0);
		return new TestGraphSummary(gb, startNode, endNode, new Edge[]{edgeAB.iterator().next(), edgeBI.iterator().next(), edgeIM.iterator().next()},
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
           - start: A
           - end: M
        */
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		final Node nodeA = Node.of("0", factory.createPoint(0., 0.));
		final Node nodeB = Node.of("1", factory.createPoint(1., 1.));
		final Node nodeC = Node.of("2", factory.createPoint(1., -1.));
		final Node nodeD = Node.of("3", factory.createPoint(2., 0.));
		final Node nodeE = Node.of("4", factory.createPoint(2., -1.));
		final Node nodeF = Node.of("5", factory.createPoint(6., -1.));
		final Node nodeG = Node.of("6", factory.createPoint(3., 1.));
		final Node nodeH = Node.of("7", factory.createPoint(4., 1.));
		final Node nodeI = Node.of("8", factory.createPoint(4., 3.));
		final Node nodeJ = Node.of("9", factory.createPoint(4., 2.));
		final Node nodeK = Node.of("10", factory.createPoint(6., 2.));
		final Node nodeM = Node.of("11", factory.createPoint(6., 3.));

		final NearNodeMergeGraph gb = new NearNodeMergeGraph(0.5);
		gb.addApproximateEdge(nodeA.getPoint(), nodeD.getPoint());
		gb.addApproximateEdge(nodeA.getPoint(), nodeC.getPoint());
		gb.addApproximateEdge(nodeA.getPoint(), nodeB.getPoint());
		gb.addApproximateEdge(nodeB.getPoint(), nodeG.getPoint());
		gb.addApproximateEdge(nodeG.getPoint(), nodeH.getPoint());
		gb.addApproximateEdge(nodeI.getPoint(), nodeM.getPoint());
		gb.addApproximateEdge(nodeC.getPoint(), nodeD.getPoint());
		gb.addApproximateEdge(nodeC.getPoint(), nodeE.getPoint());
		gb.addApproximateEdge(nodeF.getPoint(), nodeK.getPoint());
		gb.addApproximateEdge(nodeK.getPoint(), nodeM.getPoint());
		gb.addApproximateEdge(nodeJ.getPoint(), nodeK.getPoint());
		gb.addApproximateEdge(nodeJ.getPoint(), nodeI.getPoint());

		final Node startNode = new ArrayList<>(gb.getNodesNear(nodeA.getPoint())).get(0);
		final Node endNode = new ArrayList<>(gb.getNodesNear(nodeM.getPoint())).get(0);
		return new TestGraphSummary(gb, startNode, endNode, new Edge[0], new EuclideanDistanceTestEdgeWeightCalculator());
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
           - start: A
           - end: M
        */
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		final Node nodeA = Node.of("0", factory.createPoint(0., 0.));
		final Node nodeB = Node.of("1", factory.createPoint(1., 1.));
		final Node nodeC = Node.of("2", factory.createPoint(1., -1.));
		final Node nodeD = Node.of("3", factory.createPoint(2., 0.));
		final Node nodeE = Node.of("4", factory.createPoint(2., -1.));
		final Node nodeF = Node.of("5", factory.createPoint(6., -1.));
		final Node nodeG = Node.of("6", factory.createPoint(3., 1.));
		final Node nodeH = Node.of("7", factory.createPoint(4., 1.));
		final Node nodeI = Node.of("8", factory.createPoint(4., 3.));
		final Node nodeJ = Node.of("9", factory.createPoint(4., 2.));
		final Node nodeK = Node.of("10", factory.createPoint(6., 2.));
		final Node nodeM = Node.of("11", factory.createPoint(6., 3.));

		final NearNodeMergeGraph gb = new NearNodeMergeGraph(0.5);
		gb.addApproximateEdge(nodeA.getPoint(), nodeD.getPoint());
		gb.addApproximateEdge(nodeA.getPoint(), nodeC.getPoint());
		Collection<Edge> edgeAB = gb.addApproximateEdge(nodeA.getPoint(), nodeB.getPoint());
		gb.addApproximateEdge(nodeB.getPoint(), nodeG.getPoint());
		Collection<Edge> edgeBI = gb.addApproximateEdge(nodeB.getPoint(), nodeI.getPoint());
		gb.addApproximateEdge(nodeG.getPoint(), nodeH.getPoint());
		gb.addApproximateEdge(nodeH.getPoint(), nodeI.getPoint());
		Collection<Edge> edgeIM = gb.addApproximateEdge(nodeI.getPoint(), nodeM.getPoint());
		gb.addApproximateEdge(nodeD.getPoint(), nodeJ.getPoint());
		gb.addApproximateEdge(nodeC.getPoint(), nodeD.getPoint());
		gb.addApproximateEdge(nodeC.getPoint(), nodeE.getPoint());
		gb.addApproximateEdge(nodeC.getPoint(), nodeF.getPoint());
		gb.addApproximateEdge(nodeE.getPoint(), nodeF.getPoint());
		gb.addApproximateEdge(nodeF.getPoint(), nodeK.getPoint());
		gb.addApproximateEdge(nodeK.getPoint(), nodeM.getPoint());
		gb.addApproximateEdge(nodeJ.getPoint(), nodeK.getPoint());
		gb.addApproximateEdge(nodeJ.getPoint(), nodeI.getPoint());

		final Node startNode = new ArrayList<>(gb.getNodesNear(nodeA.getPoint())).get(0);
		final Node endNode = new ArrayList<>(gb.getNodesNear(nodeM.getPoint())).get(0);
		return new TestGraphSummary(gb, startNode, endNode, new Edge[]{edgeAB.iterator().next(), edgeBI.iterator().next(), edgeIM.iterator().next()},
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
           - start: A
           - end: M
        */
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		final Node nodeA = Node.of("0", factory.createPoint(0., 0.));
		final Node nodeB = Node.of("1", factory.createPoint(1., 1.));
		final Node nodeC = Node.of("2", factory.createPoint(1., -1.));
		final Node nodeD = Node.of("3", factory.createPoint(2., 0.));
		final Node nodeE = Node.of("4", factory.createPoint(2., -1.));
		final Node nodeF = Node.of("5", factory.createPoint(6., -1.));
		final Node nodeG = Node.of("6", factory.createPoint(3., 1.));
		final Node nodeH = Node.of("7", factory.createPoint(4., 1.));
		final Node nodeI = Node.of("8", factory.createPoint(4., 3.));
		final Node nodeJ = Node.of("9", factory.createPoint(4., 2.));
		final Node nodeK = Node.of("10", factory.createPoint(6., 2.));
		final Node nodeM = Node.of("11", factory.createPoint(6., 3.));

		final NearNodeMergeGraph gb = new NearNodeMergeGraph(0.5);
		gb.addApproximateEdge(nodeA.getPoint(), nodeD.getPoint());
		gb.addApproximateEdge(nodeA.getPoint(), nodeC.getPoint());
		gb.addApproximateEdge(nodeA.getPoint(), nodeB.getPoint());
		gb.addApproximateEdge(nodeB.getPoint(), nodeG.getPoint());
		gb.addApproximateEdge(nodeG.getPoint(), nodeH.getPoint());
		gb.addApproximateEdge(nodeI.getPoint(), nodeM.getPoint());
		gb.addApproximateEdge(nodeC.getPoint(), nodeD.getPoint());
		gb.addApproximateEdge(nodeC.getPoint(), nodeE.getPoint());
		gb.addApproximateEdge(nodeF.getPoint(), nodeK.getPoint());
		gb.addApproximateEdge(nodeK.getPoint(), nodeM.getPoint());
		gb.addApproximateEdge(nodeJ.getPoint(), nodeK.getPoint());
		gb.addApproximateEdge(nodeJ.getPoint(), nodeI.getPoint());

		final Node startNode = new ArrayList<>(gb.getNodesNear(nodeA.getPoint())).get(0);
		final Node endNode = new ArrayList<>(gb.getNodesNear(nodeM.getPoint())).get(0);
		return new TestGraphSummary(gb, startNode, endNode, new Edge[0], new NodeCountCalculator());
	}


	static class TestGraphSummary{

		private final Graph graph;
		private final Node start;
		private final Node end;
		private final Edge[] shortestPath;
		private final EdgeWeightCalculator calculator;


		TestGraphSummary(final Graph graph, final Node start, final Node end, final Edge[] shortestPath,
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

		final Edge[] getShortestPath(){
			return shortestPath;
		}

		final EdgeWeightCalculator getCalculator(){
			return calculator;
		}

	}

}
