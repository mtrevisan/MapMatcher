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
package io.github.mtrevisan.mapmatcher;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.helpers.PathHelper;
import io.github.mtrevisan.mapmatcher.helpers.hprtree.HilbertPackedRTree;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;
import io.github.mtrevisan.mapmatcher.mapmatching.MapMatchingStrategy;
import io.github.mtrevisan.mapmatcher.mapmatching.ViterbiMapMatching;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission.EmissionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission.GaussianEmissionCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.UniformInitialCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.ConnectedGraphTransitionPlugin;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.DirectionTransitionPlugin;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.TransitionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.exceptions.NoGraphException;
import io.github.mtrevisan.mapmatcher.mapmatching.exceptions.NoObservationsException;
import io.github.mtrevisan.mapmatcher.pathfinding.AStarPathFinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.DistanceCalculator;
import io.github.mtrevisan.mapmatcher.spatial.GPSPoint;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.simplification.RamerDouglasPeuckerSimplifier;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


/**
 * https://github.com/Nalhin/Navigation/blob/main/backend/libraries/pathfinder/
 *
 * https://github.com/coderodde/GraphSearchPal/blob/master/src/main/java/net/coderodde/gsp/model/support/AStarPathFinder.java
 * https://github.com/valhalla/valhalla/blob/master/docs/meili/algorithms.md
 */
public class Application{

	public static void main(final String[] args) throws NoObservationsException, NoGraphException{
		final GeoidalCalculator topologyCalculator = new GeoidalCalculator();
		final GeometryFactory factory = new GeometryFactory(topologyCalculator);
		final Point node11 = factory.createPoint(12.159747628109386, 45.66132709541773);
		final Point node12_31_41 = factory.createPoint(12.238140517207398, 45.65897415921759);
		final Point node22 = factory.createPoint(12.242949896905884, 45.69828882177029);
		final Point node23 = factory.createPoint(12.200627355552967, 45.732876303059044);
		final Point node32_51_61 = factory.createPoint(12.343946870589775, 45.65931029901404);
		final Point node42 = factory.createPoint(12.25545428412434, 45.61054896081151);
		final Point node52 = factory.createPoint(12.297776825477285, 45.7345547621876);
		final Point node62 = factory.createPoint(12.322785599913317, 45.610885391198394);

		//[m]
		final double distanceTolerance = 10.;
		final RamerDouglasPeuckerSimplifier simplifier = new RamerDouglasPeuckerSimplifier();
		simplifier.setDistanceTolerance(distanceTolerance);
		final Polyline edge0 = factory.createPolyline(simplifier.simplify(node11, node12_31_41));
		final Polyline edge1 = factory.createPolyline(simplifier.simplify(node12_31_41, node22, node23));
		final Polyline edge2 = factory.createPolyline(simplifier.simplify(node12_31_41, node32_51_61));
		final Polyline edge3 = factory.createPolyline(simplifier.simplify(node12_31_41, node42));
		final Polyline edge4 = factory.createPolyline(simplifier.simplify(node32_51_61, node52));
		final Polyline edge5 = factory.createPolyline(simplifier.simplify(node32_51_61, node62));
		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final HilbertPackedRTree<Polyline> tree = new HilbertPackedRTree<>();
		for(final Polyline edge : edges){
			final Region geoBoundingBox = edge.getBoundingBox();
			tree.insert(geoBoundingBox, edge);
		}

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSPoint[] observations1 = new GPSPoint[]{
			GPSPoint.of(12.142791962642718, 45.64824627395467, timestamp),
			GPSPoint.of(12.166829013921557, 45.658700732309484, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.190331908504874, 45.663553924585955, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.219176370039179, 45.65720735774349, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.237871854367, 45.65310037232308, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.243213421318018, 45.675125223889154, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.23894016775725, 45.691544896329816, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.237337697671506, 45.70684070823364, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.23306444411162, 45.725861366408196, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.215971429868546, 45.731454445518864, (timestamp = advanceTime(timestamp, 60)))
		};
		final GPSPoint[] observations2 = new GPSPoint[]{
			GPSPoint.of(12.172704737567187, 45.59108565830172, timestamp),
			GPSPoint.of(12.229859503941071, 45.627705048963094, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.241610951232218, 45.6422714215264, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.243213421318018, 45.65646065552491, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.272057882852266, 45.662060679461206, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.304641441251732, 45.66168736195718, (timestamp = advanceTime(timestamp, 60))),
			GPSPoint.of(12.331349276005653, 45.66168736195718, (timestamp = advanceTime(timestamp, 60)))
		};
		final GPSPoint[] observations = observations1;

		final Collection<Polyline> observedEdges = PathHelper.extractObservedEdges(tree, observations, 1_000.);
		final Graph graph = PathHelper.extractBidirectionalGraph(observedEdges, 5.);

		final GPSPoint[] filteredObservations = PathHelper.extractObservations(tree, observations, 900.);
		//NOTE: the initial probability is a uniform distribution reflecting the fact that there is no known bias about which is the
		// correct edge
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new ConnectedGraphTransitionPlugin())
			.withPlugin(new DirectionTransitionPlugin());
//		final TransitionProbabilityCalculator transitionCalculator = new LogExponentialTransitionCalculator(200.);
		final EmissionProbabilityCalculator emissionCalculator = new GaussianEmissionCalculator(5.);
		final DistanceCalculator distanceCalculator = new DistanceCalculator(topologyCalculator);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			distanceCalculator);
//		final MapMatchingStrategy strategy = new AStarMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
//			distanceCalculator);

