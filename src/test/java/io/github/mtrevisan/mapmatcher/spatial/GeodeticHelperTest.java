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
package io.github.mtrevisan.mapmatcher.spatial;

import io.github.mtrevisan.mapmatcher.spatial.distances.GeodeticCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * @see <a href="https://clydedacruz.github.io/openstreetmap-wkt-playground/">OpenStreetMap WKT Playground</a>
 */
class GeodeticHelperTest{

	@Test
	void should_find_closest_point_regardless_of_great_circle_utrecht_vento(){
		GeometryFactory factory = new GeometryFactory(new GeodeticCalculator());
		Coordinate start = factory.createPoint(5., 52.);
		Coordinate end = factory.createPoint(6., 51.4);
		Coordinate point = factory.createPoint(5.5, 52);

		Coordinate closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Coordinate closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(5.260_428_1, closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(51.846_089_5, closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(5.260_428_5, closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(51.846_089_2, closestPointOnTrackEndStart.getY(), 0.000_000_05);
		Assertions.assertEquals(313.809, GeodeticHelper.initialBearing(closestPointOnTrackStartEnd, start), 0.000_5);
		Assertions.assertEquals(43.809, GeodeticHelper.initialBearing(closestPointOnTrackStartEnd, point), 0.000_5);
		Assertions.assertEquals(133.809, GeodeticHelper.initialBearing(closestPointOnTrackStartEnd, end), 0.000_5);
	}

	@Test
	void should_find_closest_point_regardless_of_great_circle_chile_death_lake_island(){
		GeometryFactory factory = new GeometryFactory(new GeodeticCalculator());
		Coordinate start = factory.createPoint(29., 42.);
		Coordinate end = factory.createPoint(-70., -35.);
		Coordinate point = factory.createPoint(-22., 64.);

		Coordinate closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Coordinate closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(18.349_063_3, closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(37.978_117_7, closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(18.349_063_5, closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(37.978_117_8, closestPointOnTrackEndStart.getY(), 0.000_000_05);
	}

	@Test
	void should_find_closest_point_regardless_of_great_circle_chile_death_lake_siberia(){
		GeometryFactory factory = new GeometryFactory(new GeodeticCalculator());
		Coordinate start = factory.createPoint(29., 42.);
		Coordinate end = factory.createPoint(-70., -35.);
		Coordinate point = factory.createPoint(43.9, 66.2);

		Coordinate closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Coordinate closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(start, closestPointOnTrackStartEnd);
		Assertions.assertEquals(29., closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(42., closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(start, closestPointOnTrackEndStart);
		Assertions.assertEquals(29., closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(42., closestPointOnTrackEndStart.getY(), 0.000_000_05);
	}

	@Test
	void should_find_closest_point_regardless_of_great_circle_chile_death_lake_alexander_island(){
		GeometryFactory factory = new GeometryFactory(new GeodeticCalculator());
		Coordinate start = factory.createPoint(29., 42.);
		Coordinate end = factory.createPoint(-70., -35.);
		Coordinate point = factory.createPoint(-78.6, -72.8);

		Coordinate closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Coordinate closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(end, closestPointOnTrackStartEnd);
		Assertions.assertEquals(-70., closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(-35, closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(end, closestPointOnTrackEndStart);
		Assertions.assertEquals(-70., closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(-35., closestPointOnTrackEndStart.getY(), 0.000_000_05);
	}

	@Test
	void should_find_closest_point_regardless_of_great_circle_usa_death_lake_island(){
		GeometryFactory factory = new GeometryFactory(new GeodeticCalculator());
		Coordinate start = factory.createPoint(29., 42.);
		Coordinate end = factory.createPoint(-77., 39.);
		Coordinate point = factory.createPoint(-22., 64.);

		Coordinate closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Coordinate closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(-21.937_291_0, closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(54.928_531_5, closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(-21.937_291_1, closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(54.928_531_5, closestPointOnTrackEndStart.getY(), 0.000_000_05);
	}

	@Test
	void should_find_closest_point_regardless_of_great_circle_fontane_san_zeno(){
		GeometryFactory factory = new GeometryFactory(new GeodeticCalculator());
		Coordinate start = factory.createPoint(12.238140517207398, 45.65897415921759);
		Coordinate end = factory.createPoint(12.242949896905884, 45.69828882177029);
		Coordinate point = factory.createPoint(12.242949896905884, 45.69828882177029);

		Coordinate closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Coordinate closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(12.242_949_9, closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(45.698_288_7, closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(12.242_949_9, closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(45.698_288_8, closestPointOnTrackEndStart.getY(), 0.000_000_05);
	}

	@Test
	void should_find_closest_point_regardless_of_great_circle_san_zeno_biancade(){
		GeometryFactory factory = new GeometryFactory(new GeodeticCalculator());
		Coordinate start = factory.createPoint(12.343946870589775, 45.65931029901404);
		Coordinate end = factory.createPoint(12.238140517207398, 45.65897415921759);
		Coordinate point = factory.createPoint(12.142791962642718, 45.64824627395467);

		Coordinate closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Coordinate closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(12.238_140_5, closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(45.658_974_2, closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(12.238_140_5, closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(45.658_974_2, closestPointOnTrackEndStart.getY(), 0.000_000_05);
	}

}
