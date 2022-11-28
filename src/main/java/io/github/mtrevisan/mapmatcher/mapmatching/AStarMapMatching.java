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
import io.github.mtrevisan.mapmatcher.graph.ScoredGraph;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.EmissionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.TransitionProbabilityCalculator;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;


/**
 * @see <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">A* search algorithm</a>
 */
public class AStarMapMatching implements MapMatchingStrategy{

	private final InitialProbabilityCalculator initialProbabilityCalculator;
	private final TransitionProbabilityCalculator transitionProbabilityCalculator;
	private final EmissionProbabilityCalculator emissionProbabilityCalculator;


	public AStarMapMatching(final InitialProbabilityCalculator initialProbabilityCalculator,
			final TransitionProbabilityCalculator transitionProbabilityCalculator,
			final EmissionProbabilityCalculator emissionProbabilityCalculator){
		this.initialProbabilityCalculator = initialProbabilityCalculator;
		this.transitionProbabilityCalculator = transitionProbabilityCalculator;
		this.emissionProbabilityCalculator = emissionProbabilityCalculator;
	}

	@Override
	public Edge[] findPath(final Graph graph, final Coordinate[] observations){
		final Collection<Edge> graphEdges = graph.edges();

		final int n = graphEdges.size();
		final int m = observations.length;
		final Map<Edge, double[]> fScores = new HashMap<>();
		final Map<Edge, Edge[]> path = new HashMap<>();

		//set of discovered nodes that may need to be (re-)expanded
		final Queue<ScoredGraph<Edge>> frontier = new PriorityQueue<>();

		//NOTE: the initial probability is a uniform distribution reflecting the fact that there is no known bias about which is the
		// correct segment
		initialProbabilityCalculator.calculateInitialProbability(observations[0], graphEdges);
		emissionProbabilityCalculator.updateEmissionProbability(observations[0], graphEdges);
		for(final Edge edge : graphEdges){
			final double probability = initialProbabilityCalculator.initialProbability(edge)
				+ emissionProbabilityCalculator.emissionProbability(observations[0], edge);
			fScores.computeIfAbsent(edge, k -> new double[m])[0] = probability;
			path.computeIfAbsent(edge, k -> new Edge[n])[0] = edge;

			frontier.add(new ScoredGraph<>(edge, probability));
		}

		for(int i = 1; i < m; i ++){
			emissionProbabilityCalculator.updateEmissionProbability(observations[i], graphEdges);

			final Map<Edge, Edge[]> newPath = new HashMap<>(n);
			while(!frontier.isEmpty()){
				final Edge fromEdge = lifoExtract(frontier);
				//TODO termination condition: i == m - 1 && currentEdge is best (?)

				for(final Edge toEdge : fromEdge.geOutEdges()){
					final double probability = fScores.get(fromEdge)[i - 1]
						+ transitionProbabilityCalculator.transitionProbability(fromEdge, toEdge, observations[i - 1], observations[i]);
					if(probability < fScores.get(toEdge)[i - 1]){
						fScores.get(toEdge)[i] = probability;

						final double newProbability = probability
							+ emissionProbabilityCalculator.emissionProbability(observations[i], toEdge);
						final ScoredGraph<Edge> sg = new ScoredGraph<>(toEdge, newProbability);
						if(!frontier.contains(sg))
							frontier.add(sg);

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
		}

		//TODO it is important that the same node doesn't appear in the priority queue more than once (each entry corresponding to a
		// different path to the node, and each with a different cost). A standard approach here is to check if a node about to be added
		// already appears in the priority queue. If it does, then the priority and parent pointers are changed to correspond to the lower
		// cost path. A standard binary heap based priority queue does not directly support the operation of searching for one of its
		// elements, but it can be augmented with a hash table that maps elements to their position in the heap, allowing this
		// decrease-priority operation to be performed in logarithmic time. Alternatively, a Fibonacci heap can perform the same
		// decrease-priority operations in constant amortized time.
		double minProbability = Double.POSITIVE_INFINITY;
		Edge minProbabilityEdge = null;
		for(final Edge edge : graphEdges)
			if(fScores.get(edge)[m - 1] < minProbability){
				minProbability = fScores.get(edge)[m - 1];
				minProbabilityEdge = edge;
			}
		return (minProbabilityEdge != null? path.get(minProbabilityEdge): null);
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
	private static Edge lifoExtract(final Queue<ScoredGraph<Edge>> frontier){
		//collect elements with score equals to the first element of the queue
		double minScore = Double.NaN;
		final List<ScoredGraph<Edge>> minScores = new ArrayList<>(frontier.size());
		while(!frontier.isEmpty()){
			final ScoredGraph<Edge> nextElement = frontier.poll();
			if(Double.isNaN(minScore)){
				//first element initializes minimum score
				minScore = nextElement.getScore();
				minScores.add(nextElement);
			}
			else if(nextElement.getScore() == minScore)
				//if next element extracted has the same score as the first one, store it
				minScores.add(nextElement);
			else
				//if next element has a different (greater) score than the first one, break loop
				break;
		}
		//extract LIFO element
		final Edge fromEdge = minScores.remove(minScores.size() - 1)
			.getElement();
		//reinsert same score elements
		frontier.addAll(minScores);
		return fromEdge;
	}

}
