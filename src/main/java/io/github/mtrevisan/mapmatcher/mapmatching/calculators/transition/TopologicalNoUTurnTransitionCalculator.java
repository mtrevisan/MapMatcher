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
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.pathfinding.AStarPathFinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.NodeCountCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.List;


public class TopologicalNoUTurnTransitionCalculator implements TransitionProbabilityCalculator{

	private static final PathFindingStrategy PATH_FINDER = new AStarPathFinder(new NodeCountCalculator());

	private static final double TRANSITION_PROBABILITY_SAME_EDGE = 0.6;
	private static final double TRANSITION_PROBABILITY_DIRECTLY_CONNECTED_EDGES = 0.4;
	private static final double TRANSITION_PROBABILITY_ONE_LINK_SEPARATED_EDGES = 0.2;


	/**
	 * Calculate transition probability
	 * <p>
	 * If two segments are (r_ij is the so-called topological relationship, and a_ij = e^-r_ij):
	 * <dl>
	 *    <dt>unconnected or connected by more than 1 link</dt>
	 *    	<dd><code>r_ij = ∞</code>, thus <code>a_ij = 0</code></dd>
	 *    <dt>directly connected</dt>
	 *    	<dd><code>r_ij = 1</code>, thus <code>a_ij = 0.4</code></dd>
	 *    <dt>connected by at most 1 link</dt>
	 *    	<dd><code>r_ij = 1</code>, thus <code>a_ij = 0.4</code></dd>
	 *    <dt>the same (i = j)</dt>
	 *    	<dd><code>r_ij = 0</code>, thus <code>a_ij = 0.6</code></dd>
	 * </dl>
	 * </p>
	 */
	@Override
	public double transitionProbability(final Edge fromSegment, final Edge toSegment, final Graph graph,
			final Point previousObservation, final Point currentObservation){
		double a = 0.;
		//penalize u-turns: make then unreachable
		final boolean segmentsReversed = PathHelper.isSegmentsReversed(fromSegment, toSegment);
		if(!segmentsReversed){
			//if the node is the same
			if(fromSegment.equals(toSegment))
				a = TRANSITION_PROBABILITY_SAME_EDGE;
			else{
				final List<Node> path = PATH_FINDER.findPath(fromSegment.getTo(), toSegment.getFrom(), graph)
					.simplePath();
				final boolean mixedDirections = PathHelper.hasMixedDirections(path, fromSegment, toSegment);
				//disallow u-turn
				if(!mixedDirections && !path.isEmpty())
					a = switch(path.size()){
						case 1 -> TRANSITION_PROBABILITY_DIRECTLY_CONNECTED_EDGES;
						case 2 -> TRANSITION_PROBABILITY_ONE_LINK_SEPARATED_EDGES;
						default -> 0.;
					};
			}
		}
		return InitialProbabilityCalculator.logPr(a);
	}

}
