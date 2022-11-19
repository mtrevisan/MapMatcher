/**
 * Copyright (c) 2021 Mauro Trevisan
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

import java.util.HashMap;
import java.util.PriorityQueue;


/**
 * https://github.com/Nalhin/Navigation/blob/main/backend/libraries/pathfinder/src/main/java/com/navigation/pathfinder/pathfinding/AStarPathfinder.java
 *
 * https://github.com/navjindervirdee/Advanced-Shortest-Paths-Algorithms/tree/master/A-Star/A%20star
 * https://github.com/coderodde/GraphSearchPal/blob/master/src/main/java/net/coderodde/gsp/model/support/AStarPathFinder.java
 * https://github.com/valhalla/valhalla/blob/master/docs/meili/algorithms.md
 */
public class AStarPathfinder implements PathfindingStrategy{

	private final EdgeWeightCalculator calculator;
	private static final PathSummaryCreator pathSummaryCreator = new PathSummaryCreator();

	public AStarPathfinder(EdgeWeightCalculator calculator){
		this.calculator = calculator;
	}

	@Override
	public PathSummary findPath(Vertex start, Vertex end, Graph graph){
		var predecessorTree = new HashMap<Vertex, Edge>();
		predecessorTree.put(start, null);

		var gScores = new HashMap<Vertex, Double>();
		gScores.put(start, 0.0);

		var open = new PriorityQueue<ScoredGraphVertex>();
		open.add(new ScoredGraphVertex(start, heuristic(start, end)));

		while(!open.isEmpty()){
			var curr = open.poll().vertex();

			if(curr.equals(end)){
				return pathSummaryCreator.createUnidirectionalPath(start, end, predecessorTree);
			}

			for(var edge : graph.getVertexEdges(curr)){
				var neighbour = edge.getTo();
				var newScore = gScores.get(curr) + calculator.calculateWeight(edge);

				if(newScore < gScores.getOrDefault(neighbour, Double.MAX_VALUE)){
					gScores.put(neighbour, newScore);
					predecessorTree.put(neighbour, edge);
					open.add(new ScoredGraphVertex(neighbour, newScore + heuristic(neighbour, end)));
				}
			}
		}

		return pathSummaryCreator.createUnidirectionalPath(start, end, predecessorTree);
	}

	private double heuristic(Vertex from, Vertex to){
		return calculator.estimateWeight(from, to);
	}

}
