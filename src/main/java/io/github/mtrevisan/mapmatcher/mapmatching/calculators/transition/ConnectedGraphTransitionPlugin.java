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
package io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.ProbabilityHelper;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;


public class ConnectedGraphTransitionPlugin implements TransitionProbabilityPlugin{

	private static final double PROBABILITY_SAME_EDGE = 3. / 5.;
	private static final double PROBABILITY_DIRECTLY_CONNECTED_EDGES = 2. / 5.;
	private static final double PROBABILITY_EDGES_SEPARATED_BY_ONE_EDGE_ONLY = 1. / 5.;
	private static final double PROBABILITY_EDGES_SEPARATED_BY_MORE_THAN_ONE_EDGE = 0.;
	private static final double PROBABILITY_OFF_ROAD_EDGE = 0.3 / 5.;

	private static final double LOG_PR_SAME_EDGE = ProbabilityHelper.logPr(PROBABILITY_SAME_EDGE);
	private static final double LOG_PR_DIRECTLY_CONNECTED_EDGES = ProbabilityHelper.logPr(PROBABILITY_DIRECTLY_CONNECTED_EDGES);
	private static final double LOG_PR_EDGES_SEPARATED_BY_ONE_EDGE_ONLY = ProbabilityHelper.logPr(PROBABILITY_EDGES_SEPARATED_BY_ONE_EDGE_ONLY);
	private static final double LOG_PR_EDGES_SEPARATED_BY_MORE_THAN_ONE_EDGE = ProbabilityHelper.logPr(PROBABILITY_EDGES_SEPARATED_BY_MORE_THAN_ONE_EDGE);
	private static final double LOG_PR_OFF_ROAD_EDGE = ProbabilityHelper.logPr(PROBABILITY_OFF_ROAD_EDGE);
	private static final double LOG_PR_UNFEASIBLE = ProbabilityHelper.logPr(0.);


	@Override
	public double factor(final Edge fromEdge, final Edge toEdge, final Point previousObservation, final Point currentObservation,
			final Polyline path){
		if(path.isEmpty())
			return LOG_PR_UNFEASIBLE;

		//same edge
		if(fromEdge.equals(toEdge))
			return LOG_PR_SAME_EDGE;

		//edges are (directly) connected
		if(fromEdge.getOutEdges().contains(toEdge))
			return LOG_PR_DIRECTLY_CONNECTED_EDGES;

		//TODO edges are connected through one edge
//		if(fromEdge.getOutEdges().contains(toEdge))
//			return LOG_PR_EDGES_SEPARATED_BY_ONE_EDGE_ONLY;

		//manage off-road edges
		if(fromEdge.isOffRoad() || toEdge.isOffRoad())
			return LOG_PR_OFF_ROAD_EDGE;

		//edges are connected by more than one edge
		return LOG_PR_EDGES_SEPARATED_BY_MORE_THAN_ONE_EDGE;
	}

}
