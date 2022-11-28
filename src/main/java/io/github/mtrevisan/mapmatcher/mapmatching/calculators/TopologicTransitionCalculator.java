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
import io.github.mtrevisan.mapmatcher.helpers.WGS84GeometryHelper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;


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
	 * Pr(r_i | r_i-1) = β ⋅ exp(-β ⋅ |dist(o_i-1, o_i) - pathDistance(r_i-1, r_i)|)
	 * </p>
	 */
	@Override
	public double transitionProbability(final Edge fromSegment, final Edge toSegment,
			final Coordinate previousObservation, final Coordinate currentObservation){
		double a = 0;
		final Coordinate fromToCoordinate = fromSegment.getTo().getCoordinate();
		final Coordinate toFromCoordinate = toSegment.getFrom().getCoordinate();
		final LineString fromSegmentLineString = fromSegment.getLineString();
		if(fromToCoordinate.equals(toFromCoordinate)){
			//calculate Along-Track Distance
			double previousATD = WGS84GeometryHelper.alongTrackDistance(fromSegmentLineString, previousObservation);
			double currentATD = WGS84GeometryHelper.alongTrackDistance(fromSegmentLineString, currentObservation);
			if(previousATD == currentATD){
				final LineString toSegmentLineString = toSegment.getLineString();
				previousATD = WGS84GeometryHelper.alongTrackDistance(toSegmentLineString, previousObservation);
				currentATD = WGS84GeometryHelper.alongTrackDistance(toSegmentLineString, currentObservation);
			}
			//NOTE: take into consideration the direction of travel
			if(previousATD <= currentATD)
				a = TRANSITION_PROBABILITY_CONNECTED_EDGES;
		}
		else{
			final Coordinate fromFromCoordinates = fromSegment.getFrom().getCoordinate();
			final Coordinate toToCoordinates = toSegment.getTo().getCoordinate();
			if(fromToCoordinate.equals(toToCoordinates) && fromFromCoordinates.equals(toFromCoordinate)){
				//calculate Along-Track Distance
				final double previousATD = WGS84GeometryHelper.alongTrackDistance(fromSegmentLineString, previousObservation);
				final double currentATD = WGS84GeometryHelper.alongTrackDistance(fromSegmentLineString, currentObservation);
				//NOTE: take into consideration the direction of travel
				if(previousATD <= currentATD)
					a = 1.;
			}
		}

		return InitialProbabilityCalculator.logPr(a / (1. + TRANSITION_PROBABILITY_CONNECTED_EDGES));
	}

}
