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
package io.github.mtrevisan.mapmatcher.weight;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.GeodeticDurationCalculator;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class DurationEdgeWeightCalculatorTest{

	@Test
	void should_return_edge_duration_in_minutes(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point[] points = new Point[]{
			factory.createPoint(121.058805, 14.552797),
			factory.createPoint(120.994260, 14.593999),
			factory.createPoint(96.591876, 77.870317),
			factory.createPoint(-4.815018, 21.719527),
			factory.createPoint(23.704799, -17.727830),
			factory.createPoint(-130.279576, 58.585396)
		};
		double[] expectedDurations = new double[]{10.0, 5_939.6, 8_999.0};
		double[] maxSpeeds = new double[]{50., 80., 100.};
		for(int i = 0; i < expectedDurations.length; i ++){
			Point fromPoints = points[i << 1];
			Point toPoints = points[(i << 1) + 1];
			double expectedDuration = expectedDurations[i];
			double maxSpeed = maxSpeeds[i];
			GeodeticDurationCalculator edgeWeightCalculator = new GeodeticDurationCalculator();
			final Edge edge = Edge.createDirectEdge(new Node("0", fromPoints), new Node("1", toPoints));
			edge.setWeight(maxSpeed);
			double actualDistance = edgeWeightCalculator.calculateWeight(edge);

			Assertions.assertEquals(expectedDuration, actualDistance, 0.05);
		}
	}

	@Test
	void should_return_duration_in_minutes_between_vertices_with_max_possible_speed(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point[] points = new Point[]{
			factory.createPoint(121.058805, 14.552797),
			factory.createPoint(120.994260, 14.593999),
			factory.createPoint(96.591876, 77.870317),
			factory.createPoint(-4.815018, 21.719527),
			factory.createPoint(23.704799, -17.727830),
			factory.createPoint(-130.279576, 58.585396)
		};
		double[] expectedDistances = new double[]{3.6, 3_394.1, 6_427.9};

		for(int i = 0; i < expectedDistances.length; i ++){
			Point fromPoints = points[i << 1];
			Point toPoints = points[(i << 1) + 1];
			double expectedDistance = expectedDistances[i];
			GeodeticDurationCalculator edgeWeightCalculator = new GeodeticDurationCalculator();

			double actualDistance = edgeWeightCalculator.calculateWeight(
				new Node("0", fromPoints),
				new Node("1", toPoints));

			Assertions.assertEquals(expectedDistance, actualDistance, 0.05);
		}
	}

}
