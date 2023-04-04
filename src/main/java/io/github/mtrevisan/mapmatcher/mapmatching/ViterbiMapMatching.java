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
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.helpers.PathHelper;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission.EmissionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.TransitionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.pathfinding.AStarPathFinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.EdgeWeightCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * @see <a href="https://en.wikipedia.org/wiki/Viterbi_algorithm">Viterbi algorithm</a>
 * @see <a href="https://www.hindawi.com/journals/jat/2021/9993860/">An online map matching algorithm based on second-order Hidden Markov Model</a>
 * @see <a href="https://journals.sagepub.com/doi/pdf/10.1177/1550147718772541">Log-Viterbi algorithm applied on second-order hidden Markov model for human activity recognition</a>
 * @see <a href="https://aclanthology.org/P99-1023.pdf">A second–order Hidden Markov Model for part–of–speech tagging</a>
 * @see <a href="http://www.mit.edu/~jaillet/general/map_matching_itsc2012-final.pdf">Online map-matching based on Hidden Markov model for real-time traffic sensing applications</a>
 * @see <a href="https://www.researchgate.net/publication/320721676_Enhanced_Map-Matching_Algorithm_with_a_Hidden_Markov_Model_for_Mobile_Phone_Positioning">Enhanced map-matching algorithm with a Hidden Markov Model for mobile phone positioning</a>
 */
public class ViterbiMapMatching implements MapMatchingStrategy{

	private static final String NODE_ID_OBSERVATION_PREFIX = "obs";
	private static final String NODE_ID_EDGE_INFIX_START = "[";
	private static final String NODE_ID_EDGE_INFIX_END = "]";


	private final InitialProbabilityCalculator initialProbabilityCalculator;
	private final TransitionProbabilityCalculator transitionProbabilityCalculator;
	private final EmissionProbabilityCalculator emissionProbabilityCalculator;

	private final PathFindingStrategy pathFinder;
	private boolean offRoad;


	public ViterbiMapMatching(final InitialProbabilityCalculator initialProbabilityCalculator,
			final TransitionProbabilityCalculator transitionProbabilityCalculator,
			final EmissionProbabilityCalculator emissionProbabilityCalculator,
			final EdgeWeightCalculator edgeWeightCalculator){
		this.initialProbabilityCalculator = initialProbabilityCalculator;
		this.transitionProbabilityCalculator = transitionProbabilityCalculator;
		this.emissionProbabilityCalculator = emissionProbabilityCalculator;

		pathFinder = new AStarPathFinder(edgeWeightCalculator);
	}

