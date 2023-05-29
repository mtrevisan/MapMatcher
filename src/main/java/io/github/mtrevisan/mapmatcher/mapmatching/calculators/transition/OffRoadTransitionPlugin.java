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
package io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.ProbabilityHelper;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;


public class OffRoadTransitionPlugin implements TransitionProbabilityPlugin{

	private static final double LOG_PR_BACKWARD_DIRECTION = ProbabilityHelper.logPr(0.);

	//constant from an edge of the graph to an off-road edge
	private static final double PHI = 0.2;
	//constant from an off-road edge to another off-road edge
	private static final double PSI = 0.48;

	private static final double LOG_PR_PHI = ProbabilityHelper.logPr(PHI);
	private static final double LOG_PR_NOT_PHI = ProbabilityHelper.logPr(1. - PHI);
	private static final double LOG_PR_PSI = ProbabilityHelper.logPr(PSI);
	private static final double LOG_PR_NOT_PSI = ProbabilityHelper.logPr(1. - PSI);


	@Override
	public double factor(final Edge fromEdge, final Edge toEdge, final Point previousObservation, final Point currentObservation,
			final Polyline path){
		if(path.isEmpty())
			return LOG_PR_BACKWARD_DIRECTION;

		double logPrOffRoadFactor;
		if(!fromEdge.isOffRoad())
			//`offRoadFactor = φ` or `1 - φ`, whether `toEdge` is off-road or not
			logPrOffRoadFactor = (toEdge.isOffRoad()? LOG_PR_PHI: LOG_PR_NOT_PHI);
		else
			//`offRoadFactor = ψ` or `1 - ψ`, whether `toEdge` is off-road or not
			logPrOffRoadFactor = (toEdge.isOffRoad()? LOG_PR_PSI: LOG_PR_NOT_PSI);
		return logPrOffRoadFactor;
	}

}
