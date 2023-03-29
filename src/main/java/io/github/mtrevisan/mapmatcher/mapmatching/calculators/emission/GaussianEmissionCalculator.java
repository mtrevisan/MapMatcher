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
import io.github.mtrevisan.mapmatcher.spatial.Polyline;


public class GaussianEmissionCalculator extends EmissionProbabilityCalculator{

	private static final double K1 = 2. / StrictMath.PI;
	private static final double K2 = StrictMath.sqrt(2. * Math.PI);

	/**
	 * 0 < tau0 < 1
	 *
	 * @see <a href="https://www.hindawi.com/journals/jat/2021/9993860/">An online map matching algorithm based on second-order Hidden Markov Model</a>
	 */
	private static final double TAU0 = 0.6;


	private final double observationStandardDeviation;


	public GaussianEmissionCalculator(final double observationStandardDeviation){
		this.observationStandardDeviation = observationStandardDeviation;
	}


	/**
	 * Calculate emission probability
	 * <p>
	 * A zero-mean gaussian observation error:
	 * Pr(o_i | r_j) = 1 / (√(2 ⋅ π) ⋅ σ) ⋅ exp(-0.5 ⋅ (dist(o_i, r_j) / σ)^2), where σ = 20 m (empirically)
	 * </p>
	 *
	 * @see <a href="https://hal-enac.archives-ouvertes.fr/hal-01160130/document">Characterization of GNSS receiver position errors for user integrity monitoring in urban environments</a>
	 * @see <a href="https://www.hindawi.com/journals/jat/2021/9993860/">An online map matching algorithm based on second-order Hidden Markov Model</a>
	 */
	@Override
	public double emissionProbability(final Point observation, final Edge segment, final Point previousObservation){
		final Polyline polyline = segment.getPath();
		final double distance = observation.distance(polyline);
		final double tmp = distance / observationStandardDeviation;

		//weight given on vehicle heading, which is related to the road direction angle and the trajectory direction angle
		double tau = 1.;
		if(previousObservation != null){
			final Point previousObservationClosest = polyline.onTrackClosestPoint(previousObservation);
			final Point currentObservationClosest = polyline.onTrackClosestPoint(observation);
			if(!previousObservationClosest.equals(currentObservationClosest)){
				final double angleRoad = previousObservationClosest.initialBearing(currentObservationClosest);
				final double angleGPS = previousObservation.initialBearing(observation);
				final double angleDelta = StrictMath.abs(angleRoad - angleGPS);
				tau = TAU0 + StrictMath.exp(StrictMath.toRadians(Math.min(360. - angleDelta, angleDelta)) - K1);
			}
		}

		//expansion of:
		//final double probability = Math.exp(-0.5 * tau * tmp) / (StrictMath.sqrt(2. * Math.PI) * observationStandardDeviation);
		//return ProbabilityHelper.logPr(probability);
		//in order to overcome overflow on exponential
		return 0.5 * tau * tmp - ProbabilityHelper.logPr(K2 * observationStandardDeviation);
	}

}
