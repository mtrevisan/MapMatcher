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

import io.github.mtrevisan.mapmatcher.distances.AngularGeodeticCalculator;
import io.github.mtrevisan.mapmatcher.distances.GeodeticCalculator;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.NearLineMergeGraph;
import io.github.mtrevisan.mapmatcher.helpers.WGS84GeometryHelper;
import io.github.mtrevisan.mapmatcher.mapmatching.MapMatchingStrategy;
import io.github.mtrevisan.mapmatcher.mapmatching.ViterbiMapMatching;
import io.github.mtrevisan.mapmatcher.weight.LogMapMatchingProbabilityCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * https://github.com/Nalhin/Navigation/blob/main/backend/libraries/pathfinder/
 *
 * https://github.com/navjindervirdee/Advanced-Shortest-Paths-Algorithms/tree/master/A-Star/A%20star
 * https://github.com/coderodde/GraphSearchPal/blob/master/src/main/java/net/coderodde/gsp/model/support/AStarPathFinder.java
 * https://github.com/valhalla/valhalla/blob/master/docs/meili/algorithms.md
 */
public class Application{

	private static final double SIGMA_OBSERVATION = 4.07;
	private static final double BETA = 3.;


	public static void main(final String[] args){
		final LogMapMatchingProbabilityCalculator edgeWeightCalculator = new LogMapMatchingProbabilityCalculator(new AngularGeodeticCalculator());
//		final DistanceCalculator distanceCalculator = new AngularEarthEllipsoidalCalculator();
//		final MapMatchingStrategy strategy = new AStarMapMatching(edgeWeightCalculator, distanceCalculator);
		final MapMatchingStrategy strategy = new ViterbiMapMatching(edgeWeightCalculator);

		final Coordinate node11 = new Coordinate(12.159747628109386, 45.66132709541773);
		final Coordinate node12_31_41 = new Coordinate(12.238140517207398, 45.65897415921759);
		final Coordinate node22 = new Coordinate(12.242949896905884, 45.69828882177029);
		final Coordinate node23 = new Coordinate(12.200627355552967, 45.732876303059044);
		final Coordinate node32_51_61 = new Coordinate(12.343946870589775, 45.65931029901404);
		final Coordinate node42 = new Coordinate(12.25545428412434, 45.61054896081151);
		final Coordinate node52 = new Coordinate(12.297776825477285, 45.7345547621876);
		final Coordinate node62 = new Coordinate(12.322785599913317, 45.610885391198394);

		final LineString edge0 = WGS84GeometryHelper.createLineString(new Coordinate[]{node11, node12_31_41});
		final LineString edge1 = WGS84GeometryHelper.createLineString(new Coordinate[]{node12_31_41, node22, node23});
		final LineString edge2 = WGS84GeometryHelper.createLineString(new Coordinate[]{node12_31_41, node32_51_61});
		final LineString edge3 = WGS84GeometryHelper.createLineString(new Coordinate[]{node12_31_41, node42});
		final LineString edge4 = WGS84GeometryHelper.createLineString(new Coordinate[]{node32_51_61, node52});
		final LineString edge5 = WGS84GeometryHelper.createLineString(new Coordinate[]{node32_51_61, node62});

		final Coordinate[] observations1 = new Coordinate[]{
			new Coordinate(12.142791962642718, 45.64824627395467),
			new Coordinate(12.166829013921557, 45.658700732309484),
			new Coordinate(12.190331908504874, 45.663553924585955),
			new Coordinate(12.219176370039179, 45.65720735774349),
			new Coordinate(12.237871854367, 45.65310037232308),
			new Coordinate(12.243213421318018, 45.675125223889154),
			new Coordinate(12.23894016775725, 45.691544896329816),
			new Coordinate(12.237337697671506, 45.70684070823364),
			new Coordinate(12.23306444411162, 45.725861366408196),
			new Coordinate(12.215971429868546, 45.731454445518864)
		};
		final Coordinate[] observations2 = new Coordinate[]{
			new Coordinate(12.172704737567187, 45.59108565830172),
			new Coordinate(12.229859503941071, 45.627705048963094),
			new Coordinate(12.241610951232218, 45.6422714215264),
			new Coordinate(12.243213421318018, 45.65646065552491),
			new Coordinate(12.272057882852266, 45.662060679461206),
			new Coordinate(12.304641441251732, 45.66168736195718),
			new Coordinate(12.331349276005653, 45.66168736195718)
		};
		final Coordinate[] observations = observations1;

		final LineString[] edges = new LineString[]{edge0, edge1, edge2, edge3, edge4, edge5};
		//[m]
		final double radius = 500.;
		final Graph graph = extractGraph(edges, observations, radius);

		final Edge[] path = strategy.findPath(graph, observations);

		System.out.println(Arrays.toString(Arrays.stream(path).map(Edge::getID).toArray()));
	}

	private static Graph extractGraph(final LineString[] edges, final Coordinate[] observations, final double radius){
		final Set<LineString> observedEdges = extractObservedEdges(edges, observations, radius);

		final NearLineMergeGraph graph = new NearLineMergeGraph(radius, new GeodeticCalculator());
		int e = 0;
		//FIXME to uncomment
//		for(final LineString edge : observedEdges){
		for(final LineString edge : edges){
			final String edgeID = "E" + e;
			graph.addApproximateEdge(edgeID, edge);

			e ++;
		}
		return graph;
	}

	private static Set<LineString> extractObservedEdges(final LineString[] edges, final Coordinate[] observations,
			final double radius){
		final Set<LineString> observationsEdges = new HashSet<>(edges.length);
		for(final Coordinate observation : observations){
			final Polygon surrounding = WGS84GeometryHelper.createCircle(observation, radius);
			for(final LineString edge : edges)
				if(surrounding.intersects(edge))
					observationsEdges.add(edge);
		}
		return observationsEdges;
	}

}
