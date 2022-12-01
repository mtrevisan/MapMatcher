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

import io.github.mtrevisan.mapmatcher.distances.GeodeticCalculator;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Graph;
import io.github.mtrevisan.mapmatcher.graph.NearLineMergeGraph;
import io.github.mtrevisan.mapmatcher.helpers.JTSGeometryHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.util.Arrays;
import java.util.Collection;


class MapMatchingStrategyTest{

	@Test
	void should_connect_path(){
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

		final LineString[] edges = new LineString[]{edge0, edge1, edge2, edge3, edge4, edge5};
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

		final Edge[] connectedPath = MapMatchingStrategy.connectPath(path, graph);

		Assertions.assertArrayEquals(new Edge[]{pathEdge0, pathEdge2, pathEdge4}, connectedPath);
	}


	private static Graph extractDirectGraph(final Collection<LineString> edges, final double threshold){
		final NearLineMergeGraph graph = new NearLineMergeGraph(threshold, new GeodeticCalculator());
		int e = 0;
		for(final LineString edge : edges){
			graph.addApproximateDirectEdge("E" + e, edge);

			e ++;
		}
		return graph;
	}

}