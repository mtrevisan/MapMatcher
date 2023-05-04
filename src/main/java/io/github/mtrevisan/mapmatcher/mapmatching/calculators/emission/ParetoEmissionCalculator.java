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

		//expansion of:
		//final double probability = Math.pow(1. + shapeFactor * tmp, -1. / shapeFactor - 1.) / observationStandardDeviation;
		//return ProbabilityHelper.logPr(probability);
		//in order to overcome overflow on exponential
		return k4 * ProbabilityHelper.logPr(1. + shapeFactor * tmp) - k5;
	}

}
