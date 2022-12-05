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


	private GeodeticHelper(){}

	/**
	 * Returns the orthodromic distance (using WGS84 reference system).
	 *
	 * @param startPoint	Starting point.
	 * @param endPoint	Ending point.
	 * @return	The orthodromic distance [m].
	 */
	public static double orthodromicDistance(final Coordinate startPoint, final Coordinate endPoint){
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
	public static double initialBearing(final Coordinate startPoint, final Coordinate endPoint){
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
	public static Coordinate destination(final Coordinate startPoint, final double initialBearing, final double distance){
		final double initialAzimuth = (initialBearing > 180.? initialBearing - 360.: initialBearing);
		final GeodesicData result = REFERENCE_ELLIPSOID.Direct(startPoint.getY(), startPoint.getX(), initialAzimuth, distance,
			GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);
		return Coordinate.of(result.lon2, result.lat2);
	}

	/**
	 * Returns the closest point to a given point on a great circle.
	 * <p>
	 * NOTE: not so precise, but it's enough.
	 * </p>
	 *
	 * @param	startPoint	Coordinate of starting point of the great circle.
	 * @param	endPoint	Coordinate of ending point of the great circle.
	 * @param	point	Coordinate of the point.
	 * @return	The coordinate of the point onto the great circle that is closest to the given point.
	 *
	 * @see <a href="https://edwilliams.org/avform147.htm#XTE">Aviation Formulary V1.47</a>
	 * @see <a href="https://www.researchgate.net/publication/321358300_Intersection_and_point-to-line_solutions_for_geodesics_on_the_ellipsoid">Intersection and point-to-line solutions for geodesics on the ellipsoid</a>
	 */
	public static Coordinate onTrackClosestPoint(final Coordinate startPoint, final Coordinate endPoint, final Coordinate point){
		Coordinate onTrackPoint = startPoint;
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
			if(sinAngleAP == 1.)
				break;

			//[rad]
			final double xtd = StrictMath.asin(StrictMath.sin(distanceStartToPoint) * sinAngleAP);
			//calculate Along-Track Distance [rad]
			final double a = StrictMath.sin((Math.PI / 2. + angleAP) / 2.);
			final double b = StrictMath.sin((Math.PI / 2. - angleAP) / 2.);
			final double c = StrictMath.tan((distanceStartToPoint - xtd) / 2.);
			final double atd = 2. * EARTH_EQUATORIAL_RADIUS * StrictMath.atan((a / b) * c);
			if(Math.abs(atd) < ON_TRACK_POINT_PRECISION)
				break;

			//compute a point along the great circle from start to end point that lies at distance ATD
			onTrackPoint = destination(onTrackPoint, initialBearingStartToEnd, atd);
		}

		final double angleAB = initialBearing(startPoint, endPoint);
		final double angleAX = initialBearing(startPoint, onTrackPoint);
		final double angleBX = initialBearing(endPoint, onTrackPoint);
		final double angleBA = initialBearing(endPoint, startPoint);
		final double angleA = Math.abs(angleAX - angleAB);
		final double angleB = Math.abs(angleBX - angleBA);
		if(angleA > 90.)
			return startPoint;
		else if(angleB > 90.)
			return endPoint;
		return onTrackPoint;
	}

}
