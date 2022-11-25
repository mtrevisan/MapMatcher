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

import io.github.mtrevisan.mapmatcher.distances.DistanceCalculator;
import io.github.mtrevisan.mapmatcher.distances.GeodeticCalculator;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.helpers.GeodeticHelper;
import org.locationtech.jts.geom.Coordinate;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class LogMapMatchingProbabilityCalculator implements MapMatchingProbabilityCalculator{

	private static final double BETA = 3.;

	private final double observationStandardDeviation;
	private final DistanceCalculator distanceCalculator;

	private double initialProbability;
	private final Map<Edge, Double> emissionProbability = new HashMap<>();


	public LogMapMatchingProbabilityCalculator(final double observationStandardDeviation, final DistanceCalculator distanceCalculator){
		this.observationStandardDeviation = observationStandardDeviation;
		this.distanceCalculator = distanceCalculator;
	}


	public void calculateInitialProbability(final Coordinate observation, final Collection<Edge> segments){
		initialProbability = logPr(1. / segments.size());
	}

	@Override
	public double initialProbability(final Edge segment){
		return initialProbability;
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
	 *
	 * Otherwise
	 *
	 * <p>
	 * Pr(r_i-1 -> r_i) = dist(o_i-1, o_i) / lengthOfShortestPath(r_i-1, r_i), with o_i-1 at r_i-1 and o_i at r_i
	 * </p>
	 *
	 * Otherwise
	 *
	 * <p>
	 * Exponential function of the difference between the route length (in degrees!) and the great circle distance (in degrees!)
	 * between o_t and o_t+1
	 *
	 * Pr(r_i | r_i-1) = β ⋅ exp(-β ⋅ |dist(o_i-1, o_i) - pathDistance(r_i-1, r_i)|)
	 * </p>
	 */
	@Override
	public double transitionProbability(Edge fromSegment, Edge toSegment){
		final int intersectingPoints = intersectionPoints(fromSegment, toSegment);
		return (intersectingPoints == 2? 0.: (intersectingPoints == 1? 1.: Double.POSITIVE_INFINITY));
	}

	/**
	 * Retrieve the number of points this edge's vertices intersects the given edge's vertices.
	 *
	 * @param fromSegment	The incoming segment.
	 * @param toSegment	The outgoing segment.
	 * @return	The number of intersecting vertices.
	 */
	private static int intersectionPoints(final Edge fromSegment, final Edge toSegment){
		final Set<Coordinate> fromCoordinates = new HashSet<>(2);
		fromCoordinates.add(fromSegment.getFromCoordinate());
		fromCoordinates.add(fromSegment.getToCoordinate());
		final Set<Coordinate> toCoordinates = new HashSet<>(2);
		toCoordinates.add(toSegment.getFromCoordinate());
		toCoordinates.add(toSegment.getToCoordinate());
		toCoordinates.retainAll(fromCoordinates);
		return toCoordinates.size();
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
	 *
	 * Otherwise
	 *
	 * <p>
	 * Pr(o_i | r_j) = 1 / (√(2 ⋅  π) ⋅ σ) ⋅ exp(-0.5 ⋅ (dist(o_i, r_j) / σ)^2), where σ = 20 m (empirically)
	 * </p>
	 */
	@Override
	public double emissionProbability(final Coordinate observation, final Edge segment){
		return emissionProbability.get(segment);
	}

	public void updateEmissionProbability(final Coordinate observation, final Collection<Edge> edges){
/*		for(final Edge edge : edges){
			final double factor = Math.toRadians(GeodeticHelper.meanRadiusOfCurvature(observation.getY()));
			final double tmp = distanceCalculator.distance(observation, edge.getLineString()) * factor / observationStandardDeviation;
			final double probability = logPr(Math.exp(-0.5 * tmp * tmp) / (Math.sqrt(2. * Math.PI) * observationStandardDeviation));
			emissionProbability.put(edge, probability);
		}
/**/

/**/		//step 1. Calculate dist(p_i, r_j)
		//step 2. Calculate sum(k=1..n of dist(p_i, r_k))
		double cumulativeDistance = 0.;
		for(final Edge edge : edges){
			final double distance = distanceCalculator.distance(observation, edge.getLineString());
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
			final double logProbability = logPr(emissionProbability.get(edge) / cumulativeProbability);
			emissionProbability.put(edge, logProbability);
		}
/**/
	}

	private static double logPr(final double probability){
		return -StrictMath.log(probability);
	}


//FIXME
//	private static double edgeCost(final Geometry segment1, final Geometry segment2){
//		return logPr(transitionProbability(segment1, segment2));
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
//		return logPr(emissionProbability(observation, segment));
//	}
//
//	//A zero-mean gaussian observation error, Pr(o_i | r_i) = 1/(√(2 ⋅ π) ⋅ σ_o) ⋅ exp(-0.5 ⋅ (δ(o_i, x_i) / σ_o)^2)
//	private static double emissionProbability(final Point observation, final Geometry segment){
//		final double c = 1. / (StrictMath.sqrt(2. * Math.PI) * SIGMA_OBSERVATION);
//		final double reducedDistance = observation.distance(segment) / SIGMA_OBSERVATION;
//		return c * StrictMath.exp(-0.5 * reducedDistance * reducedDistance);
//	}

}
