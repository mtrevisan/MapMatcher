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
import io.github.mtrevisan.mapmatcher.pathfinding.BidirectionalDijkstraPathfinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathSummary;
import io.github.mtrevisan.mapmatcher.pathfinding.PathfindingStrategy;
import io.github.mtrevisan.mapmatcher.weight.DistanceEdgeWeightCalculator;
import io.github.mtrevisan.mapmatcher.weight.EdgeWeightCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.util.GeometricShapeFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * https://github.com/Nalhin/Navigation/blob/main/backend/libraries/pathfinder/
 *
 * https://github.com/navjindervirdee/Advanced-Shortest-Paths-Algorithms/tree/master/A-Star/A%20star
 * https://github.com/coderodde/GraphSearchPal/blob/master/src/main/java/net/coderodde/gsp/model/support/AStarPathFinder.java
 * https://github.com/valhalla/valhalla/blob/master/docs/meili/algorithms.md
 */
public class Application{

	private static final PrecisionModel PRECISION_MODEL = new PrecisionModel(PrecisionModel.FLOATING);
	private static final int SRID_WGS84 = 4326;
	private static final GeometryFactory FACTORY = new GeometryFactory(PRECISION_MODEL, SRID_WGS84);

	private static final double SIGMA_OBSERVATION = 4.07;
	private static final double BETA = 3.;


	public static void main(final String[] args){
//		final EdgeWeightCalculator calculator = new VertexCountEdgeWeightCalculator();
		final EdgeWeightCalculator calculator = new DistanceEdgeWeightCalculator();
//		final PathfindingStrategy strategy = new AStarPathfinder(calculator);
		final PathfindingStrategy strategy = new BidirectionalDijkstraPathfinder(calculator);
		final Vertex start = new Vertex("START", FACTORY.createPoint(new Coordinate(12.11, 45.66)));
		final Vertex end = new Vertex("END", FACTORY.createPoint(new Coordinate(12.41, 45.66)));

		final Coordinate vertex11 = new Coordinate(12.159747628109386, 45.66132709541773);
		final Coordinate vertex12_31_41 = new Coordinate(12.238140517207398, 45.65897415921759);
		final Coordinate vertex22 = new Coordinate(12.242949896905884, 45.69828882177029);
		final Coordinate vertex23 = new Coordinate(12.200627355552967, 45.732876303059044);
		final Coordinate vertex32_51_61 = new Coordinate(12.343946870589775, 45.65931029901404);
		final Coordinate vertex42 = new Coordinate(12.25545428412434, 45.61054896081151);
		final Coordinate vertex52 = new Coordinate(12.297776825477285, 45.7345547621876);
		final Coordinate vertex62 = new Coordinate(12.322785599913317, 45.610885391198394);

		final LineString edge1 = FACTORY.createLineString(new Coordinate[]{vertex11, vertex12_31_41});
		final LineString edge2 = FACTORY.createLineString(new Coordinate[]{vertex12_31_41, vertex22, vertex23});
		final LineString edge3 = FACTORY.createLineString(new Coordinate[]{vertex12_31_41, vertex32_51_61});
		final LineString edge4 = FACTORY.createLineString(new Coordinate[]{vertex12_31_41, vertex42});
		final LineString edge5 = FACTORY.createLineString(new Coordinate[]{vertex32_51_61, vertex52});
		final LineString edge6 = FACTORY.createLineString(new Coordinate[]{vertex32_51_61, vertex62});

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
//		final double radius = 1_000.;
		final double radius = 200_000_000.;
		final Graph graph = extractGraph(edges, observations, radius, start, end);

		final PathSummary pathSummary = strategy.findPath(start, end, graph);
		final List<Vertex> path = pathSummary.simplePath();
		path.remove(0);
		path.remove(path.size() - 1);

		System.out.println(path);
	}

	private static Graph extractGraph(final LineString[] edges, final Coordinates[] observations, final double radius,
			final Vertex start, final Vertex end){
		final List<List<LineString>> observedEdges = extractObservedEdges(edges, observations, radius);

		//add vertices:
		final GraphBuilder graphBuilder = new GraphBuilder()
			.addVertex(start)
			.addVertex(end);
		observedEdges.stream()
			.flatMap(List::stream)
			.distinct()
			.forEach(edge -> {
				final String id = Integer.toString(edge.hashCode());
				graphBuilder.addVertex(new Vertex(id, edge));
			});

		//add start->inner state connections
		final String startStateID = start.getId();
		for(final Geometry geometry : observedEdges.get(0)){
			final String currentStateID = Integer.toString(geometry.hashCode());
			graphBuilder.connectByIds(startStateID, currentStateID, 1.);
		}
		//add inner state->end connections
		final String endStateID = end.getId();
		for(final Geometry geometry : observedEdges.get(observedEdges.size() - 1)){
			final String currentStateID = Integer.toString(geometry.hashCode());
			graphBuilder.connectByIds(currentStateID, endStateID, 1.);
		}

		//calculate inner weights
		for(int n = 1; n < observations.length - 1; n ++){
			final Coordinates observation = observations[n];
			for(final Geometry currentGeometry : observedEdges.get(n)){
				final double nodeCost = nodeCost(observation.getPoint(), currentGeometry);
				final String currentStateID = Integer.toString(currentGeometry.hashCode());
				for(final Geometry previousGeometry : observedEdges.get(n - 1)){
					final String previousStateID = Integer.toString(previousGeometry.hashCode());
					final double edgeCost = edgeCost(previousGeometry, currentGeometry);
					graphBuilder.connectByIds(previousStateID, currentStateID, nodeCost + edgeCost);
				}
			}
			n ++;
		}

		return graphBuilder
			.asGraph();
	}

