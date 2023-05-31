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
package io.github.mtrevisan.mapmatcher.mapmatching;

import io.github.mtrevisan.mapmatcher.TestPathHelper;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.helpers.PathHelper;
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
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;


class ViterbiMapMatchingTest{

	@Test
	void should_match_E0_E1_with_bayesian_emission_probability_direct_graph() throws NoObservationsException, NoGraphException{
		final GeoidalCalculator topologyCalculator = new GeoidalCalculator();
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new DirectionTransitionPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new GaussianEmissionCalculator(5.);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			new DistanceCalculator(topologyCalculator));

		final GeometryFactory factory = new GeometryFactory(topologyCalculator);
		final Point node11 = factory.createPoint(12.159747628109386, 45.66132709541773);
		final Point node12_31_41 = factory.createPoint(12.238140517207398, 45.65897415921759);
		final Point node22 = factory.createPoint(12.242949896905884, 45.69828882177029);
		final Point node23 = factory.createPoint(12.200627355552967, 45.732876303059044);
		final Point node32_51_61 = factory.createPoint(12.343946870589775, 45.65931029901404);
		final Point node42 = factory.createPoint(12.25545428412434, 45.61054896081151);
		final Point node52 = factory.createPoint(12.297776825477285, 45.7345547621876);
		final Point node62 = factory.createPoint(12.322785599913317, 45.610885391198394);

		final Polyline edge0 = factory.createPolyline(node11, node12_31_41);
		final Polyline edge1 = factory.createPolyline(node12_31_41, node22, node23);
		final Polyline edge2 = factory.createPolyline(node12_31_41, node32_51_61);
		final Polyline edge3 = factory.createPolyline(node12_31_41, node42);
		final Polyline edge4 = factory.createPolyline(node32_51_61, node52);
		final Polyline edge5 = factory.createPolyline(node32_51_61, node62);

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSPoint[] observations = new GPSPoint[]{
			GPSPoint.of(12.142791962642718, 45.64824627395467, timestamp),
			GPSPoint.of(12.166829013921557, 45.658700732309484, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.190331908504874, 45.663553924585955, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.219176370039179, 45.65720735774349, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.237871854367, 45.65310037232308, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.243213421318018, 45.675125223889154, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.23894016775725, 45.691544896329816, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.237337697671506, 45.70684070823364, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.23306444411162, 45.725861366408196, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.215971429868546, 45.731454445518864, (timestamp = TestPathHelper.advanceTime(timestamp, 60)))
		};

		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<Polyline> observedEdges = TestPathHelper.extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = PathHelper.extractDirectGraph(observedEdges, 50.);

		final Point[] filteredObservations = TestPathHelper.extractObservations(edges, observations, 400.);
		final Collection<Map.Entry<Double, Edge[]>> paths = strategy.findPath(graph, filteredObservations, 6_700.);
		Assertions.assertFalse(paths.isEmpty());

