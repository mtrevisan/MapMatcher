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
import io.github.mtrevisan.mapmatcher.helpers.WGS84GeometryHelper;
import io.github.mtrevisan.mapmatcher.mapmatching.MapMatchingStrategy;
import io.github.mtrevisan.mapmatcher.mapmatching.ViterbiMapMatching;
import io.github.mtrevisan.mapmatcher.pathfinding.PathSummary;
import io.github.mtrevisan.mapmatcher.weight.LogMapEdgeWeightCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * https://github.com/Nalhin/Navigation/blob/main/backend/libraries/pathfinder/
 *
 * https://github.com/navjindervirdee/Advanced-Shortest-Paths-Algorithms/tree/master/A-Star/A%20star
 * https://github.com/coderodde/GraphSearchPal/blob/master/src/main/java/net/coderodde/gsp/model/support/AStarPathFinder.java
 * https://github.com/valhalla/valhalla/blob/master/docs/meili/algorithms.md
 */
public class Application{

	private static final double SIGMA_OBSERVATION = 4.07;
	private static final double BETA = 3.;


	public static void main(final String[] args){
		final LogMapEdgeWeightCalculator calculator = new LogMapEdgeWeightCalculator();
//		final MapMatchingStrategy strategy = new AStarMapMatching(calculator);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(calculator);
//		final PathfindingStrategy strategy = new BidirectionalDijkstraPathfinder(calculator);
		final Vertex start = new Vertex("START", new Coordinate(12.11, 45.66));
		final Vertex end = new Vertex("END", new Coordinate(12.41, 45.66));

		final Coordinate vertex11 = new Coordinate(12.159747628109386, 45.66132709541773);
		final Coordinate vertex12_31_41 = new Coordinate(12.238140517207398, 45.65897415921759);
		final Coordinate vertex22 = new Coordinate(12.242949896905884, 45.69828882177029);
		final Coordinate vertex23 = new Coordinate(12.200627355552967, 45.732876303059044);
		final Coordinate vertex32_51_61 = new Coordinate(12.343946870589775, 45.65931029901404);
		final Coordinate vertex42 = new Coordinate(12.25545428412434, 45.61054896081151);
		final Coordinate vertex52 = new Coordinate(12.297776825477285, 45.7345547621876);
		final Coordinate vertex62 = new Coordinate(12.322785599913317, 45.610885391198394);

		final LineString edge1 = WGS84GeometryHelper.createLineString(new Coordinate[]{vertex11, vertex12_31_41});
		final LineString edge2 = WGS84GeometryHelper.createLineString(new Coordinate[]{vertex12_31_41, vertex22, vertex23});
		final LineString edge3 = WGS84GeometryHelper.createLineString(new Coordinate[]{vertex12_31_41, vertex32_51_61});
		final LineString edge4 = WGS84GeometryHelper.createLineString(new Coordinate[]{vertex12_31_41, vertex42});
		final LineString edge5 = WGS84GeometryHelper.createLineString(new Coordinate[]{vertex32_51_61, vertex52});
		final LineString edge6 = WGS84GeometryHelper.createLineString(new Coordinate[]{vertex32_51_61, vertex62});

		final Coordinates[] observations1 = new Coordinates[]{
			Coordinates.of(45.64824627395467, 12.142791962642718),
			Coordinates.of(45.658700732309484, 12.166829013921557),
			Coordinates.of(45.663553924585955, 12.190331908504874),
			Coordinates.of(45.65720735774349, 12.219176370039179),
			Coordinates.of(45.65310037232308, 12.237871854367),
			Coordinates.of(45.675125223889154, 12.243213421318018),
			Coordinates.of(45.691544896329816, 12.23894016775725),
			Coordinates.of(45.70684070823364, 12.237337697671506),
			Coordinates.of(45.725861366408196, 12.23306444411162),
			Coordinates.of(45.731454445518864, 12.215971429868546)
		};
		final Coordinates[] observations2 = new Coordinates[]{
			Coordinates.of(45.59108565830172, 12.172704737567187),
			Coordinates.of(45.627705048963094, 12.229859503941071),
			Coordinates.of(45.6422714215264, 12.241610951232218),
			Coordinates.of(45.65646065552491, 12.243213421318018),
			Coordinates.of(45.662060679461206, 12.272057882852266),
			Coordinates.of(45.66168736195718, 12.304641441251732),
			Coordinates.of(45.66168736195718, 12.331349276005653)
		};
		final Coordinates[] observations = observations1;

		final LineString[] edges = new LineString[]{edge1, edge2, edge3, edge4, edge5, edge6};
		//[m]
		final double radius = 2_000.;
		final Graph graph = extractGraph(edges, observations, radius, start, end);

		final PathSummary pathSummary = strategy.findPath(start, end, graph, observations);
		final List<Vertex> path = pathSummary.simplePath();
		if(path.size() > 2){
			path.remove(0);
			path.remove(path.size() - 1);
		}

		System.out.println(path);
	}

	private static Graph extractGraph(final LineString[] edges, final Coordinates[] observations, final double radius,
			final Vertex start, final Vertex end){
		final Set<LineString> observedEdges = extractObservedEdges(edges, observations, radius);

		//construct topology
		final Map<Coordinate, Set<Integer>> topology = new HashMap<>();
		//FIXME to uncomment
//		for(int i = 0; i < observedEdges.length; i ++){
		for(int i = 0; i < edges.length; i ++){
			final LineString edge = edges[i];
			final Coordinate startCoordinate = edge.getCoordinateN(0);
			final Coordinate endCoordinate = edge.getCoordinateN(edge.getNumPoints() - 1);
			topology.computeIfAbsent(startCoordinate, k -> new HashSet<>(1))
				.add(i);
			topology.computeIfAbsent(endCoordinate, k -> new HashSet<>(1))
				.add(i);
		}
		//remove unconnected vertices
		topology.entrySet()
			.removeIf(entry -> entry.getValue().size() == 1);

		//add vertices
		final GraphBuilder graphBuilder = new GraphBuilder();
//		graphBuilder.addVertex(start)
//			.addVertex(end);
		//FIXME to uncomment
//		for(int i = 0; i < observedEdges.length; i ++){
		for(int i = 0; i < edges.length; i ++){
			final String edgeID = "E" + i;
			graphBuilder.addVertex(new Vertex(edgeID, edges[i]));
		}

		//add connections:
		for(final Set<Integer> values : topology.values()){
			for(final Integer initialVertex : values){
				//FIXME upon uncomment, adjust this code to retrieve the correct ID
				final String initialVertexID = "E" + initialVertex;
				for(final Integer finalVertex : values){
					//FIXME upon uncomment, adjust this code to retrieve the correct ID
					final String finalVertexID = "E" + finalVertex;
					graphBuilder.connectByIds(initialVertexID, finalVertexID);
				}
			}
		}

		return graphBuilder
			.asGraph();
	}

	private static Set<LineString> extractObservedEdges(final LineString[] edges, final Coordinates[] observations,
			final double radius){
		final Set<LineString> observationsEdges = new HashSet<>(edges.length);
		for(final Coordinates observation : observations){
			final Polygon surrounding = WGS84GeometryHelper.createCircle(observation.getPoint().getCoordinate(), radius);
			for(final LineString edge : edges)
				if(surrounding.intersects(edge))
					observationsEdges.add(edge);
		}
		return observationsEdges;
	}

}
