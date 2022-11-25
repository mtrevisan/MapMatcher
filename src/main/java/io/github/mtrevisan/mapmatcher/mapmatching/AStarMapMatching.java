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
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.graph.ScoredGraph;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.LogProbabilityCalculator;
import org.locationtech.jts.geom.Coordinate;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;


/**
 * @see <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">A* search algorithm</a>
 */
public class AStarMapMatching implements MapMatchingStrategy{

	private final LogProbabilityCalculator probabilityCalculator;


	public AStarMapMatching(final LogProbabilityCalculator mapMatchingProbabilityCalculator){
		this.probabilityCalculator = mapMatchingProbabilityCalculator;
	}

	@Override
	public Edge[] findPath(final Graph graph, final Coordinate[] observations){
		final Collection<Edge> graphEdges = graph.edges();

		final int n = graphEdges.size();
		final int m = observations.length;
		final Map<Edge, double[]> fScores = new HashMap<>();
		final Map<Edge, Edge[]> path = new HashMap<>();

		final var seenNodes = new HashSet<Node>();

		//set of discovered nodes that may need to be (re-)expanded
		final var queue = new PriorityQueue<ScoredGraph<Edge>>();

		//NOTE: the initial probability is a uniform distribution reflecting the fact that there is no known bias about which is the
		// correct segment
		probabilityCalculator.calculateInitialProbability(observations[0], graphEdges);
		probabilityCalculator.updateEmissionProbability(observations[0], graphEdges);
		for(final Edge edge : graphEdges){
			final double probability = probabilityCalculator.initialProbability(edge)
				+ probabilityCalculator.emissionProbability(observations[0], edge);
			queue.add(new ScoredGraph<>(edge, probability));
			fScores.computeIfAbsent(edge, k -> new double[m])[0] = probability;
			path.computeIfAbsent(edge, k -> new Edge[n])[0] = edge;
		}

		double minProbability;
		Edge minProbabilityEdge;
		for(int i = 1; i < m; i ++){
			probabilityCalculator.updateEmissionProbability(observations[i], graphEdges);

			final Map<Edge, Edge[]> newPath = new HashMap<>(n);
			for(final Edge currentEdge : graphEdges){
				minProbability = Double.POSITIVE_INFINITY;
				for(final Edge fromEdge : graphEdges){
					final double probability = fScores.get(fromEdge)[i - 1] + probabilityCalculator.transitionProbability(fromEdge, currentEdge);
					if(probability < minProbability){
						//record minimum probability
						minProbability = probability;
						minProbabilityEdge = fromEdge;
						fScores.get(currentEdge)[i] = probability + probabilityCalculator.emissionProbability(observations[i], currentEdge);

						//record path
						System.arraycopy(path.computeIfAbsent(minProbabilityEdge, k -> new Edge[m]), 0,
							newPath.computeIfAbsent(currentEdge, k -> new Edge[m]), 0, i);
						newPath.get(currentEdge)[i] = currentEdge;
					}
				}
			}

			path.clear();
			path.putAll(newPath);
			newPath.clear();
		}

		minProbability = Double.POSITIVE_INFINITY;
		minProbabilityEdge = null;
		for(final Edge edge : graphEdges)
			if(fScores.get(edge)[m - 1] < minProbability){
				minProbability = fScores.get(edge)[m - 1];
				minProbabilityEdge = edge;
			}
		return (minProbabilityEdge != null? path.get(minProbabilityEdge): null);
	}

}
