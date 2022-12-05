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
import io.github.mtrevisan.mapmatcher.helpers.spatial.other.GrahamScan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


//https://github.com/locationtech/jts/blob/master/modules/core/src/main/java/org/locationtech/jts/algorithm/ConvexHull.java
//https://github.com/locationtech/jts/blob/master/modules/core/src/test/java/org/locationtech/jts/algorithm/ConvexHullTest.java
//https://github.com/bkiers/GrahamScan/blob/master/src/main/cg/GrahamScan.java#L188
class PolylineTest{

	@Test
	public void orientation(){
        /*
            9       |             d
            8       |               c
            7       |                 e
            6       |
            5       |
            4       |       b
            3       |
            2       |
            1   h   | a
            0     g '------------------
           -1       f
           -2
              -2 -1 0 1 2 3 4 5 6 7 8 9
        */
		Coordinate a = Coordinate.of(1, 1);
		Coordinate b = Coordinate.of(4, 4);
		Coordinate c = Coordinate.of(8, 8);
		Coordinate d = Coordinate.of(7, 9);
		Coordinate e = Coordinate.of(9, 7);
		Coordinate f = Coordinate.of(0, -1);
		Coordinate g = Coordinate.of(-1, 0);
		Coordinate h = Coordinate.of(-2, 1);

		Assertions.assertEquals(Polyline.PolarAngleComparator.COLLINEAR, Polyline.PolarAngleComparator.orientation(a, b, c));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COLLINEAR, Polyline.PolarAngleComparator.orientation(a, c, b));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COLLINEAR, Polyline.PolarAngleComparator.orientation(b, a, c));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COLLINEAR, Polyline.PolarAngleComparator.orientation(c, b, a));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COLLINEAR, Polyline.PolarAngleComparator.orientation(e, d, c));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COLLINEAR, Polyline.PolarAngleComparator.orientation(h, f, g));

		Assertions.assertEquals(-1, Polyline.PolarAngleComparator.polarCompare(a, b, c));
		Assertions.assertEquals(1, Polyline.PolarAngleComparator.polarCompare(a, c, b));
		Assertions.assertEquals(-1, Polyline.PolarAngleComparator.polarCompare(b, a, c));
		Assertions.assertEquals(1, Polyline.PolarAngleComparator.polarCompare(c, b, a));
		Assertions.assertEquals(1, Polyline.PolarAngleComparator.polarCompare(e, d, c));
		Assertions.assertEquals(-1, Polyline.PolarAngleComparator.polarCompare(h, f, g));


		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, b, e));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, b, f));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, c, e));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, c, f));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(c, b, g));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(d, b, f));

		Assertions.assertEquals(-1, Polyline.PolarAngleComparator.polarCompare(a, b, e));
		Assertions.assertEquals(-1, Polyline.PolarAngleComparator.polarCompare(a, b, f));
		Assertions.assertEquals(-1, Polyline.PolarAngleComparator.polarCompare(a, c, e));
		Assertions.assertEquals(-1, Polyline.PolarAngleComparator.polarCompare(a, c, f));
		Assertions.assertEquals(-1, Polyline.PolarAngleComparator.polarCompare(c, b, g));
		Assertions.assertEquals(-1, Polyline.PolarAngleComparator.polarCompare(d, b, f));


		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, b, d));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, e, d));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(e, c, f));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(b, d, a));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, g, f));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(f, b, a));

		Assertions.assertEquals(1, Polyline.PolarAngleComparator.polarCompare(a, b, d));
		Assertions.assertEquals(1, Polyline.PolarAngleComparator.polarCompare(a, e, d));
		Assertions.assertEquals(1, Polyline.PolarAngleComparator.polarCompare(e, c, f));
		Assertions.assertEquals(1, Polyline.PolarAngleComparator.polarCompare(b, d, a));
		Assertions.assertEquals(1, Polyline.PolarAngleComparator.polarCompare(a, g, f));
		Assertions.assertEquals(1, Polyline.PolarAngleComparator.polarCompare(f, b, a));
	}

	@Test
	public void all_collinear() {
        /*
            6 |       d   b
            5 |         f
            4 |   a   e
            3 |     c
            2 |
            1 |
            0 '------------
              0 1 2 3 4 5 6
        */
		Coordinate a = Coordinate.of(2, 4);
		Coordinate b = Coordinate.of(6, 6);
		Coordinate c = Coordinate.of(3, 3);
		Coordinate d = Coordinate.of(4, 6);
		Coordinate e = Coordinate.of(4, 4);
		Coordinate f = Coordinate.of(5, 5);

		Polyline polyline = Polyline.of(c);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(c);
		Assertions.assertEquals(expected, convexHull);


		polyline = Polyline.of(c, e);

		convexHull = polyline.getConvexHull();

		expected = Polyline.of(c, e);
		Assertions.assertEquals(expected, convexHull);


		polyline = Polyline.of(c, e, f);

		convexHull = polyline.getConvexHull();

		expected = Polyline.of(c, f);
		Assertions.assertEquals(expected, convexHull);


		polyline = Polyline.of(c, b, e, e, e, f, c);

		convexHull = polyline.getConvexHull();

		expected = Polyline.of(c, b);
		Assertions.assertEquals(expected, convexHull);


		polyline = Polyline.of(a, b, d);

		GrahamScan.getConvexHull(polyline);
		convexHull = polyline.getConvexHull();

		expected = Polyline.of(a, b, d, a);
		Assertions.assertEquals(expected, convexHull);
	}

