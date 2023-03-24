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
package io.github.mtrevisan.mapmatcher.mapmatching;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.helpers.PathHelper;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission.EmissionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.TransitionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.pathfinding.AStarPathFinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.EdgeWeightCalculator;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * @see <a href="https://en.wikipedia.org/wiki/Viterbi_algorithm">Viterbi algorithm</a>
 * @see <a href="https://www.hindawi.com/journals/jat/2021/9993860/">An online map matching algorithm based on second-order Hidden Markov Model</a>
 * @see <a href="https://journals.sagepub.com/doi/pdf/10.1177/1550147718772541">Log-Viterbi algorithm applied on second-order hidden Markov model for human activity recognition</a>
 * @see <a href="https://aclanthology.org/P99-1023.pdf">A second–order Hidden Markov Model for part–of–speech tagging</a>
 *
 * http://www.mit.edu/~jaillet/general/map_matching_itsc2012-final.pdf
 * https://www.researchgate.net/publication/320721676_Enhanced_Map-Matching_Algorithm_with_a_Hidden_Markov_Model_for_Mobile_Phone_Positioning
 */
public class ViterbiMapMatching implements MapMatchingStrategy{

	private final InitialProbabilityCalculator initialProbabilityCalculator;
	private final TransitionProbabilityCalculator transitionProbabilityCalculator;
	private final EmissionProbabilityCalculator emissionProbabilityCalculator;

	private final PathFindingStrategy pathFinder;


	public ViterbiMapMatching(final InitialProbabilityCalculator initialProbabilityCalculator,
			final TransitionProbabilityCalculator transitionProbabilityCalculator,
			final EmissionProbabilityCalculator emissionProbabilityCalculator,
			final EdgeWeightCalculator edgeWeightCalculator){
		this.initialProbabilityCalculator = initialProbabilityCalculator;
		this.transitionProbabilityCalculator = transitionProbabilityCalculator;
		this.emissionProbabilityCalculator = emissionProbabilityCalculator;

		pathFinder = new AStarPathFinder(edgeWeightCalculator);
	}

	/*static class Label{
		int timeStep;

		Point observation;
		Point snap;
		Node closestNode;
		Edge edge;

		double probability;

		Label back;
	}*/

	//https://github.com/graphhopper/map-matching/blob/master/hmm-lib/src/main/java/com/bmw/hmm/ViterbiAlgorithm.java
	/*@Override
	public Edge[] findPath(final Graph graph, final Point[] observations, final double edgesNearObservationThreshold){
		if(graph.isEmpty())
			//no graph: cannot calculate path
			return null;

		int currentObservationIndex = extractNextObservation(observations, 0);
		if(currentObservationIndex < 0)
			//no observations: cannot calculate path
			return null;


		final PriorityQueue<Label> queue = new PriorityQueue<>(Comparator.comparing(node -> node.probability));
		Point currentObservation = observations[currentObservationIndex];
		final Collection<Edge> graphEdges = graph.edges();
		initialProbabilityCalculator.calculateInitialProbability(currentObservation, graphEdges);
		emissionProbabilityCalculator.updateEmissionProbability(currentObservation, graphEdges);
		for(final Edge edge : graphEdges){
			final Polyline edgePolyline = edge.getPolyline();
			final Point snap = edgePolyline.onTrackClosestPoint(currentObservation);
			final double probability = initialProbabilityCalculator.initialProbability(edge)
				+ emissionProbabilityCalculator.emissionProbability(currentObservation, edge, null);
			final Label label = new Label();
			label.observation = currentObservation;
			label.snap = snap;
			label.closestNode = (edgePolyline.alongTrackDistance(snap) < edgePolyline.alongTrackDistance(edgePolyline.getEndPoint())
				? edge.getFrom()
				: edge.getTo());
			label.edge = edge;
			label.probability = probability;
			queue.add(label);
		}


		double minProbability;
		Label node = null;
		int previousObservationIndex = currentObservationIndex;
		while(!queue.isEmpty()){
			node = queue.poll();

			final Point previousObservation = observations[previousObservationIndex];
			currentObservationIndex = extractNextObservation(observations, previousObservationIndex + 1);
			if(currentObservationIndex < 0)
				break;

			currentObservation = observations[currentObservationIndex];

			emissionProbabilityCalculator.updateEmissionProbability(currentObservation, graphEdges);

			final Label best = new Label();
			final Edge fromEdge = node.edge;
			for(final Edge toEdge : graphEdges){
				minProbability = Double.POSITIVE_INFINITY;

				final List<Node> pathFromTo = pathFinder.findPath(node.closestNode, toEdge.getTo(), graph)
					.simplePath();
				if(!pathFromTo.isEmpty()){
					final double probability = node.probability
						+ transitionProbabilityCalculator.transitionProbability(fromEdge, toEdge, graph, previousObservation, currentObservation,
						pathFromTo);
					if(probability < minProbability){
						best.timeStep = node.timeStep + 1;
						best.observation = currentObservation;
						best.snap = node.snap;
						best.edge = toEdge;
						best.probability = probability
							+ emissionProbabilityCalculator.emissionProbability(currentObservation, toEdge, previousObservation);
						best.back = node;
					}
				}
			}
			if(best.timeStep > 0)
				queue.add(best);

			previousObservationIndex = currentObservationIndex;
		}


		final List<Edge> result = new ArrayList<>(observations.length);
		while(node != null){
			result.add(node.edge);

			node = node.back;
		}
		Collections.reverse(result);
		return (result.isEmpty()? null: result.toArray(Edge[]::new));
	}*/

