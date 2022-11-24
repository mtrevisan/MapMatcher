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

import io.github.mtrevisan.mapmatcher.distances.DistanceCalculator;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.path.PathSummaryCreator;
import io.github.mtrevisan.mapmatcher.pathfinding.PathSummary;
import io.github.mtrevisan.mapmatcher.weight.LogMapMatchingProbabilityCalculator;
import org.locationtech.jts.geom.Coordinate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * @see <a href="https://en.wikipedia.org/wiki/Viterbi_algorithm">Viterbi algorithm</a>
 */
public class ViterbiMapMatching implements MapMatchingStrategy{

	private static final PathSummaryCreator PATH_SUMMARY_CREATOR = new PathSummaryCreator();

	private final LogMapMatchingProbabilityCalculator probabilityCalculator;
	private final DistanceCalculator distanceCalculator;


	public ViterbiMapMatching(final LogMapMatchingProbabilityCalculator mapMatchingProbabilityCalculator, final DistanceCalculator distanceCalculator){
		this.probabilityCalculator = mapMatchingProbabilityCalculator;
		this.distanceCalculator = distanceCalculator;
	}

	@Override
	public PathSummary findPath(final Graph graph, final Coordinate[] observations){
		final int n = graph.edges().size();
		final int m = observations.length;
		final Map<Edge, double[]> fScores = new HashMap<>();
		final Map<Edge, Edge[]> path = new HashMap<>();
//		final var predecessorTree = new HashMap<Node, Edge>(n);
//		predecessorTree.put(start, null);

		//calculate emission probability matrix
		final Map<Edge, double[]> emissionProbability = new HashMap<>();
		createEmissionProbability(observations, emissionProbability, graph);

		//NOTE: the initial probability is a uniform distribution reflecting the fact that there is no known bias about which is the
		// correct segment
//		final double initialProbability = logPr(1. / graph.edges().size());
		for(final Edge edge : graph.edges()){
			fScores.computeIfAbsent(edge, k -> new double[m])[0] = /*initialProbability.get(state) +*/ emissionProbability.get(edge)[0];
			path.computeIfAbsent(edge, k -> new Edge[n])[0] = edge;
		}

		//construction of Viterbi matrix
		double minProbability;
		Edge maxProbabilityEdge;
		for(int i = 1; i < m; i ++){
			probabilityCalculator.updateEmissionProbability(observations[i], graph.edges());

			final Map<Edge, Edge[]> newPath = new HashMap<>(n);
			for(final Edge currentEdge : graph.edges()){
				minProbability = Double.POSITIVE_INFINITY;
				for(final Edge fromEdge : graph.edges()){
					final double probability = fScores.get(fromEdge)[i - 1] + probabilityCalculator.transitionProbability(fromEdge, currentEdge);
					if(probability < minProbability){
						//record minimum probability
						minProbability = probability;
						maxProbabilityEdge = fromEdge;
						fScores.get(currentEdge)[i] = probability + emissionProbability.get(fromEdge)[i];
//						predecessorTree.put(edge.getTo(), edge);

						//record path
						System.arraycopy(path.computeIfAbsent(maxProbabilityEdge, k -> new Edge[m]), 0,
							newPath.computeIfAbsent(currentEdge, k -> new Edge[m]), 0, i);
						newPath.get(currentEdge)[i] = currentEdge;
					}
				}
			}

			path.clear();
			path.putAll(newPath);
			newPath.clear();
		}

		//compute the Viterbi path
		minProbability = Double.POSITIVE_INFINITY;
		maxProbabilityEdge = null;
		for(final Edge edge : graph.edges())
			if(fScores.get(edge)[m - 1] < minProbability){
				minProbability = fScores.get(edge)[m - 1];
				maxProbabilityEdge = edge;
			}

		if(maxProbabilityEdge != null){
System.out.println(Arrays.toString(Arrays.stream(path.get(maxProbabilityEdge)).map(Edge::getID).toArray()));
			final Set<Edge> vv = new HashSet<>(Arrays.asList(path.get(maxProbabilityEdge)));

//			final List<Edge> vvv = new ArrayList<>(vv);
//			final var predecessorTree = new HashMap<Node, Edge>(n);
//			predecessorTree.put(vvv.get(0), null);
//			for(int i = 1; i < vvv.size(); i ++){
//				final Edge edge = new Edge(vvv.get(i - 1), vvv.get(i), 0.);
//				predecessorTree.put(edge.getTo(), edge);
//			}
//			return PATH_SUMMARY_CREATOR.createUnidirectionalPath(vvv.get(0), vvv.get(vvv.size() - 1), predecessorTree);
		}
		return PATH_SUMMARY_CREATOR.createUnidirectionalPath(null, null, Collections.emptyMap());
	}

