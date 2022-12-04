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
package io.github.mtrevisan.mapmatcher.helpers.spatial;

import io.github.mtrevisan.mapmatcher.distances.DistanceCalculator;
import io.github.mtrevisan.mapmatcher.distances.GeodeticCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


//https://github.com/locationtech/jts/blob/master/modules/core/src/main/java/org/locationtech/jts/algorithm/ConvexHull.java
//https://github.com/locationtech/jts/blob/master/modules/core/src/test/java/org/locationtech/jts/algorithm/ConvexHullTest.java
//https://github.com/bkiers/GrahamScan/blob/master/src/main/cg/GrahamScan.java#L188
class PolylineTest{

	@Test
	void should_simplify_polyline(){
		Coordinate node12_31_41 = Coordinate.of(12.238140517207398, 45.65897415921759);
		Coordinate node22 = Coordinate.of(12.242949896905884, 45.69828882177029);
		Coordinate node23 = Coordinate.of(12.200627355552967, 45.732876303059044);
		DistanceCalculator distanceCalculator = new GeodeticCalculator();

		Polyline polyline = Polyline.ofSimplified(distanceCalculator, 2_000., node12_31_41, node22, node23);

		Assertions.assertEquals(2, polyline.size());
	}

	@Test
	void should_not_simplify_polyline(){
		Coordinate node12_31_41 = Coordinate.of(12.238140517207398, 45.65897415921759);
		Coordinate node22 = Coordinate.of(12.242949896905884, 45.69828882177029);
		Coordinate node23 = Coordinate.of(12.200627355552967, 45.732876303059044);
		DistanceCalculator distanceCalculator = new GeodeticCalculator();

		Polyline polyline = Polyline.ofSimplified(distanceCalculator, 10., node12_31_41, node22, node23);

		Assertions.assertEquals(3, polyline.size());
	}


