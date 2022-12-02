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
import io.github.mtrevisan.mapmatcher.helpers.Coordinate;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.EmissionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.TransitionProbabilityCalculator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * @see <a href="https://en.wikipedia.org/wiki/Viterbi_algorithm">Viterbi algorithm</a>
 */
public class ViterbiMapMatching implements MapMatchingStrategy{

	private final InitialProbabilityCalculator initialProbabilityCalculator;
	private final TransitionProbabilityCalculator transitionProbabilityCalculator;
	private final EmissionProbabilityCalculator emissionProbabilityCalculator;


	public ViterbiMapMatching(final InitialProbabilityCalculator initialProbabilityCalculator,
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

		//NOTE: the initial probability is a uniform distribution reflecting the fact that there is no known bias about which is the
		// correct segment
		int i = extractNextObservation(observations, 0);
		if(i < 0)
			//no observations: cannot calculate path
			return null;

		Coordinate currentObservation = observations[i];
		initialProbabilityCalculator.calculateInitialProbability(currentObservation, graphEdges);
		emissionProbabilityCalculator.updateEmissionProbability(currentObservation, graphEdges);
		for(final Edge edge : graphEdges){
			fScores.computeIfAbsent(edge, k -> new double[m])[i] = initialProbabilityCalculator.initialProbability(edge)
				+ emissionProbabilityCalculator.emissionProbability(currentObservation, edge);
			path.computeIfAbsent(edge, k -> new Edge[n])[i] = edge;
		}

		double minProbability;
		int previousObservationIndex = i;
		while(true){
			final Coordinate previousObservation = observations[previousObservationIndex];
			i = extractNextObservation(observations, previousObservationIndex + 1);
			if(i < 0)
				break;

			currentObservation = observations[i];

			emissionProbabilityCalculator.updateEmissionProbability(currentObservation, graphEdges);

			final Map<Edge, Edge[]> newPath = new HashMap<>(n);
			for(final Edge toEdge : graphEdges){
				minProbability = Double.POSITIVE_INFINITY;
				for(final Edge fromEdge : graphEdges){
					final double probability = fScores.get(fromEdge)[previousObservationIndex]
						+ transitionProbabilityCalculator.transitionProbability(fromEdge, toEdge, graph, previousObservation, currentObservation);
					if(probability < minProbability){
						//record minimum probability
						minProbability = probability;
						fScores.get(toEdge)[i] = probability
							+ emissionProbabilityCalculator.emissionProbability(currentObservation, toEdge);

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

		minProbability = Double.POSITIVE_INFINITY;
		Edge minProbabilityEdge = null;
		for(final Edge edge : graphEdges)
			if(fScores.get(edge)[previousObservationIndex] < minProbability){
				minProbability = fScores.get(edge)[previousObservationIndex];
				minProbabilityEdge = edge;
			}
		return (minProbabilityEdge != null? path.get(minProbabilityEdge): null);
	}

	private static int extractNextObservation(final Coordinate[] observations, int index){
		final int m = observations.length;
		while(index < m && observations[index] == null)
			index ++;
		return (index < m? index: -1);
	}

}
