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
package io.github.mtrevisan.mapmatcher.spatial;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.geographiclib.GeodesicMask;

import java.util.HashSet;
import java.util.Set;


/**
 * @see <a href="https://community.qlik.com/t5/QlikView-App-Dev/WGS84-Compliant-Great-Circle-Distance-Calculation/td-p/241905">WGS84 Compliant Great Circle Distance Calculation</a>
 * @see <a href="https://arxiv.org/pdf/1102.1215.pdf">Geodesics on an ellipsoid of revolution</a>
 */
public class GeodeticHelper{

	private static final Geodesic REFERENCE_ELLIPSOID = Geodesic.WGS84;

	//[m]
	private static final double EARTH_EQUATORIAL_RADIUS = REFERENCE_ELLIPSOID.EquatorialRadius();

	//[m]
	private static final double ON_TRACK_POINT_PRECISION = 0.1;

	//mean Earth radius [m]
	private static final double EARTH_RADIUS = 6_371_000.;
	private static final double EARTH_CIRCUMFERENCE = 2. * StrictMath.PI * EARTH_RADIUS;
	private static final double METERS_PER_DEGREE = EARTH_CIRCUMFERENCE / 360.;


	private GeodeticHelper(){}

	/**
	 * Returns the orthodromic distance (using WGS84 reference system).
	 *
	 * @param startPoint	Starting point.
	 * @param endPoint	Ending point.
	 * @return	The orthodromic distance [m].
	 */
	public static double orthodromicDistance(final Point startPoint, final Point endPoint){
		final GeodesicData result = REFERENCE_ELLIPSOID.Inverse(startPoint.getY(), startPoint.getX(),
			endPoint.getY(), endPoint.getX(), GeodesicMask.DISTANCE);
		return result.s12;
	}

	/**
	 * Returns the initial bearing (using WGS84 reference system) from North and clockwise.
	 *
	 * @param startPoint	Starting point.
	 * @param endPoint	Ending point.
	 * @return	The initial bearing [°].
	 */
	public static double initialBearing(final Point startPoint, final Point endPoint){
		final GeodesicData result = REFERENCE_ELLIPSOID.Inverse(startPoint.getY(), startPoint.getX(),
			endPoint.getY(), endPoint.getX(), GeodesicMask.AZIMUTH);
		return (result.azi1 < 0.? result.azi1 + 360.: result.azi1);
	}

	/**
	 * Retrieve the destination, starting from the given point, heading and distance.
	 *
	 * @param startPoint	The starting point.
	 * @param initialBearing	The initial bearing [°].
	 * @param distance	The distance to travel [m].
	 * @return	The destination.
	 */
	public static Point destination(final Point startPoint, final double initialBearing, final double distance){
		final double initialAzimuth = (initialBearing > 180.? initialBearing - 360.: initialBearing);
		final GeodesicData result = REFERENCE_ELLIPSOID.Direct(startPoint.getY(), startPoint.getX(), initialAzimuth, distance,
			GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);
		return startPoint.factory.createPoint(result.lon2, result.lat2);
	}

	/**
	 * Returns the closest point to a given point on a great circle.
	 * <p>
	 * NOTE: not so precise, but it's enough.
	 * </p>
	 *
	 * @param	startPoint	Starting point of the great circle.
	 * @param	endPoint	Ending point of the great circle.
	 * @param	point	The point.
	 * @return	The point onto the great circle that is closest to the given point.
	 *
	 * @see <a href="https://edwilliams.org/avform147.htm#XTE">Aviation Formulary V1.47</a>
	 * @see <a href="https://www.researchgate.net/publication/321358300_Intersection_and_point-to-line_solutions_for_geodesics_on_the_ellipsoid">Intersection and point-to-line solutions for geodesics on the ellipsoid</a>
	 */
	public static Point onTrackClosestPoint(final Point startPoint, final Point endPoint, final Point point){
		if(startPoint.equals(endPoint))
			return startPoint;

		//NOTE: do not use the entire calculated ATD as there may be an endless back and forth calculation between two points around
		//	the true point on track
		final Set<Double> historicalATD = new HashSet<>(3);
		double factor = 1.;

		Point onTrackPoint = startPoint;
		boolean firstIteration = true;
		while(true){
			//[m]
			final double phiA = Math.toRadians(onTrackPoint.getY());
			final double lambdaA = Math.toRadians(onTrackPoint.getX());
			double phiP = Math.toRadians(point.getY());
			double lambdaP = Math.toRadians(point.getX());
			double distanceStartToPoint = StrictMath.acos(StrictMath.sin(phiA) * StrictMath.sin(phiP)
				+ StrictMath.cos(phiA) * StrictMath.cos(phiP) * StrictMath.cos(lambdaA - lambdaP));
			if(Double.isNaN(distanceStartToPoint)){
				//for very short distances this formula is less susceptible to rounding error (what is "very short"?)
				final double aa = StrictMath.sin((phiA - phiP) / 2.);
				final double bb = StrictMath.sin((lambdaA - lambdaP) / 2.);
				distanceStartToPoint = 2. * StrictMath.asin(Math.sqrt(aa * aa + StrictMath.cos(phiA) * StrictMath.cos(phiP) * bb * bb));
			}

			//[°]
			final double initialBearingStartToPoint = initialBearing(onTrackPoint, point);
			//[°]
			final double initialBearingStartToEnd = initialBearing(onTrackPoint, endPoint);
			//[rad]
			final double angleAP = Math.toRadians(initialBearingStartToEnd - initialBearingStartToPoint);
			//calculate Cross-Track Distance [m]
			final double sinAngleAP = StrictMath.sin(angleAP);
			if(Math.abs(sinAngleAP) == 1.)
				break;

			//[rad]
			final double xtd = StrictMath.asin(StrictMath.sin(distanceStartToPoint) * sinAngleAP);
			//calculate Along-Track Distance [rad]
			double atd;
			if(firstIteration){
				final double a = StrictMath.sin((Math.PI / 2. + angleAP) / 2.);
				final double b = StrictMath.sin((Math.PI / 2. - angleAP) / 2.);
				final double c = StrictMath.tan((distanceStartToPoint - xtd) / 2.);
				atd = 2. * EARTH_EQUATORIAL_RADIUS * StrictMath.atan((a / b) * c);
			}
			else
				atd = EARTH_EQUATORIAL_RADIUS * StrictMath.atan(StrictMath.cos(angleAP) * StrictMath.tan(distanceStartToPoint));
			if(historicalATD.contains(atd)){
				factor *= 0.5;
				atd *= factor;
			}
			else
				historicalATD.add(atd);
			if(Math.abs(atd) < ON_TRACK_POINT_PRECISION)
				break;

			//compute a point along the great circle from start to end point that lies at distance ATD
			onTrackPoint = destination(onTrackPoint, initialBearingStartToEnd, atd);

			firstIteration = false;
		}

		return limitOnTrack(startPoint, endPoint, onTrackPoint);
	}

