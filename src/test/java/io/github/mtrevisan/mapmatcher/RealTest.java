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
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.ShortestPathTransitionPlugin;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition.TransitionProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.GeodeticDistanceCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Envelope;
import io.github.mtrevisan.mapmatcher.spatial.GPSPoint;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;


public class RealTest{

	private static final GeometryFactory FACTORY = new GeometryFactory(new GeoidalCalculator());


	public static void main(String[] args) throws IOException{
		//NOTE: the initial probability is a uniform distribution reflecting the fact that there is no known bias about which is the
		// correct segment
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new ShortestPathTransitionPlugin(68.4))
			.withPlugin(new DirectionTransitionPlugin());
//		final TransitionProbabilityCalculator transitionCalculator = new LogExponentialTransitionCalculator(200.);
		final EmissionProbabilityCalculator emissionCalculator = new BayesianEmissionCalculator();
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
			new GeodeticDistanceCalculator());
//		final MapMatchingStrategy strategy = new AStarMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
//			new GeodeticDistanceCalculator());

		Polyline[] roads = extractPolylines("it.highways.simplified.5.wkt")
			.toArray(Polyline[]::new);
		final HPRtree<Polyline> tree = new HPRtree<>();
		for(final Polyline road : roads){
			final Envelope geoBoundingBox = road.getBoundingBox();
			tree.insert(geoBoundingBox, road);
		}

		GPSPoint[] observations = extract("CA202RX", ";");
observations = Arrays.copyOfRange(observations, 163, 172);

		Collection<Polyline> observedEdges = PathHelper.extractObservedEdges(tree, observations, 500.);
		final Graph graph = PathHelper.extractBidirectionalGraph(observedEdges, 15.);

		final GPSPoint[] filteredObservations = PathHelper.extractObservations(tree, observations, 400.);
System.out.println(graph.toStringWithObservations(filteredObservations));
		final Edge[] path = strategy.findPath(graph, filteredObservations, 400.);
if(path != null){
	System.out.println("true: [null, null, 1.0-rev, 1.0, 13.0-rev, 24.2, 26.0-rev, 26.0, 26.0-rev]");
	System.out.println("path: " + Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));
}

		final Edge[] connectedPath = PathHelper.connectPath(path, graph, new GeodeticDistanceCalculator());
if(connectedPath.length > 0)
	System.out.println("connected path: " + Arrays.toString(Arrays.stream(connectedPath).map(e -> (e != null? e.getID(): null)).toArray()));

		final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		final Polyline pathPolyline = PathHelper.extractEdgesAsPolyline(connectedPath, factory);
if(pathPolyline != null){
	final StringJoiner sj = new StringJoiner(", ", "GEOMETRYCOLLECTION (", ")");
	sj.add(pathPolyline.toString());
	for(final GPSPoint point : filteredObservations)
		if(point != null)
			sj.add(point.toString());
	System.out.println("path polyline: " + sj);
}

		if(path != null){
			double averagePositioningError = 0.;
			int windowSize = 0;
			for(int i = 0; i < filteredObservations.length; i ++)
				if(filteredObservations[i] != null){
					averagePositioningError += filteredObservations[i].distance(path[i].getPolyline());
					windowSize ++;
				}
			averagePositioningError /= windowSize;
System.out.println("average positioning error: " + averagePositioningError);
		}
	}


	private static List<Polyline> extractPolylines(final String roadFile) throws IOException{
		final List<Polyline> polylines = new ArrayList<>();

		final InputStream is = RealTest.class.getClassLoader().getResourceAsStream(roadFile);
		try(final BufferedReader br = new BufferedReader(new InputStreamReader(is))){
			String readLine;
			while((readLine = br.readLine()) != null)
				if(!readLine.isEmpty())
					polylines.add(parsePolyline(readLine));
		}
		return polylines;
	}

	private static Polyline parsePolyline(final String line){
		if(!(line.startsWith("LINESTRING (") || line.startsWith("LINESTRING(")) && !line.endsWith(")"))
			throw new IllegalArgumentException("Unrecognized element, cannot parse line: " + line);

		List<Point> points = new ArrayList<>(0);
		int startIndex = line.indexOf('(') + 1;
		while(true){
			int separatorIndex = line.indexOf(" ", startIndex + 1);
			if(separatorIndex < 0)
				break;

			int endIndex = line.indexOf(", ", separatorIndex + 1);
			if(endIndex < 0)
				endIndex = line.indexOf(')', separatorIndex + 1);
			points.add(FACTORY.createPoint(
				Double.parseDouble(line.substring(startIndex, separatorIndex)),
				Double.parseDouble(line.substring(separatorIndex + 1, endIndex))
			));
			startIndex = endIndex + 2;
		}

		return FACTORY.createPolyline(points.toArray(Point[]::new));
	}

	private static GPSPoint[] extract(String licensePlateNumber, String separator) throws IOException{
		InputStream is = RealTest.class.getClassLoader().getResourceAsStream("\\trajectories\\" + licensePlateNumber + ".csv");

		List<GPSPoint> result = new ArrayList<>(0);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
			.withZone(ZoneId.of("UTC"));;
		try(BufferedReader in = new BufferedReader(new InputStreamReader(is))){
			String line;
			while((line = in.readLine()) != null){
				String[] cells = line.split(separator);

				if(cells.length > 0){
					double longitude = Double.parseDouble(cells[0]);
					double latitude = Double.parseDouble(cells[1]);
					ZonedDateTime timestamp = ZonedDateTime.from(dateTimeFormatter.parse(cells[2]));
					result.add(GPSPoint.of(longitude, latitude, timestamp));
				}
			}

			return result.toArray(GPSPoint[]::new);
		}
	}

}
