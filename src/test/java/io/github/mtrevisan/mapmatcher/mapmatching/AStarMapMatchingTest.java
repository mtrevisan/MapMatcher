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

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.NearLineMergeGraph;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission.BayesianEmissionCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission.EmissionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.emission.GaussianEmissionCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.UniformInitialCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.TopologicalTransitionPlugin;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.TransitionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.GeodeticDistanceCalculator;
import io.github.mtrevisan.mapmatcher.spatial.GPSPoint;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;


class AStarMapMatchingTest{

	//FIXME
//	@Test
	void should_match_E0_E1_with_bayesian_emission_probability(){
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new TopologicalTransitionPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new BayesianEmissionCalculator();
		final MapMatchingStrategy strategy = new AStarMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			new GeodeticDistanceCalculator());

		final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
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

		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<Polyline> observedEdges = extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = extractGraph(observedEdges, 1_000.);

		final Edge[] path = strategy.findPath(graph, observations, 8_350.);

		final String expected = "[E0, E0, E0, E0, E0, E1, E1, E1, E1, E1]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(Edge::getID).toArray()));
	}

	//FIXME
//	@Test
	void should_match_E0_E1_with_gaussian_emission_probability(){
		final double observationStandardDeviation = 5.;
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new TopologicalTransitionPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new GaussianEmissionCalculator(observationStandardDeviation);
		final MapMatchingStrategy strategy = new AStarMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			new GeodeticDistanceCalculator());

		final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
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

		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<Polyline> observedEdges = extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = extractGraph(observedEdges, 1_000.);

		final Edge[] path = strategy.findPath(graph, observations, 8_350.);

		final String expected = "[E0, E0, E0, E0, E0, E1, E1, E1, E1, E1]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(Edge::getID).toArray()));
	}


	//FIXME
//	@Test
	void should_match_E3_E2_with_bayesian_emission_probability(){
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new TopologicalTransitionPlugin());
		final EmissionProbabilityCalculator emissionCalculator = new BayesianEmissionCalculator();
		final MapMatchingStrategy strategy = new AStarMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			new GeodeticDistanceCalculator());

		final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
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
			GPSPoint.of(12.229859503941071, 45.627705048963094, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.241610951232218, 45.6422714215264, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.243213421318018, 45.65646065552491, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.272057882852266, 45.662060679461206, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.304641441251732, 45.66168736195718, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			GPSPoint.of(12.331349276005653, 45.66168736195718, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS)))
		};

		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<Polyline> observedEdges = extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = extractGraph(observedEdges, 1_000.);

		final Edge[] path = strategy.findPath(graph, observations, 8_350.);

		final String expected = "[E3, E3, E3, E2, E2, E2, E2]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(Edge::getID).toArray()));
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
	 * @param edges	The set of road links.
	 * @param observations	The observations.
	 * @param threshold	The threshold.
	 * @return	The list of road links whose distance is less than the given radius from each observation.
	 */
	private static Collection<Polyline> extractObservedEdges(final Polyline[] edges, final Point[] observations,
			final double threshold){
		final GeoidalCalculator geoidalCalculator = new GeoidalCalculator();
		final Set<Polyline> observationsEdges = new LinkedHashSet<>(edges.length);
		for(final Point observation : observations){
			for(final Polyline edge : edges)
				if(geoidalCalculator.distance(observation, edge) <= threshold)
					observationsEdges.add(edge);
		}
		return observationsEdges;
	}

	private static Graph extractGraph(final Collection<Polyline> edges, final double threshold){
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
