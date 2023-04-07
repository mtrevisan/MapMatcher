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
package io.github.mtrevisan.mapmatcher.mapmatching;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.helpers.FibonacciHeap;
import io.github.mtrevisan.mapmatcher.helpers.PathHelper;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission.EmissionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.TransitionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.pathfinding.AStarPathFinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.EdgeWeightCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


//TODO make it works...
/**
 * @see <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">A* search algorithm</a>
 */
public class AStarMapMatching implements MapMatchingStrategy{

	private final InitialProbabilityCalculator initialProbabilityCalculator;
	private final TransitionProbabilityCalculator transitionProbabilityCalculator;
	private final EmissionProbabilityCalculator emissionProbabilityCalculator;

	private final PathFindingStrategy pathFinder;


	public AStarMapMatching(final InitialProbabilityCalculator initialProbabilityCalculator,
			final TransitionProbabilityCalculator transitionProbabilityCalculator,
			final EmissionProbabilityCalculator emissionProbabilityCalculator,
			final EdgeWeightCalculator edgeWeightCalculator){
		this.initialProbabilityCalculator = initialProbabilityCalculator;
		this.transitionProbabilityCalculator = transitionProbabilityCalculator;
		this.emissionProbabilityCalculator = emissionProbabilityCalculator;

		pathFinder = new AStarPathFinder(edgeWeightCalculator);
	}

	@Override
	public Collection<Map.Entry<Double, Edge[]>> findPath(final Graph graph, final Point[] observations, final double edgesNearObservationThreshold){
		int i = PathHelper.extractNextObservation(observations, 0);
		if(i < 0)
			//no observations: cannot calculate path
			return null;

		final Collection<Edge> graphEdges = graph.edges();

		final int n = graphEdges.size();
		final int m = observations.length;
		final Map<Edge, double[]> score = new HashMap<>();
		final Map<Edge, Edge[]> path = new HashMap<>();

		//set of discovered nodes that may need to be (re-)expanded
		final FibonacciHeap<Edge> frontier = new FibonacciHeap<>();
		final Map<Edge, FibonacciHeap.Node<Edge>> seenNodes = new HashMap<>(n);

		Point currentObservation = observations[i];
		initialProbabilityCalculator.calculateInitialProbability(graphEdges);
		emissionProbabilityCalculator.updateEmissionProbability(currentObservation, graphEdges);
		for(final Edge edge : graphEdges){
			final double probability = initialProbabilityCalculator.initialProbability(currentObservation, edge)
				+ emissionProbabilityCalculator.emissionProbability(currentObservation, edge, (i > 0? observations[i - 1]: null));
			score.computeIfAbsent(edge, k -> new double[m])[i] = probability;
			path.computeIfAbsent(edge, k -> new Edge[n])[i] = edge;

			final FibonacciHeap.Node<Edge> frontierNode = frontier.add(edge, probability);
			seenNodes.put(edge, frontierNode);
		}

		int previousObservationIndex = i;
		while(true){
			final Point previousObservation = observations[previousObservationIndex];
			i = PathHelper.extractNextObservation(observations, previousObservationIndex + 1);
			if(i < 0)
				break;

			emissionProbabilityCalculator.updateEmissionProbability(currentObservation, graphEdges);

			final Map<Edge, Edge[]> newPath = new HashMap<>(n);
			while(!frontier.isEmpty()){
				final Edge fromEdge = lifoExtract(frontier);
				//TODO termination condition: i == m - 1 && currentEdge is best (?)

				for(final Edge toEdge : fromEdge.getOutEdges()){
					final Polyline pathAsPolyline = PathHelper.calculatePathAsPolyline(fromEdge, toEdge, graph, pathFinder);

					final double probability = score.get(fromEdge)[previousObservationIndex]
						+ transitionProbabilityCalculator.transitionProbability(fromEdge, toEdge, previousObservation, currentObservation,
						pathAsPolyline);
					if(probability < score.get(toEdge)[previousObservationIndex]){
						score.get(toEdge)[i] = probability;

						final double newProbability = probability
							+ emissionProbabilityCalculator.emissionProbability(currentObservation, toEdge, (i > 0? observations[i - 1]: null));
						//NOTE: it is important that the same node doesn't appear in the priority queue more than once (each entry corresponding
						// to a different path to the node, and each with a different cost).
						// A standard approach here is to check if a node about to be added already appears in the priority queue. If it does,
						// then the priority and parent pointers are changed to correspond to the lower cost path. A standard binary heap based
						// priority queue does not directly support the operation of searching for one of its elements, but it can be augmented
						// with a hash table that maps elements to their position in the heap, allowing this decrease-priority operation to be
						// performed in logarithmic time. Alternatively, a Fibonacci heap can perform the same decrease-priority operations in
						// constant amortized time.
						final FibonacciHeap.Node<Edge> toNode = seenNodes.get(toEdge);
						if(toNode == null){
							final FibonacciHeap.Node<Edge> frontierNode = frontier.add(toEdge, newProbability);
							seenNodes.put(toEdge, frontierNode);
						}
						else if(newProbability < toNode.getKey())
							frontier.decreaseKey(toNode, newProbability);

						//record path
						System.arraycopy(path.computeIfAbsent(fromEdge, k -> new Edge[m]), 0,
							newPath.computeIfAbsent(toEdge, k -> new Edge[m]), 0, i);
						newPath.get(toEdge)[i] = toEdge;
					}
				}
			}

			path.clear();
			path.putAll(newPath);
			newPath.clear();

			previousObservationIndex = i;
		}

		double minProbability = Double.POSITIVE_INFINITY;
		final Collection<Map.Entry<Double, Edge[]>> minProbabilityPaths = new HashSet<>(0);
		for(final Edge edge : path.keySet()){
			final double[] fScore = score.get(edge);
			if(fScore != null){
				if(fScore[previousObservationIndex] == minProbability)
					minProbabilityPaths.add(new AbstractMap.SimpleEntry<>(minProbability, path.get(edge)));
				else if(fScore[previousObservationIndex] < minProbability){
					minProbability = fScore[previousObservationIndex];
					minProbabilityPaths.clear();
					minProbabilityPaths.add(new AbstractMap.SimpleEntry<>(minProbability, path.get(edge)));
				}
			}
		}
		return minProbabilityPaths;
	}

