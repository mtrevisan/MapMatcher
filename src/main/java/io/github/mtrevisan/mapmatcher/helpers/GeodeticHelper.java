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
package io.github.mtrevisan.mapmatcher.helpers;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;

import java.awt.geom.Point2D;


/**
 * @see <a href="https://community.qlik.com/t5/QlikView-App-Dev/WGS84-Compliant-Great-Circle-Distance-Calculation/td-p/241905">WGS84 Compliant Great Circle Distance Calculation</a>
 * @see <a href="https://arxiv.org/pdf/1102.1215.pdf">Geodesics on an ellipsoid of revolution</a>
 */
public class GeodeticHelper{

	private static final DefaultGeographicCRS CRS_WGS84 = DefaultGeographicCRS.WGS84;

	//flattening of the ellipsoid, in WGS84 reference (f = 1 - EARTH_POLAR_RADIUS/EARTH_EQUATORIAL_RADIUS)
	private static final double EARTH_FLATTENING = 1. / CRS_WGS84.getDatum().getEllipsoid().getInverseFlattening();
	//e^2 = (2 - f) * f
	private static final double EARTH_ECCENTRICITY_2 = (2. - EARTH_FLATTENING) * EARTH_FLATTENING;
	//[m]
	private static final double EARTH_EQUATORIAL_RADIUS = CRS_WGS84.getDatum().getEllipsoid().getSemiMajorAxis();

	//[m]
	private static final double ON_TRACK_POINT_PRECISION = 0.1;


	private GeodeticHelper(){}

	public static double distance(final Coordinate startPoint, final Coordinate endPoint){
		final org.geotools.referencing.GeodeticCalculator calculator = new org.geotools.referencing.GeodeticCalculator(CRS_WGS84);
		calculator.setStartingGeographicPoint(startPoint.getX(), startPoint.getY());
		calculator.setDestinationGeographicPoint(endPoint.getX(), endPoint.getY());
		return calculator.getOrthodromicDistance();
	}

	public static double initialBearing(final Coordinate startPoint, final Coordinate endPoint){
		final org.geotools.referencing.GeodeticCalculator calculator = new org.geotools.referencing.GeodeticCalculator(CRS_WGS84);
		calculator.setStartingGeographicPoint(startPoint.getX(), startPoint.getY());
		calculator.setDestinationGeographicPoint(endPoint.getX(), endPoint.getY());
		final double azimuth = calculator.getAzimuth();
		return (azimuth < 0.? azimuth + 360.: azimuth);
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
		final org.geotools.referencing.GeodeticCalculator calculator = new org.geotools.referencing.GeodeticCalculator(CRS_WGS84);
		calculator.setStartingGeographicPoint(startPoint.getX(), startPoint.getY());
		calculator.setDirection((initialBearing > 180.? initialBearing - 360.: initialBearing), distance);
		final Point2D destination = calculator.getDestinationGeographicPoint();
		return new Coordinate(destination.getX(), destination.getY());
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
		//approximate Earth radius [m]
		final double radius = EARTH_EQUATORIAL_RADIUS;

		Coordinate onTrackPoint = startPoint;
		while(true){
			//S_AP [m]
			final double phiA = Math.toRadians(onTrackPoint.getY());
			final double lambdaA = Math.toRadians(onTrackPoint.getX());
			double phiP = Math.toRadians(point.getY());
			double lambdaP = Math.toRadians(point.getX());
			final double distanceStartToPoint = StrictMath.acos(StrictMath.sin(phiA) * StrictMath.sin(phiP)
				+ StrictMath.cos(phiA) * StrictMath.cos(phiP) * StrictMath.cos(lambdaA - lambdaP));
			final double aa = StrictMath.sin((phiA - phiP) / 2.);
			final double bb = StrictMath.sin((lambdaA - lambdaP) / 2.);
			final double distanceStartToPoint2 = 2. * StrictMath.asin(Math.sqrt(aa * aa
				+ StrictMath.cos(phiA) * StrictMath.cos(phiP) * bb * bb));

			//alpha_AP [°]
			final double initialBearingStartToPoint = initialBearing(onTrackPoint, point);
			//alpha_AB [°]
			final double initialBearingStartToEnd = initialBearing(onTrackPoint, endPoint);
			//[rad]
			final double angleAP = Math.toRadians(initialBearingStartToEnd - initialBearingStartToPoint);
			//calculate Cross-Track Distance [m], S_PX
			final double sinAngleAP = StrictMath.sin(angleAP);
			if(sinAngleAP == 1.)
				break;

			//[rad]
			final double xtd = StrictMath.asin(StrictMath.sin(distanceStartToPoint) * sinAngleAP);
			//calculate Along-Track Distance [rad], S_AX
			final double a = StrictMath.sin((Math.PI / 2. + angleAP) / 2.);
			final double b = StrictMath.sin((Math.PI / 2. - angleAP) / 2.);
			final double c = StrictMath.tan((distanceStartToPoint - xtd) / 2.);
			final double atd = 2. * radius * StrictMath.atan((a / b) * c);
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


	private static double calculateReducedLatitude(final double phi){
		return Math.toDegrees(StrictMath.atan((1. - EARTH_FLATTENING) * StrictMath.tan(Math.toRadians(phi))));
	}

	private static double calculateTrueLatitude(final double reducedPhi){
		return Math.toDegrees(StrictMath.atan(StrictMath.tan(Math.toRadians(reducedPhi)) / (1. - EARTH_FLATTENING)));
	}

	/**
	 * Earth mean radius of curvature by latitude.
	 *
	 * @param latitude	The latitude [°].
	 * @return	The geocentric mean radius [m].
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Earth_radius#Radii_of_curvature">Earth radius</a>
	 */
	public static double meanRadiusOfCurvature(final double latitude){
		final double m = meridionalRadiusOfCurvature(latitude);
		final double n = primeVerticalRadiusOfCurvature(latitude);
		return 2. * m * n / (m + n);
	}

	/**
	 * Earth meridional radius of curvature by latitude.
	 *
	 * @param latitude	The latitude [°].
	 * @return	The meridional radius [m].
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Earth_radius#Radii_of_curvature">Earth radius</a>
	 */
	private static double meridionalRadiusOfCurvature(final double latitude){
		final double tmp = primeVerticalRadiusOfCurvature(latitude) / EARTH_EQUATORIAL_RADIUS;
		return tmp * tmp * tmp * (1. - EARTH_ECCENTRICITY_2) * EARTH_EQUATORIAL_RADIUS;
	}

	/**
	 * Earth prime vertical radius of curvature by latitude.
	 *
	 * @param latitude	The latitude [°].
	 * @return	The prime vertical radius [m].
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Earth_radius#Radii_of_curvature">Earth radius</a>
	 */
	private static double primeVerticalRadiusOfCurvature(final double latitude){
		final double sinLat = StrictMath.sin(Math.toRadians(latitude));
		return EARTH_EQUATORIAL_RADIUS / Math.sqrt(1. - EARTH_ECCENTRICITY_2 * sinLat * sinLat);
	}

}
