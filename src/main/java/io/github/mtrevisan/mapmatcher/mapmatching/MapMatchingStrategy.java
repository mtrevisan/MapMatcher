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
import io.github.mtrevisan.mapmatcher.pathfinding.AStarPathFinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.NodeCountCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Coordinate;

import java.util.ArrayList;
import java.util.List;


public interface MapMatchingStrategy{

	PathFindingStrategy PATH_FINDER = new AStarPathFinder(new NodeCountCalculator());


	Edge[] findPath(Graph graph, Coordinate[] observations);

	static Edge[] connectPath(final Edge[] path, final Graph graph){
		final int size = path.length;
		final List<Edge> connectedPath = new ArrayList<>(size);
		if(size > 0){
			connectedPath.add(path[0]);
			for(int i = 1; i < size; i ++){
				if(path[i - 1].getOutEdges().contains(path[i]))
					connectedPath.add(path[i]);
				else{
					//add path from `path[i - 1]` to `path[i]`
					final List<Node> nodePath = PATH_FINDER.findPath(path[i - 1].getTo(), path[i].getFrom(), graph)
						.simplePath();
					assert !nodePath.isEmpty();
					for(int j = 1; j < nodePath.size(); j ++){
						final Node fromNode = nodePath.get(j - 1);
						final Node toNode = nodePath.get(j);
						final Edge edge = fromNode.findOutEdges(toNode);
						assert edge != null;
						connectedPath.add(edge);
					}
					connectedPath.add(path[i]);
				}
			}
		}
		return connectedPath.toArray(Edge[]::new);
	}

}
