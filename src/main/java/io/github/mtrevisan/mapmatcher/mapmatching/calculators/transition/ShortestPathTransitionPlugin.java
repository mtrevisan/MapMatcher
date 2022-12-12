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

import java.util.List;


public class ShortestPathTransitionPlugin implements TransitionProbabilityPlugin{

	/**
	 * 0.5 < p_same < 0.8
	 *
	 * @see <a href="https://www.hindawi.com/journals/jat/2021/9993860/">An online map matching algorithm based on second-order Hidden Markov Model</a>
	 */
	private static final double PROBABILITY_SAME_EDGE = 0.6;


	private final double inverseRateParameter;


	public ShortestPathTransitionPlugin(final double inverseRateParameter){
		this.inverseRateParameter = inverseRateParameter;
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
	 *
	 * @see <a href="https://www.hindawi.com/journals/jat/2021/9993860/">An online map matching algorithm based on second-order Hidden Markov Model</a>
	 */
	@Override
	public double factor(final Edge fromSegment, final Edge toSegment, final Graph graph,
			final Point previousObservation, final Point currentObservation, final List<Node> path){
		final double observationsDistance = previousObservation.distance(currentObservation);

		final Edge[] pathAsEdges = PathHelper.extractPathAsEdges(path);
		final Polyline pathAsPolyline = PathHelper.extractPathAsPolyline(pathAsEdges, fromSegment, toSegment,
			previousObservation, currentObservation);
		final double pathDistance = (pathAsPolyline != null
			? pathAsPolyline.alongTrackDistance(currentObservation) - pathAsPolyline.alongTrackDistance(previousObservation)
			: observationsDistance);

		//expansion of:
		//final double a = rateParameter * Math.exp(-rateParameter * Math.abs(observationsDistance - pathDistance));
		//return InitialProbabilityCalculator.logPr(path.isEmpty()? (1. - PROBABILITY_SAME_EDGE) * a: PROBABILITY_SAME_EDGE * a);
		//in order to overcome overflow on exponential
		return InitialProbabilityCalculator.logPr((pathAsPolyline == null || pathAsPolyline.isEmpty()? 1. - PROBABILITY_SAME_EDGE: PROBABILITY_SAME_EDGE) / inverseRateParameter)
			- Math.abs(observationsDistance - pathDistance) / inverseRateParameter;
	}

}