	@Override
	public Edge[] findPath(final Graph graph, final Point[] observations, final double edgesNearObservationThreshold){
		if(graph.isEmpty())
			//no graph: cannot calculate path
			return null;

		int currentObservationIndex = extractNextObservation(observations, 0);
		if(currentObservationIndex < 0)
			//no observations: cannot calculate path
			return null;

		final Collection<Edge> graphEdges = graph.edges();
		final Map<Edge, double[]> score = new HashMap<>();
		final Map<Edge, Edge[]> path = new HashMap<>();

		//calculate the initial probability:
		Point currentObservation = observations[currentObservationIndex];
		initialProbabilityCalculator.calculateInitialProbability(currentObservation, graphEdges);
		emissionProbabilityCalculator.updateEmissionProbability(currentObservation, graphEdges);
		Collection<Edge> graphEdgesNearCurrentObservation = (graph.canHaveEdgesNear() && edgesNearObservationThreshold > 0.
			? graph.getEdgesNear(currentObservation, edgesNearObservationThreshold)
			: graphEdges);
		final int n = graphEdges.size();
		final int m = observations.length;
		for(final Edge edge : graphEdgesNearCurrentObservation){
			score.computeIfAbsent(edge, k -> new double[m])[currentObservationIndex] = initialProbabilityCalculator.initialProbability(edge)
				+ emissionProbabilityCalculator.emissionProbability(currentObservation, edge, null);
			path.computeIfAbsent(edge, k -> new Edge[m])[currentObservationIndex] = edge;
		}

		double minProbability;
		int previousObservationIndex = currentObservationIndex;
		while(true){
			final Point previousObservation = observations[previousObservationIndex];
			currentObservationIndex = extractNextObservation(observations, previousObservationIndex + 1);
			if(currentObservationIndex < 0)
				break;

			currentObservation = observations[currentObservationIndex];
			//select the road links near the GPS points withing a certain distance
			Collection<Edge> graphEdgesNearPreviousObservation = graphEdgesNearCurrentObservation;
			graphEdgesNearCurrentObservation = (graph.canHaveEdgesNear() && edgesNearObservationThreshold > 0.
				? graph.getEdgesNear(currentObservation, edgesNearObservationThreshold)
				: graphEdges);

			//calculate the emission probability matrix
			emissionProbabilityCalculator.updateEmissionProbability(currentObservation, graphEdges);

			final Map<Edge, Edge[]> newPath = new HashMap<>(n);
			for(final Edge toEdge : graphEdgesNearCurrentObservation){
				minProbability = Double.POSITIVE_INFINITY;

				for(final Edge fromEdge : graphEdgesNearPreviousObservation){
					final Polyline pathAsPolyline = PathHelper.calculatePathAsPolyline(fromEdge, toEdge, graph,
						previousObservation, currentObservation, pathFinder);

					final double probability = (score.containsKey(fromEdge)? score.get(fromEdge)[previousObservationIndex]: 0.)
						//calculate the state transition probability matrix
						+ transitionProbabilityCalculator.transitionProbability(fromEdge, toEdge, previousObservation, currentObservation,
						pathAsPolyline);
					if(probability <= minProbability){
						//record minimum probability
						minProbability = probability;
						score.computeIfAbsent(toEdge, k -> new double[m])[currentObservationIndex] = probability
							+ emissionProbabilityCalculator.emissionProbability(currentObservation, toEdge, previousObservation);

						//record path
						System.arraycopy(path.computeIfAbsent(fromEdge, k -> new Edge[m]), 0,
							newPath.computeIfAbsent(toEdge, k -> new Edge[m]), 0, currentObservationIndex);
						newPath.get(toEdge)[currentObservationIndex] = toEdge;
					}
				}
			}

			path.clear();
			path.putAll(newPath);
			newPath.clear();

			previousObservationIndex = currentObservationIndex;
		}

		minProbability = Double.POSITIVE_INFINITY;
		Edge minProbabilityEdge = null;
		for(final Edge edge : graphEdges){
			final double[] fScore = score.get(edge);
			final double edgeScore = (fScore != null? fScore[previousObservationIndex]: 0.);
			if(edgeScore > 0. && edgeScore < minProbability){
				minProbability = edgeScore;
				minProbabilityEdge = edge;
			}
		}
		return (minProbabilityEdge != null? path.get(minProbabilityEdge): null);
	}

	private static int extractNextObservation(final Point[] observations, int index){
		final int m = observations.length;
		while(index < m && observations[index] == null)
			index ++;
		return (index < m? index: -1);
	}

}
