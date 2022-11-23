package io.github.mtrevisan.mapmatcher.pathfinding;

import io.github.mtrevisan.mapmatcher.distances.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.NearLineMergeGraph;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.weight.EdgeWeightCalculator;
import io.github.mtrevisan.mapmatcher.weight.VertexCountEdgeWeightCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class ShortestPathPathfindingTestGraphs{

	private static final GeometryFactory FACTORY = new GeometryFactory();


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
		final Node nodeA = new Node(new Coordinate(0., 0.));
		final Node nodeB = new Node(new Coordinate(1., 1.));
		final Node nodeC = new Node(new Coordinate(3., 1.));
		final Node nodeD = new Node(new Coordinate(2., 0.));
		final Node nodeE = new Node(new Coordinate(2., -1.));
		final Node nodeF = new Node(new Coordinate(6., -1.));
		final Node nodeG = new Node(new Coordinate(3., 1.));
		final Node nodeH = new Node(new Coordinate(4., 1.));
		final Node nodeI = new Node(new Coordinate(4., 3.));
		final Node nodeJ = new Node(new Coordinate(4., 2.));
		final Node nodeK = new Node(new Coordinate(6., 2.));
		final Node nodeM = new Node(new Coordinate(6., 3.));

		final NearLineMergeGraph gb = new NearLineMergeGraph(1., new EuclideanCalculator());
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeA.getCoordinate(), nodeD.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeA.getCoordinate(), nodeC.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeA.getCoordinate(), nodeB.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeB.getCoordinate(), nodeG.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeB.getCoordinate(), nodeI.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeG.getCoordinate(), nodeH.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeH.getCoordinate(), nodeI.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeI.getCoordinate(), nodeM.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeD.getCoordinate(), nodeJ.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeC.getCoordinate(), nodeD.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeC.getCoordinate(), nodeE.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeE.getCoordinate(), nodeF.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeF.getCoordinate(), nodeK.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeK.getCoordinate(), nodeM.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeJ.getCoordinate(), nodeK.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeJ.getCoordinate(), nodeI.getCoordinate()}));

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
		final Node nodeA = new Node(new Coordinate(0., 0.));
		final Node nodeB = new Node(new Coordinate(1., 1.));
		final Node nodeC = new Node(new Coordinate(3., 1.));
		final Node nodeD = new Node(new Coordinate(2., 0.));
		final Node nodeE = new Node(new Coordinate(2., -1.));
		final Node nodeF = new Node(new Coordinate(6., -1.));
		final Node nodeG = new Node(new Coordinate(3., 1.));
		final Node nodeH = new Node(new Coordinate(4., 1.));
		final Node nodeI = new Node(new Coordinate(4., 3.));
		final Node nodeJ = new Node(new Coordinate(4., 2.));
		final Node nodeK = new Node(new Coordinate(6., 2.));
		final Node nodeM = new Node(new Coordinate(6., 3.));

		final NearLineMergeGraph gb = new NearLineMergeGraph(1., new EuclideanCalculator());
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeA.getCoordinate(), nodeD.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeA.getCoordinate(), nodeC.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeA.getCoordinate(), nodeB.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeB.getCoordinate(), nodeG.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeG.getCoordinate(), nodeH.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeI.getCoordinate(), nodeM.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeC.getCoordinate(), nodeD.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeC.getCoordinate(), nodeE.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeF.getCoordinate(), nodeK.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeK.getCoordinate(), nodeM.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeJ.getCoordinate(), nodeK.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeJ.getCoordinate(), nodeI.getCoordinate()}));

		final Node startNode = new ArrayList<>(gb.getNodesNear(nodeA.getCoordinate())).get(0);
		final Node endNode = new ArrayList<>(gb.getNodesNear(nodeM.getCoordinate())).get(0);
		return new TestGraphSummary(gb, startNode, endNode, new ArrayList<>(), new EuclideanDistanceTestEdgeWeightCalculator());
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
		final Node nodeA = new Node(new Coordinate(0., 0.));
		final Node nodeB = new Node(new Coordinate(1., 1.));
		final Node nodeC = new Node(new Coordinate(3., 1.));
		final Node nodeD = new Node(new Coordinate(2., 0.));
		final Node nodeE = new Node(new Coordinate(2., -1.));
		final Node nodeF = new Node(new Coordinate(6., -1.));
		final Node nodeG = new Node(new Coordinate(3., 1.));
		final Node nodeH = new Node(new Coordinate(4., 1.));
		final Node nodeI = new Node(new Coordinate(4., 3.));
		final Node nodeJ = new Node(new Coordinate(4., 2.));
		final Node nodeK = new Node(new Coordinate(6., 2.));
		final Node nodeM = new Node(new Coordinate(6., 3.));

		final NearLineMergeGraph gb = new NearLineMergeGraph(1., new EuclideanCalculator());
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeA.getCoordinate(), nodeD.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeA.getCoordinate(), nodeC.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeA.getCoordinate(), nodeB.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeB.getCoordinate(), nodeG.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeB.getCoordinate(), nodeI.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeG.getCoordinate(), nodeH.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeH.getCoordinate(), nodeI.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeI.getCoordinate(), nodeM.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeD.getCoordinate(), nodeJ.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeC.getCoordinate(), nodeD.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeC.getCoordinate(), nodeE.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeC.getCoordinate(), nodeF.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeE.getCoordinate(), nodeF.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeF.getCoordinate(), nodeK.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeK.getCoordinate(), nodeM.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeJ.getCoordinate(), nodeK.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeJ.getCoordinate(), nodeI.getCoordinate()}));

		final Node startNode = new ArrayList<>(gb.getNodesNear(nodeA.getCoordinate())).get(0);
		final Node endNode = new ArrayList<>(gb.getNodesNear(nodeM.getCoordinate())).get(0);
		return new TestGraphSummary(gb, startNode, endNode, new ArrayList<>(Arrays.asList(nodeA, nodeB, nodeI, nodeM)),
			new VertexCountEdgeWeightCalculator());
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
		final Node nodeA = new Node(new Coordinate(0., 0.));
		final Node nodeB = new Node(new Coordinate(1., 1.));
		final Node nodeC = new Node(new Coordinate(3., 1.));
		final Node nodeD = new Node(new Coordinate(2., 0.));
		final Node nodeE = new Node(new Coordinate(2., -1.));
		final Node nodeF = new Node(new Coordinate(6., -1.));
		final Node nodeG = new Node(new Coordinate(3., 1.));
		final Node nodeH = new Node(new Coordinate(4., 1.));
		final Node nodeI = new Node(new Coordinate(4., 3.));
		final Node nodeJ = new Node(new Coordinate(4., 2.));
		final Node nodeK = new Node(new Coordinate(6., 2.));
		final Node nodeM = new Node(new Coordinate(6., 3.));

		final NearLineMergeGraph gb = new NearLineMergeGraph(1., new EuclideanCalculator());
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeA.getCoordinate(), nodeD.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeA.getCoordinate(), nodeC.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeA.getCoordinate(), nodeB.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeB.getCoordinate(), nodeG.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeG.getCoordinate(), nodeH.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeI.getCoordinate(), nodeM.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeC.getCoordinate(), nodeD.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeC.getCoordinate(), nodeE.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeF.getCoordinate(), nodeK.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeK.getCoordinate(), nodeM.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeJ.getCoordinate(), nodeK.getCoordinate()}));
		gb.addApproximateEdge(FACTORY.createLineString(new Coordinate[]{nodeJ.getCoordinate(), nodeI.getCoordinate()}));

		final Node startNode = new ArrayList<>(gb.getNodesNear(nodeA.getCoordinate())).get(0);
		final Node endNode = new ArrayList<>(gb.getNodesNear(nodeM.getCoordinate())).get(0);
		return new TestGraphSummary(gb, startNode, endNode, new ArrayList<>(), new VertexCountEdgeWeightCalculator());
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
