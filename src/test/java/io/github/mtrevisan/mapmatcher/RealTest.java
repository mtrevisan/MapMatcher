/**
 * Copyright (c) 2023 Mauro Trevisan
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
import io.github.mtrevisan.mapmatcher.pathfinding.AStarPathFinder;
import io.github.mtrevisan.mapmatcher.pathfinding.PathFindingStrategy;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.DistanceCalculator;
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


/**
 * https://github.com/Nalhin/Navigation/blob/main/backend/libraries/pathfinder/
 *
 * https://github.com/coderodde/GraphSearchPal/blob/master/src/main/java/net/coderodde/gsp/model/support/AStarPathFinder.java
 * https://github.com/valhalla/valhalla/blob/master/docs/meili/algorithms.md
 */
public class RealTest{

	private static final GeometryFactory FACTORY = new GeometryFactory(new GeoidalCalculator());


	public static void main(final String[] args) throws IOException{
		final GeoidalCalculator topologyCalculator = new GeoidalCalculator();
		//NOTE: the initial probability is a uniform distribution reflecting the fact that there is no known bias about which is the
		// correct segment
		final InitialProbabilityCalculator initialCalculator = new UniformInitialCalculator();
		final TransitionProbabilityCalculator transitionCalculator = new TransitionProbabilityCalculator()
			.withPlugin(new ShortestPathTransitionPlugin(90.))
			.withPlugin(new DirectionTransitionPlugin());
//		final TransitionProbabilityCalculator transitionCalculator = new LogExponentialTransitionCalculator(200.);
		final EmissionProbabilityCalculator emissionCalculator = new BayesianEmissionCalculator();
//		final EmissionProbabilityCalculator emissionCalculator = new GaussianEmissionCalculator(10.);
		final DistanceCalculator distanceCalculator = new DistanceCalculator(topologyCalculator);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
				distanceCalculator)
			.withOffRoad();
//		final MapMatchingStrategy strategy = new AStarMapMatching(initialCalculator, transitionCalculator, emissionCalculator,
//			distanceCalculator);

		final Polyline[] roads = extractPolylines("it.highways.simplified.5.wkt")
			.toArray(Polyline[]::new);
		final HPRtree<Polyline> tree = new HPRtree<>();
		for(final Polyline road : roads){
			final Envelope geoBoundingBox = road.getBoundingBox();
			tree.insert(geoBoundingBox, road);
		}

		GPSPoint[] observations = extract("CA202RX", ";");
//observations = Arrays.copyOfRange(observations, 172, 182);

//FIXME if the observations went from 176 to 182, than then path will become a mess...
//https://www1.pub.informatik.uni-wuerzburg.de/pub/haunert/pdf/HaunertBudig2012.pdf
//https://kops.uni-konstanz.de/server/api/core/bitstreams/324b2478-0f44-496a-a276-4463237646f8/content
//test/resources/ijgi-11-00538-v2.pdf

//observations = Arrays.copyOfRange(observations, 176, 182);
observations = Arrays.copyOfRange(observations, 160, 169);
//observations = Arrays.copyOfRange(observations, 170, 185);
//observations = Arrays.copyOfRange(observations, 400, 500);

		final Collection<Polyline> observedEdges = PathHelper.extractObservedEdges(tree, observations, 500.);
		final Graph graph = PathHelper.extractDirectGraph(observedEdges, 1.);

		final GPSPoint[] filteredObservations = PathHelper.extractObservations(tree, observations, 400.);
		//estimated noise
		final double[] observationNoises = new double[filteredObservations.length];
		final GeometryFactory factory = new GeometryFactory(topologyCalculator);
		for(int i = 0; i < filteredObservations.length; i ++)
			if(filteredObservations[i] != null)
				observationNoises[i] = StrictMath.hypot(filteredObservations[i].getX() - observations[i].getX(),
					filteredObservations[i].getY() - observations[i].getY());
System.out.println("observation noises: " + Arrays.toString(observationNoises));
System.out.println("graph & observations: " + graph.toStringWithObservations(filteredObservations));
		final Edge[] path = strategy.findPath(graph, filteredObservations, 400.);
System.out.println("true: [null, null, null, null, null, 11, 11, 6, 4]");
if(path != null)
	System.out.println("path: " + Arrays.toString(Arrays.stream(path).map(e -> (e != null? e.getID(): null)).toArray()));
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

		if(path != null){
			double averagePositioningError = 0.;
			int windowSize = 0;
			for(int i = 0; i < filteredObservations.length; i ++)
				if(filteredObservations[i] != null && path[i] != null){
					averagePositioningError += filteredObservations[i].distance(path[i].getPath());
					windowSize ++;
				}
			averagePositioningError /= windowSize;
System.out.println("average positioning error: " + averagePositioningError);
		}

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