	@Test
	void convex_hull_identical_points(){
		Coordinate point = Coordinate.of(12.238140517207398, 45.65897415921759);
		Polyline polyline = Polyline.of(point, point, point, point, point, point, point, point);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(point);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void line_collinear_equal23(){
		Coordinate point1 = Coordinate.of(30, 220);
		Coordinate point2 = Coordinate.of(240, 220);
		Coordinate point3 = Coordinate.of(240, 220);
		Polyline polyline = Polyline.of(point1, point2, point3);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(point1, point2);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void line_collinear_equal123_456(){
		Coordinate point1 = Coordinate.of(130, 240);
		Coordinate point2 = Coordinate.of(130, 240);
		Coordinate point3 = Coordinate.of(130, 240);
		Coordinate point4 = Coordinate.of(570, 240);
		Coordinate point5 = Coordinate.of(570, 240);
		Coordinate point6 = Coordinate.of(570, 240);
		Coordinate point7 = Coordinate.of(650, 240);
		Polyline polyline = Polyline.of(point1, point2, point3, point4, point5, point6, point7);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(point7, point1);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_collinear_equal12(){
		Coordinate point1 = Coordinate.of(0, 0);
		Coordinate point2 = Coordinate.of(0, 0);
		Coordinate point3 = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(point1, point2, point3);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(point1, point3);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_point_collinear_equal23(){
		Coordinate point1 = Coordinate.of(0, 0);
		Coordinate point2 = Coordinate.of(10, 0);
		Coordinate point3 = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(point1, point2, point3);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(point1, point3);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_point_collinear_equal_none(){
		Coordinate point1 = Coordinate.of(0, 0);
		Coordinate point2 = Coordinate.of(5, 0);
		Coordinate point3 = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(point1, point2, point3);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(point3, point1);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_point(){
		Coordinate point1 = Coordinate.of(0, 0);
		Coordinate point2 = Coordinate.of(5, 1);
		Coordinate point3 = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(point1, point2, point3);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(point1, point2, point3);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_point_linear(){
		Coordinate point1 = Coordinate.of(0, 0);
		Coordinate point2 = Coordinate.of(0, 0);
		Coordinate point3 = Coordinate.of(5, 0);
		Coordinate point4 = Coordinate.of(5, 0);
		Coordinate point5 = Coordinate.of(10, 0);
		Coordinate point6 = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(point1, point2, point3, point4, point5, point6);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(point6, point1);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void collinear_points(){
		Coordinate point1 = Coordinate.of(-0.2, -0.1);
		Coordinate point2 = Coordinate.of(0., -0.1);
		Coordinate point3 = Coordinate.of(0.2, -0.1);
		Coordinate point4 = Coordinate.of(0., -0.1);
		Coordinate point5 = Coordinate.of(-0.2, 0.1);
		Coordinate point6 = Coordinate.of(0., 0.1);
		Coordinate point7 = Coordinate.of(0.2, 0.1);
		Coordinate point8 = Coordinate.of(0., 0.1);
		Polyline polyline = Polyline.of(point1, point2, point3, point4, point5, point6, point7, point8);

		Polyline convexHull = polyline.getConvexHull();

		Assertions.assertEquals(Polyline.of(Coordinate.of(-0.2, -0.1), Coordinate.of(-0.2, 0.1), Coordinate.of(0.2, 0.1),
			Coordinate.of(0.2, -0.1), Coordinate.of(-0.2, -0.1)), convexHull);
	}

	/**
	 * @see <a href="https://trac.osgeo.org/geos/ticket/850">Ticket 850</a>
	 */
	@Test
	void geos_850(){
//		checkConvexHull("01040000001100000001010000002bd3a24002bcb0417ff59d2051e25c4101010000003aebcec70a8b3cbfdb123fe713a2e8be0101000000afa0bb8638b770bf7fc1d77d0dda1cbf01010000009519cb944ce070bf1a46cd7df4201dbf010100000079444b4cd1937cbfa6ca29ada6a928bf010100000083323f09e16c7cbfd36d07ee0b8828bf01010000009081b8f066967ebf915fbc9ebe652abf0101000000134cf280633bc1bf37b754972dbe6dbf0101000000ea992c094df585bf1bbabc8a42f332bf0101000000c0a13c7fb31186bf9af7b10cc50b33bf0101000000a0bba15a0a7188bf8fba7870e91735bf01010000000fc8701903db93bf93bdbe93b52241bf01010000007701a73b29cc90bfb770bc3732fe3cbf010100000036fa45b75b8b8cbf1cfca5bf59a238bf0101000000a54e773f7f287ebf910d4621e5062abf01010000004b5b5dc4196f55bfa51f0579717f02bf01010000007e549489513a5fbfa57bacea34f30abf", "POLYGON ((-0.1346248988744213 -0.0036307230426677, -0.0019059940589774 -0.0000514030956167, 280756800.63603467 7571780.50964105, -0.1346248988744213 -0.0036307230426677))", 0.000000000001);
		Coordinate point1 = Coordinate.of(0, 0);
		Coordinate point2 = Coordinate.of(5, 0);
		Coordinate point3 = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(point1, point2, point3);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(point3, point1);
		Assertions.assertEquals(expected, convexHull);
	}

	/**
	 * Tests robustness issue in radial sort.
	 *
	 * @see <a href="https://github.com/libgeos/geos/issues/722">Issue 722</a>
	 */
	@Test
	void collinear_points_tiny_x(){
		Coordinate point1 = Coordinate.of(-0.2, -0.1);
		Coordinate point2 = Coordinate.of(1.38777878e-17, -0.1);
		Coordinate point3 = Coordinate.of(0.2, -0.1);
		Coordinate point4 = Coordinate.of(-1.38777878e-17, -0.1);
		Coordinate point5 = Coordinate.of(-0.2, 0.1);
		Coordinate point6 = Coordinate.of(1.38777878e-17, 0.1);
		Coordinate point7 = Coordinate.of(0.2, 0.1);
		Coordinate point8 = Coordinate.of(-1.38777878e-17, 0.1);
		Polyline polyline = Polyline.of(point1, point2, point3, point4, point5, point6, point7, point8);

		Polyline convexHull = polyline.getConvexHull();

		Assertions.assertEquals(Polyline.of(Coordinate.of(-0.2, -0.1), Coordinate.of(-0.2, 0.1), Coordinate.of(-0.2, 0.1),
			Coordinate.of(0.2, 0.1), Coordinate.of(0.2, -0.1), Coordinate.of(-0.2, -0.1)), convexHull);
	}

	@Test
	void collinear_points_less_tiny_x(){
		Coordinate point1 = Coordinate.of(-0.2, -0.1);
		Coordinate point2 = Coordinate.of(1.38777878e-7, -0.1);
		Coordinate point3 = Coordinate.of(0.2, -0.1);
		Coordinate point4 = Coordinate.of(-1.38777878e-7, -0.1);
		Coordinate point5 = Coordinate.of(-0.2, 0.1);
		Coordinate point6 = Coordinate.of(1.38777878e-7, 0.1);
		Coordinate point7 = Coordinate.of(0.2, 0.1);
		Coordinate point8 = Coordinate.of(-1.38777878e-7, 0.1);
		Polyline polyline = Polyline.of(point1, point2, point3, point4, point5, point6, point7, point8);

		Polyline convexHull = polyline.getConvexHull();

		Assertions.assertEquals(Polyline.of(Coordinate.of(-0.2, -0.1), Coordinate.of(-0.2, 0.1), Coordinate.of(0.2, 0.1),
			Coordinate.of(0.2, -0.1), Coordinate.of(-0.2, -0.1)), convexHull);
	}

	/**
	 * Test case fails in GEOS due to incorrect fix to radial sorting.
	 * <p>
	 * This did not trigger a failure in JTS, probably because the sorting is less strict.
	 * </p>
	 */
	@Test
	void geos_sort_failure(){
		Coordinate point1 = Coordinate.of(140, 350);
		Coordinate point2 = Coordinate.of(510, 140);
		Coordinate point3 = Coordinate.of(110, 140);
		Coordinate point4 = Coordinate.of(250, 290);
		Coordinate point5 = Coordinate.of(250, 50);
		Coordinate point6 = Coordinate.of(300, 370);
		Coordinate point7 = Coordinate.of(450, 310);
		Coordinate point8 = Coordinate.of(440, 160);
		Coordinate point9 = Coordinate.of(290, 280);
		Coordinate point10 = Coordinate.of(220, 160);
		Coordinate point11 = Coordinate.of(100, 260);
		Coordinate point12 = Coordinate.of(320, 230);
		Coordinate point13 = Coordinate.of(200, 280);
		Coordinate point14 = Coordinate.of(360, 130);
		Coordinate point15 = Coordinate.of(330, 210);
		Coordinate point16 = Coordinate.of(380, 80);
		Coordinate point17 = Coordinate.of(220, 210);
		Coordinate point18 = Coordinate.of(380, 310);
		Coordinate point19 = Coordinate.of(260, 150);
		Coordinate point20 = Coordinate.of(260, 110);
		Coordinate point21 = Coordinate.of(170, 130);
		Polyline polyline = Polyline.of(point1, point2, point3, point4, point5, point6, point7, point8, point9, point10, point11, point12,
			point13, point14, point15, point16, point17, point18, point19, point20, point21);

		Polyline convexHull = polyline.getConvexHull();

		Assertions.assertEquals(Polyline.of(Coordinate.of(100, 260), Coordinate.of(140, 350), Coordinate.of(300, 370),
			Coordinate.of(450, 310), Coordinate.of(510, 140), Coordinate.of(380, 80), Coordinate.of(250, 50),
			Coordinate.of(110, 140), Coordinate.of(100, 260)), convexHull);
	}

	/*
	  * Coordinate p0 = new Coordinate(219.3649559090992, 140.84159161824724);
     * Coordinate p1 = new Coordinate(168.9018919682399, -5.713787599646864);
     *
     * Coordinate p = new Coordinate(186.80814046338352, 46.28973405831556); int
     * orient = orientationIndex(p0, p1, p); int orientInv =
     * orientationIndex(p1, p0, p);
     */

}
