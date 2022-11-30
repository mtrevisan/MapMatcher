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
package io.github.mtrevisan.mapmatcher.mapmatching.calculators;

import io.github.mtrevisan.mapmatcher.distances.DistanceCalculator;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import org.locationtech.jts.geom.Coordinate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class LogBayesianEmissionCalculator implements EmissionProbabilityCalculator{

	private final DistanceCalculator distanceCalculator;

	private final Map<Edge, Double> emissionProbability = new HashMap<>();


	public LogBayesianEmissionCalculator(final DistanceCalculator distanceCalculator){
		this.distanceCalculator = distanceCalculator;
	}


	@Override
	public void updateEmissionProbability(final Coordinate observation, final Collection<Edge> edges){
		//step 1. Calculate dist(p_i, r_j)
		//step 2. Calculate sum(k=1..n of dist(p_i, r_k))
		double cumulativeDistance = 0.;
		for(final Edge edge : edges){
			final double distance = distanceCalculator.distance(edge.getLineString(), observation);
			emissionProbability.put(edge, distance);
			cumulativeDistance += distance;
		}

		//step 3. Calculate Pr(r_j | p_i)
		double cumulativeProbability = 0.;
		for(final Edge edge : edges){
			final double probability = cumulativeDistance / emissionProbability.get(edge);
			emissionProbability.put(edge, probability);
			cumulativeProbability += probability;
		}

		//step 4. Calculate Pr(p_i | r_j)
		for(final Edge edge : edges){
			final double logProbability = InitialProbabilityCalculator.logPr(emissionProbability.get(edge) / cumulativeProbability);
			emissionProbability.put(edge, logProbability);
		}
	}

	/**
	 * Calculate emission probability
	 * <p>
	 * With the Bayesian rule: <code>Pr(o_i | r_j) = Pr(r_j | o_i) ⋅ Pr(o_i) / sum(k=1..n of Pr(r_k | o_i) ⋅ Pr(o_i))</code>, where
	 * <code>i=1..m, j=1..n</code> presume that <code>Pr(o_i)</code> follows a uniform distribution, therefore:
	 * <code>Pr(o_i | r_j) = Pr(r_j | o_i) / sum(k=1..n of Pr(r_k | o_i))</code>
	 * <code>Pr(r_j | o_i)</code> is the probability that is the correct segment out of the candidate segments given that measured location
	 * is <code>o_i</code>.
	 * It's computed by assuming that, for most of the GPS points, the closer a segment is to the observed point, the higher the
	 * probabilities that it is the correct segment. Considering the relationship of distance and observation probability as an inverse
	 * proportion, first it's computed the probability of the perpendicular distance from GPS point <code>o_i</code> to the segment
	 * <code>r_j<code> over the summation of the distances from <code>o_i</code> to all the candidate segments, and then use reciprocal
	 * relation of the probability based on distances to approximate observation probability.
	 * This leads to: <code>Pr(r_j | o_i) = 1 / (δ(o_i, r_j) / sum(k=1..n of δ(o_i, r_k)))</code>
	 * </p>
	 */
	@Override
	public double emissionProbability(final Coordinate observation, final Edge segment){
		return emissionProbability.get(segment);
	}

}
