/**
 * Copyright (c) 2023 Mauro Trevisan
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
import io.github.mtrevisan.mapmatcher.spatial.Polyline;


//in urban environments
public class ParetoEmissionCalculator extends EmissionProbabilityCalculator{

	private static final double K1 = 2. / StrictMath.PI;

	/**
	 * 0 < tau0 < 1
	 *
	 * @see <a href="https://www.hindawi.com/journals/jat/2021/9993860/">An online map matching algorithm based on second-order Hidden Markov Model</a>
	 */
	private static final double TAU0 = 0.6;


	private final double observationStandardDeviation;

	private final double shapeFactor;
	private final double k4;
	private final double k5;


	public ParetoEmissionCalculator(final double observationStandardDeviation, final double shapeFactor){
		this.observationStandardDeviation = observationStandardDeviation;

		this.shapeFactor = shapeFactor;
		k4 = -(1. / shapeFactor + 1.);
		k5 = ProbabilityHelper.logPr(observationStandardDeviation);
	}


	/**
	 * @see <a href="https://hal-enac.archives-ouvertes.fr/hal-01160130/document">Characterization of GNSS receiver position errors for user integrity monitoring in urban environments</a>
	 */
	@Override
	public double emissionProbability(final Point observation, final Edge edge, final Point previousObservation){
		final Polyline polyline = edge.getPath();
		final double distance = observation.distance(polyline);
		final double tmp = distance / observationStandardDeviation;

		//weight given on vehicle heading, which is related to the road direction angle and the trajectory direction angle
		final double tau = headingWeight(observation, previousObservation, polyline);

		//expansion of:
		//final double probability = Math.pow(1. + shapeFactor * tmp, -1. / shapeFactor - 1.) / observationStandardDeviation;
		//return ProbabilityHelper.logPr(probability);
		//in order to overcome overflow on exponential
		return k4 * ProbabilityHelper.logPr(1. + shapeFactor * tau * tmp) - k5;
	}

	private static double headingWeight(final Point currentObservation, final Point previousObservation, final Polyline polyline){
		double tau = 1.;
		if(previousObservation != null){
			final Point previousObservationClosest = polyline.onTrackClosestPoint(previousObservation);
			final Point currentObservationClosest = polyline.onTrackClosestPoint(currentObservation);
			if(!previousObservationClosest.equals(currentObservationClosest)){
				final double angleRoad = previousObservationClosest.initialBearing(currentObservationClosest);
				final double angleGPS = previousObservation.initialBearing(currentObservation);
				final double angleDelta = calculateAngleDifference(angleRoad, angleGPS);
				tau = TAU0 + StrictMath.exp(StrictMath.toRadians(angleDelta) - K1);
			}
		}
		return tau;
	}

	private static double calculateAngleDifference(final double angleRoad, final double angleGPS){
		final double angleDelta = StrictMath.abs(angleRoad - angleGPS);
		return Math.min(360. - angleDelta, angleDelta);
	}

}
