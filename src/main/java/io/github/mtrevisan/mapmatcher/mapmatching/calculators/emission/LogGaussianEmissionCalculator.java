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
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Coordinate;
import io.github.mtrevisan.mapmatcher.spatial.distances.DistanceCalculator;

import java.util.Collection;


public class LogGaussianEmissionCalculator implements EmissionProbabilityCalculator{

	private final double observationStandardDeviation;
	private final DistanceCalculator distanceCalculator;


	public LogGaussianEmissionCalculator(final double observationStandardDeviation, final DistanceCalculator distanceCalculator){
		this.observationStandardDeviation = observationStandardDeviation;
		this.distanceCalculator = distanceCalculator;
	}


	@Override
	public void updateEmissionProbability(final Coordinate observation, final Collection<Edge> edges){}

	/**
	 * Calculate emission probability
	 * <p>
	 * A zero-mean gaussian observation error:
	 * Pr(o_i | r_j) = 1 / (√(2 ⋅ π) ⋅ σ) ⋅ exp(-0.5 ⋅ (dist(o_i, r_j) / σ)^2), where σ = 20 m (empirically)
	 * </p>
	 *
	 * @see <a href="https://hal-enac.archives-ouvertes.fr/hal-01160130/document">Characterization of GNSS receiver position errors for user integrity monitoring in urban environments</a>
	 */
	@Override
	public double emissionProbability(final Coordinate observation, final Edge segment,
			final Coordinate previousObservation){
		final double distance = distanceCalculator.distance(observation, segment.getPolyline());
		final double tmp = distance / observationStandardDeviation;

		//weight given on vehicle heading, which is related to the road direction angle and the trajectory direction angle
		double tau = 1.;
		if(previousObservation != null){
			final Coordinate previousObservationClosest = distanceCalculator.onTrackClosestPoint(previousObservation, segment.getPolyline());
			final Coordinate currentObservationClosest = distanceCalculator.onTrackClosestPoint(observation, segment.getPolyline());
			final double angleRoad = distanceCalculator.initialBearing(previousObservationClosest, currentObservationClosest);
			final double angleGPS = distanceCalculator.initialBearing(previousObservation, observation);
			tau = Math.exp(Math.toRadians(Math.abs(angleRoad - angleGPS)) - 2. / Math.PI);
		}

		//expansion of:
		//final double probability = Math.exp(-0.5 * tau * tmp * tmp) / (Math.sqrt(2. * Math.PI) * observationStandardDeviation);
		//return InitialProbabilityCalculator.logPr(probability);
		//in order to overcome overflow on exponential
		return 0.5 * tau * tmp * tmp - InitialProbabilityCalculator.logPr(Math.sqrt(2. * Math.PI) * observationStandardDeviation);
	}

}
