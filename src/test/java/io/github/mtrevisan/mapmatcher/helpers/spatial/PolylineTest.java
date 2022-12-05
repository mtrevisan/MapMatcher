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
	public void lowest_coordinate1(){
        /*
            6    |       d
            5    |     b   g
            4    |   a   e   i
            3    |     c   h
            2    |       f
            1    |
            0    '------------
           -1
           -2
              -1 0 1 2 3 4 5 6
        */
		Coordinate a = Coordinate.of(2, 4);
		Coordinate b = Coordinate.of(3, 5);
		Coordinate c = Coordinate.of(3, 3);
		Coordinate d = Coordinate.of(4, 6);
		Coordinate e = Coordinate.of(4, 4);
		Coordinate f = Coordinate.of(4, 2);
		Coordinate g = Coordinate.of(5, 5);
		Coordinate h = Coordinate.of(5, 3);
		Coordinate i = Coordinate.of(6, 4);

		int lowestIndex = Polyline.getLowestPoint(new Coordinate[]{a, b, c, d, e, f, g, h, i});

		Assertions.assertEquals(5, lowestIndex);
	}

	@Test
	public void lowest_coordinate2(){
        /*
            6    |       d
            5    |     b   g
            4    |   a   e   i
            3    |     c   h
            2    |       f
            1    |
            0    '------------
           -1  j             k
           -2
              -1 0 1 2 3 4 5 6
        */
		Coordinate a = Coordinate.of(2, 4);
		Coordinate b = Coordinate.of(3, 5);
		Coordinate c = Coordinate.of(3, 3);
		Coordinate d = Coordinate.of(4, 6);
		Coordinate e = Coordinate.of(4, 4);
		Coordinate f = Coordinate.of(4, 2);
		Coordinate g = Coordinate.of(5, 5);
		Coordinate h = Coordinate.of(5, 3);
		Coordinate i = Coordinate.of(6, 4);
		Coordinate j = Coordinate.of(-1, -1);
		Coordinate k = Coordinate.of(6, -1);

		int lowestIndex = Polyline.getLowestPoint(new Coordinate[]{a, b, c, d, e, f, g, h, i, j, k});

		Assertions.assertEquals(9, lowestIndex);
	}


	@Test
	public void sorted_coordinate_set(){
        /*
            6    |
            5    |
            4    |
            3    |
            2    g   a
            1    f b
            0    c-e-d--------
           -1
           -2
              -1 0 1 2 3 4 5 6
        */
		Coordinate a = Coordinate.of(2, 2);
		Coordinate b = Coordinate.of(1, 1);
		Coordinate c = Coordinate.of(0, 0);
		Coordinate d = Coordinate.of(2, 0);
		Coordinate e = Coordinate.of(1, 0);
		Coordinate f = Coordinate.of(0, 1);
		Coordinate g = Coordinate.of(0, 2);

		Coordinate[] coordinates = {a, b, c, d, e, f, g};
		Polyline.polarSort(coordinates);

		Assertions.assertEquals(7, coordinates.length);
		Assertions.assertEquals(c, coordinates[0]);
		Assertions.assertEquals(e, coordinates[1]);
		Assertions.assertEquals(d, coordinates[2]);
		Assertions.assertEquals(b, coordinates[3]);
		Assertions.assertEquals(a, coordinates[4]);
		Assertions.assertEquals(f, coordinates[5]);
		Assertions.assertEquals(g, coordinates[6]);
	}


	@Test
	public void orientation1(){
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

		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(a, b, c));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(a, c, b));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(b, a, c));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(c, b, a));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(e, d, c));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(h, f, g));


		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, b, e));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, b, f));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, c, e));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, c, f));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(c, b, g));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(d, b, f));

		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(a, b, e));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(a, b, f));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(a, c, e));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(a, c, f));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(c, b, g));
		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(d, b, f));


		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, b, d));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, e, d));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(e, c, f));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(b, d, a));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, g, f));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(f, b, a));

		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(a, b, d));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(a, e, d));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(e, c, f));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(b, d, a));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(a, g, f));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.polarCompare(f, b, a));
	}

	@Test
	public void orientation2(){
		Coordinate a = Coordinate.of(219.3649559090992, 140.84159161824724);
		Coordinate b = Coordinate.of(168.9018919682399, -5.713787599646864);
		Coordinate c = Coordinate.of(186.80814046338352, 46.28973405831556);

		Assertions.assertEquals(Polyline.PolarAngleComparator.CLOCKWISE, Polyline.PolarAngleComparator.orientation(a, b, c));
		Assertions.assertEquals(Polyline.PolarAngleComparator.COUNTER_CLOCKWISE, Polyline.PolarAngleComparator.orientation(b, a, c));
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

		convexHull = polyline.getConvexHull();

		expected = Polyline.of(a, b, d, a);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	public void test1() {
        /*
            6 |       d
            5 |     b   g
            4 |   a   e   i
            3 |     c   h
            2 |       f
            1 |
            0 '------------
              0 1 2 3 4 5 6
        */
		Coordinate a = Coordinate.of(2, 4);
		Coordinate b = Coordinate.of(3, 5);
		Coordinate c = Coordinate.of(3, 3);
		Coordinate d = Coordinate.of(4, 6);
		Coordinate e = Coordinate.of(4, 4);
		Coordinate f = Coordinate.of(4, 2);
		Coordinate g = Coordinate.of(5, 5);
		Coordinate h = Coordinate.of(5, 3);
		Coordinate i = Coordinate.of(6, 4);

		Polyline polyline = Polyline.of(a, b, c, d, e, f, g, h, i);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(f, i, d, a, f);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	public void test2(){
        /*
            6       |       d   m
            5       |     b   g
            4       |   a   e   i
            3 j     |     c   h
            2       |       f
            1       |
            0       '------------
           -1
           -2                   k
           -3
              -2 -1 0 1 2 3 4 5 6
        */
		Coordinate a = Coordinate.of(2, 4);
		Coordinate b = Coordinate.of(3, 5);
		Coordinate c = Coordinate.of(3, 3);
		Coordinate d = Coordinate.of(4, 6);
		Coordinate e = Coordinate.of(4, 4);
		Coordinate f = Coordinate.of(4, 2);
		Coordinate g = Coordinate.of(5, 5);
		Coordinate h = Coordinate.of(5, 3);
		Coordinate i = Coordinate.of(6, 4);
		Coordinate j = Coordinate.of(-2, 3);
		Coordinate k = Coordinate.of(6, -2);
		Coordinate m = Coordinate.of(6, 6);

		Polyline polyline = Polyline.of(a, b, c, d, e, f, g, h, i, j, k, m);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(k, m, d, j, k);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	public void test3(){
        /*
            large   |                         m
            .       |
            .       |
            7  j    |
            6       |       d
            5       |     b   g
            4       |   a   e   i
            3       |     c   h
            2       |       f
            1       |
            0       '--------------------------
           -1
           -2                       k
           -3
              -2 -1 0 1 2 3 4 5 6 7 8 . . large
        */
		Coordinate a = Coordinate.of(2, 4);
		Coordinate b = Coordinate.of(3, 5);
		Coordinate c = Coordinate.of(3, 3);
		Coordinate d = Coordinate.of(4, 6);
		Coordinate e = Coordinate.of(4, 4);
		Coordinate f = Coordinate.of(4, 2);
		Coordinate g = Coordinate.of(5, 5);
		Coordinate h = Coordinate.of(5, 3);
		Coordinate i = Coordinate.of(6, 4);
		Coordinate j = Coordinate.of(-2, 7);
		Coordinate k = Coordinate.of(8, -2);
		Coordinate m = Coordinate.of(Integer.MAX_VALUE, Integer.MAX_VALUE);

		Polyline polyline = Polyline.of(a, b, c, d, e, f, g, h, i, j, k, m);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(k, m, j, k);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	public void collinear(){
        /*
            6 |
            5 |
            4 |   a
            3 |     c
            2 |       f
            1 |
            0 '------------
              0 1 2 3 4 5 6
        */
		Coordinate a = Coordinate.of(2, 4);
		Coordinate b = Coordinate.of(3, 3);
		Coordinate c = Coordinate.of(4, 2);

		Polyline polyline = Polyline.of(a, b, c);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(c, a);
		Assertions.assertEquals(expected, convexHull);
	}


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
	void line_collinear_equal23(){
		Coordinate a = Coordinate.of(30, 220);
		Coordinate b = Coordinate.of(240, 220);
		Coordinate c = Coordinate.of(240, 220);
		Polyline polyline = Polyline.of(a, b, c);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(a, b);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void line_collinear_equal123_456(){
		Coordinate a = Coordinate.of(130, 240);
		Coordinate b = Coordinate.of(130, 240);
		Coordinate c = Coordinate.of(130, 240);
		Coordinate d = Coordinate.of(570, 240);
		Coordinate e = Coordinate.of(570, 240);
		Coordinate f = Coordinate.of(570, 240);
		Coordinate g = Coordinate.of(650, 240);
		Polyline polyline = Polyline.of(a, b, c, d, e, f, g);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(a, g);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_collinear_equal12(){
		Coordinate a = Coordinate.of(0, 0);
		Coordinate b = Coordinate.of(0, 0);
		Coordinate c = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(a, b, c);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(a, c);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_coordinate_collinear_equal23(){
		Coordinate a = Coordinate.of(0, 0);
		Coordinate b = Coordinate.of(10, 0);
		Coordinate c = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(a, b, c);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(a, c);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_coordinate_collinear_equal_none(){
		Coordinate a = Coordinate.of(0, 0);
		Coordinate b = Coordinate.of(5, 0);
		Coordinate c = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(a, b, c);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(a, c);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_coordinate(){
		Coordinate a = Coordinate.of(0, 0);
		Coordinate b = Coordinate.of(5, 1);
		Coordinate c = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(a, b, c);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(a, c, b, a);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_coordinate_linear(){
		Coordinate a = Coordinate.of(0, 0);
		Coordinate b = Coordinate.of(0, 0);
		Coordinate c = Coordinate.of(5, 0);
		Coordinate d = Coordinate.of(5, 0);
		Coordinate e = Coordinate.of(10, 0);
		Coordinate f = Coordinate.of(10, 0);
		Polyline polyline = Polyline.of(a, b, c, d, e, f);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(a, f);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void collinear_coordinates(){
        /*
            2      |
            1  e   f/h g
            0      '------
           -1  a   b/d c
              -2-1 0 1 2 3
        */
		Coordinate a = Coordinate.of(-0.2, -0.1);
		Coordinate b = Coordinate.of(0., -0.1);
		Coordinate c = Coordinate.of(0.2, -0.1);
		Coordinate d = Coordinate.of(0., -0.1);
		Coordinate e = Coordinate.of(-0.2, 0.1);
		Coordinate f = Coordinate.of(0., 0.1);
		Coordinate g = Coordinate.of(0.2, 0.1);
		Coordinate h = Coordinate.of(0., 0.1);
		Polyline polyline = Polyline.of(a, b, c, d, e, f, g, h);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(a, c, g, e, a);
		Assertions.assertEquals(expected, convexHull);
	}

	/**
	 * @see <a href="https://trac.osgeo.org/geos/ticket/850">Ticket 850</a>
	 */
	@Test
	void geos_850(){
		Coordinate a = Coordinate.of(280756800.63603467, 7571780.5096410504);
		Coordinate b = Coordinate.of(-0.00043553364940478493, -1.1745985126662545e-05);
		Coordinate c = Coordinate.of(-0.0040809829767810965, -0.00011006076189068978);
		Coordinate d = Coordinate.of(-0.0041201583341660313, -0.00011111728913462023);
		Coordinate e = Coordinate.of(-0.006976907320408115, -0.00018816146492247227);
		Coordinate f = Coordinate.of(-0.0069397726510486172, -0.00018715997340633273);
		Coordinate g = Coordinate.of(-0.0074676533800189931, -0.000201396483469504);
		Coordinate h = Coordinate.of(-0.13462489887442128, -0.0036307230426676734);
		Coordinate i = Coordinate.of(-0.010721780626750072, -0.00028915762480866283);
		Coordinate j = Coordinate.of(-0.010775949783764172, -0.00029061852246303201);
		Coordinate k = Coordinate.of(-0.011934357539045426, -0.0003218598289746266);
		Coordinate l = Coordinate.of(-0.019390152385490519, -0.00052293649740946452);
		Coordinate m = Coordinate.of(-0.016403812662021146, -0.00044239736574681491);
		Coordinate n = Coordinate.of(-0.013937679796751739, -0.00037588778618408299);
		Coordinate o = Coordinate.of(-0.0073628397580766435, -0.00019856974598662623);
		Coordinate p = Coordinate.of(-0.0013082267409651623, -3.5281801617658642e-05);
		Coordinate q = Coordinate.of(-0.0019059940589774278, -5.14030956166791e-05);
		Polyline polyline = Polyline.of(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(h, a, q, h);
		Assertions.assertEquals(expected, convexHull);
	}

	/**
	 * Tests robustness issue in radial sort.
	 *
	 * @see <a href="https://github.com/libgeos/geos/issues/722">Issue 722</a>
	 */
	@Test
	void collinear_coordinates_tiny_x(){
        /*
            2      |
            1  e  f/h  g
            0     d'------
           -1  a    b  c
              -2-1 0 1 2 3
        */
		Coordinate a = Coordinate.of(-0.2, -0.1);
		Coordinate b = Coordinate.of(1.38777878e-17, -0.1);
		Coordinate c = Coordinate.of(0.2, -0.1);
		Coordinate d = Coordinate.of(-1.38777878e-17, -0.1);
		Coordinate e = Coordinate.of(-0.2, 0.1);
		Coordinate f = Coordinate.of(1.38777878e-17, 0.1);
		Coordinate g = Coordinate.of(0.2, 0.1);
		Coordinate h = Coordinate.of(-1.38777878e-17, 0.1);
		Polyline polyline = Polyline.of(a, b, c, d, e, f, g, h);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(a, c, g, e, a);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void collinear_coordinates_less_tiny_x(){
        /*
            2      |
            1  e  h|f  g
            0      '------
           -1  a  d b  c
              -2-1 0 1 2 3
        */
		Coordinate a = Coordinate.of(-0.2, -0.1);
		Coordinate b = Coordinate.of(1.38777878e-7, -0.1);
		Coordinate c = Coordinate.of(0.2, -0.1);
		Coordinate d = Coordinate.of(-1.38777878e-7, -0.1);
		Coordinate e = Coordinate.of(-0.2, 0.1);
		Coordinate f = Coordinate.of(1.38777878e-7, 0.1);
		Coordinate g = Coordinate.of(0.2, 0.1);
		Coordinate h = Coordinate.of(-1.38777878e-7, 0.1);
		Polyline polyline = Polyline.of(a, b, c, d, e, f, g, h);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(a, c, g, e, a);
		Assertions.assertEquals(expected, convexHull);
	}

	/**
	 * Test case fails in GEOS due to incorrect fix to radial sorting.
	 * <p>
	 * This did not trigger a failure in JTS, probably because the sorting is less strict.
	 * </p>
	 */
	@Test
	void geos_sort_failure(){
		Coordinate a = Coordinate.of(140, 350);
		Coordinate b = Coordinate.of(510, 140);
		Coordinate c = Coordinate.of(110, 140);
		Coordinate d = Coordinate.of(250, 290);
		Coordinate e = Coordinate.of(250, 50);
		Coordinate f = Coordinate.of(300, 370);
		Coordinate g = Coordinate.of(450, 310);
		Coordinate h = Coordinate.of(440, 160);
		Coordinate i = Coordinate.of(290, 280);
		Coordinate j = Coordinate.of(220, 160);
		Coordinate k = Coordinate.of(100, 260);
		Coordinate l = Coordinate.of(320, 230);
		Coordinate m = Coordinate.of(200, 280);
		Coordinate n = Coordinate.of(360, 130);
		Coordinate o = Coordinate.of(330, 210);
		Coordinate p = Coordinate.of(380, 80);
		Coordinate q = Coordinate.of(220, 210);
		Coordinate r = Coordinate.of(380, 310);
		Coordinate s = Coordinate.of(260, 150);
		Coordinate t = Coordinate.of(260, 110);
		Coordinate u = Coordinate.of(170, 130);
		Polyline polyline = Polyline.of(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u);

		Polyline convexHull = polyline.getConvexHull();

		Polyline expected = Polyline.of(e, p, b, g, f, a, k, c, e);
		Assertions.assertEquals(expected, convexHull);
	}

}
