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
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;


public class ShortestPathTransitionPlugin implements TransitionProbabilityPlugin{

	/**
	 * 0.5 < p_same < 0.8
	 *
	 * @see <a href="https://www.hindawi.com/journals/jat/2021/9993860/">An online map matching algorithm based on second-order Hidden Markov Model</a>
	 */
	private static final double PROBABILITY_SAME_EDGE = 0.6;
	private static final double LOG_PR_PROBABILITY_SAME_EDGE = InitialProbabilityCalculator.logPr(PROBABILITY_SAME_EDGE);
	private static final double LOG_PR_PROBABILITY_DIFFERENT_EDGE = InitialProbabilityCalculator.logPr(1. - PROBABILITY_SAME_EDGE);
	private static final double PROBABILITY_UNCONNECTED_EDGES = 0.;

	private static final double PHI = 10.;
	private static final double PSI = 1.5;
	private static final double LOG_PR_PHI = InitialProbabilityCalculator.logPr(PHI);
	private static final double LOG_PR_PSI = InitialProbabilityCalculator.logPr(PSI);
	private static final double LOG_PR_PHI_1 = InitialProbabilityCalculator.logPr(PHI + 1.);
	private static final double LOG_PR_PSI_1 = InitialProbabilityCalculator.logPr(PSI + 1.);


	/** The <code>γ</code> parameter of an exponential probability distribution (<code>γ = 1 / β</code>). */
	private final double inverseRateParameter;
	private final double logPrInverseRateParameter;

	private boolean offRoad;


	/**
	 * @param rateParameter	The <code>β</code> parameter of an exponential probability distribution.
	 */
	public ShortestPathTransitionPlugin(final double rateParameter){
		inverseRateParameter = 1. / rateParameter;
		logPrInverseRateParameter = InitialProbabilityCalculator.logPr(inverseRateParameter);
	}

	public ShortestPathTransitionPlugin withOffRoad(){
		offRoad = true;

		return this;
	}

	public boolean isOffRoad(){
		return offRoad;
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
	public double factor(final Edge fromSegment, final Edge toSegment, final Point previousObservation, final Point currentObservation,
			Polyline path){
		double logPrOffRoadFactor = 0.;
		if(offRoad){
			if(!fromSegment.isOffRoad())
				//logPrOffRoadFactor = -ln(1 / (phi + 1)) or -ln(phi / (phi + 1)), whether `toSegment` is off-road or not
				logPrOffRoadFactor = (toSegment.isOffRoad()? 0.: LOG_PR_PHI) - LOG_PR_PHI_1;
			else
				//logPrOffRoadFactor = -ln(1 / (psi + 1)) or -ln(psi / (psi + 1)), whether `toSegment` is off-road or not
				logPrOffRoadFactor = (toSegment.isOffRoad()? 0.: LOG_PR_PSI) - LOG_PR_PSI_1;
		}

		if(fromSegment.equals(toSegment)){
			final double observationsDistance = previousObservation.distance(currentObservation);
			final Polyline segmentPath = fromSegment.getPath();
			final Point previousOnTrackPoint = segmentPath.onTrackClosestPoint(previousObservation);
			final Point currentOnTrackPoint = segmentPath.onTrackClosestPoint(currentObservation);
			final double pathDistance = (previousOnTrackPoint.equals(currentOnTrackPoint)
				? 0.
				: segmentPath.alongTrackDistance(currentOnTrackPoint) - segmentPath.alongTrackDistance(previousOnTrackPoint));

			//expansion of:
			//final double a = rateParameter * Math.exp(-rateParameter * Math.abs(observationsDistance - pathDistance));
			//return InitialProbabilityCalculator.logPr((sameSegment? PROBABILITY_SAME_EDGE: 1. - PROBABILITY_SAME_EDGE) * a);
			//in order to overcome overflow on exponential
			return logPrOffRoadFactor
				+ LOG_PR_PROBABILITY_SAME_EDGE
				+ logPrInverseRateParameter
				+ Math.abs(observationsDistance - pathDistance) * inverseRateParameter;
		}

		if(offRoad && path.isEmpty()){
			if(fromSegment.isOffRoad()){
				final Point candidatePoint = toSegment.getPath().onTrackClosestPoint(fromSegment.getFrom().getPoint());
				if(candidatePoint.equals(fromSegment.getTo().getPoint())){
					final Point[][] cut = toSegment.getPath().cutHard(candidatePoint);
					path = fromSegment.getPath()
						.append(cut[1]);
				}
			}
			else if(toSegment.isOffRoad()){
				final Point candidatePoint = fromSegment.getPath().onTrackClosestPoint(toSegment.getTo().getPoint());
				if(candidatePoint.equals(toSegment.getFrom().getPoint())){
					final Point[][] cut = fromSegment.getPath().cutHard(candidatePoint);
					path = toSegment.getPath()
						.prepend(cut[0]);
				}
			}
		}

		if(path.isEmpty())
			return InitialProbabilityCalculator.logPr(PROBABILITY_UNCONNECTED_EDGES);

		final double observationsDistance = previousObservation.distance(currentObservation);
		final Point previousOnTrackPoint = path.onTrackClosestPoint(previousObservation);
		final Point currentOnTrackPoint = path.onTrackClosestPoint(currentObservation);
		final double pathDistance = (previousOnTrackPoint.equals(currentOnTrackPoint)
			? 0.
			: path.alongTrackDistance(currentOnTrackPoint) - path.alongTrackDistance(previousOnTrackPoint));

		//expansion of:
		//final double a = rateParameter * Math.exp(-rateParameter * Math.abs(observationsDistance - pathDistance));
		//return InitialProbabilityCalculator.logPr((sameSegment? PROBABILITY_SAME_EDGE: 1. - PROBABILITY_SAME_EDGE) * a);
		//in order to overcome overflow on exponential
		return logPrOffRoadFactor
			+ LOG_PR_PROBABILITY_DIFFERENT_EDGE
			+ logPrInverseRateParameter
			+ Math.abs(observationsDistance - pathDistance) * inverseRateParameter;
	}

}
