/**
 * Copyright (c) 2021 Mauro Trevisan
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
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.path.PathSummaryCreator;
import io.github.mtrevisan.mapmatcher.weight.EdgeWeightCalculator;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;


public class BidirectionalDijkstraPathfinder implements PathfindingStrategy{

	private static final PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();
	private static final BidirectionalCenterVertexFinder centerVertexFinder = new BidirectionalCenterVertexFinder();
	private final EdgeWeightCalculator calculator;

	public BidirectionalDijkstraPathfinder(EdgeWeightCalculator calculator){
		this.calculator = calculator;
	}

	@Override
	public PathSummary findPath(Vertex start, Vertex end, Graph graph){
		var minDistancesForward = new HashMap<Vertex, Double>();
		var minDistancesBackward = new HashMap<Vertex, Double>();
		minDistancesForward.put(start, 0.0);
		minDistancesBackward.put(end, 0.0);

		var predecessorTreeBackward = new HashMap<Vertex, Edge>();
		var predecessorTreeForward = new HashMap<Vertex, Edge>();
		predecessorTreeForward.put(start, null);
		predecessorTreeBackward.put(end, null);

		var pqForward = new PriorityQueue<ScoredGraphVertex>();
		var pqBackward = new PriorityQueue<ScoredGraphVertex>();
		pqForward.add(new ScoredGraphVertex(start, 0.0));
		pqBackward.add(new ScoredGraphVertex(end, 0.0));

		var reversedGraph = graph.reversed();

		while(! pqForward.isEmpty() && ! pqBackward.isEmpty()){
			var currForward = pqForward.poll();
			if(predecessorTreeBackward.containsKey(currForward.vertex())){
				var center = centerVertexFinder.findCenterVertex(currForward.vertex(), minDistancesForward.get(currForward.vertex()) + minDistancesBackward.get(currForward.vertex()), pqForward, pqBackward);
				return pathSummaryCreator.createBidirectionalPath(start, center, end, predecessorTreeForward, predecessorTreeBackward);
			}
			visitVertex(currForward, graph, pqForward, predecessorTreeForward, minDistancesForward);

			var currBackward = pqBackward.poll();
			if(predecessorTreeForward.containsKey(currBackward.vertex())){
				var center = centerVertexFinder.findCenterVertex(currBackward.vertex(), minDistancesForward.get(currBackward.vertex()) + minDistancesBackward.get(currBackward.vertex()), pqForward, pqBackward);

				return pathSummaryCreator.createBidirectionalPath(start, center, end, predecessorTreeForward, predecessorTreeBackward);
			}
			visitVertex(currBackward, reversedGraph, pqBackward, predecessorTreeBackward, minDistancesBackward);
		}

		return pathSummaryCreator.createBidirectionalPath(start, start, end, predecessorTreeForward, predecessorTreeBackward);
	}

	private void visitVertex(ScoredGraphVertex curr, Graph graph, Queue<ScoredGraphVertex> pq, Map<Vertex, Edge> predecessorTree, Map<Vertex, Double> minDistances){
		var currVertex = curr.vertex();
		var distanceSoFar = curr.score();

		if(distanceSoFar > minDistances.getOrDefault(currVertex, Double.MAX_VALUE)){
			return;
		}

		for(var edge : graph.getVertexEdges(currVertex)){
			var neighbour = edge.getTo();
			var distance = distanceSoFar + calculator.calculateWeight(edge);

			if(distance < minDistances.getOrDefault(neighbour, Double.MAX_VALUE)){
				minDistances.put(neighbour, distance);
				predecessorTree.put(neighbour, edge);
				pq.add(new ScoredGraphVertex(neighbour, distance));
			}
		}
	}

}
