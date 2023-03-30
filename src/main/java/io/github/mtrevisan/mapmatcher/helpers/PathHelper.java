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
import io.github.mtrevisan.mapmatcher.graph.NearNodeMergeGraph;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.helpers.filters.GPSPositionSpeedFilter;
import io.github.mtrevisan.mapmatcher.helpers.hprtree.HPRtree;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.spatial.Envelope;
import io.github.mtrevisan.mapmatcher.spatial.GPSPoint;
import io.github.mtrevisan.mapmatcher.spatial.GeodeticHelper;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PathHelper{

	public static final String REVERSED_EDGE_SUFFIX = "-rev";

	private PathHelper(){}


	public static Edge[] connectPath(final Edge[] path, final Graph graph, final PathFindingStrategy pathFinder){
		final int size = (path != null? path.length: 0);
		final List<Edge> connectedPath = new ArrayList<>(size);
		if(size > 0){
			int previousIndex = nextNonNullEdge(path, 0);
			if(previousIndex >= 0)
				connectedPath.add(path[previousIndex]);
			while(true){
				final int currentIndex = nextNonNullEdge(path, previousIndex + 1);
				if(currentIndex < 0)
					break;

				if(!path[previousIndex].equals(path[currentIndex])){
					if(path[previousIndex].getOutEdges().contains(path[currentIndex]))
						connectedPath.add(path[currentIndex]);
					else{
						//add path from `path[index]` to `path[i]`
						final Edge[] edgePath = pathFinder.findPath(path[previousIndex].getTo(), path[currentIndex].getFrom(), graph);
						for(int i = 0; i < edgePath.length; i ++)
							connectedPath.add(edgePath[i]);
						if(edgePath.length == 0)
							connectedPath.add(null);
						connectedPath.add(path[currentIndex]);
					}
				}

				previousIndex = currentIndex;
			}
		}
		return connectedPath.toArray(Edge[]::new);
	}

	private static int nextNonNullEdge(final Edge[] path, int index){
		final int m = path.length;
		while(index < m && path[index] == null)
			index ++;
		return (index < m? index: -1);
	}

	public static Polyline calculatePathAsPolyline(final Edge fromEdge, final Edge toEdge, final Graph graph,
			final PathFindingStrategy pathFinder){
		final GeometryFactory factory = graph.getFactory();
		if(fromEdge.equals(toEdge))
			return factory.createEmptyPolyline();

		//NOTE: not the closest but `fromEdge.getTo()/toEdge.getFrom()`, in order to avoid wrong connection from `fromEdge.getFrom()` to
		// `currentNode` that does not pass through `fromEdge.getTo()` and `toEdge.getFrom()`
		final Node previousNode = fromEdge.getTo();
		final Node currentNode = toEdge.getFrom();
		final Edge[] pathFromTo = pathFinder.findPath(previousNode, currentNode, graph);

		Polyline polylineFromTo;
		final Point[] fromPath = fromEdge.getPath().getPoints();
		final Point[] toPath = toEdge.getPath().getPoints();
		if(previousNode.equals(currentNode)){
			final Point[] points = Arrays.copyOf(fromPath, fromPath.length + toPath.length);
			System.arraycopy(toPath, 0, points, fromPath.length, toPath.length);
			polylineFromTo = factory.createPolyline(points);
		}
		else{
			final List<Polyline> polylines = extractEdgesAsPolyline(pathFromTo, factory);
			if(polylines.isEmpty())
				polylineFromTo = factory.createEmptyPolyline();
			else
				//prepend previousNode path start, append currentNode to path end
				polylineFromTo = polylines.get(0).prepend(fromPath)
					.append(toPath);
		}

		return polylineFromTo;
	}

	public static List<Polyline> extractEdgesAsPolyline(final Edge[] connectedPath, final GeometryFactory factory){
		final List<Polyline> result = new ArrayList<>(0);
		if(connectedPath.length > 0){
			//merge segments
			final List<Point> mergedPoints = new ArrayList<>();
			for(int i = 0; i < connectedPath.length; i ++){
				final Edge edge = connectedPath[i];
				if(edge == null){
					result.add(factory.createPolyline(mergedPoints.toArray(Point[]::new)));
					mergedPoints.clear();
					continue;
				}

				final Point[] edgePoints = edge.getPath()
					.getPoints();
				for(final Point point : edgePoints)
					mergedPoints.add(point);
			}
			if(!mergedPoints.isEmpty())
				result.add(factory.createPolyline(mergedPoints.toArray(Point[]::new)));
		}
		return result;
	}

	public static boolean isEdgesTheSame(final Edge fromEdge, final Edge toEdge){
		return fromEdge.equals(toEdge);
	}

	public static boolean isEdgesTheSameOrReversed(final Edge fromEdge, final Edge toEdge){
		return (fromEdge.equals(toEdge) || isEdgesReversed(fromEdge, toEdge));
	}

	public static boolean isEdgesReversed(final Edge fromEdge, final Edge toEdge){
		return (fromEdge.getFrom().getPoint().equals(toEdge.getTo().getPoint())
			&& fromEdge.getTo().getPoint().equals(toEdge.getFrom().getPoint()));
	}

	public static boolean isGoingBackward(final Point previousObservation, final Point currentObservation, final Polyline polyline){
		//calculate Along-Track Distance
		final double previousATD = polyline.alongTrackDistance(previousObservation);
		final double currentATD = polyline.alongTrackDistance(currentObservation);
		return (currentATD < previousATD);
	}


	public static int extractNextObservation(final Point[] observations, int index){
		final int m = observations.length;
		while(index < m && observations[index] == null)
			index ++;
		return (index < m? index: -1);
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
		final Set<Polyline> observedEdges = new HashSet<>(0);
		int currentObservationIndex = PathHelper.extractNextObservation(observations, 0);
		while(currentObservationIndex >= 0){
			final Point observation = observations[currentObservationIndex];

			//construct the envelope
			final Point northEast = GeodeticHelper.destination(observation, 45., threshold);
			final Point southWest = GeodeticHelper.destination(observation, 225., threshold);
			final Envelope envelope = Envelope.of(northEast, southWest);

			//skip observations inside current envelope
			do{
				currentObservationIndex = PathHelper.extractNextObservation(observations, currentObservationIndex + 1);
			}while(currentObservationIndex >= 0 && envelope.intersects(observations[currentObservationIndex]));

			//add observed edges to final set
			observedEdges.addAll(tree.query(envelope));
		}

		return observedEdges;
	}

	public static GPSPoint[] extractObservations(final HPRtree<Polyline> tree, final GPSPoint[] observations, final double threshold){
		final GPSPoint[] feasibleObservations = new GPSPoint[observations.length];

		if(observations.length > 0){
			//step 1. Use Kalman filter to smooth the coordinates
			final GPSPositionSpeedFilter kalmanFilter = new GPSPositionSpeedFilter(3., 5.);
			feasibleObservations[0] = observations[0];
			for(int i = 1; i < observations.length; i ++){
				final GPSPoint observation = observations[i];
				if(observation == null)
					continue;

				kalmanFilter.updatePosition(observation.getY(), observation.getX(),
					ChronoUnit.SECONDS.between(observations[i - 1].getTimestamp(), observation.getTimestamp()));
				final double[] position = kalmanFilter.getPosition();
				feasibleObservations[i] = GPSPoint.of(position[1], position[0], observation.getTimestamp());
			}

			//step 2. Retain all observation that are within a certain radius from an edge
			for(int i = 0; i < feasibleObservations.length; i ++){
				final GPSPoint observation = feasibleObservations[i];
				if(observation == null)
					continue;

				final Point northEast = GeodeticHelper.destination(observation, 45., threshold);
				final Point southWest = GeodeticHelper.destination(observation, 225., threshold);
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
		}

		return feasibleObservations;
	}


	//create id as `<segment>(.<leg>)?
	public static Graph extractDirectGraph(final Collection<Polyline> edges, final double threshold){
		final NearNodeMergeGraph graph = new NearNodeMergeGraph(threshold)
			.withTree();
		int e = 0;
		for(final Polyline edge : edges){
			final String id = String.valueOf(e);
			graph.addApproximateDirectEdge(id, edge);

			e ++;
		}
		return graph;
	}

	//create id as `<segment>(.<leg>)?(-rev)?`
	public static Graph extractBidirectionalGraph(final Collection<Polyline> edges, final double threshold){
		final NearNodeMergeGraph graph = new NearNodeMergeGraph(threshold)
			.withTree();
		int e = 0;
		for(final Polyline edge : edges){
			final String id = String.valueOf(e);
			graph.addApproximateDirectEdge(id, edge);
			//add reversed
			graph.addApproximateDirectEdge(id + REVERSED_EDGE_SUFFIX, edge.reverse());

			e ++;
		}
		return graph;
	}

}
