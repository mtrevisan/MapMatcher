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
package io.github.mtrevisan.mapmatcher.mapmatching;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.NearLineMergeGraph;
import io.github.mtrevisan.mapmatcher.helpers.PathHelper;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.GeodeticDistanceCalculator;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;


class MapMatchingStrategyTest{

	@Test
	void should_connect_path(){
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

		final Polyline[] edges = new Polyline[]{edge0, edge1, edge2, edge3, edge4, edge5};
		final Graph graph = extractDirectGraph(Arrays.asList(edges), 50.);

		Edge pathEdge0 = null;
		Edge pathEdge2 = null;
		Edge pathEdge4 = null;
		for(final Edge e : graph.edges()){
			if(e.getID().equals("E0"))
				pathEdge0 = e;
			else if(e.getID().equals("E2"))
				pathEdge2 = e;
			else if(e.getID().equals("E4"))
				pathEdge4 = e;
		}
		final Edge[] path = new Edge[]{pathEdge0, pathEdge4};

		final Edge[] connectedPath = PathHelper.connectPath(path, graph, new GeodeticDistanceCalculator());

		Assertions.assertArrayEquals(new Edge[]{pathEdge0, pathEdge2, pathEdge4}, connectedPath);
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

}