		final Edge[] path = paths.iterator().next().getValue();
		final String expected = "[null, 0, 0, 0, 1, 1, 1, 1, null, null]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));
	}

	@Test
	void should_match_E0_E3_E1_with_gaussian_emission_probability_direct_graph() throws NoObservationsException, NoGraphException{
		final GeoidalCalculator topologyCalculator = new GeoidalCalculator();
		final double observationStandardDeviation = 5.;
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new ConnectedGraphTransitionPlugin())
			.withPlugin(new DirectionTransitionPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new GaussianEmissionCalculator(observationStandardDeviation);
		final DistanceCalculator distanceCalculator = new DistanceCalculator(topologyCalculator);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator, distanceCalculator);

		final GeometryFactory factory = new GeometryFactory(topologyCalculator);
		final Point node11 = factory.createPoint(12.159747628109386, 45.66132709541773);
		final Point node12_31_41 = factory.createPoint(12.238140517207398, 45.65897415921759);
		final Point node22 = factory.createPoint(12.242949896905884, 45.69828882177029);
		final Point node23 = factory.createPoint(12.200627355552967, 45.732876303059044);
		final Point node32_51_61 = factory.createPoint(12.343946870589775, 45.65931029901404);
		final Point node42 = factory.createPoint(12.25545428412434, 45.61054896081151);
		final Point node52 = factory.createPoint(12.297776825477285, 45.7345547621876);
		final Point node62 = factory.createPoint(12.322785599913317, 45.610885391198394);

		final Polyline edge0 = factory.createPolyline(node11, node12_31_41);
		final Polyline edge1 = factory.createPolyline(node12_31_41, node22, node23);
		final Polyline edge2 = factory.createPolyline(node12_31_41, node32_51_61);
		final Polyline edge3 = factory.createPolyline(node12_31_41, node42);
		final Polyline edge4 = factory.createPolyline(node32_51_61, node52);
		final Polyline edge5 = factory.createPolyline(node32_51_61, node62);

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSPoint[] observations = new GPSPoint[]{
			GPSPoint.of(12.142791962642718, 45.64824627395467, timestamp),
			GPSPoint.of(12.166829013921557, 45.658700732309484, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.190331908504874, 45.663553924585955, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.219176370039179, 45.65720735774349, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.237871854367, 45.65310037232308, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.243213421318018, 45.675125223889154, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.23894016775725, 45.691544896329816, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.237337697671506, 45.70684070823364, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.23306444411162, 45.725861366408196, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.215971429868546, 45.731454445518864, (timestamp = TestPathHelper.advanceTime(timestamp, 60)))
		};

		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<Polyline> observedEdges = TestPathHelper.extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = PathHelper.extractBidirectionalGraph(observedEdges, 50.);

		final Point[] filteredObservations = TestPathHelper.extractObservations(edges, observations, 400.);
		final Collection<Map.Entry<Double, Edge[]>> paths = strategy.findPath(graph, filteredObservations, 6_700.);
		Assertions.assertFalse(paths.isEmpty());

		final Edge[] path = paths.iterator().next().getValue();

		Assertions.assertEquals(329.1, PathHelper.averagePositionError(path, filteredObservations), 0.1);

		final PathFindingStrategy pathFinder = new AStarPathFinder(distanceCalculator);
		final Edge[] connectedPath = PathHelper.connectPath(path, graph, pathFinder);

		final List<Polyline> pathPolylines = PathHelper.extractEdgesAsPolyline(connectedPath, factory);

		final String expected = "[null, 0, 0, 0, 0, 1, 1, 1, null, null]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));
		Assertions.assertEquals("LINESTRING (12.159747628109386 45.66132709541773, 12.238140517207398 45.65897415921759, 12.242949896905884 45.69828882177029, 12.200627355552967 45.732876303059044)", pathPolylines.get(0).toString());
	}

	@Test
	void should_match_E0_E1_with_gaussian_emission_probability_and_all_observations_direct_graph() throws NoObservationsException, NoGraphException{
		final GeoidalCalculator topologyCalculator = new GeoidalCalculator();
		final double observationStandardDeviation = 5.;
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new DirectionTransitionPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new GaussianEmissionCalculator(observationStandardDeviation);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			new DistanceCalculator(topologyCalculator));

		final GeometryFactory factory = new GeometryFactory(topologyCalculator);
		final Point node11 = factory.createPoint(12.159747628109386, 45.66132709541773);
		final Point node12_31_41 = factory.createPoint(12.238140517207398, 45.65897415921759);
		final Point node22 = factory.createPoint(12.242949896905884, 45.69828882177029);
		final Point node23 = factory.createPoint(12.200627355552967, 45.732876303059044);
		final Point node32_51_61 = factory.createPoint(12.343946870589775, 45.65931029901404);
		final Point node42 = factory.createPoint(12.25545428412434, 45.61054896081151);
		final Point node52 = factory.createPoint(12.297776825477285, 45.7345547621876);
		final Point node62 = factory.createPoint(12.322785599913317, 45.610885391198394);

		final Polyline edge0 = factory.createPolyline(node11, node12_31_41);
		final Polyline edge1 = factory.createPolyline(node12_31_41, node22, node23);
		final Polyline edge2 = factory.createPolyline(node12_31_41, node32_51_61);
		final Polyline edge3 = factory.createPolyline(node12_31_41, node42);
		final Polyline edge4 = factory.createPolyline(node32_51_61, node52);
		final Polyline edge5 = factory.createPolyline(node32_51_61, node62);

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSPoint[] observations = new GPSPoint[]{
			GPSPoint.of(12.142791962642718, 45.64824627395467, timestamp),
			GPSPoint.of(12.166829013921557, 45.658700732309484, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.190331908504874, 45.663553924585955, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.219176370039179, 45.65720735774349, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.237871854367, 45.65310037232308, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.243213421318018, 45.675125223889154, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.23894016775725, 45.691544896329816, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.237337697671506, 45.70684070823364, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.23306444411162, 45.725861366408196, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.215971429868546, 45.731454445518864, (timestamp = TestPathHelper.advanceTime(timestamp, 60)))
		};

		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<Polyline> observedEdges = TestPathHelper.extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = PathHelper.extractDirectGraph(observedEdges, 50.);

		final Point[] filteredObservations = TestPathHelper.extractObservations(edges, observations, 2_000.);
		final Collection<Map.Entry<Double, Edge[]>> paths = strategy.findPath(graph, filteredObservations, 8_400.);
		Assertions.assertFalse(paths.isEmpty());

		final Edge[] path = paths.iterator().next().getValue();
		final String expected = "[0, 0, 0, 0, 1, 1, 1, 1, 1, 1]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));
	}

	@Test
	void should_match_E2_with_bayesian_emission_probability_direct_graph() throws NoObservationsException, NoGraphException{
		final GeoidalCalculator topologyCalculator = new GeoidalCalculator();
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new ConnectedGraphTransitionPlugin())
			.withPlugin(new DirectionTransitionPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new GaussianEmissionCalculator(5.);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			new DistanceCalculator(topologyCalculator));

		final GeometryFactory factory = new GeometryFactory(topologyCalculator);
		final Point node11 = factory.createPoint(12.159747628109386, 45.66132709541773);
		final Point node12_31_41 = factory.createPoint(12.238140517207398, 45.65897415921759);
		final Point node22 = factory.createPoint(12.242949896905884, 45.69828882177029);
		final Point node23 = factory.createPoint(12.200627355552967, 45.732876303059044);
		final Point node32_51_61 = factory.createPoint(12.343946870589775, 45.65931029901404);
		final Point node42 = factory.createPoint(12.25545428412434, 45.61054896081151);
		final Point node52 = factory.createPoint(12.297776825477285, 45.7345547621876);
		final Point node62 = factory.createPoint(12.322785599913317, 45.610885391198394);

		final Polyline edge0 = factory.createPolyline(node11, node12_31_41);
		final Polyline edge1 = factory.createPolyline(node12_31_41, node22, node23);
		final Polyline edge2 = factory.createPolyline(node12_31_41, node32_51_61);
		final Polyline edge3 = factory.createPolyline(node12_31_41, node42);
		final Polyline edge4 = factory.createPolyline(node32_51_61, node52);
		final Polyline edge5 = factory.createPolyline(node32_51_61, node62);

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSPoint[] observations = new GPSPoint[]{
			GPSPoint.of(12.172704737567187, 45.59108565830172, timestamp),
			GPSPoint.of(12.229859503941071, 45.627705048963094, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.241610951232218, 45.6422714215264, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.243213421318018, 45.65646065552491, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.272057882852266, 45.662060679461206, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.304641441251732, 45.66168736195718, (timestamp = TestPathHelper.advanceTime(timestamp, 2))),
			GPSPoint.of(12.331349276005653, 45.66168736195718, (timestamp = TestPathHelper.advanceTime(timestamp, 60)))
		};

		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<Polyline> observedEdges = TestPathHelper.extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = PathHelper.extractDirectGraph(observedEdges, 50.);

		final Point[] filteredObservations = TestPathHelper.extractObservations(edges, observations, 400.);
		final Collection<Map.Entry<Double, Edge[]>> paths = strategy.findPath(graph, filteredObservations, 8_100.);
		Assertions.assertFalse(paths.isEmpty());

		final Edge[] path = paths.iterator().next().getValue();

		Assertions.assertEquals(607.4, PathHelper.averagePositionError(path, filteredObservations), 0.1);

		final String expected = "[null, null, 2, 2, 2, 2, 2]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));
	}


	@Test
	void should_match_E0_E3_E1_with_bayesian_emission_probability_bidirectional_graph() throws NoObservationsException, NoGraphException{
		final GeoidalCalculator topologyCalculator = new GeoidalCalculator();
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new ConnectedGraphTransitionPlugin())
			.withPlugin(new DirectionTransitionPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new GaussianEmissionCalculator(5.);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			new DistanceCalculator(topologyCalculator));

		final GeometryFactory factory = new GeometryFactory(topologyCalculator);
		final Point node11 = factory.createPoint(12.159747628109386, 45.66132709541773);
		final Point node12_31_41 = factory.createPoint(12.238140517207398, 45.65897415921759);
		final Point node22 = factory.createPoint(12.242949896905884, 45.69828882177029);
		final Point node23 = factory.createPoint(12.200627355552967, 45.732876303059044);
		final Point node32_51_61 = factory.createPoint(12.343946870589775, 45.65931029901404);
		final Point node42 = factory.createPoint(12.25545428412434, 45.61054896081151);
		final Point node52 = factory.createPoint(12.297776825477285, 45.7345547621876);
		final Point node62 = factory.createPoint(12.322785599913317, 45.610885391198394);

		final Polyline edge0 = factory.createPolyline(node11, node12_31_41);
		final Polyline edge1 = factory.createPolyline(node12_31_41, node22, node23);
		final Polyline edge2 = factory.createPolyline(node12_31_41, node32_51_61);
		final Polyline edge3 = factory.createPolyline(node12_31_41, node42);
		final Polyline edge4 = factory.createPolyline(node32_51_61, node52);
		final Polyline edge5 = factory.createPolyline(node32_51_61, node62);

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSPoint[] observations = new GPSPoint[]{
			GPSPoint.of(12.142791962642718, 45.64824627395467, timestamp),
			GPSPoint.of(12.166829013921557, 45.658700732309484, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.190331908504874, 45.663553924585955, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.219176370039179, 45.65720735774349, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.237871854367, 45.65310037232308, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.243213421318018, 45.675125223889154, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.23894016775725, 45.691544896329816, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.237337697671506, 45.70684070823364, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.23306444411162, 45.725861366408196, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.215971429868546, 45.731454445518864, (timestamp = TestPathHelper.advanceTime(timestamp, 60)))
		};

		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<Polyline> observedEdges = TestPathHelper.extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = PathHelper.extractBidirectionalGraph(observedEdges, 50.);

		final Point[] filteredObservations = TestPathHelper.extractObservations(edges, observations, 400.);
		final Collection<Map.Entry<Double, Edge[]>> paths = strategy.findPath(graph, filteredObservations, 8_100.);
		Assertions.assertFalse(paths.isEmpty());

		final Edge[] path = paths.iterator().next().getValue();

		Assertions.assertEquals(329.1, PathHelper.averagePositionError(path, filteredObservations), 0.1);

		final String expected = "[null, 0, 0, 0, 0, 1, 1, 1, null, null]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));
	}

	@Test
	void should_match_E0_E3_E1_with_gaussian_emission_probability_bidirectional_graph() throws NoObservationsException, NoGraphException{
		final GeoidalCalculator topologyCalculator = new GeoidalCalculator();
		final double observationStandardDeviation = 5.;
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new ConnectedGraphTransitionPlugin())
			.withPlugin(new DirectionTransitionPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new GaussianEmissionCalculator(observationStandardDeviation);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			new DistanceCalculator(topologyCalculator));

		final GeometryFactory factory = new GeometryFactory(topologyCalculator);
		final Point node11 = factory.createPoint(12.159747628109386, 45.66132709541773);
		final Point node12_31_41 = factory.createPoint(12.238140517207398, 45.65897415921759);
		final Point node22 = factory.createPoint(12.242949896905884, 45.69828882177029);
		final Point node23 = factory.createPoint(12.200627355552967, 45.732876303059044);
		final Point node32_51_61 = factory.createPoint(12.343946870589775, 45.65931029901404);
		final Point node42 = factory.createPoint(12.25545428412434, 45.61054896081151);
		final Point node52 = factory.createPoint(12.297776825477285, 45.7345547621876);
		final Point node62 = factory.createPoint(12.322785599913317, 45.610885391198394);

		final Polyline edge0 = factory.createPolyline(node11, node12_31_41);
		final Polyline edge1 = factory.createPolyline(node12_31_41, node22, node23);
		final Polyline edge2 = factory.createPolyline(node12_31_41, node32_51_61);
		final Polyline edge3 = factory.createPolyline(node12_31_41, node42);
		final Polyline edge4 = factory.createPolyline(node32_51_61, node52);
		final Polyline edge5 = factory.createPolyline(node32_51_61, node62);

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSPoint[] observations = new GPSPoint[]{
			GPSPoint.of(12.142791962642718, 45.64824627395467, timestamp),
			GPSPoint.of(12.166829013921557, 45.658700732309484, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.190331908504874, 45.663553924585955, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.219176370039179, 45.65720735774349, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.237871854367, 45.65310037232308, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.243213421318018, 45.675125223889154, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.23894016775725, 45.691544896329816, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.237337697671506, 45.70684070823364, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.23306444411162, 45.725861366408196, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.215971429868546, 45.731454445518864, (timestamp = TestPathHelper.advanceTime(timestamp, 60)))
		};

		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<Polyline> observedEdges = TestPathHelper.extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = PathHelper.extractBidirectionalGraph(observedEdges, 50.);

		final Point[] filteredObservations = TestPathHelper.extractObservations(edges, observations, 400.);
		final Collection<Map.Entry<Double, Edge[]>> paths = strategy.findPath(graph, filteredObservations, 6_700.);
		Assertions.assertFalse(paths.isEmpty());

		final Edge[] path = paths.iterator().next().getValue();

		Assertions.assertEquals(329.1, PathHelper.averagePositionError(path, filteredObservations), 0.1);

		final String expected = "[null, 0, 0, 0, 0, 1, 1, 1, null, null]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));
	}

	@Test
	void should_match_E0_E3_E1_with_gaussian_emission_probability_and_all_observations_bidirectional_graph() throws NoObservationsException, NoGraphException{
		final GeoidalCalculator topologyCalculator = new GeoidalCalculator();
		final double observationStandardDeviation = 5.;
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new ConnectedGraphTransitionPlugin())
			.withPlugin(new DirectionTransitionPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new GaussianEmissionCalculator(observationStandardDeviation);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			new DistanceCalculator(topologyCalculator));

		final GeometryFactory factory = new GeometryFactory(topologyCalculator);
		final Point node11 = factory.createPoint(12.159747628109386, 45.66132709541773);
		final Point node12_31_41 = factory.createPoint(12.238140517207398, 45.65897415921759);
		final Point node22 = factory.createPoint(12.242949896905884, 45.69828882177029);
		final Point node23 = factory.createPoint(12.200627355552967, 45.732876303059044);
		final Point node32_51_61 = factory.createPoint(12.343946870589775, 45.65931029901404);
		final Point node42 = factory.createPoint(12.25545428412434, 45.61054896081151);
		final Point node52 = factory.createPoint(12.297776825477285, 45.7345547621876);
		final Point node62 = factory.createPoint(12.322785599913317, 45.610885391198394);

		final Polyline edge0 = factory.createPolyline(node11, node12_31_41);
		final Polyline edge1 = factory.createPolyline(node12_31_41, node22, node23);
		final Polyline edge2 = factory.createPolyline(node12_31_41, node32_51_61);
		final Polyline edge3 = factory.createPolyline(node12_31_41, node42);
		final Polyline edge4 = factory.createPolyline(node32_51_61, node52);
		final Polyline edge5 = factory.createPolyline(node32_51_61, node62);

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSPoint[] observations = new GPSPoint[]{
			GPSPoint.of(12.142791962642718, 45.64824627395467, timestamp),
			GPSPoint.of(12.166829013921557, 45.658700732309484, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.190331908504874, 45.663553924585955, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.219176370039179, 45.65720735774349, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.237871854367, 45.65310037232308, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.243213421318018, 45.675125223889154, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.23894016775725, 45.691544896329816, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.237337697671506, 45.70684070823364, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.23306444411162, 45.725861366408196, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.215971429868546, 45.731454445518864, (timestamp = TestPathHelper.advanceTime(timestamp, 60)))
		};

		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<Polyline> observedEdges = TestPathHelper.extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = PathHelper.extractBidirectionalGraph(observedEdges, 50.);

		final Point[] filteredObservations = TestPathHelper.extractObservations(edges, observations, 2_000.);
		final Collection<Map.Entry<Double, Edge[]>> paths = strategy.findPath(graph, filteredObservations, 8_400.);
		Assertions.assertFalse(paths.isEmpty());

		final Edge[] path = paths.iterator().next().getValue();

		Assertions.assertEquals(648.4, PathHelper.averagePositionError(path, filteredObservations), 0.1);

		final String expected = "[0, 0, 0, 0, 0, 1, 1, 1, 1, 1]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));
	}

	@Test
	void should_match_E3_E2_with_bayesian_emission_probability_bidirectional_graph() throws NoObservationsException, NoGraphException{
		final GeoidalCalculator topologyCalculator = new GeoidalCalculator();
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new ConnectedGraphTransitionPlugin())
			.withPlugin(new DirectionTransitionPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new GaussianEmissionCalculator(5.);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			new DistanceCalculator(topologyCalculator));

		final GeometryFactory factory = new GeometryFactory(topologyCalculator);
		final Point node11 = factory.createPoint(12.159747628109386, 45.66132709541773);
		final Point node12_31_41 = factory.createPoint(12.238140517207398, 45.65897415921759);
		final Point node22 = factory.createPoint(12.242949896905884, 45.69828882177029);
		final Point node23 = factory.createPoint(12.200627355552967, 45.732876303059044);
		final Point node32_51_61 = factory.createPoint(12.343946870589775, 45.65931029901404);
		final Point node42 = factory.createPoint(12.25545428412434, 45.61054896081151);
		final Point node52 = factory.createPoint(12.297776825477285, 45.7345547621876);
		final Point node62 = factory.createPoint(12.322785599913317, 45.610885391198394);

		final Polyline edge0 = factory.createPolyline(node11, node12_31_41);
		final Polyline edge1 = factory.createPolyline(node12_31_41, node22, node23);
		final Polyline edge2 = factory.createPolyline(node12_31_41, node32_51_61);
		final Polyline edge3 = factory.createPolyline(node12_31_41, node42);
		final Polyline edge4 = factory.createPolyline(node32_51_61, node52);
		final Polyline edge5 = factory.createPolyline(node32_51_61, node62);

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSPoint[] observations = new GPSPoint[]{
			GPSPoint.of(12.172704737567187, 45.59108565830172, timestamp),
			GPSPoint.of(12.229859503941071, 45.627705048963094, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.241610951232218, 45.6422714215264, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.243213421318018, 45.65646065552491, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.272057882852266, 45.662060679461206, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.304641441251732, 45.66168736195718, (timestamp = TestPathHelper.advanceTime(timestamp, 2))),
			GPSPoint.of(12.331349276005653, 45.66168736195718, (timestamp = TestPathHelper.advanceTime(timestamp, 60)))
		};

		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<Polyline> observedEdges = TestPathHelper.extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = PathHelper.extractBidirectionalGraph(observedEdges, 50.);

		final Point[] filteredObservations = TestPathHelper.extractObservations(edges, observations, 400.);
		final Collection<Map.Entry<Double, Edge[]>> paths = strategy.findPath(graph, filteredObservations, 8_100.);
		Assertions.assertFalse(paths.isEmpty());

		final Edge[] path = paths.iterator().next().getValue();

		Assertions.assertEquals(273.7, PathHelper.averagePositionError(path, filteredObservations), 0.1);

		final String expected = "[null, null, 3-rev, 2, 2, 2, 2]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));
	}

}