System.out.println("graph & observations: " + graph.toStringWithObservations(filteredObservations));
		final Collection<Map.Entry<Double, Edge[]>> paths = strategy.findPath(graph, filteredObservations, 400.);
		System.out.println("paths: " + paths.size());
		final Edge[] path = paths.iterator().next().getValue();
if(path != null){
	System.out.println("true: [null, 0, 0, 0, 0, 1, 1, 1, null, 1]");
	System.out.println("path: " + Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));
}
else
	System.out.println("path is NULL");

		final PathFindingStrategy pathFinder = new AStarPathFinder(distanceCalculator);
		final Edge[] connectedPath = PathHelper.connectPath(path, graph, pathFinder);
if(connectedPath.length > 0)
	System.out.println("connected path: " + Arrays.toString(Arrays.stream(connectedPath).map(e -> (e != null? e.getID(): null)).toArray()));

		final List<Polyline> pathPolylines = PathHelper.extractEdgesAsPolyline(connectedPath, factory);
if(!pathPolylines.isEmpty()){
	final StringJoiner sj = new StringJoiner(", ", "GEOMETRYCOLLECTION (", ")");
	for(final Polyline pathPolyline : pathPolylines)
		sj.add(pathPolyline.toString());
	for(final GPSPoint point : filteredObservations)
		if(point != null)
			sj.add(point.toString());
	System.out.println("path polyline: " + sj);
}

final double averagePositionError = PathHelper.averagePositionError(path, filteredObservations);
System.out.println("average position error: " + averagePositionError);
System.out.println("average position standard deviation: " + PathHelper.averagePositionStandardDeviation(path, filteredObservations, averagePositionError));

		//first-order to second-order HMM modifications (O(n^w), where w is the window size):
		//The observation probability of the second-order HMM `P(g_t−1, g_t | c^i_t−1, c^j_t)` can be obtained from the first-order
		//HMM: `P(g_t−1, g_t | c^i_t−1, c^j) = P(c^j_t | c^i_t−1) · P(g_t−1 | c^i_t−1) · P(g_t | c^j_t)`
		//The state transition probability `P(c^i_t | c^j_t-2, c^k_t-1) = β · e^(-k_t · β)`, where `β = 1/λ`, and λ is the mean of k_t, and
		//	k_t is the difference between the great-circle distance from g_t-1 to g_t+1 and the route length from c^i_t-1 to c^j_t+1:
		//	k_t = |sum(n=t-2..t-1, dist(g^i_n, g^j_n+1)) - sum(n=t-2..t-1, routeDist(c^i_n, c^j_n+1)) |
		//P(g_t | c^i_t) = 1/(sqrt(2 * pi) * sigma_t) * e^(-0.5 * tau * rho * dist(g_t, c^i_t) / sigma_t) is the observation probability,
		// sigma_t is the standard deviation of a gaussian random variable that corresponds to the average great-circle distance between g_t
		//	and its candidate points, tau is a weight given on vehicle heading = nu + e^|alpha_road - alpha_gps| / e^(2/pi), rho is a weight
		//	reflecting the effect of road including road level and driver's travel preference
	}

	//	public static void main(final String[] args){
//		final double observationStandardDeviation = 5.;
//		final LogMapMatchingProbabilityCalculator probabilityCalculator = new LogMapMatchingProbabilityCalculator(observationStandardDeviation);
//		final MapMatchingStrategy strategy = new ViterbiMapMatching(probabilityCalculator);
//
//		final GPSPoint[] observations1 = new GPSPoint[]{
//			new GPSPoint(12.142791962642718, 45.64824627395467),
//			new GPSPoint(12.166829013921557, 45.658700732309484),
//			new GPSPoint(12.190331908504874, 45.663553924585955),
//			new GPSPoint(12.219176370039179, 45.65720735774349),
//			new GPSPoint(12.237871854367, 45.65310037232308),
//			new GPSPoint(12.243213421318018, 45.675125223889154),
//			new GPSPoint(12.23894016775725, 45.691544896329816),
//			new GPSPoint(12.237337697671506, 45.70684070823364),
//			new GPSPoint(12.23306444411162, 45.725861366408196),
//			new GPSPoint(12.215971429868546, 45.731454445518864)
//		};
//		final GPSPoint[] observations2 = new GPSPoint[]{
//			new GPSPoint(12.172704737567187, 45.59108565830172),
//			new GPSPoint(12.229859503941071, 45.627705048963094),
//			new GPSPoint(12.241610951232218, 45.6422714215264),
//			new GPSPoint(12.243213421318018, 45.65646065552491),
//			new GPSPoint(12.272057882852266, 45.662060679461206),
//			new GPSPoint(12.304641441251732, 45.66168736195718),
//			new GPSPoint(12.331349276005653, 45.66168736195718)
//		};
//		final GPSPoint[] observations = observations2;
//
//		final LineString[] edges = readEdges();
//		//all italy
//		final Collection<LineString> observedEdges = extractObservedEdges(edges, observations, 1_000_000.);
//		final Graph graph = extractGraph(observedEdges, 1_000.);
//
//		final Edge[] path = strategy.findPath(graph, observations);
//
//		if(path != null)
//			System.out.println(Arrays.toString(Arrays.stream(path).map(Edge::getID).toArray()));
//	}


	private static ZonedDateTime advanceTime(final ZonedDateTime timestamp, final int amountToAdd){
		return timestamp.plus(amountToAdd, ChronoUnit.SECONDS);
	}

}