	private static Point limitOnTrack(final Point startPoint, final Point endPoint, Point onTrackPoint){
		if(Math.abs(initialBearing(startPoint, onTrackPoint) - initialBearing(startPoint, endPoint)) > 90.)
			onTrackPoint = startPoint;
		else if(Math.abs(initialBearing(endPoint, onTrackPoint) - initialBearing(endPoint, startPoint)) > 90.)
			onTrackPoint = endPoint;
		return onTrackPoint;
	}

	/**
	 * @param startPoint1	Start point of the first geodesic.
	 * @param endPoint1	End point of the first geodesic.
	 * @param startPoint2	Start point of the second geodesic.
	 * @param endPoint2	End point of the second geodesic.
	 * @return	The intersection point.
	 *
	 * @see <a href="https://www.mdpi.com/2076-3417/11/11/5129">Accurate algorithms for spatial operations on the spheroid in a spatial database management system</a>
	 * @see <a href="https://kth.diva-portal.org/smash/get/diva2:1065075/FULLTEXT01.pdf">Tests of new solutions to the direct and indirect geodetic problems on the ellipsoid</a>
	 */
	public static Point intersection(final Point startPoint1, final Point endPoint1, final Point startPoint2, final Point endPoint2){
		double lonA = startPoint1.getX();
		double latA = startPoint1.getY();
		final double lonB = endPoint1.getX();
		final double latB = endPoint1.getY();
		double lonC = startPoint2.getX();
		double latC = startPoint2.getY();
		final double lonD = endPoint2.getX();
		final double latD = endPoint2.getY();
		double lonX, latX;

		boolean firstIteration = true;
		while(true){
			final double initialAzimuthAB = REFERENCE_ELLIPSOID.Inverse(latA, lonA, latB, lonB, GeodesicMask.AZIMUTH)
				.azi1;
			final double initialAzimuthCD = REFERENCE_ELLIPSOID.Inverse(latC, lonC, latD, lonD, GeodesicMask.AZIMUTH)
				.azi1;
			final GeodesicData inverseAC = REFERENCE_ELLIPSOID.Inverse(latA, lonA, latC, lonC,
				GeodesicMask.AZIMUTH | GeodesicMask.DISTANCE);

			final double relativeAzimuthA = Math.toRadians(inverseAC.azi1 - initialAzimuthAB);
			final double relativeAzimuthC = Math.toRadians(initialAzimuthCD - inverseAC.azi2) + Math.PI;

			final double angleAC = inverseAC.s12 / EARTH_EQUATORIAL_RADIUS;
			final double sinAC = StrictMath.sin(angleAC);
			final double cosAC = StrictMath.cos(angleAC);
			final double angleAX = StrictMath.atan(sinAC
				/ (cosAC * StrictMath.cos(relativeAzimuthA) + StrictMath.sin(relativeAzimuthA) / StrictMath.tan(relativeAzimuthC)));
			final double distanceAX = EARTH_EQUATORIAL_RADIUS * angleAX;

			double angleCX = StrictMath.atan(sinAC
				/ (cosAC * StrictMath.cos(relativeAzimuthC) + StrictMath.sin(relativeAzimuthC) / StrictMath.tan(relativeAzimuthA)));
			final double distanceCX = EARTH_EQUATORIAL_RADIUS * angleCX;

			GeodesicData intersectionAB = REFERENCE_ELLIPSOID.Direct(latA, lonA, initialAzimuthAB, distanceAX,
				GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);
			GeodesicData intersectionCD = REFERENCE_ELLIPSOID.Direct(latC, lonC, initialAzimuthCD, distanceCX,
				GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);

			if(firstIteration){
				//avoid selecting almost antipodal intersection:
				if(!isOnTrack(lonA, latA, lonB, latB, intersectionAB.lon2, intersectionAB.lat2))
					intersectionAB = REFERENCE_ELLIPSOID.Direct(latA, lonA, initialAzimuthAB + 180., distanceAX,
						GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);
				if(!isOnTrack(lonC, latC, lonD, latD, intersectionCD.lon2, intersectionCD.lat2))
					intersectionCD = REFERENCE_ELLIPSOID.Direct(latC, lonC, initialAzimuthCD + 180., distanceCX,
						GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);
			}
			firstIteration = false;

			if(Math.abs(distanceAX) < ON_TRACK_POINT_PRECISION && Math.abs(distanceCX) < ON_TRACK_POINT_PRECISION){
				latX = (intersectionAB.lat2 + intersectionCD.lat2) / 2.;
				lonX = (intersectionAB.lon2 + intersectionCD.lon2) / 2.;
				break;
			}
			else{
				latA = intersectionAB.lat2;
				lonA = intersectionAB.lon2;
				latC = intersectionCD.lat2;
				lonC = intersectionCD.lon2;
			}
		}

		@SuppressWarnings("SuspiciousNameCombination")
		Point intersection = startPoint1.factory.createPoint(lonX, latX);
		intersection = limitIntersection(startPoint1, endPoint1, startPoint2, endPoint2, intersection);
		intersection = limitIntersection(startPoint2, endPoint2, startPoint1, endPoint1, intersection);
		return intersection;
	}

