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
package io.github.mtrevisan.mapmatcher.weight;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Vertex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class LogMapEdgeWeightCalculator implements EdgeWeightCalculator{

	private static final double SIGMA_OBSERVATION = 4.07;
	private static final double BETA = 3.;

	private Map<Vertex, Double> emissionProbability;


	public void updateEmissionProbability(final Coordinates observation, final Collection<Edge> edges){
		emissionProbability = calculateEmissionProbability(observation, edges);
	}

	/**
	 * Calculate transition probability
	 * <p>
	 * If two segments are (r_ij is the so-called topological relationship, and a_ij = e^-r_ij):
	 * <dl>
	 *    <dt>unconnected</dt>
	 *    	<dd><code>r_ij = ∞</code>, thus <code>a_ij = 0</code>, and <code>-ln(a_ij) = ∞</code></dd>
	 *    <dt>connected</dt>
	 *    	<dd><code>r_ij = 1</code>, thus <code>a_ij = 0.36787944117</code>, and <code>-ln(a_ij) = 1</code></dd>
	 *    <dt>the same (i = j)</dt>
	 *    	<dd><code>r_ij = 0</code>, thus <code>a_ij = 1</code>, and <code>-ln(a_ij) = 0</code></dd>
	 * </dl>
	 * </p>
	 */
	@Override
	public double calculateWeight(final Edge edge){
		return (edge.getFrom().equals(edge.getTo())? 0.: 1.);
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
	public double calculateWeight(final Vertex from, final Vertex to){
		return emissionProbability.getOrDefault(from, 0.);
	}

	private Map<Vertex, Double> calculateEmissionProbability(final Coordinates observation, final Collection<Edge> edges){
		final Map<Vertex, Double> probabilities = new HashMap<>(edges.size());
		double sum = 0.;
		for(final Edge edge : edges){
			final double distance = observation.getPoint().distance(edge.getLineString());
			probabilities.put(edge.getTo(), distance);
			sum += distance;
		}
		sum = 0.;
		for(final Edge edge : edges){
			final double value = sum / probabilities.get(edge.getTo());
			probabilities.put(edge.getTo(), value);
			sum += value;
		}
		for(final Edge edge : edges)
			probabilities.put(edge.getTo(), logPr(probabilities.get(edge.getTo()) / sum));
		return probabilities;
	}

	private static double logPr(final double probability){
		return -StrictMath.log(probability);
	}



//	private static double edgeCost(final Geometry segment1, final Geometry segment2){
//		return -StrictMath.log(transitionProbability(segment1, segment2));
//	}
//
//	//exponential function of the difference between the route length (in degrees!) and the great circle distance (in degrees!)
//	//between o_t and o_t+1, Pr(r_i | r_i-1) = β ⋅ exp(-β ⋅ |δ(o_i-1, o_i) - σ(x_i-1, x_i)|)
//	/*
//	Pr(r_i | r_i-1) = 1/(2 ⋅ π ⋅ σ_p) * exp(-0.5 ⋅ (||p_t - x_t_i||great_circle / σ_p)^2) where x_t_i is the point on road segment r_i
//	nearest the measurement p_t at time t, and σ_p can be thought of as an estimate of the standard deviation of GPS noise
//	(Newson and Krumm (2009) derive σ_p from the median absolute deviation over their dataset, arriving at a value of 4.07)
//
//	p(d_t) = 1/β ⋅ exp(-d_t / β) where d_t is the difference between the great circle distance and route-traveled distance between time t
//	and t+1
//	*/
//	private static double transitionProbability(final Geometry segment1, final Geometry segment2){
//		//calculating route distance is expensive (https://github.com/valhalla/valhalla/blob/master/docs/meili/algorithms.md)
//		//		final double delta = Math.abs(routeLength(segment1, segment2) - segment1.distance(segment2));
//		final double delta = segment1.distance(segment2);
//		return BETA * StrictMath.exp(-BETA * delta);
//	}
//
//	private static double nodeCost(final Point observation, final Geometry segment){
//		return -StrictMath.log(emissionProbability(observation, segment));
//	}
//
//	//A zero-mean gaussian observation error, Pr(o_i | r_i) = 1/(√(2 ⋅ π) ⋅ σ_o) ⋅ exp(-0.5 ⋅ (δ(o_i, x_i) / σ_o)^2)
//	private static double emissionProbability(final Point observation, final Geometry segment){
//		final double c = 1. / (StrictMath.sqrt(2. * Math.PI) * SIGMA_OBSERVATION);
//		final double reducedDistance = observation.distance(segment) / SIGMA_OBSERVATION;
//		return c * StrictMath.exp(-0.5 * reducedDistance * reducedDistance);
//	}

}