	private static List<List<LineString>> extractObservedEdges(final LineString[] edges, final Coordinates[] observations,
			final double radius){
		final List<List<LineString>> observationsEdges = new ArrayList<>(observations.length);
		for(final Coordinates observation : observations){
			final Polygon surrounding = createSurrounding(observation.getPoint().getCoordinate(), radius);

			final List<LineString> observationEdges = new ArrayList<>(edges.length);
			for(final LineString edge : edges)
				if(surrounding.intersects(edge))
					observationEdges.add(edge);
			observationsEdges.add(observationEdges);
		}
		return observationsEdges;
	}

	private static Polygon createSurrounding(final Coordinate origin, final double radius){
		final double lat = Math.toRadians(origin.getY());
		//precision is within 1 cm [m/°]
		final double metersPerDegreeInLatitude = 111_132.954 - 559.822 * StrictMath.cos(2. * lat)
			+ 1.175 * StrictMath.cos(4. * lat);
		final double metersPerDegreesInLongitude = 111_132.954 * StrictMath.cos(lat);

		final GeometricShapeFactory gsf = new GeometricShapeFactory(FACTORY);
		gsf.setWidth(radius * 2. / metersPerDegreesInLongitude);
		gsf.setHeight(radius * 2. / metersPerDegreeInLatitude);
		gsf.setCentre(origin);
		return gsf.createEllipse();
	}

	private static final class MapMatchingState{
		Geometry geometry;
		double probability;

		static MapMatchingState of(final Geometry geometry, final double probability){
			return new MapMatchingState(geometry, probability);
		}

		private MapMatchingState(final Geometry geometry, final double probability){
			this.geometry = geometry;
			this.probability = probability;
		}

		void addProbability(final double probability){
			this.probability += probability;
		}
	}

	private static double nodeCost(final Point observation, final Geometry segment){
		return -StrictMath.log(emissionProbability(observation, segment));
	}

	private static double edgeCost(final Geometry segment1, final Geometry segment2){
		return -StrictMath.log(transitionProbability(segment1, segment2));
	}

	//A zero-mean gaussian observation error, Pr(o_i | r_i) = 1/(√(2 ⋅ π) ⋅ σ_o) ⋅ exp(-0.5 ⋅ (δ(o_i, x_i) / σ_o)^2)
	private static double emissionProbability(final Point observation, final Geometry segment){
		final double c = 1. / (StrictMath.sqrt(2. * Math.PI) * SIGMA_OBSERVATION);
		final double reducedDistance = observation.distance(segment) / SIGMA_OBSERVATION;
		return c * StrictMath.exp(-0.5 * reducedDistance * reducedDistance);
	}

	//exponential function of the difference between the route length (in degrees!) and the great circle distance (in degrees!)
	//between o_t and o_t+1, Pr(r_i | r_i-1) = β ⋅ exp(-β ⋅* |δ(o_i-1, o_i) - σ(x_i-1, x_i)|)
	/*
	Pr(r_i | r_i-1) = 1/(2 ⋅ π ⋅ σ_p) * exp(-0.5 ⋅ (||p_t - x_t_i||great_circle / σ_p)^2) where x_t_i is the point on road segment r_i
	nearest the measurement p_t at time t, and σ_p can be thought of as an estimate of the standard deviation of GPS noise
	(Newson and Krumm (2009) derive σ_p from the median absolute deviation over their dataset, arriving at a value of 4.07)

	p(d_t) = 1/β ⋅ exp(-d_t / β) where d_t is the difference between the great circle distance and route-traveled distance between time t
	and t+1
	*/
	private static double transitionProbability(final Geometry segment1, final Geometry segment2){
		//calculating route distance is expensive (https://github.com/valhalla/valhalla/blob/master/docs/meili/algorithms.md)
//		final double delta = Math.abs(routeLength(segment1, segment2) - segment1.distance(segment2));
final double delta = segment1.distance(segment2);
		return BETA * StrictMath.exp(-BETA * delta);
	}

}
