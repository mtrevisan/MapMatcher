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
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.helpers.PathHelper;
import io.github.mtrevisan.mapmatcher.pathfinding.AStarPathFinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.NodeCountCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Coordinate;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.distances.DistanceCalculator;

import java.util.List;


public class LogExponentialTransitionCalculator implements TransitionProbabilityCalculator{

	private static final PathFindingStrategy PATH_FINDER = new AStarPathFinder(new NodeCountCalculator());

	private final double rateParameter;
	private final DistanceCalculator distanceCalculator;


	public LogExponentialTransitionCalculator(final double rateParameter, final DistanceCalculator distanceCalculator){
		this.rateParameter = rateParameter;
		this.distanceCalculator = distanceCalculator;
	}

	/**
	 * Calculate transition probability
	 * <p>
	 * Exponential function of the difference between the route length and the great circle distance between <code>o_t</code> and
	 * <code>o_t+1</code>.<br/>
	 * </code>Pr(r_i | r_i-1) = β ⋅ exp(-β ⋅ |dist(o_i-1, o_i) - pathDistance(x_i-1, x_i)|)</code> where <code>β</code> is 3 (empirically),
	 * and <code>x_k</code> is the projection of the observation <code>o_k</code> onto the segment <code>r</code>.
	 * </p>
	 *
	 * Otherwise
	 *
	 * <p>
	 * <code>Pr(r_i-1 -> r_i) = dist(o_i-1, o_i) / pathDistance(x_i-1, x_i)</code>, where <code>x_k</code> is the projection of the
	 * observation <code>o_k</code> onto the segment <code>r</code>.
	 * </p>
	 */
	@Override
	public double transitionProbability(final Edge fromSegment, final Edge toSegment, final Graph graph,
			final Coordinate previousObservation, final Coordinate currentObservation){
		final double observationsDistance = distanceCalculator.distance(previousObservation, currentObservation);

		final List<Node> path = PATH_FINDER.findPath(fromSegment.getTo(), toSegment.getFrom(), graph)
			.simplePath();
		final Polyline pathAsPolyline = PathHelper.extractPathAsPolyline(path);
		final double pathDistance = distanceCalculator.alongTrackDistance(currentObservation, pathAsPolyline)
			- distanceCalculator.alongTrackDistance(previousObservation, pathAsPolyline);

		//expansion of:
		//final double a = rateParameter * Math.exp(-rateParameter * Math.abs(observationsDistance - pathDistance));
		//return InitialProbabilityCalculator.logPr(a);
		//in order to overcome overflow on exponential
		return -Math.log(rateParameter) - rateParameter * Math.abs(observationsDistance - pathDistance);
	}

}
