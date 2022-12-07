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
package io.github.mtrevisan.mapmatcher.spatial.convexhull;

import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.distances.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.spatial.distances.GeodeticCalculator;
import io.github.mtrevisan.mapmatcher.spatial.simplification.RamerDouglasPeuckerSimplifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


//https://github.com/locationtech/jts/blob/master/modules/core/src/main/java/org/locationtech/jts/algorithm/ConvexHull.java
//https://github.com/locationtech/jts/blob/master/modules/core/src/test/java/org/locationtech/jts/algorithm/ConvexHullTest.java
//https://github.com/bkiers/GrahamScan/blob/master/src/main/cg/GrahamScan.java#L188
class GrahamScanTest{

	@Test
	public void lowest_point1(){
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
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(2, 4);
		Point b = factory.createPoint(3, 5);
		Point c = factory.createPoint(3, 3);
		Point d = factory.createPoint(4, 6);
		Point e = factory.createPoint(4, 4);
		Point f = factory.createPoint(4, 2);
		Point g = factory.createPoint(5, 5);
		Point h = factory.createPoint(5, 3);
		Point i = factory.createPoint(6, 4);

		int lowestIndex = GrahamScan.getLowestPoint(new Point[]{a, b, c, d, e, f, g, h, i});

		Assertions.assertEquals(5, lowestIndex);
	}

	@Test
	public void lowest_point2(){
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
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(2, 4);
		Point b = factory.createPoint(3, 5);
		Point c = factory.createPoint(3, 3);
		Point d = factory.createPoint(4, 6);
		Point e = factory.createPoint(4, 4);
		Point f = factory.createPoint(4, 2);
		Point g = factory.createPoint(5, 5);
		Point h = factory.createPoint(5, 3);
		Point i = factory.createPoint(6, 4);
		Point j = factory.createPoint(-1, -1);
		Point k = factory.createPoint(6, -1);

		int lowestIndex = GrahamScan.getLowestPoint(new Point[]{a, b, c, d, e, f, g, h, i, j, k});

		Assertions.assertEquals(9, lowestIndex);
	}


