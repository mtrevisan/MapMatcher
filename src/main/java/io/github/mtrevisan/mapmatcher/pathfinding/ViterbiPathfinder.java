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

import java.util.HashMap;
import java.util.LinkedList;


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
		final var predecessorTree = new HashMap<Vertex, Edge>();
		predecessorTree.put(start, null);

		//set of discovered nodes that may need to be (re-)expanded
		final var queue = new LinkedList<Vertex>();
		queue.add(start);
		while(!queue.isEmpty()){
			final var current = queue.pop();
			if(current.equals(end))
				break;

			for(final var edge : graph.getVertexEdges(current)){
				final var neighbor = edge.getTo();
				final var newWeight = current.getWeight() + calculator.calculateWeight(edge);

				if(newWeight < neighbor.getWeight()){
					predecessorTree.put(neighbor, edge);

					//store the cost of the cheapest path from start to this node
					neighbor.setWeight(newWeight + calculator.calculateWeight(neighbor, end));
				}

				if(!queue.contains(neighbor))
					queue.add(neighbor);
			}
		}

		return PATH_SUMMARY_CREATOR.createUnidirectionalPath(start, end, predecessorTree);
	}

}
