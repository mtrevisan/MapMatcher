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
import io.github.mtrevisan.mapmatcher.helpers.hprtree.HPRtree;
import io.github.mtrevisan.mapmatcher.mapmatching.MapMatchingStrategy;
import io.github.mtrevisan.mapmatcher.mapmatching.ViterbiMapMatching;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission.BayesianEmissionCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission.EmissionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.UniformInitialCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.DirectionTransitionPlugin;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.NoUTurnTransitionPlugin;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.ShortestPathTransitionPlugin;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.TopologicalTransitionPlugin;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.TransitionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.GeodeticDistanceCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Envelope;
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


/**
 * https://github.com/Nalhin/Navigation/blob/main/backend/libraries/pathfinder/
 *
 * https://github.com/coderodde/GraphSearchPal/blob/master/src/main/java/net/coderodde/gsp/model/support/AStarPathFinder.java
 * https://github.com/valhalla/valhalla/blob/master/docs/meili/algorithms.md
 */
public class Application{

	public static void main(final String[] args){
		final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
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
		final Polyline edge1 = factory.createPolyline(simplifier.simplify(node12_31_41, node22));
		final Polyline edge1bis = factory.createPolyline(simplifier.simplify(node22, node23));
		final Polyline edge2 = factory.createPolyline(simplifier.simplify(node12_31_41, node32_51_61));
		final Polyline edge3 = factory.createPolyline(simplifier.simplify(node12_31_41, node42));
		final Polyline edge4 = factory.createPolyline(simplifier.simplify(node32_51_61, node52));
		final Polyline edge5 = factory.createPolyline(simplifier.simplify(node32_51_61, node62));
		final Polyline[] polylines = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5, edge1bis};
		final HPRtree<Polyline> tree = new HPRtree<>();
		for(final Polyline polyline : polylines){
			final Envelope geoBoundingBox = polyline.getBoundingBox();
			tree.insert(geoBoundingBox, polyline);
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
		final Graph graph = PathHelper.extractBidirectionalGraph(observedEdges, 500.);

		final GPSPoint[] filteredObservations = PathHelper.extractObservations(tree, observations, 390.);
		//NOTE: the initial probability is a uniform distribution reflecting the fact that there is no known bias about which is the
		// correct segment
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
//			.withPlugin(new ShortestPathTransitionPlugin(3.))
			.withPlugin(new TopologicalTransitionPlugin())
//			.withPlugin(new NoUTurnTransitionPlugin())
//			.withPlugin(new DirectionTransitionPlugin())
			;
//		final TransitionProbabilityCalculator transitionCalculator = new LogExponentialTransitionCalculator(200.)
//			.withPlugin(new NoUTurnPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new BayesianEmissionCalculator();
		final GeodeticDistanceCalculator distanceCalculator = new GeodeticDistanceCalculator();
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			distanceCalculator);
//		final MapMatchingStrategy strategy = new AStarMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
//			distanceCalculator);
System.out.println("graph & observations: " + graph.toStringWithObservations(filteredObservations));
		final Edge[] path = strategy.findPath(graph, filteredObservations, 400.);
if(path != null)
	System.out.println("path: " + Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));

		final Edge[] connectedPath = PathHelper.connectPath(path, graph, distanceCalculator);
if(connectedPath.length > 0)
	System.out.println("connected path: " + Arrays.toString(Arrays.stream(connectedPath).map(e -> (e != null? e.getID(): null)).toArray()));

		final Polyline pathPolyline = PathHelper.extractPathAsPolyline(connectedPath, observations[0], observations[observations.length - 1]);
if(pathPolyline != null)
	System.out.println("path polyline: " + pathPolyline);
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
