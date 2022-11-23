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
import io.github.mtrevisan.mapmatcher.graph.ScoredGraphVertex;
import io.github.mtrevisan.mapmatcher.path.PathSummaryCreator;
import io.github.mtrevisan.mapmatcher.pathfinding.PathSummary;
import io.github.mtrevisan.mapmatcher.weight.LogMapEdgeWeightCalculator;
import org.locationtech.jts.geom.Coordinate;

import java.util.Collection;
import java.util.HashMap;
import java.util.PriorityQueue;


/**
 * @see <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">A* search algorithm</a>
 */
public class AStarMapMatching implements MapMatchingStrategy{

	private static final PathSummaryCreator PATH_SUMMARY_CREATOR = new PathSummaryCreator();

	private final LogMapEdgeWeightCalculator calculator;


	public AStarMapMatching(final LogMapEdgeWeightCalculator calculator){
		this.calculator = calculator;
	}

	//TODO
	@Override
	public PathSummary findPath(final Graph graph, final Coordinate[] observations){
		//the node immediately preceding a given node on the cheapest path from start to the given node currently known
		final var predecessorTree = new HashMap<Node, Edge>();
		final Node start = new Node(null);
		final Node end = new Node(null);
		predecessorTree.put(start, null);

		//the cost of the cheapest path from start to given node currently known
		final var gScoresPrevious = new HashMap<Node, Double>();
		final var gScoresNext = new HashMap<Node, Double>();
		gScoresPrevious.put(start, 0.);

		//set of discovered nodes that may need to be (re-)expanded
		final var queue = new PriorityQueue<ScoredGraphVertex>();
		//NOTE: the score here is `gScore[n] + h(n)`; it represents the current best guess as to how cheap a path could be from start to
		// finish if it goes through the given node
		var fScore = heuristic(start, end);
		queue.add(new ScoredGraphVertex(start, fScore));

		for(int i = 0; i < observations.length; i ++){
			while(!queue.isEmpty()){
				final var current = queue.poll()
					.vertex();
				if(current.equals(end))
					break;

				final Collection<Edge> startingNodes = current.geOutEdges();
				calculator.updateEmissionProbability(observations[i], startingNodes);

				for(final var edge : startingNodes){
					final var neighbor = edge.getTo();
					final var newScore = gScoresPrevious.get(current) + calculator.calculateWeight(edge);

					if(newScore < gScoresPrevious.getOrDefault(neighbor, Double.MAX_VALUE)){
						gScoresPrevious.put(neighbor, newScore);
						predecessorTree.put(neighbor, edge);

						fScore = newScore + heuristic(neighbor, end);
						final ScoredGraphVertex sgv = new ScoredGraphVertex(neighbor, fScore);
						if(!queue.contains(sgv))
							queue.add(sgv);
					}
				}
			}
		}

		return PATH_SUMMARY_CREATOR.createUnidirectionalPath(start, end, predecessorTree);
	}

	/** Estimates the cost to reach the final node from given node (emissionProbability). */
	private double heuristic(final Node from, final Node to){
		return calculator.calculateWeight(from, to);
	}

}