	public ViterbiMapMatching withOffRoad(){
		offRoad = true;

		return this;
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
	public Collection<Map.Entry<Double, Edge[]>> findPath(final Graph graph, final Point[] observations, final double edgesNearObservationThreshold){
		if(graph.isEmpty())
			//no graph: cannot calculate path
			return null;

		int currentObservationIndex = PathHelper.extractNextObservation(observations, 0);
		if(currentObservationIndex < 0)
			//no observations: cannot calculate path
			return null;

		final Collection<Edge> graphEdges = graph.edges();
		final Map<Edge, Map<Integer, Double>> score = new HashMap<>();
		final Map<Edge, Edge[]> path = new HashMap<>();

		Point currentObservation = observations[currentObservationIndex];
		Collection<Edge> graphEdgesNearCurrentObservation = (graph.canHaveEdgesNear() && edgesNearObservationThreshold > 0.
			? graph.getEdgesNear(currentObservation, edgesNearObservationThreshold)
			: graphEdges);
		if(offRoad)
			//augment graphEdgesNearCurrentObservation with an edge between a candidate from an edge and currentObservation
			graphEdgesNearCurrentObservation = calculateOffRoadEdges(graphEdgesNearCurrentObservation, observations,
				-1, currentObservationIndex);

		//calculate the initial probability:
		initialProbabilityCalculator.calculateInitialProbability(currentObservation, graphEdgesNearCurrentObservation);
		emissionProbabilityCalculator.updateEmissionProbability(currentObservation, graphEdgesNearCurrentObservation);

		final int n = graphEdges.size();
		final int m = observations.length;
		for(final Edge edge : graphEdgesNearCurrentObservation){
			final double probability = initialProbabilityCalculator.initialProbability(currentObservation, edge)
				+ emissionProbabilityCalculator.emissionProbability(currentObservation, edge, null);
			score.computeIfAbsent(edge, k -> new HashMap<>(m)).put(currentObservationIndex, probability);
			path.computeIfAbsent(edge, k -> new Edge[m])[currentObservationIndex] = edge;
		}

		double minProbability;
		int previousObservationIndex = currentObservationIndex;
		while(true){
			final Point previousObservation = observations[previousObservationIndex];
			currentObservationIndex = PathHelper.extractNextObservation(observations, previousObservationIndex + 1);
			if(currentObservationIndex < 0)
				break;

			currentObservation = observations[currentObservationIndex];
			//select the road links near the GPS points withing a certain distance
			final Collection<Edge> graphEdgesNearPreviousObservation = graphEdgesNearCurrentObservation;
			graphEdgesNearCurrentObservation = (graph.canHaveEdgesNear() && edgesNearObservationThreshold > 0.
				? graph.getEdgesNear(currentObservation, edgesNearObservationThreshold)
				: graphEdges);
			if(offRoad)
				//augment graphEdgesNearCurrentObservation with an edge between a candidate from an edge and currentObservation
				graphEdgesNearCurrentObservation = calculateOffRoadEdges(graphEdgesNearCurrentObservation, observations,
					previousObservationIndex, currentObservationIndex);

			//calculate the emission probability matrix
			emissionProbabilityCalculator.updateEmissionProbability(currentObservation, graphEdgesNearCurrentObservation);

			final Map<Edge, Edge[]> newPath = new HashMap<>(n);
			for(final Edge toEdge : graphEdgesNearCurrentObservation){
				minProbability = Double.POSITIVE_INFINITY;

				for(final Edge fromEdge : graphEdgesNearPreviousObservation){
if(fromEdge.getID().equals("13") && toEdge.getID().equals("4"))
	System.out.println();
					Polyline pathAsPolyline = PathHelper.calculatePathAsPolyline(fromEdge, toEdge, graph, pathFinder);
					if(offRoad && pathAsPolyline.isEmpty())
						pathAsPolyline = calculateOffRoadPath(fromEdge, toEdge, pathAsPolyline);

					double probability = score.getOrDefault(fromEdge, Collections.emptyMap())
						.getOrDefault(previousObservationIndex, Double.POSITIVE_INFINITY);
					if(Double.isFinite(probability))
						probability += transitionProbabilityCalculator.transitionProbability(fromEdge, toEdge,
							previousObservation, currentObservation, pathAsPolyline);

					if(Double.isFinite(probability) && probability <= minProbability){
						//record minimum probability
						minProbability = probability;
						probability += emissionProbabilityCalculator.emissionProbability(currentObservation, toEdge, previousObservation);

						score.computeIfAbsent(toEdge, k -> new HashMap<>(m)).put(currentObservationIndex, probability);

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

		final Collection<Map.Entry<Double, Edge[]>> minProbabilityPaths = new ArrayList<>(0);
		final int sortIndex = previousObservationIndex;
		final Function<Edge, Double> sortScore = edge -> score.getOrDefault(edge,
			Collections.emptyMap()).getOrDefault(sortIndex, Double.POSITIVE_INFINITY);
		path.entrySet().removeIf(edgeEntry -> Double.isInfinite(sortScore.apply(edgeEntry.getKey())));
		while(!path.isEmpty()){
			final Edge minimumEdge = Collections.min(path.entrySet(), Comparator.comparingDouble(entry -> sortScore.apply(entry.getKey())))
				.getKey();
			final double minimumProbability = sortScore.apply(minimumEdge);
			minProbabilityPaths.add(new AbstractMap.SimpleEntry<>(minimumProbability, path.remove(minimumEdge)));
		}
		return minProbabilityPaths;
	}

	private static List<Edge> calculateOffRoadEdges(final Collection<Edge> candidateEdges, final Point[] observations,
			final int previousObservationIndex, final int currentObservationIndex){
		final List<Edge> augmentedEdges = new ArrayList<>(candidateEdges.size() * 3);
		augmentedEdges.addAll(candidateEdges);

		final Point currentObservation = observations[currentObservationIndex];
		if(previousObservationIndex >= 0){
			//add edge between previous and current observation
			final Point previousObservation = observations[previousObservationIndex];
			final Node offRoadPreviousNode = Node.of(NODE_ID_OBSERVATION_PREFIX + previousObservationIndex, previousObservation);
			final Node offRoadCurrentNode = Node.of(NODE_ID_OBSERVATION_PREFIX + currentObservationIndex, currentObservation);
			augmentedEdges.add(Edge.createDirectOffRoadEdge(offRoadPreviousNode, offRoadCurrentNode));
		}

		final Node observationNode = Node.of(NODE_ID_OBSERVATION_PREFIX + currentObservationIndex, currentObservation);
		for(final Edge candidateEdge : candidateEdges){
			//add edge between current observation and projection on candidate edge
			final Point projectedPoint = candidateEdge.getPath().onTrackClosestPoint(currentObservation);
			final Node projectedNode = Node.of(NODE_ID_OBSERVATION_PREFIX + currentObservationIndex
				+ NODE_ID_EDGE_INFIX_START + candidateEdge.getID() + NODE_ID_EDGE_INFIX_END, projectedPoint);
			//NOTE: avoid connecting to an edge in the opposite direction
//			if(!candidateEdge.getTo().equals(projectedNode))
				augmentedEdges.add(Edge.createDirectOffRoadEdge(observationNode, projectedNode)
					.withToProjected(candidateEdge));
			if(previousObservationIndex >= 0 /*&& !candidateEdge.getFrom().equals(projectedNode)*/)
				augmentedEdges.add(Edge.createDirectOffRoadEdge(projectedNode, observationNode)
					.withFromProjected(candidateEdge));
		}
		return augmentedEdges;
	}

	private static Polyline calculateOffRoadPath(final Edge fromEdge, final Edge toEdge, Polyline path){
		if(fromEdge.isOffRoad() && !toEdge.isOffRoad() && toEdge.equals(fromEdge.getToProjected())){
			final Point[][] cut = toEdge.getPath().cutHard(fromEdge.getTo().getPoint());
			path = fromEdge.getPath()
				.append(cut[1]);
		}
		else if(!fromEdge.isOffRoad() && toEdge.isOffRoad() && fromEdge.equals(toEdge.getFromProjected())){
			final Point[][] cut = fromEdge.getPath().cutHard(toEdge.getTo().getPoint());
			path = toEdge.getPath()
				.prepend(cut[0]);
		}
		else if(fromEdge.isOffRoad() && toEdge.isOffRoad() && fromEdge.getTo().equals(toEdge.getFrom()))
			path = fromEdge.getPath()
				.append(toEdge.getPath());
		return path;
	}

}
