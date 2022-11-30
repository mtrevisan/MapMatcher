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

import io.github.mtrevisan.mapmatcher.distances.AngularGeodeticCalculator;
import io.github.mtrevisan.mapmatcher.distances.DistanceCalculator;
import io.github.mtrevisan.mapmatcher.distances.GeodeticCalculator;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.NearLineMergeGraph;
import io.github.mtrevisan.mapmatcher.helpers.GPSCoordinate;
import io.github.mtrevisan.mapmatcher.helpers.JTSGeometryHelper;
import io.github.mtrevisan.mapmatcher.helpers.kalman.GPSPositionSpeedFilter;
import io.github.mtrevisan.mapmatcher.mapmatching.MapMatchingStrategy;
import io.github.mtrevisan.mapmatcher.mapmatching.ViterbiMapMatching;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.EmissionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.LogBayesianEmissionCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.TopologicTransitionCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.TransitionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.UniformInitialCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
		final DistanceCalculator distanceCalculator = new AngularGeodeticCalculator();
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TopologicTransitionCalculator();
		final EmissionProbabilityCalculator emissionCalculator = new LogBayesianEmissionCalculator(distanceCalculator);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator);
//		final MapMatchingStrategy strategy = new AStarMapMatching(initialCalculator, transitionCalculator, probabilityCalculator);

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
		final GPSCoordinate[] observations1 = new GPSCoordinate[]{
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
		final GPSCoordinate[] observations2 = new GPSCoordinate[]{
			new GPSCoordinate(12.172704737567187, 45.59108565830172, timestamp),
			new GPSCoordinate(12.229859503941071, 45.627705048963094, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.241610951232218, 45.6422714215264, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.243213421318018, 45.65646065552491, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.272057882852266, 45.662060679461206, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.304641441251732, 45.66168736195718, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.331349276005653, 45.66168736195718, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS)))
		};
		final GPSCoordinate[] observations = observations1;

		final LineString[] edges = new LineString[]{edge0, edge1, edge2, edge3, edge4, edge5};

		final Collection<LineString> observedEdges = extractObservedEdges(edges, observations, 100_000.);
		final Graph graph = extractGraph(observedEdges, 500.);

		final Coordinate[] filteredObservations = extractObservations(edges, observations, 400.);
		final Edge[] path = strategy.findPath(graph, filteredObservations);

if(path != null)
	System.out.println(Arrays.toString(Arrays.stream(path).map(Edge::getID).toArray()));
	}

//	public static void main(final String[] args){
//		final double observationStandardDeviation = 200.;
//		final DistanceCalculator distanceCalculator = new AngularGeodeticCalculator();
//		final LogMapMatchingProbabilityCalculator probabilityCalculator = new LogMapMatchingProbabilityCalculator(observationStandardDeviation,
//			distanceCalculator);
//		final MapMatchingStrategy strategy = new ViterbiMapMatching(probabilityCalculator);
//
//		final Coordinate[] observations1 = new Coordinate[]{
//			new Coordinate(12.142791962642718, 45.64824627395467),
//			new Coordinate(12.166829013921557, 45.658700732309484),
//			new Coordinate(12.190331908504874, 45.663553924585955),
//			new Coordinate(12.219176370039179, 45.65720735774349),
//			new Coordinate(12.237871854367, 45.65310037232308),
//			new Coordinate(12.243213421318018, 45.675125223889154),
//			new Coordinate(12.23894016775725, 45.691544896329816),
//			new Coordinate(12.237337697671506, 45.70684070823364),
//			new Coordinate(12.23306444411162, 45.725861366408196),
//			new Coordinate(12.215971429868546, 45.731454445518864)
//		};
//		final Coordinate[] observations2 = new Coordinate[]{
//			new Coordinate(12.172704737567187, 45.59108565830172),
//			new Coordinate(12.229859503941071, 45.627705048963094),
//			new Coordinate(12.241610951232218, 45.6422714215264),
//			new Coordinate(12.243213421318018, 45.65646065552491),
//			new Coordinate(12.272057882852266, 45.662060679461206),
//			new Coordinate(12.304641441251732, 45.66168736195718),
//			new Coordinate(12.331349276005653, 45.66168736195718)
//		};
//		final Coordinate[] observations = observations2;
//
//		final LineString[] edges = readEdges();
//		//all italy
//		final Collection<LineString> observedEdges = extractObservedEdges(edges, observations, 1_000_000.);
//		final Graph graph = extractGraph(observedEdges, 200.);
//
//		final Edge[] path = strategy.findPath(graph, observations);
//
//		if(path != null)
//			System.out.println(Arrays.toString(Arrays.stream(path).map(Edge::getID).toArray()));
//	}

	private static LineString[] readEdges(){
		final List<String> lines = readFile("src/main/resources/map.eura.txt");

		final List<LineString> edges = new ArrayList<>();
		try{
			final WKTReader reader = JTSGeometryHelper.getWktReader();
			for(final String line : lines){
				final Geometry segment = reader.read(line);
				edges.add((LineString)segment);
			}
		}
		catch(final ParseException e){
			e.printStackTrace();
		}
		return edges.toArray(LineString[]::new);
	}

	private static List<String> readFile(final String filename){
		final List<String> lines = new ArrayList<>();
		final File f = new File(filename);
		try(final BufferedReader br = new BufferedReader(new FileReader(f))){
			String readLine;
			while((readLine = br.readLine()) != null)
				if(!readLine.isEmpty())
					lines.add(readLine);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return lines;
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
		final Set<LineString> observationsEdges = new LinkedHashSet<>(edges.length);
		for(final Coordinate observation : observations)
			observationsEdges.addAll(extractObservedEdges(edges, observation, threshold));
		return observationsEdges;
	}

	private static Collection<LineString> extractObservedEdges(final LineString[] edges, final Coordinate observation,
			final double threshold){
		final Set<LineString> observationsEdges = new LinkedHashSet<>(edges.length);
		final Polygon surrounding = JTSGeometryHelper.createCircle(observation, threshold);
		for(final LineString edge : edges)
			if(surrounding.intersects(edge))
				observationsEdges.add(edge);
		return observationsEdges;
	}

	private static Coordinate[] extractObservations(final LineString[] edges, final GPSCoordinate[] observations, final double threshold){
		final GPSCoordinate[] feasibleObservations = new GPSCoordinate[observations.length];

		//step 1. Use Kalman filter to smooth the coordinates
		final GPSPositionSpeedFilter kalmanFilter = new GPSPositionSpeedFilter(3., 5.);
		feasibleObservations[0] = observations[0];
		for(int i = 1; i < observations.length; i ++){
			kalmanFilter.updatePosition(observations[i].getY(), observations[i].getX(),
				ChronoUnit.SECONDS.between(observations[i - 1].getTimestamp(), observations[i].getTimestamp()));
			final double[] position = kalmanFilter.getPosition();
			feasibleObservations[i] = new GPSCoordinate(position[1], position[0], observations[i].getTimestamp());
		}

		//step 2. Retain all observation that are within a certain radius from an edge
		for(int i = 0; i < feasibleObservations.length; i ++){
			final GPSCoordinate observation = feasibleObservations[i];
			final Polygon surrounding = JTSGeometryHelper.createCircle(observation, threshold);
			boolean edgesFound = false;
			for(final LineString edge : edges)
				if(surrounding.intersects(edge)){
					edgesFound = true;
					break;
				}
			if(!edgesFound)
				feasibleObservations[i] = null;
		}

		return feasibleObservations;
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