//	@Test
//	public void getConvexHullTest() {
//
//        /*
//            6 |       d
//            5 |     b   g
//            4 |   a   e   i
//            3 |     c   h
//            2 |       f
//            1 |
//            0 '------------
//              0 1 2 3 4 5 6
//        */
//		Coordinate a = Coordinate.of(2, 4);
//		Coordinate b = Coordinate.of(3, 5);
//		Coordinate c = Coordinate.of(3, 3);
//		Coordinate d = Coordinate.of(4, 6);
//		Coordinate e = Coordinate.of(4, 4);
//		Coordinate f = Coordinate.of(4, 2);
//		Coordinate g = Coordinate.of(5, 5);
//		Coordinate h = Coordinate.of(5, 3);
//		Coordinate i = Coordinate.of(6, 4);
//
//		List<Coordinate> convexHull = GrahamScan.getConvexHull(Arrays.asList(a, b, c, d, e, f, g, h, i));
//
//		Assertions.assertEquals(convexHull.size(), is(5));
//
//		Assertions.assertEquals(convexHull.get(0), is(f));
//		Assertions.assertEquals(convexHull.get(1), is(i));
//		Assertions.assertEquals(convexHull.get(2), is(d));
//		Assertions.assertEquals(convexHull.get(3), is(a));
//		Assertions.assertEquals(convexHull.get(4), is(f));
//
//        /*
//            6       |       d   m
//            5       |     b   g
//            4       |   a   e   i
//            3 j     |     c   h
//            2       |       f
//            1       |
//            0       '------------
//           -1
//           -2                   k
//           -3
//              -2 -1 0 1 2 3 4 5 6
//        */
//		Coordinate j = Coordinate.of(-2, 3);
//		Coordinate k = Coordinate.of(6, -2);
//		Coordinate m = Coordinate.of(6, 6);
//
//		convexHull = GrahamScan.getConvexHull(Arrays.asList(a, b, c, d, e, f, g, h, i, j, k, m));
//
//		Assertions.assertEquals(convexHull.size(), is(5));
//
//		Assertions.assertEquals(convexHull.get(0), is(k));
//		Assertions.assertEquals(convexHull.get(1), is(m));
//		Assertions.assertEquals(convexHull.get(2), is(d));
//		Assertions.assertEquals(convexHull.get(3), is(j));
//		Assertions.assertEquals(convexHull.get(4), is(k));
//
//        /*
//            large   |                         m
//            .       |
//            .       |
//            7  j    |
//            6       |       d
//            5       |     b   g
//            4       |   a   e   i
//            3       |     c   h
//            2       |       f
//            1       |
//            0       '--------------------------
//           -1
//           -2                       k
//           -3
//              -2 -1 0 1 2 3 4 5 6 7 8 . . large
//        */
//		j = Coordinate.of(-2, 7);
//		k = Coordinate.of(8, -2);
//		m = Coordinate.of(Integer.MAX_VALUE, Integer.MAX_VALUE);
//
//		convexHull = GrahamScan.getConvexHull(Arrays.asList(a, b, c, d, e, f, g, h, i, j, k, m));
//
//		Assertions.assertEquals(convexHull.size(), is(4));
//
//		Assertions.assertEquals(convexHull.get(0), is(k));
//		Assertions.assertEquals(convexHull.get(1), is(m));
//		Assertions.assertEquals(convexHull.get(2), is(j));
//		Assertions.assertEquals(convexHull.get(3), is(k));
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void getConvexHullTestFail() {
//        /*
//            6 |
//            5 |
//            4 |   a
//            3 |     c
//            2 |       f
//            1 |
//            0 '------------
//              0 1 2 3 4 5 6
//        */
//		Coordinate a = Coordinate.of(2, 4);
//		Coordinate b = Coordinate.of(3, 3);
//		Coordinate c = Coordinate.of(4, 2);
//
//		GrahamScan.getConvexHull(Arrays.asList(a, b, c));
//	}
//
//	@Test
//	public void getLowestCoordinateTest() {
//
//        /*
//            6    |       d
//            5    |     b   g
//            4    |   a   e   i
//            3    |     c   h
//            2    |       f
//            1    |
//            0    '------------
//           -1
//           -2
//              -1 0 1 2 3 4 5 6
//        */
//		Coordinate a = Coordinate.of(2, 4);
//		Coordinate b = Coordinate.of(3, 5);
//		Coordinate c = Coordinate.of(3, 3);
//		Coordinate d = Coordinate.of(4, 6);
//		Coordinate e = Coordinate.of(4, 4);
//		Coordinate f = Coordinate.of(4, 2);
//		Coordinate g = Coordinate.of(5, 5);
//		Coordinate h = Coordinate.of(5, 3);
//		Coordinate i = Coordinate.of(6, 4);
//
//		Coordinate lowest = GrahamScan.getLowestCoordinate(Arrays.asList(a, b, c, d, e, f, g, h, i));
//
//		Assertions.assertEquals(lowest, is(f));
//
//        /*
//            6    |       d
//            5    |     b   g
//            4    |   a   e   i
//            3    |     c   h
//            2    |       f
//            1    |
//            0    '------------
//           -1  j             k
//           -2
//              -1 0 1 2 3 4 5 6
//        */
//		Coordinate j = Coordinate.of(-1, -1);
//		Coordinate k = Coordinate.of(6, -1);
//
//		lowest = GrahamScan.getLowestCoordinate(Arrays.asList(a, b, c, d, e, f, g, h, i, j, k));
//
//		Assertions.assertEquals(lowest, is(j));
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void getCoordinatesTestFail1() {
//		GrahamScan.getConvexHull(new int[]{1, 2, 3, 4, 5}, new int[]{1, 2, 3, 4});
//	}
//
//	@Test
//	public void getSortedCoordinateSetTest() {
//
//        /*
//            6    |
//            5    |
//            4    |
//            3    |
//            2    g   a
//            1    f b
//            0    c-e-d--------
//           -1
//           -2
//              -1 0 1 2 3 4 5 6
//        */
//		Coordinate a = Coordinate.of(2, 2);
//		Coordinate b = Coordinate.of(1, 1);
//		Coordinate c = Coordinate.of(0, 0);
//		Coordinate d = Coordinate.of(2, 0);
//		Coordinate e = Coordinate.of(1, 0);
//		Coordinate f = Coordinate.of(0, 1);
//		Coordinate g = Coordinate.of(0, 2);
//		Coordinate h = Coordinate.of(2, 2); // duplicate
//		Coordinate i = Coordinate.of(2, 2); // duplicate
//
//		List<Coordinate> Coordinates = Arrays.asList(a, b, c, d, e, f, g, h, i);
//
//		Set<Coordinate> set = GrahamScan.getSortedCoordinateSet(Coordinates);
//		Coordinate[] array = set.toArray(Coordinate.of[set.size()]);
//
//		Assertions.assertEquals(set.size(), is(7));
//
//		Assertions.assertEquals(array[0], is(c));
//		Assertions.assertEquals(array[1], is(e));
//		Assertions.assertEquals(array[2], is(d));
//		Assertions.assertEquals(array[3], is(b));
//		Assertions.assertEquals(array[4], is(a));
//		Assertions.assertEquals(array[5], is(f));
//		Assertions.assertEquals(array[6], is(g));
//	}


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


//	@Test
//	void convex_hull_identical_Coordinates(){
//		Coordinate Coordinate = Coordinate.of(12.238140517207398, 45.65897415921759);
//		Polyline polyline = Polyline.of(Coordinate, Coordinate, Coordinate, Coordinate, Coordinate, Coordinate, Coordinate, Coordinate);
//
//		Polyline convexHull = polyline.getConvexHull();
//
//		Polyline expected = Polyline.of(Coordinate);
//		Assertions.assertEquals(expected, convexHull);
//	}

	@Test
	void line_collinear_equal23(){
		Coordinate Coordinate1 = Coordinate.of(30, 220);
		Coordinate Coordinate2 = Coordinate.of(240, 220);
		Coordinate Coordinate3 = Coordinate.of(240, 220);
		Polyline polyline = Polyline.of(Coordinate1, Coordinate2, Coordinate3);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(Coordinate1, Coordinate2);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void line_collinear_equal123_456(){
		Coordinate Coordinate1 = Coordinate.of(130, 240);
		Coordinate Coordinate2 = Coordinate.of(130, 240);
		Coordinate Coordinate3 = Coordinate.of(130, 240);
		Coordinate Coordinate4 = Coordinate.of(570, 240);
		Coordinate Coordinate5 = Coordinate.of(570, 240);
		Coordinate Coordinate6 = Coordinate.of(570, 240);
		Coordinate Coordinate7 = Coordinate.of(650, 240);
		Polyline polyline = Polyline.of(Coordinate1, Coordinate2, Coordinate3, Coordinate4, Coordinate5, Coordinate6, Coordinate7);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(Coordinate1, Coordinate7);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_collinear_equal12(){
		Coordinate Coordinate1 = Coordinate.of(0, 0);
		Coordinate Coordinate2 = Coordinate.of(0, 0);
		Coordinate Coordinate3 = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(Coordinate1, Coordinate2, Coordinate3);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(Coordinate1, Coordinate3);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_Coordinate_collinear_equal23(){
		Coordinate Coordinate1 = Coordinate.of(0, 0);
		Coordinate Coordinate2 = Coordinate.of(10, 0);
		Coordinate Coordinate3 = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(Coordinate1, Coordinate2, Coordinate3);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(Coordinate1, Coordinate3);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_Coordinate_collinear_equal_none(){
		Coordinate Coordinate1 = Coordinate.of(0, 0);
		Coordinate Coordinate2 = Coordinate.of(5, 0);
		Coordinate Coordinate3 = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(Coordinate1, Coordinate2, Coordinate3);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(Coordinate1, Coordinate3);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_Coordinate(){
		Coordinate Coordinate1 = Coordinate.of(0, 0);
		Coordinate Coordinate2 = Coordinate.of(5, 1);
		Coordinate Coordinate3 = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(Coordinate1, Coordinate2, Coordinate3);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(Coordinate1, Coordinate2, Coordinate3);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_Coordinate_linear(){
		Coordinate Coordinate1 = Coordinate.of(0, 0);
		Coordinate Coordinate2 = Coordinate.of(0, 0);
		Coordinate Coordinate3 = Coordinate.of(5, 0);
		Coordinate Coordinate4 = Coordinate.of(5, 0);
		Coordinate Coordinate5 = Coordinate.of(10, 0);
		Coordinate Coordinate6 = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(Coordinate1, Coordinate2, Coordinate3, Coordinate4, Coordinate5, Coordinate6);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(Coordinate1, Coordinate6);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void collinear_Coordinates(){
		Coordinate Coordinate1 = Coordinate.of(-0.2, -0.1);
		Coordinate Coordinate2 = Coordinate.of(0., -0.1);
		Coordinate Coordinate3 = Coordinate.of(0.2, -0.1);
		Coordinate Coordinate4 = Coordinate.of(0., -0.1);
		Coordinate Coordinate5 = Coordinate.of(-0.2, 0.1);
		Coordinate Coordinate6 = Coordinate.of(0., 0.1);
		Coordinate Coordinate7 = Coordinate.of(0.2, 0.1);
		Coordinate Coordinate8 = Coordinate.of(0., 0.1);
		Polyline polyline = Polyline.of(Coordinate1, Coordinate2, Coordinate3, Coordinate4, Coordinate5, Coordinate6, Coordinate7, Coordinate8);

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
		Coordinate Coordinate1 = Coordinate.of(0, 0);
		Coordinate Coordinate2 = Coordinate.of(5, 0);
		Coordinate Coordinate3 = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(Coordinate1, Coordinate2, Coordinate3);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(Coordinate1, Coordinate3);
		Assertions.assertEquals(expected, convexHull);
	}

	/**
	 * Tests robustness issue in radial sort.
	 *
	 * @see <a href="https://github.com/libgeos/geos/issues/722">Issue 722</a>
	 */
	@Test
	void collinear_Coordinates_tiny_x(){
		Coordinate Coordinate1 = Coordinate.of(-0.2, -0.1);
		Coordinate Coordinate2 = Coordinate.of(1.38777878e-17, -0.1);
		Coordinate Coordinate3 = Coordinate.of(0.2, -0.1);
		Coordinate Coordinate4 = Coordinate.of(-1.38777878e-17, -0.1);
		Coordinate Coordinate5 = Coordinate.of(-0.2, 0.1);
		Coordinate Coordinate6 = Coordinate.of(1.38777878e-17, 0.1);
		Coordinate Coordinate7 = Coordinate.of(0.2, 0.1);
		Coordinate Coordinate8 = Coordinate.of(-1.38777878e-17, 0.1);
		Polyline polyline = Polyline.of(Coordinate1, Coordinate2, Coordinate3, Coordinate4, Coordinate5, Coordinate6, Coordinate7, Coordinate8);

		Polyline convexHull = polyline.getConvexHull();

		Assertions.assertEquals(Polyline.of(Coordinate.of(-0.2, -0.1), Coordinate.of(-0.2, 0.1), Coordinate.of(-0.2, 0.1),
			Coordinate.of(0.2, 0.1), Coordinate.of(0.2, -0.1), Coordinate.of(-0.2, -0.1)), convexHull);
	}

	@Test
	void collinear_Coordinates_less_tiny_x(){
		Coordinate Coordinate1 = Coordinate.of(-0.2, -0.1);
		Coordinate Coordinate2 = Coordinate.of(1.38777878e-7, -0.1);
		Coordinate Coordinate3 = Coordinate.of(0.2, -0.1);
		Coordinate Coordinate4 = Coordinate.of(-1.38777878e-7, -0.1);
		Coordinate Coordinate5 = Coordinate.of(-0.2, 0.1);
		Coordinate Coordinate6 = Coordinate.of(1.38777878e-7, 0.1);
		Coordinate Coordinate7 = Coordinate.of(0.2, 0.1);
		Coordinate Coordinate8 = Coordinate.of(-1.38777878e-7, 0.1);
		Polyline polyline = Polyline.of(Coordinate1, Coordinate2, Coordinate3, Coordinate4, Coordinate5, Coordinate6, Coordinate7, Coordinate8);

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
		Coordinate Coordinate1 = Coordinate.of(140, 350);
		Coordinate Coordinate2 = Coordinate.of(510, 140);
		Coordinate Coordinate3 = Coordinate.of(110, 140);
		Coordinate Coordinate4 = Coordinate.of(250, 290);
		Coordinate Coordinate5 = Coordinate.of(250, 50);
		Coordinate Coordinate6 = Coordinate.of(300, 370);
		Coordinate Coordinate7 = Coordinate.of(450, 310);
		Coordinate Coordinate8 = Coordinate.of(440, 160);
		Coordinate Coordinate9 = Coordinate.of(290, 280);
		Coordinate Coordinate10 = Coordinate.of(220, 160);
		Coordinate Coordinate11 = Coordinate.of(100, 260);
		Coordinate Coordinate12 = Coordinate.of(320, 230);
		Coordinate Coordinate13 = Coordinate.of(200, 280);
		Coordinate Coordinate14 = Coordinate.of(360, 130);
		Coordinate Coordinate15 = Coordinate.of(330, 210);
		Coordinate Coordinate16 = Coordinate.of(380, 80);
		Coordinate Coordinate17 = Coordinate.of(220, 210);
		Coordinate Coordinate18 = Coordinate.of(380, 310);
		Coordinate Coordinate19 = Coordinate.of(260, 150);
		Coordinate Coordinate20 = Coordinate.of(260, 110);
		Coordinate Coordinate21 = Coordinate.of(170, 130);
		Polyline polyline = Polyline.of(Coordinate1, Coordinate2, Coordinate3, Coordinate4, Coordinate5, Coordinate6, Coordinate7, Coordinate8, Coordinate9, Coordinate10, Coordinate11, Coordinate12,
			Coordinate13, Coordinate14, Coordinate15, Coordinate16, Coordinate17, Coordinate18, Coordinate19, Coordinate20, Coordinate21);

		Polyline convexHull = polyline.getConvexHull();

		Assertions.assertEquals(Polyline.of(Coordinate.of(100, 260), Coordinate.of(140, 350), Coordinate.of(300, 370),
			Coordinate.of(450, 310), Coordinate.of(510, 140), Coordinate.of(380, 80), Coordinate.of(250, 50),
			Coordinate.of(110, 140), Coordinate.of(100, 260)), convexHull);
	}

	/*
	  * Coordinate p0 = Coordinate.of(219.3649559090992, 140.84159161824724);
     * Coordinate p1 = Coordinate.of(168.9018919682399, -5.713787599646864);
     *
     * Coordinate p = Coordinate.of(186.80814046338352, 46.28973405831556); int
     * orient = orientationIndex(p0, p1, p); int orientInv =
     * orientationIndex(p1, p0, p);
     */

}
