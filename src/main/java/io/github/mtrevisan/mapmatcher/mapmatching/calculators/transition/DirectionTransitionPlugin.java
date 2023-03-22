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
import io.github.mtrevisan.mapmatcher.helpers.PathHelper;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.topologies.TopologyCalculator;


public class DirectionTransitionPlugin implements TransitionProbabilityPlugin{

	private static final double PROBABILITY_UNCONNECTED_EDGES = Double.POSITIVE_INFINITY;
	private static final double PROBABILITY_SAME_POINT = 0.;


	@Override
	public double factor(final Edge fromSegment, final Edge toSegment, final Point previousObservation, final Point currentObservation,
			final Polyline path){
		final int size = path.size();
		double logPr = PROBABILITY_UNCONNECTED_EDGES;
		if(size == 1)
			logPr = PROBABILITY_SAME_POINT;
		else if(size > 1){
			//FIXME something's not right, even if from and to are reversed, path is ok, so this calculation cannot be right
			final Point previousOnTrackPoint = path.onTrackClosestPoint(previousObservation);
			final Point currentOnTrackPoint = path.onTrackClosestPoint(currentObservation);
			final TopologyCalculator topologyCalculator = previousObservation.getDistanceCalculator();

			final double onPathInitialBearing;
			if(PathHelper.isGoingBackward(previousOnTrackPoint, currentOnTrackPoint, path))
				onPathInitialBearing = topologyCalculator.initialBearing(currentOnTrackPoint, previousOnTrackPoint);
			else
				onPathInitialBearing = topologyCalculator.initialBearing(previousOnTrackPoint, currentOnTrackPoint);

			//direction from previous to current observation
			final double observationInitialBearing = topologyCalculator.initialBearing(previousObservation, currentObservation);

			//angle difference
			final double angleDelta = Math.abs(observationInitialBearing - onPathInitialBearing);

			logPr = InitialProbabilityCalculator.logPr(Math.abs(StrictMath.cos(Math.toRadians(Math.min(360. - angleDelta, angleDelta)))));
		}

		return logPr;
	}

}
