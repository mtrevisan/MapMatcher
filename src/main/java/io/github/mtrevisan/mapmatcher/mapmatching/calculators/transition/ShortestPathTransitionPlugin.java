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


public class ShortestPathTransitionPlugin implements TransitionProbabilityPlugin{

	/**
	 * 0.5 < p_same < 0.8
	 *
	 * @see <a href="https://www.hindawi.com/journals/jat/2021/9993860/">An online map matching algorithm based on second-order Hidden Markov Model</a>
	 */
	private static final double PROBABILITY_SAME_EDGE = 0.6;

	private static final double LOG_PR_SAME_EDGE = ProbabilityHelper.logPr(PROBABILITY_SAME_EDGE);
	private static final double LOG_PR_DIFFERENT_EDGE = ProbabilityHelper.logPr(1. - PROBABILITY_SAME_EDGE);
	private static final double LOG_PR_UNFEASIBLE = ProbabilityHelper.logPr(0.);


	/** The <code>γ</code> parameter of an exponential probability distribution (<code>γ = 1 / β</code>). */
	private final double inverseRateParameter;
	private final double logPrInverseRateParameter;


	/**
	 * @param rateParameter	The <code>β</code> parameter of an exponential probability distribution.
	 */
	public ShortestPathTransitionPlugin(final double rateParameter){
		inverseRateParameter = 1. / rateParameter;
		logPrInverseRateParameter = ProbabilityHelper.logPr(inverseRateParameter);
	}

	/**
	 * Calculate transition probability
	 * <p>
	 * Exponential function of the difference between the route length and the great circle distance between <code>o_t</code> and
	 * <code>o_t+1</code>.<br/>
	 * </code>Pr(r_i | r_i-1) = 0.5 ⋅ γ ⋅ exp(-γ ⋅ |dist(o_i-1, o_i) - pathDistance(x_i-1, x_i)|)</code> where <code>γ</code> is 3 (empirically),
	 * <code>γ = 1 / β</code>, and <code>x_k</code> is the projection of the observation <code>o_k</code> onto the edge <code>r</code>.
	 * </p>
	 *
	 * Otherwise
	 *
	 * <p>
	 * <code>Pr(r_i-1 -> r_i) = dist(o_i-1, o_i) / pathDistance(x_i-1, x_i)</code>, where <code>x_k</code> is the projection of the
	 * observation <code>o_k</code> onto the edge <code>r</code>.
	 * </p>
	 *
	 * @see <a href="https://www.hindawi.com/journals/jat/2021/9993860/">An online map matching algorithm based on second-order Hidden Markov Model</a>
	 * @see <a href="https://www1.pub.informatik.uni-wuerzburg.de/pub/haunert/pdf/HaunertBudig2012.pdf">An algorithm for Map Matching given incomplete road data</a>
	 */
	@Override
	public double factor(final Edge fromEdge, final Edge toEdge, final Point previousObservation, final Point currentObservation,
			final Polyline path){
		if(path.isEmpty())
			return LOG_PR_UNFEASIBLE;

		final double probability = (fromEdge.equals(toEdge)? LOG_PR_SAME_EDGE: LOG_PR_DIFFERENT_EDGE);

		//TODO: lim x->-ε = +inf, lim x->+ε = 0, flex point between 0 and +ε
		final double pathDistance = path.alongTrackDistance(currentObservation) - path.alongTrackDistance(previousObservation);
		if(pathDistance < 0.)
			//the direction of the observations projected onto the path is opposite to the direction of the path
			return probability + LOG_PR_UNFEASIBLE;

		final double observationsDistance = previousObservation.distance(currentObservation);

		//FIXME
		//	in this case: GEOMETRYCOLLECTION(LINESTRING(9.307739 45.356954400000006,9.3067789 45.356556600000005,9.3060155 45.35658710000001,9.3053899 45.356827100000004,9.3047236 45.35734980000001,9.3043893 45.35754560000001,9.3034702 45.35787650000003),POINT(9.310615 45.359275000000025),POINT(9.307181999999884 45.35665399999942))
		//	prev is out of path, therefore dist(prev, curr) = 396 m, dist_path(prev_proj, curr_proj) = 54
		//	that cannot be!
		//	this plugin works only on a fully connected graph!

		//expansion of:
		//final double a = rateParameter * Math.exp(-rateParameter * Math.abs(observationsDistance - pathDistance));
		//return ProbabilityHelper.logPr((sameEdge? PROBABILITY_SAME_EDGE: 1. - PROBABILITY_SAME_EDGE) * a);
		//in order to overcome overflow on exponential
		return probability
			+ logPrInverseRateParameter
			+ Math.abs(observationsDistance - pathDistance) * inverseRateParameter;
	}

}