	protected void createEmissionProbability(final Coordinate[] observations, final Map<Edge, double[]> emissionProbability,
			final Graph graph){
		for(int observationIndex = 0; observationIndex < observations.length; observationIndex ++){
			final Coordinate point = observations[observationIndex];
			//step 1. Calculate dist(p_i, r_j)
			final int m = observations.length;
			for(final Edge edge : graph.edges())
				emissionProbability.computeIfAbsent(edge, k -> new double[m])[observationIndex]
					= distanceCalculator.distance(point, edge.getLineString());

			//step 2. Calculate sum(k=1..n of dist(p_i, r_k))
			double cumulativeDistance = 0.;
			for(final Edge edge : graph.edges())
				cumulativeDistance += emissionProbability.get(edge)[observationIndex];

			//step 3. Calculate Pr(r_j | p_i)
			for(final Edge edge : graph.edges())
				emissionProbability.get(edge)[observationIndex] = cumulativeDistance / emissionProbability.get(edge)[observationIndex];

			//step 4. Calculate Pr(p_i | r_j)
			double cumulativeProbability = 0.;
			for(final Edge edge : graph.edges())
				cumulativeProbability += emissionProbability.get(edge)[observationIndex];
			for(final Edge edge : graph.edges())
				emissionProbability.get(edge)[observationIndex] = logPr(emissionProbability.get(edge)[observationIndex] / cumulativeProbability);
		}
	}

	private static double logPr(final double probability){
		return -StrictMath.log(probability);
	}

//	@Override
//	public PathSummary findPath2(final Node start, final Node end, final Graph graph, final Coordinates[] observations){
//		//for a node, this is the node immediately preceding it on the cheapest path from start to the given node currently known
//		final int numberOfVertices = graph.vertices().size();
//		final var predecessorTree = new HashMap<Node, Edge>(numberOfVertices);
//		predecessorTree.put(start, null);
//
//		//the current best guess as to how cheap a path could be from start to finish if it goes through the given node
//		//NOTE: the initial probability is a uniform distribution reflecting the fact that there is no known bias about which is the
//		// correct segment
////		final double initialProbability = logPr(1. / graph.edges().size());
////		for(final Edge startingNode : startingNodes)
////			startingNode.setWeight(initialProbability + calculator.calculateWeight(startingNode.getTo(), null));
//		final var fScoresPrevious = new HashMap<String, Double>(numberOfVertices);
//		fScoresPrevious.put(start.getId(), 0.);
//		final var fScoresNext = new HashMap<String, Double>(numberOfVertices);
//
//		//set of discovered nodes that may need to be (re-)expanded
//		final var queue = new LinkedList<Node>();
//		for(final var observation : observations){
//			final var startingNodes = start.getOutEdges();
//			calculator.updateEmissionProbability(observation, startingNodes);
//
//			queue.clear();
//			queue.add(start);
//			final var seenVertices = new HashSet<String>(numberOfVertices);
//			while(!queue.isEmpty()){
//				final var current = queue.pop();
//				if(current.equals(end))
//					break;
//
//				seenVertices.add(current.getId());
//				final var edges = current.getOutEdges();
//				var minProbability = Double.POSITIVE_INFINITY;
//				final var minProbabilityEdges = new HashMap<Double, Set<Edge>>(edges.size());
//				for(final var edge : edges){
//					final var probability = fScoresPrevious.getOrDefault(edge.getTo().getId(), calculator.calculateWeight(edge.getFrom(), edge.getTo()))
//						+ calculator.calculateWeight(edge);
//					if(probability <= minProbability){
//						minProbability = probability;
//						minProbabilityEdges.computeIfAbsent(probability, k -> new HashSet<>(1))
//							.add(edge);
//					}
//				}
//				final var minEdges = minProbabilityEdges.getOrDefault(minProbability, Collections.emptySet());
//				for(final var minEdge : minEdges){
//					final var neighbor = minEdge.getTo();
//					final var neighborID = neighbor.getId();
//					if(!seenVertices.contains(neighborID)){
//						predecessorTree.put(neighbor, minEdge);
//						//store the cost of the cheapest path from start to this node
//						final var newScore = minProbability + calculator.calculateWeight(neighbor, end);
//						fScoresNext.put(neighborID, newScore + calculator.calculateWeight(minEdge));
//
//						if(!queue.contains(neighbor))
//							//further explore path
//							queue.add(neighbor);
//					}
//				}
//			}
//
//			fScoresPrevious.clear();
//			fScoresPrevious.putAll(fScoresNext);
//			fScoresNext.clear();
//		}
//
//		return PATH_SUMMARY_CREATOR.createUnidirectionalPath(start, end, predecessorTree);
//	}

}
