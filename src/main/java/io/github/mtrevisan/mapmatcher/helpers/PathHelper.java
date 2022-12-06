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
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.ArrayList;
import java.util.List;


public class PathHelper{

	private PathHelper(){}

	public static boolean hasMixedDirections(final List<Node> path, final Edge fromSegment, final Edge toSegment){
		final List<Edge> pathEdges = extractPathAsEdges(path);
		pathEdges.add(0, fromSegment);
		pathEdges.add(toSegment);
		int reverseCount = 0;
		for(final Edge edge : pathEdges)
			if(edge.getID().endsWith("-rev"))
				reverseCount ++;
		return (reverseCount > 0 && reverseCount < pathEdges.size());
	}

	public static Polyline extractPathAsPolyline(final List<Node> path){
		Polyline polyline = null;

		//search for a feasible path between the projection onto fromSegment and the one onto toSegment
		if(!path.isEmpty()){
			final List<Edge> pathAsEdges = extractPathAsEdges(path);

			//calculate number of points
			int size = 0;
			for(final Edge edge : pathAsEdges)
				size += edge.getPolyline().size() - (size > 0? 1: 0);

			//merge segments
			if(size > 0){
				final Point[] mergedPoints = new Point[size];
				size = 0;
				for(final Edge edge : pathAsEdges){
					final Point[] points = edge.getPolyline().getPoints();
					final int count = points.length - (size > 0? 1: 0);
					assert size == 0 || mergedPoints[size - 1].equals(points[0]);
					System.arraycopy(points, (size > 0? 1: 0), mergedPoints, size, count);
					size += count;
				}
				final GeometryFactory factory = path.get(0).getPoint().getFactory();
				polyline = factory.createPolyline(mergedPoints);
			}
		}
		return polyline;
	}

	public static List<Edge> extractPathAsEdges(final List<Node> path){
		final int size = path.size();
		final List<Edge> connectedPath = new ArrayList<>(Math.max(size - 1, 0));
		for(int i = 1; i < size; i ++)
			for(final Edge edge : path.get(i - 1).getOutEdges())
				if(edge.getTo().equals(path.get(i))){
					connectedPath.add(edge);
					break;
				}
		return connectedPath;
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
