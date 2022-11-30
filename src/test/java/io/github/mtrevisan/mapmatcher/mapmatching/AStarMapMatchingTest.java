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

import io.github.mtrevisan.mapmatcher.distances.DistanceCalculator;
import io.github.mtrevisan.mapmatcher.distances.GeodeticCalculator;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.NearLineMergeGraph;
import io.github.mtrevisan.mapmatcher.helpers.GPSCoordinate;
import io.github.mtrevisan.mapmatcher.helpers.JTSGeometryHelper;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.EmissionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.LogBayesianEmissionCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.LogGaussianEmissionCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.TopologicTransitionCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.TransitionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.UniformInitialCalculator;
import org.junit.jupiter.api.Assertions;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;


class AStarMapMatchingTest{

//	@Test
	void should_match_E0_E1_with_bayesian_emission_probability(){
		final DistanceCalculator distanceCalculator = new GeodeticCalculator();
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TopologicTransitionCalculator(distanceCalculator);
		final EmissionProbabilityCalculator emissionCalculator = new LogBayesianEmissionCalculator(distanceCalculator);
		final MapMatchingStrategy strategy = new AStarMapMatching(initialCalculator, transitionCalculator, emissionCalculator);

		final Coordinate node11 = new Coordinate(12.159747628109386, 45.66132709541773);
		final Coordinate node12_31_41 = new Coordinate(12.238140517207398, 45.65897415921759);
		final Coordinate node22 = new Coordinate(12.242949896905884, 45.69828882177029);
		final Coordinate node23 = new Coordinate(12.200627355552967, 45.732876303059044);
		final Coordinate node32_51_61 = new Coordinate(12.343946870589775, 45.65931029901404);
		final Coordinate node42 = new Coordinate(12.25545428412434, 45.61054896081151);
		final Coordinate node52 = new Coordinate(12.297776825477285, 45.7345547621876);
		final Coordinate node62 = new Coordinate(12.322785599913317, 45.610885391198394);

		final LineString edge0 = JTSGeometryHelper.createLineString(new Coordinate[]{node11, node12_31_41});
		final LineString edge1 = JTSGeometryHelper.createLineString(new Coordinate[]{node12_31_41, node22, node23});
		final LineString edge2 = JTSGeometryHelper.createLineString(new Coordinate[]{node12_31_41, node32_51_61});
		final LineString edge3 = JTSGeometryHelper.createLineString(new Coordinate[]{node12_31_41, node42});
		final LineString edge4 = JTSGeometryHelper.createLineString(new Coordinate[]{node32_51_61, node52});
		final LineString edge5 = JTSGeometryHelper.createLineString(new Coordinate[]{node32_51_61, node62});

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSCoordinate[] observations = new GPSCoordinate[]{
			new GPSCoordinate(12.142791962642718, 45.64824627395467, timestamp),
			new GPSCoordinate(12.166829013921557, 45.658700732309484, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.190331908504874, 45.663553924585955, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.219176370039179, 45.65720735774349, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.237871854367, 45.65310037232308, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.243213421318018, 45.675125223889154, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.23894016775725, 45.691544896329816, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.237337697671506, 45.70684070823364, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.23306444411162, 45.725861366408196, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.215971429868546, 45.731454445518864, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS)))
		};

		final LineString[] edges = new LineString[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<LineString> observedEdges = extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = extractGraph(observedEdges, 1_000.);

		final Edge[] path = strategy.findPath(graph, observations);

		final String expected = "[E0, E0, E0, E0, E0, E1, E1, E1, E1, E1]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(Edge::getID).toArray()));
	}

