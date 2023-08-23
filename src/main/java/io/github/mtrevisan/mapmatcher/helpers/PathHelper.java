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
package io.github.mtrevisan.mapmatcher.helpers;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.NearNodeMergeGraph;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.helpers.filters.GPSPositionSpeedFilter;
import io.github.mtrevisan.mapmatcher.helpers.hprtree.HPRTree;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.spatial.Envelope;
import io.github.mtrevisan.mapmatcher.spatial.GPSPoint;
import io.github.mtrevisan.mapmatcher.spatial.GeodeticHelper;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PathHelper{

	private static final Logger LOGGER = LoggerFactory.getLogger(PathHelper.class);


	public static final String REVERSED_EDGE_SUFFIX = "-rev";

	private PathHelper(){}


	public static double averagePositionError(final Edge[] path, final Point[] observations){
		if(path == null)
			throw new IllegalArgumentException("Path must be non-null");
		if(observations == null)
			throw new IllegalArgumentException("Observations must be non-null");
		if(path.length != observations.length)
			throw new IllegalArgumentException("Length of path must match length of observations");

		double cumulativeError = 0.;
		int windowSize = 0;
		for(int i = 0; i < observations.length; i ++)
			if(observations[i] != null && path[i] != null && !path[i].isOffRoad()){
				cumulativeError += observations[i].distance(path[i].getPath());
				windowSize ++;
			}
		return cumulativeError / windowSize;
	}

	public static double averagePositionStandardDeviation(final Edge[] path, final Point[] observations, final double averageError){
		if(path == null)
			throw new IllegalArgumentException("Path must be non-null");
		if(observations == null)
			throw new IllegalArgumentException("Observations must be non-null");
		if(path.length != observations.length)
			throw new IllegalArgumentException("Length of path must match length of observations");

		double cumulativeError = 0.;
		int windowSize = 0;
		for(int i = 0; i < observations.length; i ++)
			if(observations[i] != null && path[i] != null && !path[i].isOffRoad()){
				final double delta = observations[i].distance(path[i].getPath()) - averageError;
				cumulativeError += delta * delta;
				windowSize ++;
			}
		return Math.sqrt(cumulativeError / windowSize);
	}


	public static void restrictSolutions(final Collection<Map.Entry<Double, Edge[]>> paths, final double percentile){
		if(paths.size() > 1){
			final double minimumCost = paths.iterator().next().getKey();
			final double costLimit = minimumCost * (1. + percentile);
			paths.removeIf(path -> path.getKey() > costLimit);
		}
	}

	public static Edge[] connectPath(final Edge[] path, final Graph graph, final PathFindingStrategy pathFinder){
		final int size = (path != null? path.length: 0);
		final List<Edge> connectedPath = new ArrayList<>(size);

		//FIXME could be any of obs0-obs0[17], or obs0[17]-obs0, or obs0-obs1, or 5
		Edge previousEdge = null;
		int currentIndex = -1;
		while(true){
			currentIndex = nextNonNullEdge(path, currentIndex + 1);
			if(currentIndex < 0)
				break;

			final Edge currentEdge = path[currentIndex];
			if(currentEdge.equals(previousEdge))
				//same edge, skip to the next
				continue;


			if(previousEdge == null)
				connectedPath.add(currentEdge);
			else{
				final Edge previousEdgeToProjected = previousEdge.getToProjected();
				final Edge currentEdgeFromProjected = currentEdge.getFromProjected();
				final boolean chooseFromProjection = (currentEdgeFromProjected != null);
				final Edge fromEdge = (chooseFromProjection? currentEdgeFromProjected: currentEdge);
				final Edge toEdge = (previousEdgeToProjected != null? previousEdgeToProjected: previousEdge);
				if(toEdge.equals(fromEdge))
					connectedPath.add(currentEdge);
				else{
					final Edge[] edgePath = pathFinder.findPath(toEdge.getTo(), fromEdge.getFrom(), graph);
					if(edgePath == null)
						connectedPath.add(null);
					else if(edgePath.length > 0)
						connectedPath.addAll(Arrays.asList(edgePath));
					else
						connectedPath.add(fromEdge);

					if(chooseFromProjection && !currentEdgeFromProjected.equals(previousEdge))
						connectedPath.add(currentEdge);
				}
			}


			previousEdge = currentEdge;
		}



//		int previousIndex = nextNonNullEdge(path, 0);
//		if(previousIndex < 0)
//			connectedPath.add(null);
//		else{
//			final GeometryFactory factory = graph.getFactory();
//
//			previousEdge = path[previousIndex];
//			while(true){
//				currentIndex = nextNonNullEdge(path, previousIndex + 1);
//				if(currentIndex < 0)
//					break;
//
//				Edge currentEdge = path[currentIndex];
//				if(previousEdge.equals(currentEdge)){
//					//same edge, skip to the next
//					previousIndex = currentIndex;
//					continue;
//				}
//
//
//				Edge connectionToRoadEdge = null;
//				if(previousEdge.isOffRoad()){
//					Edge previousToProjected = previousEdge.getToProjected();
//					if(previousToProjected != null && previousToProjected.getFrom().getID().equals(previousEdge.getFrom().getID()))
//						previousToProjected = currentEdge;
//					if(previousToProjected != null){
//						final Point[] cutTo = previousToProjected.getPath().cutHard(previousEdge.getTo().getPoint())[1];
//						final Node connectionToRoadNodeStart = Node.of(previousEdge.getTo().getID(), cutTo[0]);
//						final Node connectionToRoadNodeEnd = previousToProjected.getTo();
//						connectionToRoadEdge = Edge.createDirectEdge(connectionToRoadNodeStart, connectionToRoadNodeEnd,
//							factory.createPolyline(cutTo));
//
//						if(!connectionToRoadNodeEnd.equals(currentEdge.getTo()))
//							previousEdge = connectionToRoadEdge;
//						else
//							currentEdge = null;
//					}
//				}
//
//
//				Edge connectionFromRoadEdge = null;
//				if(currentEdge != null && currentEdge.isOffRoad()){
//					Edge currentFromProjected = currentEdge.getFromProjected();
//					if(currentFromProjected != null && currentFromProjected.getTo().getID().equals(currentEdge.getTo().getID()))
//						currentFromProjected = previousEdge;
//					if(currentFromProjected != null){
//						final Point[] cutFrom = currentFromProjected.getPath().cutHard(currentEdge.getFrom().getPoint())[0];
//						final Node connectionFromRoadNodeStart = currentFromProjected.getFrom();
//						final Node connectionFromRoadNodeEnd = Node.of(currentEdge.getFrom().getID(), cutFrom[cutFrom.length - 1]);
//						connectionFromRoadEdge = Edge.createDirectEdge(connectionFromRoadNodeStart, connectionFromRoadNodeEnd,
//							factory.createPolyline(cutFrom));
//
//						if(connectionFromRoadNodeStart.equals(previousEdge.getFrom())){
//							if(!currentFromProjected.getID().equals(previousEdge.getID()))
//								//restore edge
//								connectedPath.add(previousEdge);
//
//							previousEdge = null;
//						}
//					}
//				}
//
//
//				if(previousEdge != null)
//					connectedPath.add(previousEdge);
//
//				if(connectionToRoadEdge != null && connectionToRoadEdge != previousEdge)
//					connectedPath.add(connectionToRoadEdge);
//
//				if(previousEdge != null && currentEdge != null){
//					final Edge[] edgePath = pathFinder.findPath(previousEdge.getTo(), currentEdge.getFrom(), graph);
//					if(edgePath.length > 0)
//						connectedPath.addAll(Arrays.asList(edgePath));
//					else if(!previousEdge.getTo().equals(currentEdge.getFrom()))
//						connectedPath.add(null);
//				}
//
//				if(connectionFromRoadEdge != null && connectionFromRoadEdge != currentEdge)
//					connectedPath.add(connectionFromRoadEdge);
//
//				if(currentEdge != null)
//					connectedPath.add(currentEdge);
//
//
//				previousIndex = currentIndex;
//				previousEdge = connectedPath.remove(connectedPath.size() - 1);
//			}
//
//			if(previousEdge != null)
//				connectedPath.add(previousEdge);
//		}

		return connectedPath.toArray(Edge[]::new);
	}

	private static int nextNonNullEdge(final Edge[] path, int index){
		final int m = (path != null? path.length: 0);
		while(index < m && path[index] == null)
			index ++;
		return (index < m? index: -1);
	}

	public static Polyline calculatePathAsPolyline(final Edge fromEdge, final Edge toEdge, final Graph graph,
			final PathFindingStrategy pathFinder){
		if(fromEdge.equals(toEdge))
			return fromEdge.getPath();

		//NOTE: not the closest but `fromEdge.getTo()/toEdge.getFrom()`, in order to avoid wrong connection from `fromEdge.getFrom()` to
		// `currentNode` that does not pass through `fromEdge.getTo()` and `toEdge.getFrom()`
		final Node previousNode = fromEdge.getTo();
		final Node currentNode = toEdge.getFrom();

		final Polyline polylineFromTo;
		if(previousNode.equals(currentNode))
			polylineFromTo = fromEdge.getPath()
				.append(toEdge.getPath());
		else{
			final Edge[] pathFromTo = pathFinder.findPath(previousNode, currentNode, graph);
			final GeometryFactory factory = graph.getFactory();
			final List<Polyline> polylines = extractEdgesAsPolyline(pathFromTo, factory);
			if(polylines == null || polylines.isEmpty())
				polylineFromTo = factory.createEmptyPolyline();
			else
				//prepend previousNode path start, append currentNode to path end
				polylineFromTo = fromEdge.getPath()
					.append(polylines.get(0))
					.append(toEdge.getPath());
		}
		return polylineFromTo;
	}

	public static List<Polyline> extractEdgesAsPolyline(final Edge[] connectedPath, final GeometryFactory factory){
		if(connectedPath == null)
			return null;

		final List<Polyline> result = new ArrayList<>(0);

		final List<Point> mergedPoints = new ArrayList<>();
		for(int i = 0; i < connectedPath.length; i ++){
			final Edge fromEdge = connectedPath[i];
			if(fromEdge == null){
				//close previous polyline
				result.add(factory.createPolyline(mergedPoints.toArray(Point[]::new)));

				//open a new polyline
				mergedPoints.clear();
				continue;
			}

			//FIXME pyramid of doom
			if(i < connectedPath.length - 1){
				final Edge toEdge = connectedPath[i + 1];
				if(toEdge != null && fromEdge.isOffRoad() && !toEdge.isOffRoad() && toEdge.equals(fromEdge.getToProjected())){
					mergedPoints.addAll(Arrays.asList(fromEdge.getPath().getPoints()));

					final Point[][] cut = toEdge.getPath().cutHard(fromEdge.getTo().getPoint());
					connectedPath[i + 1] = Edge.createDirectEdge(toEdge.getFrom(), Node.of(null, cut[1][cut[1].length - 1]), factory.createPolyline(cut[1]));
				}
				else if(toEdge != null && !fromEdge.isOffRoad() && toEdge.isOffRoad() && fromEdge.equals(toEdge.getFromProjected())){
					final Point[][] cut = fromEdge.getPath().cutHard(toEdge.getTo().getPoint());
					mergedPoints.addAll(Arrays.asList(cut[0]));
				}
				else /*if(fromEdge.isOffRoad() && toEdge.isOffRoad() && fromEdge.getTo().equals(toEdge.getFrom()))*/
					mergedPoints.addAll(Arrays.asList(fromEdge.getPath().getPoints()));
			}
			else
				mergedPoints.addAll(Arrays.asList(fromEdge.getPath().getPoints()));
		}

		if(!mergedPoints.isEmpty())
			result.add(factory.createPolyline(mergedPoints.toArray(Point[]::new)));

		return result;
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
	public static Collection<Polyline> extractObservedEdges(final HPRTree<Polyline> tree, final Point[] observations,
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

	public static GPSPoint[] extractObservations(final HPRTree<Polyline> tree, final GPSPoint[] observations, final double threshold){
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


	//create id as `<edge>(.<leg>)?
	public static Graph extractDirectGraph(final Collection<Polyline> edges, final double threshold){
		final NearNodeMergeGraph graph = new NearNodeMergeGraph(threshold)
			.withTree();
		int e = 0;
		for(final Polyline edge : edges){
			final String id = String.valueOf(e);
			if(graph.addApproximateDirectEdge(id, edge).isEmpty())
				LOGGER.error("Cannot insert edge {}, may be reversed", edge);

			e ++;
		}
		return graph;
	}

	//create id as `<edge>(.<leg>)?(-rev)?`
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
