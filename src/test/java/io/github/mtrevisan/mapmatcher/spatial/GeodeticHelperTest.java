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

import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * @see <a href="https://clydedacruz.github.io/openstreetmap-wkt-playground/">OpenStreetMap WKT Playground</a>
 */
class GeodeticHelperTest{

	@Test
	void closest_point_regardless_of_great_circle_utrecht_vento(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start = factory.createPoint(5., 52.);
		Point end = factory.createPoint(6., 51.4);
		Point point = factory.createPoint(5.5, 52);

		Point closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Point closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(5.260_428_1, closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(51.846_089_5, closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(5.260_428_5, closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(51.846_089_2, closestPointOnTrackEndStart.getY(), 0.000_000_05);
		Assertions.assertEquals(313.809, GeodeticHelper.initialBearing(closestPointOnTrackStartEnd, start), 0.000_5);
		Assertions.assertEquals(43.809, GeodeticHelper.initialBearing(closestPointOnTrackStartEnd, point), 0.000_5);
		Assertions.assertEquals(133.809, GeodeticHelper.initialBearing(closestPointOnTrackStartEnd, end), 0.000_5);
	}

	@Test
	void closest_point_regardless_of_great_circle_chile_death_lake_island(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start = factory.createPoint(29., 42.);
		Point end = factory.createPoint(-70., -35.);
		Point point = factory.createPoint(-22., 64.);

		Point closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Point closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(18.349_063_3, closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(37.978_117_7, closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(18.349_063_5, closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(37.978_117_8, closestPointOnTrackEndStart.getY(), 0.000_000_05);
	}

	@Test
	void closest_point_regardless_of_great_circle_chile_death_lake_siberia(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start = factory.createPoint(29., 42.);
		Point end = factory.createPoint(-70., -35.);
		Point point = factory.createPoint(43.9, 66.2);

		Point closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Point closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(start, closestPointOnTrackStartEnd);
		Assertions.assertEquals(29., closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(42., closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(start, closestPointOnTrackEndStart);
		Assertions.assertEquals(29., closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(42., closestPointOnTrackEndStart.getY(), 0.000_000_05);
	}

	@Test
	void closest_point_regardless_of_great_circle_chile_death_lake_alexander_island(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start = factory.createPoint(29., 42.);
		Point end = factory.createPoint(-70., -35.);
		Point point = factory.createPoint(-78.6, -72.8);

		Point closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Point closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(end, closestPointOnTrackStartEnd);
		Assertions.assertEquals(-70., closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(-35, closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(end, closestPointOnTrackEndStart);
		Assertions.assertEquals(-70., closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(-35., closestPointOnTrackEndStart.getY(), 0.000_000_05);
	}

	@Test
	void closest_point_regardless_of_great_circle_usa_death_lake_island(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start = factory.createPoint(29., 42.);
		Point end = factory.createPoint(-77., 39.);
		Point point = factory.createPoint(-22., 64.);

		Point closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Point closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(-21.937_291_0, closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(54.928_531_5, closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(-21.937_291_1, closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(54.928_531_5, closestPointOnTrackEndStart.getY(), 0.000_000_05);
	}

	@Test
	void closest_point_regardless_of_great_circle_fontane_san_zeno(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start = factory.createPoint(12.238140517207398, 45.65897415921759);
		Point end = factory.createPoint(12.242949896905884, 45.69828882177029);
		Point point = factory.createPoint(12.242949896905884, 45.69828882177029);

		Point closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Point closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(12.242_949_9, closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(45.698_288_7, closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(12.242_949_9, closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(45.698_288_8, closestPointOnTrackEndStart.getY(), 0.000_000_05);
	}

	@Test
	void closest_point_regardless_of_great_circle_san_zeno_biancade(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start = factory.createPoint(12.343946870589775, 45.65931029901404);
		Point end = factory.createPoint(12.238140517207398, 45.65897415921759);
		Point point = factory.createPoint(12.142791962642718, 45.64824627395467);

		Point closestPointOnTrackStartEnd = GeodeticHelper.onTrackClosestPoint(start, end, point);
		Point closestPointOnTrackEndStart = GeodeticHelper.onTrackClosestPoint(end, start, point);

		Assertions.assertEquals(12.238_140_5, closestPointOnTrackStartEnd.getX(), 0.000_000_05);
		Assertions.assertEquals(45.658_974_2, closestPointOnTrackStartEnd.getY(), 0.000_000_05);
		Assertions.assertEquals(12.238_140_5, closestPointOnTrackEndStart.getX(), 0.000_000_05);
		Assertions.assertEquals(45.658_974_2, closestPointOnTrackEndStart.getY(), 0.000_000_05);
	}


	@Test
	void intersection1(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start1 = factory.createPoint(4.5, 51.5);
		Point end1 = factory.createPoint(5.5, 52.);
		Point start2 = factory.createPoint(6., 51.4);
		Point end2 = factory.createPoint(5., 52.);

		Point intersection = GeodeticHelper.intersection(start1, end1, start2, end2);

		Assertions.assertEquals(5.227_457, intersection.getX(), 0.000_000_5);
		Assertions.assertEquals(51.865_665, intersection.getY(), 0.000_000_5);
	}

	@Test
	void intersection2(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start1 = factory.createPoint(5., 52.);
		Point end1 = factory.createPoint(6., 51.4);
		Point start2 = factory.createPoint(4.5, 51.5);
		Point end2 = factory.createPoint(5.5, 52.);

		Point intersection = GeodeticHelper.intersection(start1, end1, start2, end2);

		Assertions.assertEquals(5.227_457, intersection.getX(), 0.000_000_5);
		Assertions.assertEquals(51.865_665, intersection.getY(), 0.000_000_5);
	}

	@Test
	void intersection3(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start1 = factory.createPoint(29., 42.);
		Point end1 = factory.createPoint(-77., 39.);
		Point start2 = factory.createPoint(0., 6.);
		Point end2 = factory.createPoint(-22., 64.);

		Point intersection = GeodeticHelper.intersection(start1, end1, start2, end2);

		Assertions.assertEquals(-14.563_856, intersection.getX(), 0.000_000_5);
		Assertions.assertEquals(54.717_030, intersection.getY(), 0.000_000_5);
	}

	@Test
	void intersection4(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start1 = factory.createPoint(-92., 35.);
		Point end1 = factory.createPoint(52., 40.);
		Point start2 = factory.createPoint(20., -8.);
		Point end2 = factory.createPoint(-95., 49.);

		Point intersection = GeodeticHelper.intersection(start1, end1, start2, end2);

		Assertions.assertEquals(-79.282_802, intersection.getX(), 0.000_000_5);
		Assertions.assertEquals(50.479_097, intersection.getY(), 0.000_000_5);
	}

//	/**
//	 * Returns the point of intersection of two paths defined by point and bearing.
//	 *
//	 * @param   {LatLon}      p1 - First point.
//	 * @param   {number}      brng1 - Initial bearing from first point.
//	 * @param   {LatLon}      p2 - Second point.
//	 * @param   {number}      brng2 - Initial bearing from second point.
//	 * @returns {LatLon|null} Destination point (null if no unique intersection defined).
//	 *
//	 * @example
//	 *   const p1 = new LatLon(51.8853, 0.2545), brng1 = 108.547;
//	 *   const p2 = new LatLon(49.0034, 2.5735), brng2 =  32.435;
//	 *   const pInt = LatLon.intersection(p1, brng1, p2, brng2); // 50.9078°N, 004.5084°E
//	 */
//	static double[] intersection1(Point p1, double brng1, Point p2, double brng2) {
//		// see www.edwilliams.org/avform.htm#Intersection
//
//		double phi1 = Math.toRadians(p1.getY());
//		double lambda1 = Math.toRadians(p1.getX());
//		double phi2 = Math.toRadians(p2.getY());
//		double lambda2 = Math.toRadians(p2.getX());
//		double theta13 = Math.toRadians(brng1);
//		double theta23 = Math.toRadians(brng2);
//		double deltaPhi = phi2 - phi1;
//		double deltaLambda = lambda2 - lambda1;
//
//		// angular distance p1-p2
//		double delta12 = 2 * Math.asin(Math.sqrt(Math.sin(deltaPhi/2) * Math.sin(deltaPhi/2)
//			+ Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda/2) * Math.sin(deltaLambda/2)));
//		if (Math.abs(delta12) < 0.1)
//			// coincident points
//			return new double[]{p1.getX(), p1.getY()};
//
//		// initial/final bearings between points
//		double cosThetaA = (Math.sin(phi2) - Math.sin(phi1)*Math.cos(delta12)) / (Math.sin(delta12)*Math.cos(phi1));
//		double cosThetaB = (Math.sin(phi1) - Math.sin(phi2)*Math.cos(delta12)) / (Math.sin(delta12)*Math.cos(phi2));
//		// protect against rounding errors
//		double thetaA = Math.acos(Math.min(Math.max(cosThetaA, -1), 1));
//		// protect against rounding errors
//		double thetaB = Math.acos(Math.min(Math.max(cosThetaB, -1), 1));
//
//		double theta12 = Math.sin(lambda2-lambda1)>0 ? thetaA : 2*Math.PI-thetaA;
//		double theta21 = Math.sin(lambda2-lambda1)>0 ? 2*Math.PI-thetaB : thetaB;
//
//		// angle 2-1-3
//		double alpha1 = theta13 - theta12;
//		// angle 1-2-3
//		double alpha2 = theta21 - theta23;
//
//		if (Math.sin(alpha1) == 0 && Math.sin(alpha2) == 0)
//			// infinite intersections
//			return null;
//		if (Math.sin(alpha1) * Math.sin(alpha2) < 0)
//			// ambiguous intersection (antipodal/360°)
//			return null;
//
//		double cosAlpha3 = -Math.cos(alpha1)*Math.cos(alpha2) + Math.sin(alpha1)*Math.sin(alpha2)*Math.cos(delta12);
//
//		double delta13 = Math.atan2(Math.sin(delta12)*Math.sin(alpha1)*Math.sin(alpha2), Math.cos(alpha2) + Math.cos(alpha1)*cosAlpha3);
//
//		double phi3 = Math.asin(Math.min(Math.max(Math.sin(phi1)*Math.cos(delta13) + Math.cos(phi1)*Math.sin(delta13)*Math.cos(theta13), -1), 1));
//
//		double deltaLambda13 = Math.atan2(Math.sin(theta13)*Math.sin(delta13)*Math.cos(phi1), Math.cos(delta13) - Math.sin(phi1)*Math.sin(phi3));
//		double lambda3 = lambda1 + deltaLambda13;
//
//		double lat = Math.toDegrees(phi3);
//		double lon = Math.toDegrees(lambda3);
//
//		return new double[]{lon, lat};
//	}

	@Test
	void intersection5(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start1 = factory.createPoint(14.5, 54.);
		Point end1 = factory.createPoint(14.6, 54.2);
		Point start2 = factory.createPoint(14.4, 54.1);
		Point end2 = factory.createPoint(14.7, 54.);

		Point intersection = GeodeticHelper.intersection(start1, end1, start2, end2);

		Assertions.assertEquals(14.528_548, intersection.getX(), 0.000_000_5);
		Assertions.assertEquals(54.057_301, intersection.getY(), 0.000_000_5);
	}

	@Test
	void intersection_polar(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start1 = factory.createPoint(5., 90.);
		Point end1 = factory.createPoint(5., 0.);
		Point start2 = factory.createPoint(-30., 70.);
		Point end2 = factory.createPoint(40., 70.);

		Point intersection = GeodeticHelper.intersection(start1, end1, start2, end2);

		Assertions.assertEquals(5., intersection.getX(), 0.000_000_5);
		Assertions.assertEquals(73.400_298, intersection.getY(), 0.000_000_5);
	}

	@Test
	void intersection_near_pole(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start1 = factory.createPoint(-170., 85.);
		Point end1 = factory.createPoint(12., -15.);
		Point start2 = factory.createPoint(-58., 26.);
		Point end2 = factory.createPoint(120., 75.);

		Point intersection = GeodeticHelper.intersection(start1, end1, start2, end2);

		Assertions.assertEquals(34.247_995, intersection.getX(), 0.000_000_5);
		Assertions.assertEquals(89.526_196, intersection.getY(), 0.000_000_5);
	}

	@Test
	void intersection_very_long_geodesic(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start1 = factory.createPoint(105., 63.);
		Point end1 = factory.createPoint(79., 42.);
		Point end2 = factory.createPoint(255., 15.);
		Point start2 = factory.createPoint(85., 57.);

		Point intersection = GeodeticHelper.intersection(start1, end1, start2, end2);

		Assertions.assertNull(intersection);
	}

	@Test
	void intersection_almost_parallel(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start1 = factory.createPoint(-42., 40.);
		Point end1 = factory.createPoint(63., 65.5);
		Point start2 = factory.createPoint(-41.8, 40.);
		Point end2 = factory.createPoint(62.9, 65.6);

		Point intersection = GeodeticHelper.intersection(start1, end1, start2, end2);

		Assertions.assertEquals(15.438_099, intersection.getX(), 0.000_000_5);
		Assertions.assertEquals(68.646_910, intersection.getY(), 0.000_000_5);
	}

	@Test
	void intersection_no_intersection(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		Point start1 = factory.createPoint(0., 40.);
		Point end1 = factory.createPoint(15., 40.);
		Point start2 = factory.createPoint(0., 45.);
		Point end2 = factory.createPoint(15., 45.);

		Point intersection = GeodeticHelper.intersection(start1, end1, start2, end2);

		Assertions.assertNull(intersection);
	}

}
