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
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.graph.ScoredGraphNode;
import io.github.mtrevisan.mapmatcher.path.PathSummaryCreator;
import io.github.mtrevisan.mapmatcher.weight.EdgeWeightCalculator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;


/**
 * @see <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">A* search algorithm</a>
 */
public class AStarPathfinder implements PathfindingStrategy{

	private static final PathSummaryCreator PATH_SUMMARY_CREATOR = new PathSummaryCreator();

	private final EdgeWeightCalculator calculator;


	public AStarPathfinder(final EdgeWeightCalculator calculator){
		this.calculator = calculator;
	}

	@Override
	public PathSummary findPath(final Node start, final Node end, final Graph graph){
		//the node immediately preceding a given node on the cheapest path from start to the given node currently known
		final var predecessorTree = new HashMap<Node, Edge>();
		predecessorTree.put(start, null);

		//the cost of the cheapest path from start to given node currently known
		final var gScores = new HashMap<Node, Double>();
		gScores.put(start, 0.);

		final var seenNodes = new HashSet<Node>();

		//set of discovered nodes that may need to be (re-)expanded
		final var queue = new PriorityQueue<ScoredGraphNode>();
		//NOTE: the score here is `gScore[n] + h(n)`; it represents the current best guess as to how cheap a path could be from start to
		// finish if it goes through the given node
		var fScore = heuristic(start, end);
		queue.add(new ScoredGraphNode(start, fScore));

		while(!queue.isEmpty()){
			final var current = queue.poll()
				.node();
			if(current.equals(end))
				break;

			for(final var edge : current.geOutEdges()){
				final var neighbor = edge.getTo();
				if(seenNodes.contains(neighbor))
					continue;

				final var newScore = gScores.get(current) + calculator.calculateWeight(edge);
				if(newScore < gScores.getOrDefault(neighbor, Double.MAX_VALUE)){
					gScores.put(neighbor, newScore);
					predecessorTree.put(neighbor, edge);

					fScore = newScore + heuristic(neighbor, end);
					final ScoredGraphNode sgv = new ScoredGraphNode(neighbor, fScore);
					if(!queue.contains(sgv))
						queue.add(sgv);
				}
			}

			seenNodes.add(current);
		}

		return PATH_SUMMARY_CREATOR.createUnidirectionalPath(start, end, predecessorTree);
	}

	/** Estimates the cost to reach the final node from given node (emissionProbability). */
	private double heuristic(final Node from, final Node to){
		return calculator.calculateWeight(from, to);
	}

}
