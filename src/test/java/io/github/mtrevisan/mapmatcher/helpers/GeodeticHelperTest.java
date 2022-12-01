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
package io.github.mtrevisan.mapmatcher.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;


class GeodeticHelperTest{

	@Test
	void should_find_closest_point_regardless_of_great_circle1(){
		Coordinate start = new Coordinate(5., 52.);
		Coordinate end = new Coordinate(6., 51.4);
		Coordinate point = new Coordinate(5.5, 52);

		Coordinate closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Coordinate closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(5.260_428_497, closestPointOnTrackStartEnd.getX(), 0.000_000_000_5);
		Assertions.assertEquals(51.846_089_226, closestPointOnTrackStartEnd.getY(), 0.000_000_000_5);
		Assertions.assertEquals(5.260_428_490, closestPointOnTrackEndStart.getX(), 0.000_000_000_5);
		Assertions.assertEquals(51.846_089_230, closestPointOnTrackEndStart.getY(), 0.000_000_000_5);
		Assertions.assertEquals(313.808_743_51, GeodeticHelper.initialBearing(closestPointOnTrackStartEnd, start), 0.000_000_005);
		Assertions.assertEquals(43.808_743_07, GeodeticHelper.initialBearing(closestPointOnTrackStartEnd, point), 0.000_000_005);
		Assertions.assertEquals(133.808_743_51, GeodeticHelper.initialBearing(closestPointOnTrackStartEnd, end), 0.000_000_005);
	}

	@Test
	void should_find_closest_point_regardless_of_great_circle2(){
		Coordinate start = new Coordinate(29., 42.);
		Coordinate end = new Coordinate(-70., -35.);
		Coordinate point = new Coordinate(-22., 64.);

		Coordinate closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Coordinate closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(18.348_254_074, closestPointOnTrackStartEnd.getX(), 0.000_000_000_5);
		Assertions.assertEquals(37.977_755_160, closestPointOnTrackStartEnd.getY(), 0.000_000_000_5);
		Assertions.assertEquals(18.349_063_775, closestPointOnTrackEndStart.getX(), 0.000_000_000_5);
		Assertions.assertEquals(37.978_117_884, closestPointOnTrackEndStart.getY(), 0.000_000_000_5);
	}

	@Test
	void should_find_closest_point_regardless_of_great_circle3(){
		Coordinate start = new Coordinate(29., 42.);
		Coordinate end = new Coordinate(-77., 39.);
		Coordinate point = new Coordinate(-22., 64.);

		Coordinate closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Coordinate closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(-21.938_370_234, closestPointOnTrackStartEnd.getX(), 0.000_000_000_5);
		Assertions.assertEquals(54.928_529_597, closestPointOnTrackStartEnd.getY(), 0.000_000_000_5);
		Assertions.assertEquals(-21.936_180_572, closestPointOnTrackEndStart.getX(), 0.000_000_000_5);
		Assertions.assertEquals(54.928_533_442, closestPointOnTrackEndStart.getY(), 0.000_000_000_5);
	}

	@Test
	void should_find_closest_point_regardless_of_great_circle4(){
		Coordinate start = new Coordinate(12.238140517207398, 45.65897415921759);
		Coordinate end = new Coordinate(12.242949896905884, 45.69828882177029);
		Coordinate point = new Coordinate(12.242949896905884, 45.69828882177029);

		Coordinate closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Coordinate closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(12.242_949_897, closestPointOnTrackStartEnd.getX(), 0.000_000_000_5);
		Assertions.assertEquals(45.698_288_822, closestPointOnTrackStartEnd.getY(), 0.000_000_000_5);
		Assertions.assertEquals(12.242_949_897, closestPointOnTrackEndStart.getX(), 0.000_000_000_5);
		Assertions.assertEquals(45.698_288_822, closestPointOnTrackEndStart.getY(), 0.000_000_000_5);
	}

	@Test
	void should_find_closest_point_regardless_of_great_circle5(){
		Coordinate start = new Coordinate(12.343946870589775, 45.65931029901404);
		Coordinate end = new Coordinate(12.238140517207398, 45.65897415921759);
		Coordinate point = new Coordinate(12.142791962642718, 45.64824627395467);

		Coordinate closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Coordinate closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(12.238_140_517, closestPointOnTrackStartEnd.getX(), 0.000_000_000_5);
		Assertions.assertEquals(45.658_974_159, closestPointOnTrackStartEnd.getY(), 0.000_000_000_5);
		Assertions.assertEquals(12.238_140_517, closestPointOnTrackEndStart.getX(), 0.000_000_000_5);
		Assertions.assertEquals(45.658_974_159, closestPointOnTrackEndStart.getY(), 0.000_000_000_5);
	}

}
