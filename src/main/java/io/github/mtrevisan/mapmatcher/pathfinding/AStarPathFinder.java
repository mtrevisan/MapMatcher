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
import io.github.mtrevisan.mapmatcher.helpers.FibonacciHeap;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.EdgeWeightCalculator;
import io.github.mtrevisan.mapmatcher.pathfinding.path.PathSummary;
import io.github.mtrevisan.mapmatcher.pathfinding.path.PathSummaryCreator;
import io.github.mtrevisan.mapmatcher.pathfinding.path.SingleDirectionalPathSummary;

import java.util.HashMap;
import java.util.HashSet;


/**
 * @see <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">A* search algorithm</a>
 */
public class AStarPathFinder implements PathFindingStrategy{

	private static final PathSummaryCreator PATH_SUMMARY_CREATOR = new PathSummaryCreator();

	private final EdgeWeightCalculator calculator;


	public AStarPathFinder(final EdgeWeightCalculator calculator){
		this.calculator = calculator;
	}

	@Override
	public PathSummary findPath(final Node start, final Node end, final Graph graph){
		if(start.equals(end))
			//early exit (return the node itself)
			return SingleDirectionalPathSummary.ofNode(start);

		//the node immediately preceding a given node on the cheapest path from start to the given node currently known
		final var predecessorTree = new HashMap<Node, Edge>();
		predecessorTree.put(start, null);

		//the cost of the cheapest path from start to given node currently known
		final var gScores = new HashMap<Node, Double>();
		gScores.put(start, 0.);

		//set of discovered nodes that may need to be (re-)expanded
		final var frontier = new FibonacciHeap<Node>();
		final var seenNodes = new HashSet<Node>(graph.nodes().size());
		//NOTE: the score here is `gScore[n] + h(n)`; it represents the current best guess as to how cheap a path could be from start to
		// finish if it goes through the given node
		var fScore = heuristic(start, end);
		frontier.add(start, fScore);
		seenNodes.add(start);

		while(!frontier.isEmpty()){
			final var fromNode = frontier.poll();
			if(fromNode.equals(end))
				break;

			for(final var edge : fromNode.getOutEdges()){
				final var toNode = edge.getTo();
				if(seenNodes.contains(toNode))
					continue;

				final var newScore = gScores.get(fromNode) + calculator.calculateWeight(edge);
				if(newScore < gScores.getOrDefault(toNode, Double.POSITIVE_INFINITY)){
					gScores.put(toNode, newScore);
					predecessorTree.put(toNode, edge);

					fScore = newScore + heuristic(toNode, end);
					if(!seenNodes.contains(toNode)){
						frontier.add(toNode, fScore);
						seenNodes.add(toNode);
					}
				}
			}

			seenNodes.add(fromNode);
		}

		return PATH_SUMMARY_CREATOR.createUnidirectionalPath(start, end, predecessorTree);
	}

	/** Estimates the cost to reach the final node from given node (emissionProbability). */
	private double heuristic(final Node from, final Node to){
		return calculator.calculateWeight(from, to);
	}

}
