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
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.helpers.PathHelper;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.topologies.TopologyCalculator;

import java.util.List;


public class DirectionTransitionPlugin implements TransitionProbabilityPlugin{

	private static final double PROBABILITY_SAME_EDGE = Math.exp(-0.5);


	@Override
	public double factor(final Edge fromSegment, final Edge toSegment, final Graph graph,
			final Point previousObservation, final Point currentObservation, final List<Node> path){
		//direction from previous to current point on track
		final Edge[] pathAsEdges = PathHelper.extractPathAsEdges(path);
		final Polyline pathAsPolyline = PathHelper.extractPathAsPolyline(pathAsEdges, fromSegment, toSegment,
			previousObservation, currentObservation);
		if(pathAsPolyline == null)
			return PROBABILITY_SAME_EDGE;

		final Point previousOnTrackPoint = pathAsPolyline.onTrackClosestPoint(previousObservation);
		final Point currentOnTrackPoint = pathAsPolyline.onTrackClosestPoint(currentObservation);
		if(previousOnTrackPoint.equals(currentOnTrackPoint))
			return PROBABILITY_SAME_EDGE;


		final TopologyCalculator calculator = previousObservation.getDistanceCalculator();

		final double onPathInitialBearing;
		if(pathAsPolyline.alongTrackDistance(previousOnTrackPoint) <= pathAsPolyline.alongTrackDistance(currentOnTrackPoint))
			onPathInitialBearing = calculator.initialBearing(previousOnTrackPoint, currentOnTrackPoint);
		else
			onPathInitialBearing = calculator.initialBearing(currentOnTrackPoint, previousOnTrackPoint);

		//direction from previous to current observation
		final double observationInitialBearing = calculator.initialBearing(previousObservation, currentObservation);

		//angle difference
		final double initialBearingDifference = Math.abs(observationInitialBearing - onPathInitialBearing);

		return InitialProbabilityCalculator.logPr(Math.abs(StrictMath.cos(Math.toRadians(initialBearingDifference))));
	}

}
