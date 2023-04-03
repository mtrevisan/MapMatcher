/**
 * Copyright (c) 2021 Mauro Trevisan
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


public class DirectionTransitionPlugin implements TransitionProbabilityPlugin{

	private static final double PROBABILITY_SAME_EDGE = 0.9;
	private static final double LOG_PR_SAME_EDGE = ProbabilityHelper.logPr(PROBABILITY_SAME_EDGE);
	private static final double LOG_PR_DIFFERENT_EDGE = ProbabilityHelper.logPr(1. - PROBABILITY_SAME_EDGE);


	@Override
	public double factor(final Edge fromEdge, final Edge toEdge, final Point previousObservation, final Point currentObservation,
			final Polyline path){
		final double previousATD = path.alongTrackDistance(previousObservation);
		final double currentATD = path.alongTrackDistance(currentObservation);
		return (previousATD <= currentATD? LOG_PR_SAME_EDGE: LOG_PR_DIFFERENT_EDGE);
	}

}
