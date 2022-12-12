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
import io.github.mtrevisan.mapmatcher.graph.NearLineMergeGraph;
import io.github.mtrevisan.mapmatcher.helpers.PathHelper;
import io.github.mtrevisan.mapmatcher.helpers.filters.GPSPositionSpeedFilter;
import io.github.mtrevisan.mapmatcher.helpers.hprtree.HPRtree;
import io.github.mtrevisan.mapmatcher.mapmatching.MapMatchingStrategy;
import io.github.mtrevisan.mapmatcher.mapmatching.ViterbiMapMatching;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission.EmissionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission.LogBayesianEmissionCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.UniformInitialCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.plugins.DirectionPlugin;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.plugins.NoUTurnPlugin;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.TopologicalTransitionCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.TransitionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Envelope;
import io.github.mtrevisan.mapmatcher.spatial.GPSPoint;
import io.github.mtrevisan.mapmatcher.spatial.GeodeticHelper;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.simplification.RamerDouglasPeuckerSimplifier;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * https://github.com/Nalhin/Navigation/blob/main/backend/libraries/pathfinder/
 *
 * https://github.com/coderodde/GraphSearchPal/blob/master/src/main/java/net/coderodde/gsp/model/support/AStarPathFinder.java
 * https://github.com/valhalla/valhalla/blob/master/docs/meili/algorithms.md
 */
public class Application{

	public static void main(final String[] args){
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TopologicalTransitionCalculator()
			.withPlugin(new NoUTurnPlugin())
			.withPlugin(new DirectionPlugin());
//		final TransitionProbabilityCalculator transitionCalculator = new LogExponentialTransitionCalculator(200.)
//			.withPlugin(new NoUTurnPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new LogBayesianEmissionCalculator();
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator);
//		final MapMatchingStrategy strategy = new AStarMapMatching(initialCalculator, transitionCalculator, probabilityCalculator);

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
		final Polyline edge1 = factory.createPolyline(simplifier.simplify(node12_31_41, node22, node23));
		final Polyline edge2 = factory.createPolyline(simplifier.simplify(node12_31_41, node32_51_61));
		final Polyline edge3 = factory.createPolyline(simplifier.simplify(node12_31_41, node42));
		final Polyline edge4 = factory.createPolyline(simplifier.simplify(node32_51_61, node52));
		final Polyline edge5 = factory.createPolyline(simplifier.simplify(node32_51_61, node62));
		final Polyline[] polylines = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final HPRtree<Polyline> tree = new HPRtree<>();
		for(final Polyline polyline : polylines){
			final Envelope geoBoundingBox = polyline.getBoundingBox();
			tree.insert(geoBoundingBox, polyline);
		}

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSPoint[] observations1 = new GPSPoint[]{
			GPSPoint.of(12.142791962642718, 45.64824627395467, timestamp),
			GPSPoint.of(12.166829013921557, 45.658700732309484, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.190331908504874, 45.663553924585955, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.219176370039179, 45.65720735774349, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.237871854367, 45.65310037232308, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.243213421318018, 45.675125223889154, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.23894016775725, 45.691544896329816, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.237337697671506, 45.70684070823364, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.23306444411162, 45.725861366408196, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.215971429868546, 45.731454445518864, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS)))
		};
		final GPSPoint[] observations2 = new GPSPoint[]{
			GPSPoint.of(12.172704737567187, 45.59108565830172, timestamp),
			GPSPoint.of(12.229859503941071, 45.627705048963094, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.241610951232218, 45.6422714215264, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.243213421318018, 45.65646065552491, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.272057882852266, 45.662060679461206, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.304641441251732, 45.66168736195718, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.331349276005653, 45.66168736195718, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS)))
		};
		final GPSPoint[] observations = observations1;

		final Collection<Polyline> observedEdges = extractObservedEdges(tree, observations, 100_000.);
		final Graph graph = extractBidirectionalGraph(observedEdges, 1_000.);

		final Point[] filteredObservations = extractObservations(tree, observations, 400.);
		final Edge[] path = strategy.findPath(graph, filteredObservations);
if(path != null)
	System.out.println("path: " + Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));

		final Edge[] connectedPath = PathHelper.connectPath(path, graph);
if(path != null)
	System.out.println("connected path: " + Arrays.toString(Arrays.stream(connectedPath).map(e -> (e != null? e.getID(): null)).toArray()));

		final Polyline pathPolyline = PathHelper.extractPathAsPolyline(connectedPath, observations[0], observations[observations.length - 1]);
if(path != null)
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
	 * @param threshold	The threshold.
	 * @return	The list of road links whose distance is less than the given radius from each observation.
	 */
	private static Collection<Polyline> extractObservedEdges(final HPRtree<Polyline> tree, final Point[] observations,
			final double threshold){
		final Set<Polyline> observationsEdges = new LinkedHashSet<>(0);
		for(final Point observation : observations)
			observationsEdges.addAll(extractObservedEdges(tree, observation, threshold));
		return observationsEdges;
	}

	private static Collection<Polyline> extractObservedEdges(final HPRtree<Polyline> tree, final Point observation, final double threshold){
		final Point north = GeodeticHelper.destination(observation, 0., threshold);
		final Point east = GeodeticHelper.destination(observation, 90., threshold);
		final Point sud = GeodeticHelper.destination(observation, 180., threshold);
		final Point west = GeodeticHelper.destination(observation, 270., threshold);
		final Envelope envelope = Envelope.ofEmpty();
		envelope.expandToInclude(north, east, sud, west);
		return tree.query(envelope);
	}

	private static Point[] extractObservations(final HPRtree<Polyline> tree, final GPSPoint[] observations, final double threshold){
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
			final Point north = GeodeticHelper.destination(observation, 0., threshold);
			final Point east = GeodeticHelper.destination(observation, 90., threshold);
			final Point sud = GeodeticHelper.destination(observation, 180., threshold);
			final Point west = GeodeticHelper.destination(observation, 270., threshold);
			final Envelope envelope = Envelope.ofEmpty();
			envelope.expandToInclude(north, east, sud, west);
			final List<Polyline> edges = tree.query(envelope);
			if(edges.isEmpty())
				feasibleObservations[i] = null;
		}

		return feasibleObservations;
	}

	private static Graph extractDirectGraph(final Collection<Polyline> edges, final double threshold){
		final NearLineMergeGraph graph = new NearLineMergeGraph(threshold);
		int e = 0;
		for(final Polyline edge : edges){
			graph.addApproximateDirectEdge("E" + e, edge);

			e ++;
		}
		return graph;
	}

	private static Graph extractBidirectionalGraph(final Collection<Polyline> edges, final double threshold){
		final NearLineMergeGraph graph = new NearLineMergeGraph(threshold);
		int e = 0;
		for(final Polyline edge : edges){
			graph.addApproximateDirectEdge("E" + e, edge);
			graph.addApproximateDirectEdge("E" + e + "-rev", edge.reverse());

			e ++;
		}
		return graph;
	}

}
