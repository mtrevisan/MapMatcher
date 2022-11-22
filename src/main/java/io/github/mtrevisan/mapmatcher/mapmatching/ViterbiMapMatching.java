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

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.path.PathSummaryCreator;
import io.github.mtrevisan.mapmatcher.pathfinding.PathSummary;
import io.github.mtrevisan.mapmatcher.weight.LogMapEdgeWeightCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * @see <a href="https://en.wikipedia.org/wiki/Viterbi_algorithm">Viterbi algorithm</a>
 */
public class ViterbiMapMatching implements MapMatchingStrategy{

	private static final PrecisionModel PRECISION_MODEL = new PrecisionModel(PrecisionModel.FLOATING);
	private static final int SRID_WGS84 = 4326;
	private static final GeometryFactory FACTORY = new GeometryFactory(PRECISION_MODEL, SRID_WGS84);


	private static final PathSummaryCreator PATH_SUMMARY_CREATOR = new PathSummaryCreator();

	private final LogMapEdgeWeightCalculator calculator;


	public ViterbiMapMatching(final LogMapEdgeWeightCalculator calculator){
		this.calculator = calculator;
	}

	@Override
	public PathSummary findPath(final Vertex start, final Vertex end, final Graph graph, final Coordinates[] observations){
		final int n = graph.vertices().size();
		final int m = observations.length;
		final double[][] fScores = new double[m][n];
		int[][] path = new int[n][m];
//		final var predecessorTree = new HashMap<Vertex, Edge>(n);
//		predecessorTree.put(start, null);

		//calculate emission probability matrix
		final double[][] emissionProbability = new double[m][n];
		createEmissionProbability(observations, emissionProbability, graph);

		//NOTE: the initial probability is a uniform distribution reflecting the fact that there is no known bias about which is the
		// correct segment
//		final double initialProbability = logPr(1. / graph.edges().size());
//		for(final Edge startingNode : startingNodes)
//			startingNode.setWeight(initialProbability + calculator.calculateWeight(startingNode.getTo(), null));
		for(int state = 0; state < n; state ++){
			fScores[0][state] = /*initialProbability[state] +*/ emissionProbability[0][state];
			path[state][0] = state;
		}

		//construction of Viterbi matrix
		double minProbability;
		int maxProbabilityState;
		for(int i = 1; i < m; i ++){
			final int[][] newPath = new int[n][m];
			for(int currentState = 0; currentState < n; currentState ++){
				final var currentStateID = "E" + currentState;

				minProbability = Double.POSITIVE_INFINITY;
				for(int fromState = 0; fromState < n; fromState ++){
					final var fromStateID = "E" + fromState;
					final var fromStateVertex = graph.vertices().stream()
						.filter(v -> v.getId().equals(fromStateID))
						.findFirst()
						.get();

					final Edge edge = graph.getVertexEdges(fromStateVertex).stream()
						.filter(e -> e.getFrom().getId().equals(fromStateID) && e.getTo().getId().equals(currentStateID))
						.findFirst()
						.orElse(null);
					final var tmp = (edge == null? Double.POSITIVE_INFINITY: calculator.calculateWeight(edge));
					final double probability = fScores[i - 1][fromState] + tmp;
					if(probability < minProbability){
						//record minimum probability
						minProbability = probability;
						maxProbabilityState = fromState;
						fScores[i][currentState] = probability + emissionProbability[i][currentState];
//						predecessorTree.put(edge.getTo(), edge);

						//record path
						System.arraycopy(path[maxProbabilityState], 0, newPath[currentState], 0, i);
						newPath[currentState][i] = currentState;
					}
				}
			}
			path = newPath;
		}

		//compute the Viterbi path
		minProbability = Double.POSITIVE_INFINITY;
		maxProbabilityState = -1;
		for(int state = 0; state < n; state ++){
			if(fScores[m - 1][state] < minProbability){
				minProbability = fScores[m - 1][state];
				maxProbabilityState = state;
			}
		}

		final Set<Vertex> vv = new LinkedHashSet<>();
		if(maxProbabilityState >= 0){
System.out.println(Arrays.toString(path[maxProbabilityState]));
			for(final int id : path[maxProbabilityState]){
				final var vID = "E" + id;
				final var vx = graph.vertices().stream()
					.filter(v -> v.getId().equals(vID))
					.findFirst()
					.get();
				vv.add(vx);
			}
			final List<Vertex> vvv = new ArrayList<>(vv);

			final var predecessorTree = new HashMap<Vertex, Edge>(n);
			predecessorTree.put(vvv.get(0), null);
			for(int i = 1; i < vvv.size(); i ++){
				final Edge edge = new Edge(vvv.get(i - 1), vvv.get(i), 0.);
				predecessorTree.put(edge.getTo(), edge);
			}
			return PATH_SUMMARY_CREATOR.createUnidirectionalPath(vvv.get(0), vvv.get(vvv.size() - 1), predecessorTree);
		}
		return PATH_SUMMARY_CREATOR.createUnidirectionalPath(start, end, Collections.emptyMap());
	}

	protected void createEmissionProbability(final Coordinates[] observations, final double[][] emissionProbability, final Graph graph){
		for(int observationIndex = 0; observationIndex < observations.length; observationIndex ++){
			final Geometry point = FACTORY.createPoint(
				new Coordinate(observations[observationIndex].getLongitude(), observations[observationIndex].getLatitude())
			);
			//step 1. Calculate dist(p_i, r_j)
			final int n = emissionProbability[0].length;
			for(int k = 0; k < n; k ++){
				final var stateID = "E" + k;
				final var state = graph.vertices().stream()
					.filter(v -> v.getId().equals(stateID))
					.findFirst()
					.get()
					.getGeometry();
				//calculate distance from current position to segment
				emissionProbability[observationIndex][k] = point.distance(state);
			}

			//step 2. Calculate sum(k=1..n of dist(p_i, r_k))
			double cumulativeDistance = 0.;
			for(int k = 0; k < n; k ++){
				cumulativeDistance += emissionProbability[observationIndex][k];
			}

			//step 3. Calculate Pr(r_j | p_i)
			for(int k = 0; k < n; k ++){
				emissionProbability[observationIndex][k] = cumulativeDistance / emissionProbability[observationIndex][k];
			}

			//step 4. Calculate Pr(p_i | r_j)
			double cumulativeProbability = 0.;
			for(int k = 0; k < n; k ++){
				cumulativeProbability += emissionProbability[observationIndex][k];
			}
			for(int k = 0; k < n; k ++){
				emissionProbability[observationIndex][k] = logPr(emissionProbability[observationIndex][k] / cumulativeProbability);
			}
		}
	}

	private static double logPr(final double probability){
		return -StrictMath.log(probability);
	}

//	@Override
//	public PathSummary findPath2(final Vertex start, final Vertex end, final Graph graph, final Coordinates[] observations){
//		//for a node, this is the node immediately preceding it on the cheapest path from start to the given node currently known
//		final int numberOfVertices = graph.vertices().size();
//		final var predecessorTree = new HashMap<Vertex, Edge>(numberOfVertices);
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
//		final var queue = new LinkedList<Vertex>();
//		for(final var observation : observations){
//			final var startingNodes = graph.getVertexEdges(start);
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
//				final var edges = graph.getVertexEdges(current);
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