	@Test
	public void sorted_point_set(){
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
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(2, 2);
		Point b = factory.createPoint(1, 1);
		Point c = factory.createPoint(0, 0);
		Point d = factory.createPoint(2, 0);
		Point e = factory.createPoint(1, 0);
		Point f = factory.createPoint(0, 1);
		Point g = factory.createPoint(0, 2);

		Point[] points = {a, b, c, d, e, f, g};
		GrahamScan.polarSort(points);

		Assertions.assertEquals(7, points.length);
		Assertions.assertEquals(c, points[0]);
		Assertions.assertEquals(e, points[1]);
		Assertions.assertEquals(d, points[2]);
		Assertions.assertEquals(b, points[3]);
		Assertions.assertEquals(a, points[4]);
		Assertions.assertEquals(f, points[5]);
		Assertions.assertEquals(g, points[6]);
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
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(1, 1);
		Point b = factory.createPoint(4, 4);
		Point c = factory.createPoint(8, 8);
		Point d = factory.createPoint(7, 9);
		Point e = factory.createPoint(9, 7);
		Point f = factory.createPoint(0, -1);
		Point g = factory.createPoint(-1, 0);
		Point h = factory.createPoint(-2, 1);

		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COLLINEAR, GrahamScan.PolarAngleComparator.orientation(a, b, c));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COLLINEAR, GrahamScan.PolarAngleComparator.orientation(a, c, b));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COLLINEAR, GrahamScan.PolarAngleComparator.orientation(b, a, c));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COLLINEAR, GrahamScan.PolarAngleComparator.orientation(c, b, a));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COLLINEAR, GrahamScan.PolarAngleComparator.orientation(e, d, c));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COLLINEAR, GrahamScan.PolarAngleComparator.orientation(h, f, g));

		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(a, b, c));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(a, c, b));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(b, a, c));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(c, b, a));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(e, d, c));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(h, f, g));


		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(a, b, e));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(a, b, f));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(a, c, e));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(a, c, f));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(c, b, g));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(d, b, f));

		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(a, b, e));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(a, b, f));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(a, c, e));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(a, c, f));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(c, b, g));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(d, b, f));


		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(a, b, d));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(a, e, d));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(e, c, f));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(b, d, a));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(a, g, f));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(f, b, a));

		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(a, b, d));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(a, e, d));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(e, c, f));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(b, d, a));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(a, g, f));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.polarCompare(f, b, a));
	}

	@Test
	public void orientation2(){
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(219.3649559090992, 140.84159161824724);
		Point b = factory.createPoint(168.9018919682399, -5.713787599646864);
		Point c = factory.createPoint(186.80814046338352, 46.28973405831556);

		Assertions.assertEquals(GrahamScan.PolarAngleComparator.CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(a, b, c));
		Assertions.assertEquals(GrahamScan.PolarAngleComparator.COUNTER_CLOCKWISE, GrahamScan.PolarAngleComparator.orientation(b, a, c));
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
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(2, 4);
		Point b = factory.createPoint(6, 6);
		Point c = factory.createPoint(3, 3);
		Point d = factory.createPoint(4, 6);
		Point e = factory.createPoint(4, 4);
		Point f = factory.createPoint(5, 5);

		Polyline polyline = factory.createPolyline(c);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(c);
		Assertions.assertEquals(expected, convexHull);


		polyline = factory.createPolyline(c, e);

		convexHull = GrahamScan.getConvexHull(polyline);

		expected = factory.createPolyline(c, e);
		Assertions.assertEquals(expected, convexHull);


		polyline = factory.createPolyline(c, e, f);

		convexHull = GrahamScan.getConvexHull(polyline);

		expected = factory.createPolyline(c, f);
		Assertions.assertEquals(expected, convexHull);


		polyline = factory.createPolyline(c, b, e, e, e, f, c);

		convexHull = GrahamScan.getConvexHull(polyline);

		expected = factory.createPolyline(c, b);
		Assertions.assertEquals(expected, convexHull);


		polyline = factory.createPolyline(a, b, d);

		convexHull = GrahamScan.getConvexHull(polyline);

		expected = factory.createPolyline(a, b, d, a);
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
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(2, 4);
		Point b = factory.createPoint(3, 5);
		Point c = factory.createPoint(3, 3);
		Point d = factory.createPoint(4, 6);
		Point e = factory.createPoint(4, 4);
		Point f = factory.createPoint(4, 2);
		Point g = factory.createPoint(5, 5);
		Point h = factory.createPoint(5, 3);
		Point i = factory.createPoint(6, 4);

		Polyline polyline = factory.createPolyline(a, b, c, d, e, f, g, h, i);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(f, i, d, a, f);
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
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(2, 4);
		Point b = factory.createPoint(3, 5);
		Point c = factory.createPoint(3, 3);
		Point d = factory.createPoint(4, 6);
		Point e = factory.createPoint(4, 4);
		Point f = factory.createPoint(4, 2);
		Point g = factory.createPoint(5, 5);
		Point h = factory.createPoint(5, 3);
		Point i = factory.createPoint(6, 4);
		Point j = factory.createPoint(-2, 3);
		Point k = factory.createPoint(6, -2);
		Point m = factory.createPoint(6, 6);

		Polyline polyline = factory.createPolyline(a, b, c, d, e, f, g, h, i, j, k, m);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(k, m, d, j, k);
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
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(2, 4);
		Point b = factory.createPoint(3, 5);
		Point c = factory.createPoint(3, 3);
		Point d = factory.createPoint(4, 6);
		Point e = factory.createPoint(4, 4);
		Point f = factory.createPoint(4, 2);
		Point g = factory.createPoint(5, 5);
		Point h = factory.createPoint(5, 3);
		Point i = factory.createPoint(6, 4);
		Point j = factory.createPoint(-2, 7);
		Point k = factory.createPoint(8, -2);
		Point m = factory.createPoint(Integer.MAX_VALUE, Integer.MAX_VALUE);

		Polyline polyline = factory.createPolyline(a, b, c, d, e, f, g, h, i, j, k, m);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(k, m, j, k);
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
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(2, 4);
		Point b = factory.createPoint(3, 3);
		Point c = factory.createPoint(4, 2);

		Polyline polyline = factory.createPolyline(a, b, c);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(c, a);
		Assertions.assertEquals(expected, convexHull);
	}


	@Test
	void should_simplify_polyline(){
		final GeometryFactory factory = new GeometryFactory(new GeodeticCalculator());
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
		final GeometryFactory factory = new GeometryFactory(new GeodeticCalculator());
		Point node12_31_41 = factory.createPoint(12.238140517207398, 45.65897415921759);
		Point node22 = factory.createPoint(12.242949896905884, 45.69828882177029);
		Point node23 = factory.createPoint(12.200627355552967, 45.732876303059044);

		RamerDouglasPeuckerSimplifier simplifier = new RamerDouglasPeuckerSimplifier();
		simplifier.setDistanceTolerance(10.);
		Polyline polyline = factory.createPolyline(simplifier.simplify(node12_31_41, node22, node23));

		Assertions.assertEquals(3, polyline.size());
	}


	@Test
	void line_collinear_equal23(){
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(30, 220);
		Point b = factory.createPoint(240, 220);
		Point c = factory.createPoint(240, 220);
		Polyline polyline = factory.createPolyline(a, b, c);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(a, b);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void line_collinear_equal123_456(){
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(130, 240);
		Point b = factory.createPoint(130, 240);
		Point c = factory.createPoint(130, 240);
		Point d = factory.createPoint(570, 240);
		Point e = factory.createPoint(570, 240);
		Point f = factory.createPoint(570, 240);
		Point g = factory.createPoint(650, 240);
		Polyline polyline = factory.createPolyline(a, b, c, d, e, f, g);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(a, g);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_collinear_equal12(){
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(0, 0);
		Point b = factory.createPoint(0, 0);
		Point c = factory.createPoint(10, 0);
		Polyline polyline = factory.createPolyline(a, b, c);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(a, c);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_point_collinear_equal23(){
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(0, 0);
		Point b = factory.createPoint(10, 0);
		Point c = factory.createPoint(10, 0);
		Polyline polyline = factory.createPolyline(a, b, c);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(a, c);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_point_collinear_equal_none(){
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(0, 0);
		Point b = factory.createPoint(5, 0);
		Point c = factory.createPoint(10, 0);
		Polyline polyline = factory.createPolyline(a, b, c);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(a, c);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_point(){
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(0, 0);
		Point b = factory.createPoint(5, 1);
		Point c = factory.createPoint(10, 0);
		Polyline polyline = factory.createPolyline(a, b, c);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(a, c, b, a);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void multi_point_linear(){
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(0, 0);
		Point b = factory.createPoint(0, 0);
		Point c = factory.createPoint(5, 0);
		Point d = factory.createPoint(5, 0);
		Point e = factory.createPoint(10, 0);
		Point f = factory.createPoint(10, 0);
		Polyline polyline = factory.createPolyline(a, b, c, d, e, f);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(a, f);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void collinear_points(){
        /*
            2      |
            1  e   f/h g
            0      '------
           -1  a   b/d c
              -2-1 0 1 2 3
        */
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(-0.2, -0.1);
		Point b = factory.createPoint(0., -0.1);
		Point c = factory.createPoint(0.2, -0.1);
		Point d = factory.createPoint(0., -0.1);
		Point e = factory.createPoint(-0.2, 0.1);
		Point f = factory.createPoint(0., 0.1);
		Point g = factory.createPoint(0.2, 0.1);
		Point h = factory.createPoint(0., 0.1);
		Polyline polyline = factory.createPolyline(a, b, c, d, e, f, g, h);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(a, c, g, e, a);
		Assertions.assertEquals(expected, convexHull);
	}

	/**
	 * @see <a href="https://trac.osgeo.org/geos/ticket/850">Ticket 850</a>
	 */
	@Test
	void geos_850(){
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(280756800.63603467, 7571780.5096410504);
		Point b = factory.createPoint(-0.00043553364940478493, -1.1745985126662545e-05);
		Point c = factory.createPoint(-0.0040809829767810965, -0.00011006076189068978);
		Point d = factory.createPoint(-0.0041201583341660313, -0.00011111728913462023);
		Point e = factory.createPoint(-0.006976907320408115, -0.00018816146492247227);
		Point f = factory.createPoint(-0.0069397726510486172, -0.00018715997340633273);
		Point g = factory.createPoint(-0.0074676533800189931, -0.000201396483469504);
		Point h = factory.createPoint(-0.13462489887442128, -0.0036307230426676734);
		Point i = factory.createPoint(-0.010721780626750072, -0.00028915762480866283);
		Point j = factory.createPoint(-0.010775949783764172, -0.00029061852246303201);
		Point k = factory.createPoint(-0.011934357539045426, -0.0003218598289746266);
		Point l = factory.createPoint(-0.019390152385490519, -0.00052293649740946452);
		Point m = factory.createPoint(-0.016403812662021146, -0.00044239736574681491);
		Point n = factory.createPoint(-0.013937679796751739, -0.00037588778618408299);
		Point o = factory.createPoint(-0.0073628397580766435, -0.00019856974598662623);
		Point p = factory.createPoint(-0.0013082267409651623, -3.5281801617658642e-05);
		Point q = factory.createPoint(-0.0019059940589774278, -5.14030956166791e-05);
		Polyline polyline = factory.createPolyline(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(h, a, q, h);
		Assertions.assertEquals(expected, convexHull);
	}

	/**
	 * Tests robustness issue in radial sort.
	 *
	 * @see <a href="https://github.com/libgeos/geos/issues/722">Issue 722</a>
	 */
	@Test
	void collinear_points_tiny_x(){
        /*
            2      |
            1  e  f/h  g
            0     d'------
           -1  a    b  c
              -2-1 0 1 2 3
        */
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(-0.2, -0.1);
		Point b = factory.createPoint(1.38777878e-17, -0.1);
		Point c = factory.createPoint(0.2, -0.1);
		Point d = factory.createPoint(-1.38777878e-17, -0.1);
		Point e = factory.createPoint(-0.2, 0.1);
		Point f = factory.createPoint(1.38777878e-17, 0.1);
		Point g = factory.createPoint(0.2, 0.1);
		Point h = factory.createPoint(-1.38777878e-17, 0.1);
		Polyline polyline = factory.createPolyline(a, b, c, d, e, f, g, h);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(a, c, g, e, a);
		Assertions.assertEquals(expected, convexHull);
	}

	@Test
	void collinear_points_less_tiny_x(){
        /*
            2      |
            1  e  h|f  g
            0      '------
           -1  a  d b  c
              -2-1 0 1 2 3
        */
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(-0.2, -0.1);
		Point b = factory.createPoint(1.38777878e-7, -0.1);
		Point c = factory.createPoint(0.2, -0.1);
		Point d = factory.createPoint(-1.38777878e-7, -0.1);
		Point e = factory.createPoint(-0.2, 0.1);
		Point f = factory.createPoint(1.38777878e-7, 0.1);
		Point g = factory.createPoint(0.2, 0.1);
		Point h = factory.createPoint(-1.38777878e-7, 0.1);
		Polyline polyline = factory.createPolyline(a, b, c, d, e, f, g, h);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(a, c, g, e, a);
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
		final GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point a = factory.createPoint(140, 350);
		Point b = factory.createPoint(510, 140);
		Point c = factory.createPoint(110, 140);
		Point d = factory.createPoint(250, 290);
		Point e = factory.createPoint(250, 50);
		Point f = factory.createPoint(300, 370);
		Point g = factory.createPoint(450, 310);
		Point h = factory.createPoint(440, 160);
		Point i = factory.createPoint(290, 280);
		Point j = factory.createPoint(220, 160);
		Point k = factory.createPoint(100, 260);
		Point l = factory.createPoint(320, 230);
		Point m = factory.createPoint(200, 280);
		Point n = factory.createPoint(360, 130);
		Point o = factory.createPoint(330, 210);
		Point p = factory.createPoint(380, 80);
		Point q = factory.createPoint(220, 210);
		Point r = factory.createPoint(380, 310);
		Point s = factory.createPoint(260, 150);
		Point t = factory.createPoint(260, 110);
		Point u = factory.createPoint(170, 130);
		Polyline polyline = factory.createPolyline(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u);

		Polyline convexHull = GrahamScan.getConvexHull(polyline);

		Polyline expected = factory.createPolyline(e, p, b, g, f, a, k, c, e);
		Assertions.assertEquals(expected, convexHull);
	}

}
