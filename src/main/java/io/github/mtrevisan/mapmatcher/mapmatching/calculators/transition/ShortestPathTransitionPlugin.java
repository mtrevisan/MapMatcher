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


public class ShortestPathTransitionPlugin implements TransitionProbabilityPlugin{

	/**
	 * 0.5 < p_same < 0.8
	 *
	 * @see <a href="https://www.hindawi.com/journals/jat/2021/9993860/">An online map matching algorithm based on second-order Hidden Markov Model</a>
	 */
	private static final double PROBABILITY_SAME_EDGE = 0.6;
	private static final double LOG_PR_SAME_EDGE = ProbabilityHelper.logPr(PROBABILITY_SAME_EDGE);
	private static final double LOG_PR_DIFFERENT_EDGE = ProbabilityHelper.logPr(1. - PROBABILITY_SAME_EDGE);
	private static final double LOG_PR_UNCONNECTED_EDGES = ProbabilityHelper.logPr(0.);

	private static final double PHI = 10.;
	private static final double PSI = 1.5;
	private static final double LOG_PR_PHI = ProbabilityHelper.logPr(PHI);
	private static final double LOG_PR_PSI = ProbabilityHelper.logPr(PSI);
	private static final double LOG_PR_PHI_1 = ProbabilityHelper.logPr(PHI + 1.);
	private static final double LOG_PR_PSI_1 = ProbabilityHelper.logPr(PSI + 1.);


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
	 * <code>γ = 1 / β</code>, and <code>x_k</code> is the projection of the observation <code>o_k</code> onto the segment <code>r</code>.
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
	 * @see <a href="https://www1.pub.informatik.uni-wuerzburg.de/pub/haunert/pdf/HaunertBudig2012.pdf">An algorithm for Map Matching given incomplete road data</a>
	 */
	@Override
	public double factor(final Edge fromEdge, final Edge toEdge, final Point previousObservation, final Point currentObservation,
			final Polyline path){
		if(path.isEmpty())
			return LOG_PR_UNCONNECTED_EDGES;

		return (fromEdge.equals(toEdge)? LOG_PR_SAME_EDGE: LOG_PR_DIFFERENT_EDGE)
			+ calculateOffRoadFactor(fromEdge, toEdge)
			+ calculateLogPr(previousObservation, currentObservation, path);
	}

	private static double calculateOffRoadFactor(final Edge fromEdge, final Edge toEdge){
		double logPrOffRoadFactor;
		if(!fromEdge.isOffRoad())
			//`offRoadFactor = 1 / (phi + 1)` or `phi / (phi + 1)`, whether `toEdge` is off-road or not
			logPrOffRoadFactor = (toEdge.isOffRoad()? 0.: LOG_PR_PHI) - LOG_PR_PHI_1;
		else
			//`offRoadFactor = 1 / (psi + 1)` or `psi / (psi + 1)`, whether `toEdge` is off-road or not
			logPrOffRoadFactor = (toEdge.isOffRoad()? 0.: LOG_PR_PSI) - LOG_PR_PSI_1;
		return logPrOffRoadFactor;
	}

	private double calculateLogPr(final Point previousObservation, final Point currentObservation, final Polyline path){
		final double observationsDistance = previousObservation.distance(currentObservation);
		final Point previousOnTrackPoint = path.onTrackClosestPoint(previousObservation);
		final Point currentOnTrackPoint = path.onTrackClosestPoint(currentObservation);
		final double pathDistance = (previousOnTrackPoint.equals(currentOnTrackPoint)
			? 0.
			: path.alongTrackDistance(currentOnTrackPoint) - path.alongTrackDistance(previousOnTrackPoint));

		//expansion of:
		//final double a = rateParameter * Math.exp(-rateParameter * Math.abs(observationsDistance - pathDistance));
		//return ProbabilityHelper.logPr((sameSegment? PROBABILITY_SAME_EDGE: 1. - PROBABILITY_SAME_EDGE) * a);
		//in order to overcome overflow on exponential
		return logPrInverseRateParameter
			+ Math.abs(observationsDistance - pathDistance) * inverseRateParameter;
	}

}
