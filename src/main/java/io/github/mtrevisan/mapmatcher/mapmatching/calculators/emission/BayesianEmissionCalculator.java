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
package io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.ProbabilityHelper;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Deprecated
public class BayesianEmissionCalculator extends EmissionProbabilityCalculator{

	private final Map<Edge, Double> emissionProbability = new HashMap<>();


	/**
	 * Calculate emission probability
	 * <p>
	 * With the Bayesian rule: <code>Pr(o_i | r_j) = Pr(r_j | o_i) ⋅ Pr(o_i) / sum(k=1..n of Pr(r_k | o_i) ⋅ Pr(o_i))</code>, where
	 * <code>i=1..m, j=1..n</code> presume that <code>Pr(o_i)</code> follows a uniform distribution, therefore:
	 * <code>Pr(o_i | r_j) = Pr(r_j | o_i) / sum(k=1..n of Pr(r_k | o_i))</code>
	 * <code>Pr(r_j | o_i)</code> is the probability that is the correct edge out of the candidate edges given that measured location
	 * is <code>o_i</code>.
	 * It's computed by assuming that, for most of the GPS points, the closer a edge is to the observed point, the higher the
	 * probabilities that it is the correct edge. Considering the relationship of distance and observation probability as an inverse
	 * proportion, first it's computed the probability of the perpendicular distance from GPS point <code>o_i</code> to the edge
	 * <code>r_j<code> over the summation of the distances from <code>o_i</code> to all the candidate edges, and then use reciprocal
	 * relation of the probability based on distances to approximate observation probability.
	 * This leads to: <code>Pr(r_j | o_i) = (1 / δ(o_i, r_j)) / sum(k=1..n of 1 / δ(o_i, r_k))</code> and
	 * </p>
	 *
	 * @see <a href="https://www.mdpi.com/2220-9964/6/11/327">Enhanced map-matching algorithm with a hidden markov model for mobile phone positioning</a>
	 */
	@Override
	public void updateEmissionProbability(final Point observation, final Collection<Edge> edges){
		//step 1. Calculate 1 / dist(o_i, r_j)
		//step 2. Calculate sum(k=1..n of 1 / dist(o_i, r_k))
		double cumulative = 0.;
		for(final Edge edge : edges){
			double distance = observation.distance(edge.getPath());
			//NOTE: if the projection point is on the extension line of the section, the distance from the trajectory point to the nearest
			// point of the edge is calculated (see "A self-adjusting online map matching method")
			if(distance < Double.MIN_VALUE)
				distance = edge.getPath().onTrackClosestNode(observation).distance(observation);
			assert (distance >= Double.MIN_VALUE) : "Cannot use bayesian emission calculator because one of the distances is zero";

			emissionProbability.put(edge, 1. / distance);
			cumulative += distance;
		}

		//step 3. Calculate Pr(r_j | o_i)
		//step 4. Calculate sum(k=1..n of Pr(r_k | o_i))
		double cumulativeProbability = 0.;
		for(final Edge edge : edges){
			final double probability = emissionProbability.get(edge) / cumulative;
			emissionProbability.put(edge, probability);
			cumulativeProbability += probability;
		}

		//step 5. Calculate -ln(Pr(o_i | r_j))
		final double logPrCumulativeProbability = ProbabilityHelper.logPr(cumulativeProbability);
		for(final Edge edge : edges){
			final double logProbability = ProbabilityHelper.logPr(emissionProbability.get(edge)) - logPrCumulativeProbability;
			emissionProbability.put(edge, logProbability);
		}
	}

	@Override
	public double emissionProbability(final Point observation, final Edge edge, final Point previousObservation){
		return emissionProbability.getOrDefault(edge, Double.POSITIVE_INFINITY);
	}

}
