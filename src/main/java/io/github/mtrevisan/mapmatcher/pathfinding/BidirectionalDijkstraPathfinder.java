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
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.path.PathSummaryCreator;
import io.github.mtrevisan.mapmatcher.weight.EdgeWeightCalculator;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;


public class BidirectionalDijkstraPathfinder implements PathfindingStrategy{

	private static final PathSummaryCreator PATH_SUMMARY_CREATOR = new PathSummaryCreator();
	private static final BidirectionalCenterVertexFinder CENTER_VERTEX_FINDER = new BidirectionalCenterVertexFinder();

	private final EdgeWeightCalculator calculator;


	public BidirectionalDijkstraPathfinder(final EdgeWeightCalculator calculator){
		this.calculator = calculator;
	}

	@Override
	public PathSummary findPath(final Vertex start, final Vertex end, final Graph graph){
		final var minDistancesForward = new HashMap<Vertex, Double>();
		final var minDistancesBackward = new HashMap<Vertex, Double>();
		minDistancesForward.put(start, 0.0);
		minDistancesBackward.put(end, 0.0);

		final var predecessorTreeBackward = new HashMap<Vertex, Edge>();
		final var predecessorTreeForward = new HashMap<Vertex, Edge>();
		predecessorTreeForward.put(start, null);
		predecessorTreeBackward.put(end, null);

		final var pqForward = new PriorityQueue<ScoredGraphVertex>();
		final var pqBackward = new PriorityQueue<ScoredGraphVertex>();
		pqForward.add(new ScoredGraphVertex(start, 0.0));
		pqBackward.add(new ScoredGraphVertex(end, 0.0));

		final var reversedGraph = graph.reversed();

		while(!pqForward.isEmpty() && !pqBackward.isEmpty()){
			final var currForward = pqForward.poll();
			if(predecessorTreeBackward.containsKey(currForward.vertex())){
				final var candidateBidirectionalScore = minDistancesForward.get(currForward.vertex())
					+ minDistancesBackward.get(currForward.vertex());
				final var center = CENTER_VERTEX_FINDER.findCenterVertex(currForward.vertex(), candidateBidirectionalScore,
					pqForward, pqBackward);
				return PATH_SUMMARY_CREATOR.createBidirectionalPath(start, center, end, predecessorTreeForward, predecessorTreeBackward);
			}

			visitVertex(currForward, graph, pqForward, predecessorTreeForward, minDistancesForward);

			final var currBackward = pqBackward.poll();
			if(predecessorTreeForward.containsKey(currBackward.vertex())){
				final var candidateBidirectionalScore = minDistancesForward.get(currBackward.vertex())
					+ minDistancesBackward.get(currBackward.vertex());
				var center = CENTER_VERTEX_FINDER.findCenterVertex(currBackward.vertex(), candidateBidirectionalScore,
					pqForward, pqBackward);

				return PATH_SUMMARY_CREATOR.createBidirectionalPath(start, center, end, predecessorTreeForward, predecessorTreeBackward);
			}
			visitVertex(currBackward, reversedGraph, pqBackward, predecessorTreeBackward, minDistancesBackward);
		}

		return PATH_SUMMARY_CREATOR.createBidirectionalPath(start, start, end, predecessorTreeForward, predecessorTreeBackward);
	}

	private void visitVertex(final ScoredGraphVertex curr, final Graph graph, final Queue<ScoredGraphVertex> pq,
			final Map<Vertex, Edge> predecessorTree, final Map<Vertex, Double> minDistances){
		final var currVertex = curr.vertex();
		final var distanceSoFar = curr.score();
		if(distanceSoFar > minDistances.getOrDefault(currVertex, Double.MAX_VALUE))
			return;

		for(final var edge : graph.getVertexEdges(currVertex)){
			final var neighbour = edge.getTo();
			final var distance = distanceSoFar + calculator.calculateWeight(edge);
			if(distance < minDistances.getOrDefault(neighbour, Double.MAX_VALUE)){
				minDistances.put(neighbour, distance);
				predecessorTree.put(neighbour, edge);
				pq.add(new ScoredGraphVertex(neighbour, distance));
			}
		}
	}

}
