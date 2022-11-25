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

import io.github.mtrevisan.mapmatcher.graph.Edge;
import org.locationtech.jts.geom.Coordinate;

import java.util.HashSet;
import java.util.Set;


public class TopologicTransitionCalculator implements TransitionProbabilityCalculator{

	private static final double BETA = 3.;
	private static final double TRANSITION_PROBABILITY_CONNECTED_EDGES = Math.exp(-1.);


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
		final double a = (intersectingPoints == 2? 1.: (intersectingPoints == 1? TRANSITION_PROBABILITY_CONNECTED_EDGES: 0.));
		return InitialProbabilityCalculator.logPr(a / (1. + TRANSITION_PROBABILITY_CONNECTED_EDGES));
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

}
