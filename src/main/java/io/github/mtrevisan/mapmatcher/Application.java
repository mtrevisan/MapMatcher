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
package io.github.mtrevisan.mapmatcher;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.GraphBuilder;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.pathfinding.AStarPathfinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathfindingStrategy;
import io.github.mtrevisan.mapmatcher.weight.DistanceEdgeWeightCalculator;
import io.github.mtrevisan.mapmatcher.weight.EdgeWeightCalculator;


public class Application{

	public static void main(final String[] args){
		EdgeWeightCalculator calculator = new DistanceEdgeWeightCalculator();
		PathfindingStrategy strategy = new AStarPathfinder(calculator);
		Vertex start = new Vertex(1, new Coordinates(45.714662, 12.193859));
		Vertex end = new Vertex(10, new Coordinates(46.062345, 12.078982));
		Graph graph = new GraphBuilder()
			.addVertex(start)
			.addVertex(new Vertex(2, new Coordinates(45.982746, 12.302965)))
			.addVertex(new Vertex(3, new Coordinates(46.007074, 12.285113)))
			.addVertex(end)
			.connectByIds(1, 2, 1)
			.connectByIds(1, 3, 1)
			.connectByIds(2, 3, 1)
			.connectByIds(2, 10, 1)
			.connectByIds(3, 10, 1)
			.asGraph();
		strategy.findPath(start, end, graph);
	}

}
