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
package io.github.mtrevisan.mapmatcher.mapmatching.calculators;

import io.github.mtrevisan.mapmatcher.distances.DistanceCalculator;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.helpers.Coordinate;
import io.github.mtrevisan.mapmatcher.helpers.Polyline;
import io.github.mtrevisan.mapmatcher.pathfinding.AStarPathFinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.NodeCountCalculator;

import java.util.ArrayList;
import java.util.List;


public class TopologicalTransitionCalculator implements TransitionProbabilityCalculator{

	private static final PathFindingStrategy PATH_FINDER = new AStarPathFinder(new NodeCountCalculator());

	private static final double TRANSITION_PROBABILITY_CONNECTED_EDGES = Math.exp(-1.);

	private final DistanceCalculator distanceCalculator;


	public TopologicalTransitionCalculator(final DistanceCalculator distanceCalculator){
		this.distanceCalculator = distanceCalculator;
	}

	/**
	 * Calculate transition probability
	 * <p>
	 * If two segments are (r_ij is the so-called topological relationship, and a_ij = e^-r_ij):
	 * <dl>
	 *    <dt>unconnected</dt>
	 *    	<dd><code>r_ij = ∞</code>, thus <code>a_ij = 0</code>, and <code>-ln(a_ij) = ∞</code></dd>
	 *    <dt>connected</dt>
	 *    	<dd><code>r_ij = 1</code>, thus <code>a_ij = 0.36787944117</code>, and <code>-ln(a_ij) = 1</code></dd>
	 *    <dt>the same (i = j)</dt>
	 *    	<dd><code>r_ij = 0</code>, thus <code>a_ij = 1</code>, and <code>-ln(a_ij) = 0</code></dd>
	 * </dl>
	 * </p>
	 *
	 * Otherwise
	 *
	 * <p>
	 * Pr(r_i-1 -> r_i) = dist(o_i-1, o_i) / pathDistance(x_i-1, x_i), where x_K is the projection of the observation o_k onto the segment r
	 * </p>
	 *
	 * Otherwise
	 *
	 * <p>
	 * Exponential function of the difference between the route length and the great circle distance between o_t and o_t+1
	 * Pr(r_i | r_i-1) = β ⋅ exp(-β ⋅ |dist(o_i-1, o_i) - pathDistance(x_i-1, x_i)|) (β "can be" 3), where x_k is the projection of the
	 * observation o_k onto the segment r
	 * </p>
	 */
	@Override
	public double transitionProbability(final Edge fromSegment, final Edge toSegment, final Graph graph,
			final Coordinate previousObservation, final Coordinate currentObservation){
		double a = 0.;
		//if the node is the same
		if(fromSegment.equals(toSegment))
			a = 1. / (1. + TRANSITION_PROBABILITY_CONNECTED_EDGES);
		else{
			final List<Node> path = PATH_FINDER.findPath(fromSegment.getTo(), toSegment.getFrom(), graph)
				.simplePath();
			if(!path.isEmpty())
				a = TRANSITION_PROBABILITY_CONNECTED_EDGES / (1. + TRANSITION_PROBABILITY_CONNECTED_EDGES);
		}
		return InitialProbabilityCalculator.logPr(a);
	}

	private static Polyline extractPathAsPolyline(final Node from, final Node to, final Graph graph){
		Polyline polyline = null;

		//search for a feasible path between the projection onto fromSegment and the one onto toSegment
		final List<Node> path = PATH_FINDER.findPath(from, to, graph)
			.simplePath();
		if(!path.isEmpty()){
			final List<Polyline> edges = new ArrayList<>(path.size() - 1);
			int size = 0;
			for(int i = 1; i < path.size(); i ++){
				final Node currentNode = path.get(i - 1);
				final Node nextNode = path.get(i);
				final Edge currentNextEdge = currentNode.findOutEdges(nextNode);
				assert currentNextEdge != null;
				final Polyline currentNextPolyline = currentNextEdge.getPolyline();
				size += currentNextPolyline.size() - (size > 0? 1: 0);
				edges.add(currentNextPolyline);
			}

			//merge segments
			if(size > 0){
				final Coordinate[] mergedCoordinates = new Coordinate[size];
				size = 0;
				for(final Polyline edgePolyline : edges){
					final Coordinate[] coordinates = edgePolyline.getCoordinates();
					final int count = coordinates.length - (size > 0? 1: 0);
					assert size == 0 || mergedCoordinates[size - 1].equals(coordinates[0]);
					System.arraycopy(coordinates, (size > 0? 1: 0), mergedCoordinates, size, count);
					size += count;
				}
				polyline = Polyline.of(mergedCoordinates);
			}
		}
		return polyline;
	}

	private static boolean isSegmentsReversed(final Edge fromSegment, final Edge toSegment){
		return (fromSegment.getFrom().getCoordinate().equals(toSegment.getTo().getCoordinate())
			&& fromSegment.getTo().getCoordinate().equals(toSegment.getFrom().getCoordinate()));
	}

	private boolean isGoingForward(final Coordinate previousObservation, final Coordinate currentObservation, final Polyline polyline){
		//calculate Along-Track Distance
		final double previousATD = distanceCalculator.alongTrackDistance(previousObservation, polyline);
		final double currentATD = distanceCalculator.alongTrackDistance(currentObservation, polyline);
		return (previousATD <= currentATD);
	}

}