	private static boolean isOnTrack(final double lon1, final double lat1, final double lon2, final double lat2,
			double lonP, double latP){
		boolean onTrack = true;
		final double initialAzimuth1P = REFERENCE_ELLIPSOID.Inverse(lat1, lon1, latP, lonP, GeodesicMask.AZIMUTH)
			.azi1;
		final double initialAzimuth12 = REFERENCE_ELLIPSOID.Inverse(lat1, lon1, lat2, lon2, GeodesicMask.AZIMUTH)
			.azi1;
		if(Math.abs(initialAzimuth1P - initialAzimuth12) > 90.)
			onTrack = false;
		else{
			final double initialAzimuth2P = REFERENCE_ELLIPSOID.Inverse(lat2, lon2, latP, lonP, GeodesicMask.AZIMUTH)
				.azi1;
			final double initialAzimuth21 = REFERENCE_ELLIPSOID.Inverse(lat2, lon2, lat1, lon1, GeodesicMask.AZIMUTH)
				.azi1;
			if(Math.abs(initialAzimuth2P - initialAzimuth21) > 90.)
				onTrack = false;
		}
		return onTrack;
	}

	private static Point limitIntersection(final Point startPoint1, final Point endPoint1, final Point startPoint2, final Point endPoint2,
			Point intersection){
		if(intersection != null){
			if(Math.abs(initialBearing(startPoint1, intersection) - initialBearing(startPoint1, endPoint1)) > 90.)
				intersection = limitIntersection(startPoint2, endPoint2, startPoint1);
			else if(Math.abs(initialBearing(endPoint1, intersection) - initialBearing(endPoint1, startPoint1)) > 90.)
				intersection = limitIntersection(startPoint2, endPoint2, endPoint1);
		}
		return intersection;
	}

	private static Point limitIntersection(final Point startPoint, final Point endPoint, final Point point){
		final Point intersection = onTrackClosestPoint(startPoint, endPoint, point);
		return (intersection != null && orthodromicDistance(point, intersection) < ON_TRACK_POINT_PRECISION? point: null);
	}


	/**
	 * Returns the equivalent measurement error sigma in latitude.
	 *
	 * @param measurementErrorSigma	The measurement error sigma [m].
	 * @return	[°].
	 */
	public static double measurementErrorSigmaInLatitude(final double measurementErrorSigma){
		return measurementErrorSigma / METERS_PER_DEGREE;
	}

	/**
	 * Returns the equivalent measurement error sigma in longitude.
	 *
	 * @param measurementErrorSigma	The measurement error sigma [m].
	 * @param latitude	The latitude.
	 * @return	[°].
	 */
	public static double measurementErrorSigmaInLongitude(final double measurementErrorSigma, final double latitude){
		final double circumference = 2. * StrictMath.PI * EARTH_RADIUS * StrictMath.cos(StrictMath.toRadians(latitude));
		return measurementErrorSigma * 360. / circumference;
	}

}
