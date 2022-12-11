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
package io.github.mtrevisan.mapmatcher.helpers;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.pathfinding.AStarPathFinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.NodeCountCalculator;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.ArrayList;
import java.util.List;


public class PathHelper{

	private static final PathFindingStrategy PATH_FINDER = new AStarPathFinder(new NodeCountCalculator());


	private PathHelper(){}


	public static boolean hasMixedDirections(final List<Node> path, final Edge fromSegment, final Edge toSegment){
		final Edge[] pathEdges = extractPathAsEdges(path);
		final Edge[] augmentedPathEdges = new Edge[pathEdges.length + 2];
		augmentedPathEdges[0] = fromSegment;
		augmentedPathEdges[augmentedPathEdges.length - 1] = toSegment;
		System.arraycopy(pathEdges, 0, augmentedPathEdges, 1, pathEdges.length);

		int reverseCount = 0;
		for(final Edge edge : augmentedPathEdges)
			if(edge.getID().endsWith("-rev"))
				reverseCount ++;
		return (reverseCount > 0 && reverseCount < augmentedPathEdges.length);
	}

	public static Edge[] extractPathAsEdges(final List<Node> path){
		final int size = path.size();
		final Edge[] connectedPath = new Edge[Math.max(size - 1, 0)];
		for(int i = 1; i < size; i ++)
			for(final Edge edge : path.get(i - 1).getOutEdges())
				if(edge.getTo().equals(path.get(i))){
					connectedPath[i - 1] = edge;
					break;
				}
		return connectedPath;
	}


	public static Edge[] connectPath(final Edge[] path, final Graph graph){
		final int size = (path != null? path.length: 0);
		final List<Edge> connectedPath = new ArrayList<>(size);
		if(size > 0){
			int previousIndex = extractNextNonNullEdge(path, 0);
			if(previousIndex >= 0)
				connectedPath.add(path[previousIndex]);
			while(true){
				final int currentIndex = extractNextNonNullEdge(path, previousIndex + 1);
				if(currentIndex < 0)
					break;

				if(!path[previousIndex].equals(path[currentIndex])){
					if(path[previousIndex].getOutEdges().contains(path[currentIndex]))
						connectedPath.add(path[currentIndex]);
					else{
						//add path from `path[index]` to `path[i]`
						final List<Node> nodePath = PATH_FINDER.findPath(path[previousIndex].getTo(), path[currentIndex].getFrom(), graph)
							.simplePath();
						assert !nodePath.isEmpty();
						for(int j = 1; j < nodePath.size(); j ++){
							final Node fromNode = nodePath.get(j - 1);
							final Node toNode = nodePath.get(j);
							final Edge edge = fromNode.findOutEdges(toNode);
							assert edge != null;
							connectedPath.add(edge);
						}
						connectedPath.add(path[currentIndex]);
					}
				}

				previousIndex = currentIndex;
			}
		}
		return connectedPath.toArray(Edge[]::new);
	}

	private static int extractNextNonNullEdge(final Edge[] path, int index){
		final int m = path.length;
		while(index < m && path[index] == null)
			index ++;
		return (index < m? index: -1);
	}

	public static Polyline extractPathAsPolyline(final Edge[] connectedPath){
		Polyline polyline = null;

		//search for a feasible path between the projection onto fromSegment and the one onto toSegment
		if(connectedPath != null && connectedPath.length > 0){
			//calculate number of points
			int size = 0;
			for(final Edge edge : connectedPath)
				size += edge.getPolyline().size() - (size > 0? 1: 0);

			//merge segments
			if(size > 0){
				final Point[] mergedPoints = new Point[size];
				size = 0;
				for(final Edge edge : connectedPath){
					final Point[] points = edge.getPolyline().getPoints();
					final int count = points.length - (size > 0? 1: 0);
					assert size == 0 || mergedPoints[size - 1].equals(points[0]);
					System.arraycopy(points, (size > 0? 1: 0), mergedPoints, size, count);
					size += count;
				}
				final GeometryFactory factory = connectedPath[0].getPolyline().getFactory();
				polyline = factory.createPolyline(mergedPoints);
			}
		}

		return polyline;
	}

	public static boolean isSegmentsReversed(final Edge fromSegment, final Edge toSegment){
		return (fromSegment.getFrom().getPoint().equals(toSegment.getTo().getPoint())
			&& fromSegment.getTo().getPoint().equals(toSegment.getFrom().getPoint()));
	}

	public static boolean isGoingForward(final Point previousObservation, final Point currentObservation, final Polyline polyline){
		//calculate Along-Track Distance
		final double previousATD = polyline.alongTrackDistance(previousObservation);
		final double currentATD = polyline.alongTrackDistance(currentObservation);
		return (previousATD <= currentATD);
	}

}