	/**
	 * Extract the next element from priority queue.
	 * <p>
	 * Ties are broken so the queue behaves in a LIFO manner, A* will behave like depth-first search among equal cost paths (avoiding exploring more than one equally optimal solution)
	 * </p>
	 *
	 * @param frontier	The priority queue.
	 * @return	The Last-In First-Out min-priority element.
	 */
	private static Edge lifoExtract(final FibonacciHeap<Edge> frontier){
		//collect elements with score equals to the first element of the queue
		double minScore = Double.NaN;
		final List<FibonacciHeap.Node<Edge>> minScores = new ArrayList<>(frontier.size());
		while(!frontier.isEmpty()){
			final FibonacciHeap.Node<Edge> nextElement = frontier.peek();
			frontier.poll();
			if(Double.isNaN(minScore)){
				//first element initializes minimum score
				minScore = nextElement.getKey();
				minScores.add(nextElement);
			}
			else if(nextElement.getKey() == minScore)
				//if next element extracted has the same score as the first one, store it
				minScores.add(nextElement);
			else
				//if next element has a different (greater) score than the first one, break loop
				break;
		}
		//extract LIFO element
		final Edge fromEdge = minScores.remove(minScores.size() - 1)
			.getData();
		//reinsert same score elements
		for(final FibonacciHeap.Node<Edge> element : minScores)
			frontier.add(element.getData(), element.getKey());
		return fromEdge;
	}

}