//	@Test
	void should_match_E0_E1_with_gaussian_emission_probability(){
		final double observationStandardDeviation = 5.;
		final DistanceCalculator distanceCalculator = new GeodeticCalculator();
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TopologicTransitionCalculator(distanceCalculator);
		final EmissionProbabilityCalculator emissionCalculator = new LogGaussianEmissionCalculator(observationStandardDeviation,
			distanceCalculator);
		final MapMatchingStrategy strategy = new AStarMapMatching(initialCalculator, transitionCalculator, emissionCalculator);

		final Coordinate node11 = new Coordinate(12.159747628109386, 45.66132709541773);
		final Coordinate node12_31_41 = new Coordinate(12.238140517207398, 45.65897415921759);
		final Coordinate node22 = new Coordinate(12.242949896905884, 45.69828882177029);
		final Coordinate node23 = new Coordinate(12.200627355552967, 45.732876303059044);
		final Coordinate node32_51_61 = new Coordinate(12.343946870589775, 45.65931029901404);
		final Coordinate node42 = new Coordinate(12.25545428412434, 45.61054896081151);
		final Coordinate node52 = new Coordinate(12.297776825477285, 45.7345547621876);
		final Coordinate node62 = new Coordinate(12.322785599913317, 45.610885391198394);

		final LineString edge0 = JTSGeometryHelper.createLineString(new Coordinate[]{node11, node12_31_41});
		final LineString edge1 = JTSGeometryHelper.createLineString(new Coordinate[]{node12_31_41, node22, node23});
		final LineString edge2 = JTSGeometryHelper.createLineString(new Coordinate[]{node12_31_41, node32_51_61});
		final LineString edge3 = JTSGeometryHelper.createLineString(new Coordinate[]{node12_31_41, node42});
		final LineString edge4 = JTSGeometryHelper.createLineString(new Coordinate[]{node32_51_61, node52});
		final LineString edge5 = JTSGeometryHelper.createLineString(new Coordinate[]{node32_51_61, node62});

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSCoordinate[] observations = new GPSCoordinate[]{
			new GPSCoordinate(12.142791962642718, 45.64824627395467, timestamp),
			new GPSCoordinate(12.166829013921557, 45.658700732309484, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.190331908504874, 45.663553924585955, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.219176370039179, 45.65720735774349, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.237871854367, 45.65310037232308, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.243213421318018, 45.675125223889154, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.23894016775725, 45.691544896329816, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.237337697671506, 45.70684070823364, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.23306444411162, 45.725861366408196, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.215971429868546, 45.731454445518864, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS)))
		};

		final LineString[] edges = new LineString[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<LineString> observedEdges = extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = extractGraph(observedEdges, 1_000.);

		final Edge[] path = strategy.findPath(graph, observations);

		final String expected = "[E0, E0, E0, E0, E0, E1, E1, E1, E1, E1]";
		Assertions.assertEquals(expected, Arrays.toString(Arrays.stream(path).map(Edge::getID).toArray()));
	}


//	@Test
	void should_match_E3_E2_with_bayesian_emission_probability(){
		final DistanceCalculator distanceCalculator = new GeodeticCalculator();
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TopologicTransitionCalculator(distanceCalculator);
		final EmissionProbabilityCalculator emissionCalculator = new LogBayesianEmissionCalculator(distanceCalculator);
		final MapMatchingStrategy strategy = new AStarMapMatching(initialCalculator, transitionCalculator, emissionCalculator);

		final Coordinate node11 = new Coordinate(12.159747628109386, 45.66132709541773);
		final Coordinate node12_31_41 = new Coordinate(12.238140517207398, 45.65897415921759);
		final Coordinate node22 = new Coordinate(12.242949896905884, 45.69828882177029);
		final Coordinate node23 = new Coordinate(12.200627355552967, 45.732876303059044);
		final Coordinate node32_51_61 = new Coordinate(12.343946870589775, 45.65931029901404);
		final Coordinate node42 = new Coordinate(12.25545428412434, 45.61054896081151);
		final Coordinate node52 = new Coordinate(12.297776825477285, 45.7345547621876);
		final Coordinate node62 = new Coordinate(12.322785599913317, 45.610885391198394);

		final LineString edge0 = JTSGeometryHelper.createLineString(new Coordinate[]{node11, node12_31_41});
		final LineString edge1 = JTSGeometryHelper.createLineString(new Coordinate[]{node12_31_41, node22, node23});
		final LineString edge2 = JTSGeometryHelper.createLineString(new Coordinate[]{node12_31_41, node32_51_61});
		final LineString edge3 = JTSGeometryHelper.createLineString(new Coordinate[]{node12_31_41, node42});
		final LineString edge4 = JTSGeometryHelper.createLineString(new Coordinate[]{node32_51_61, node52});
		final LineString edge5 = JTSGeometryHelper.createLineString(new Coordinate[]{node32_51_61, node62});

		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSCoordinate[] observations = new GPSCoordinate[]{
			new GPSCoordinate(12.172704737567187, 45.59108565830172, timestamp),
			new GPSCoordinate(12.229859503941071, 45.627705048963094, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.241610951232218, 45.6422714215264, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.243213421318018, 45.65646065552491, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.272057882852266, 45.662060679461206, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.304641441251732, 45.66168736195718, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.331349276005653, 45.66168736195718, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS)))
		};

		final LineString[] edges = new LineString[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Collection<LineString> observedEdges = extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = extractGraph(observedEdges, 1_000.);

		final Edge[] path = strategy.findPath(graph, observations);

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
	private static Collection<LineString> extractObservedEdges(final LineString[] edges, final Coordinate[] observations,
			final double threshold){
		final GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
		final Set<LineString> observationsEdges = new LinkedHashSet<>(edges.length);
		for(final Coordinate observation : observations){
			for(final LineString edge : edges)
				if(geodeticCalculator.distance(observation, edge) <= threshold)
					observationsEdges.add(edge);
		}
		return observationsEdges;
	}

	private static Graph extractGraph(final Collection<LineString> edges, final double threshold){
		final NearLineMergeGraph graph = new NearLineMergeGraph(threshold, new GeodeticCalculator());
		int e = 0;
		for(final LineString edge : edges){
			graph.addApproximateDirectEdge("E" + e, edge);
			graph.addApproximateDirectEdge("E" + e + "-rev", edge.reverse());

			e ++;
		}
		return graph;
	}

}
