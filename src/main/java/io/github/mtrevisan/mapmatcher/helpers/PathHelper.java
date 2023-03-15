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
import io.github.mtrevisan.mapmatcher.graph.NearLineMergeGraph;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.helpers.filters.GPSPositionSpeedFilter;
import io.github.mtrevisan.mapmatcher.helpers.hprtree.HPRtree;
import io.github.mtrevisan.mapmatcher.pathfinding.AStarPathFinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.EdgeWeightCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Envelope;
import io.github.mtrevisan.mapmatcher.spatial.GPSPoint;
import io.github.mtrevisan.mapmatcher.spatial.GeodeticHelper;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class PathHelper{

	public static final String REVERSED_EDGE_SUFFIX = "-rev";

	private PathHelper(){}


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


	public static Edge[] connectPath(final Edge[] path, final Graph graph, final EdgeWeightCalculator calculator){
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
						final PathFindingStrategy pathFinder = new AStarPathFinder(calculator);
						final List<Node> nodePath = pathFinder.findPath(path[previousIndex].getTo(), path[currentIndex].getFrom(), graph)
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

	public static Polyline extractPathAsPolyline(final Edge[] connectedPath, final Point previousObservation, final Point currentObservation){
		if(connectedPath.length == 0)
			return null;

		final Edge fromSegment = connectedPath[0];
		final Edge toSegment = connectedPath[connectedPath.length - 1];
		return extractPathAsPolyline(connectedPath, fromSegment, toSegment, previousObservation, currentObservation);
	}

	public static Polyline extractPathAsPolyline(final List<Node> path, final Edge fromSegment, final Edge toSegment,
			final Point previousObservation, final Point currentObservation){
		final Edge[] pathAsEdges = extractPathAsEdges(path);
		return extractPathAsPolyline(pathAsEdges, fromSegment, toSegment, previousObservation, currentObservation);
	}

	public static Polyline extractPathAsPolyline(final Edge[] connectedPath, final Edge fromSegment, final Edge toSegment,
			final Point previousObservation, final Point currentObservation){
		//cut first segment
		final Point[][] fromSegmentCut = fromSegment.getPolyline().cut(previousObservation);
		//cut last segment
		final Point[][] toSegmentCut = toSegment.getPolyline().cut(currentObservation);

		//merge segments
		int size = fromSegmentCut[1].length + toSegmentCut[0].length;
		for(int i = 1; i < connectedPath.length - 1; i ++)
			size += connectedPath[i].getPolyline().size();
		final Point[] mergedPoints = new Point[size];
		size = fromSegmentCut[1].length;
		System.arraycopy(fromSegmentCut[1], 0, mergedPoints, 0, size);

		for(int i = 1; i < connectedPath.length - 1; i ++){
			final Edge edge = connectedPath[i];
			final Point[] points = edge.getPolyline().getPoints();
			System.arraycopy(points, 0, mergedPoints, size, points.length);

			size += points.length;
		}

		System.arraycopy(toSegmentCut[0], 0, mergedPoints, size, toSegmentCut[0].length);

		final GeometryFactory factory = previousObservation.getFactory();
		return factory.createPolyline(mergedPoints);
	}

	public static boolean isSegmentsTheSame(final Edge fromSegment, final Edge toSegment){
		return fromSegment.equals(toSegment);
	}

	public static boolean isSegmentsTheSameOrReversed(final Edge fromSegment, final Edge toSegment){
		return (fromSegment.equals(toSegment) || isSegmentsReversed(fromSegment, toSegment));
	}

	public static boolean isSegmentsReversed(final Edge fromSegment, final Edge toSegment){
		return (fromSegment.getFrom().getPoint().equals(toSegment.getTo().getPoint())
			&& fromSegment.getTo().getPoint().equals(toSegment.getFrom().getPoint()));
	}

	public static boolean isGoingBackward(final Point previousObservation, final Point currentObservation, final Polyline polyline){
		//calculate Along-Track Distance
		final double previousATD = polyline.alongTrackDistance(previousObservation);
		final double currentATD = polyline.alongTrackDistance(currentObservation);
		return (currentATD < previousATD);
	}


	/**
	 * Extract a set of candidate road links within a certain distance to all observation.
	 * <p>
	 * Measurement error <code>ε_m = ε_p + ε_r</code>, where </ode>ε_p</code> is the positioning error (<em>20 m</em>),
	 * <code>ε_r = 0.5 * w / sin(α / 2)</code> is the road error, <code>w</code> is the road width (max <em>8 m</em>), and <code>α</code>
	 * is the angle between two intersecting roads (consider it to be <em>90°</em>).
	 * This lead to <code>ε_m = 20 + 5.7 = 26 m</code>, a savvy choice is <em>50 m</em>.
	 * </p>
	 *
	 * @param tree	The set of road links.
	 * @param observations	The observations.
	 * @param threshold	The threshold [m].
	 * @return	The list of road links whose distance is less than the given radius from each observation.
	 */
	public static Collection<Polyline> extractObservedEdges(final HPRtree<Polyline> tree, final Point[] observations,
		final double threshold){
		//collect max and min of X and Y:
		double maxX = Double.NEGATIVE_INFINITY;
		double minX = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		for(final Point observation : observations){
			final double x = observation.getX();
			if(x > maxX)
				maxX = x;
			else if(x < minX)
				minX = x;

			final double y = observation.getY();
			if(y > maxY)
				maxY = y;
			else if(y < minY)
				minY = y;
		}

		//create an envelope around max and min:
		final GeometryFactory factory = observations[0].getFactory();
		final Point max = Point.of(factory, maxX, maxY);
		final Point min = Point.of(factory, minX, minY);
		final Point northEast = GeodeticHelper.destination(GeodeticHelper.destination(max, 0., threshold), 90., threshold);
		final Point southWest = GeodeticHelper.destination(GeodeticHelper.destination(min, 180., threshold), 270., threshold);
		final Envelope envelope = Envelope.of(northEast, southWest);

		//query the tree
		return tree.query(envelope);
	}

	public static GPSPoint[] extractObservations(final HPRtree<Polyline> tree, final GPSPoint[] observations, final double threshold){
		final GPSPoint[] feasibleObservations = new GPSPoint[observations.length];

		//step 1. Use Kalman filter to smooth the coordinates
		final GPSPositionSpeedFilter kalmanFilter = new GPSPositionSpeedFilter(3., 5.);
		feasibleObservations[0] = observations[0];
		for(int i = 1; i < observations.length; i ++){
			kalmanFilter.updatePosition(observations[i].getY(), observations[i].getX(),
				ChronoUnit.SECONDS.between(observations[i - 1].getTimestamp(), observations[i].getTimestamp()));
			final double[] position = kalmanFilter.getPosition();
			feasibleObservations[i] = GPSPoint.of(position[1], position[0], observations[i].getTimestamp());
		}

		//step 2. Retain all observation that are within a certain radius from an edge
		for(int i = 0; i < feasibleObservations.length; i ++){
			final GPSPoint observation = feasibleObservations[i];
			final Point northEast = GeodeticHelper.destination(GeodeticHelper.destination(observation, 0., threshold), 90., threshold);
			final Point southWest = GeodeticHelper.destination(GeodeticHelper.destination(observation, 180., threshold), 270., threshold);
			final Envelope envelope = Envelope.of(northEast, southWest);

			final List<Polyline> edges = tree.query(envelope);
			double minDistance = Double.POSITIVE_INFINITY;
			for(final Polyline edge : edges){
				final double distance = observation.distance(edge);
				if(distance < minDistance)
					minDistance = distance;
			}
			if(edges.isEmpty() || minDistance > threshold)
				feasibleObservations[i] = null;
		}

		return feasibleObservations;
	}


	public static Graph extractDirectGraph(final Collection<Polyline> edges, final double threshold){
		final NearLineMergeGraph graph = new NearLineMergeGraph(threshold)
			.withTree();
		int e = 0;
		for(final Polyline edge : edges){
			graph.addApproximateDirectEdge(String.valueOf(e), edge.getStartPoint(), edge.getEndPoint());

			e ++;
		}
		return graph;
	}

	public static Graph extractBidirectionalGraph(final Collection<Polyline> edges, final double threshold){
		final NearLineMergeGraph graph = new NearLineMergeGraph(threshold)
			.withTree();
		int e = 0;
		for(final Polyline edge : edges){
			graph.addApproximateDirectEdge(String.valueOf(e), edge.getStartPoint(), edge.getEndPoint());
			final Polyline reverse = edge.reverse();
			graph.addApproximateDirectEdge(e + REVERSED_EDGE_SUFFIX, reverse.getStartPoint(), reverse.getEndPoint());

			e ++;
		}
		return graph;
	}

}
