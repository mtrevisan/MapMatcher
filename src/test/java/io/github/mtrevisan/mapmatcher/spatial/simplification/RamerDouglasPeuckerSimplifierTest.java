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
package io.github.mtrevisan.mapmatcher.spatial.simplification;

import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.topologies.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class RamerDouglasPeuckerSimplifierTest{

	@Test
	void should_simplify_polyline(){
		final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point node12_31_41 = factory.createPoint(12.238140517207398, 45.65897415921759);
		Point node22 = factory.createPoint(12.242949896905884, 45.69828882177029);
		Point node23 = factory.createPoint(12.200627355552967, 45.732876303059044);

		RamerDouglasPeuckerSimplifier simplifier = new RamerDouglasPeuckerSimplifier();
		simplifier.setDistanceTolerance(2_000.);
		Polyline polyline = factory.createPolyline(simplifier.simplify(node12_31_41, node22, node23));

		Assertions.assertEquals(2, polyline.size());
	}

	@Test
	void should_not_simplify_polyline(){
		final GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point node12_31_41 = factory.createPoint(12.238140517207398, 45.65897415921759);
		Point node22 = factory.createPoint(12.242949896905884, 45.69828882177029);
		Point node23 = factory.createPoint(12.200627355552967, 45.732876303059044);

		RamerDouglasPeuckerSimplifier simplifier = new RamerDouglasPeuckerSimplifier();
		simplifier.setDistanceTolerance(10.);
		Polyline polyline = factory.createPolyline(simplifier.simplify(node12_31_41, node22, node23));

		Assertions.assertEquals(3, polyline.size());
	}

	@Test
	void simple(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point[] points = new Point[]{
			factory.createPoint(1., 1.),
			factory.createPoint(1.3, 2.),
			factory.createPoint(2., 1.2),
			factory.createPoint(3., 1.)
		};

		RamerDouglasPeuckerSimplifier simplifier = new RamerDouglasPeuckerSimplifier();
		simplifier.setDistanceTolerance(0.5);

		Point[] reducedPoints = simplifier.simplify(points);

		Point[] expected = new Point[]{
			factory.createPoint(1., 1.),
			factory.createPoint(1.3, 2.),
			factory.createPoint(3., 1.)
		};
		Assertions.assertArrayEquals(expected, reducedPoints);
	}

}
