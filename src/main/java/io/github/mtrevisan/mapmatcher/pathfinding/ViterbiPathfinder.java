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
package io.github.mtrevisan.mapmatcher.pathfinding;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.path.PathSummaryCreator;
import io.github.mtrevisan.mapmatcher.weight.EdgeWeightCalculator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


/**
 * @see <a href="https://en.wikipedia.org/wiki/Viterbi_algorithm">Viterbi algorithm</a>
 */
public class ViterbiPathfinder implements PathfindingStrategy{

	private static final PathSummaryCreator PATH_SUMMARY_CREATOR = new PathSummaryCreator();

	private final EdgeWeightCalculator calculator;


	public ViterbiPathfinder(final EdgeWeightCalculator calculator){
		this.calculator = calculator;
	}

	@Override
	public PathSummary findPath(final Vertex start, final Vertex end, final Graph graph){
		//for a node, this is the node immediately preceding it on the cheapest path from start to the given node currently known
		final int numberOfVertices = graph.vertices().size();
		final var predecessorTree = new HashMap<Vertex, Edge>(numberOfVertices);
		predecessorTree.put(start, null);

		//the current best guess as to how cheap a path could be from start to finish if it goes through the given node
		final var fScores = new HashMap<String, Double>(numberOfVertices);
		fScores.put(start.getId(), 0.);

		//set of discovered nodes that may need to be (re-)expanded
		final var queue = new LinkedList<Vertex>();
		queue.add(start);
		final var seenVertices = new HashSet<String>(numberOfVertices);
		while(!queue.isEmpty()){
			final var current = queue.pop();
			if(current.equals(end))
				break;

			seenVertices.add(current.getId());
			final var edges = graph.getVertexEdges(current);
			var minProbability = Double.POSITIVE_INFINITY;
			final var minProbabilityEdges = new HashMap<Double, Set<Edge>>(edges.size());
			for(final var edge : edges){
				final var probability = fScores.getOrDefault(edge.getTo().getId(), calculator.calculateWeight(edge.getFrom(), edge.getTo()))
					+ calculator.calculateWeight(edge);
				if(probability <= minProbability){
					minProbability = probability;
					minProbabilityEdges.computeIfAbsent(probability, k -> new HashSet<>(1))
						.add(edge);
				}
			}
			final var minEdges = minProbabilityEdges.getOrDefault(minProbability, Collections.emptySet());
			for(final var minEdge : minEdges){
				final var neighbor = minEdge.getTo();
				final var neighborID = neighbor.getId();
				final var newScore = minProbability + calculator.calculateWeight(neighbor, end);
				final var alreadySeen = seenVertices.contains(neighborID);
				if(!alreadySeen || newScore < fScores.get(neighborID)){
					predecessorTree.put(neighbor, minEdge);
					//store the cost of the cheapest path from start to this node
					fScores.put(neighborID, newScore);
				}

				if(!alreadySeen && !queue.contains(neighbor))
					//further explore path
					queue.add(neighbor);
			}
		}

		return PATH_SUMMARY_CREATOR.createUnidirectionalPath(start, end, predecessorTree);
	}

}
