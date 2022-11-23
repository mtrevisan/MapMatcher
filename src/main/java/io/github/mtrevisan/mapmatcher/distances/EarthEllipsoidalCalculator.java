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
package io.github.mtrevisan.mapmatcher.distances;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;


/**
 * @see <a href="https://github.com/grumlimited/geocalc/blob/master/src/main/java/com/grum/geocalc/EarthCalc.java">EarthCalc</a>
 */
public class EarthEllipsoidalCalculator implements DistanceCalculator{

	//flattening of the ellipsoid, in WGS84 reference
	private static final double EARTH_FLATTENING = 1. / 298.257_223_563;
	//length of semi-major axis of the ellipsoid (radius at the equator), in WGS84 reference [m]
	private static final double EARTH_EQUATORIAL_RADIUS = 6_378_137.;
	//length of semi-minor axis of the ellipsoid (radius at the poles) [m]
	public static final double EARTH_POLAR_RADIUS = (1. - EARTH_FLATTENING) * EARTH_EQUATORIAL_RADIUS;

	//this corresponds to an accuracy of approximately 0.1 m
	private static final double CONVERGENCE_THRESHOLD = 1.e-8;
	private static final int ITERATION_LIMIT = 10;


	/**
	 * Calculate distance, (azimuth) bearing and final bearing between two points using inverse Vincenty formula.
	 *
	 * @param startPoint	The start point.
	 * @param endPoint	The end point.
	 * @return	The distance [m].
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Vincenty%27s_formulae">Vincenty's formulae</a>
	 */
	@Override
	public double distance(final Coordinate startPoint, final Coordinate endPoint){
		final double lambda1 = Math.toRadians(startPoint.getX());
		final double lambda2 = Math.toRadians(endPoint.getX());
		final double phi1 = Math.toRadians(startPoint.getY());
		final double phi2 = Math.toRadians(endPoint.getY());

		//U1 and U2 are the reduced latitude (latitude on the auxiliary sphere)
		final double deltaLambda = lambda2 - lambda1;
		final double tanU1 = (1. - EARTH_FLATTENING) * StrictMath.tan(phi1);
		final double cosU1 = 1. / Math.sqrt(1. + tanU1 * tanU1);
		final double sinU1 = tanU1 * cosU1;

		final double tanU2 = (1. - EARTH_FLATTENING) * StrictMath.tan(phi2);
		final double cosU2 = 1. / Math.sqrt(1. + tanU2 * tanU2);
		final double sinU2 = tanU2 * cosU2;

		//difference in longitude of the points on the auxiliary sphere
		double lambda = deltaLambda;
		double lambdaPrime, sinLambda, cosLambda;
		double sigma, sinSigma, cosSigma, cos2SigmaM;
		//alpha is the forward azimuth of the geodesic at the equator, if it were extended that far
		double cos2Alpha;
		int iteration = ITERATION_LIMIT;
		do{
			sinLambda = StrictMath.sin(lambda);
			cosLambda = StrictMath.cos(lambda);
			final double tmp1 = cosU2 * sinLambda;
			final double tmp2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
			sinSigma = Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2);
			if(sinSigma == 0.)
				//points are coincident
				return 0.;

			cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
			//angular separation between points
			sigma = StrictMath.atan2(sinSigma, cosSigma);
			final double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
			cos2Alpha = 1. - sinAlpha * sinAlpha;
			//NOTE: if cos2Alpha = 0, then the points are on the equator
			cos2SigmaM = (cos2Alpha != 0.? cosSigma - 2. * sinU1 * sinU2 / cos2Alpha: 0.);

			final double c = EARTH_FLATTENING / 16. * cos2Alpha * (4. + EARTH_FLATTENING * (4. - 3. * cos2Alpha));
			lambdaPrime = lambda;
			lambda = deltaLambda + (1. - c) * EARTH_FLATTENING * sinAlpha
				* (sigma + c * sinSigma * (cos2SigmaM + c * cosSigma * (-1. + 2. * cos2SigmaM * cos2SigmaM)));
		}while(Math.abs(lambda - lambdaPrime) > CONVERGENCE_THRESHOLD && -- iteration > 0);

		//between two nearly antipodal points, the iterative formula may fail to converge
		if(iteration == 0)
			throw new IllegalStateException("Formula failed to converge");

		final double u2 = cos2Alpha * (EARTH_EQUATORIAL_RADIUS * EARTH_EQUATORIAL_RADIUS / (EARTH_POLAR_RADIUS * EARTH_POLAR_RADIUS) - 1.);
		final double aa = 1. + u2 / 16384. * (4096. + u2 * (-768. + u2 * (320. - 175. * u2)));
		final double bb = u2 / 1024. * (256. + u2 * (-128. + u2 * (74. - 47. * u2)));
		final double deltaSigma = bb * sinSigma * (cos2SigmaM
			+ bb / 4. * (cosSigma * (-1. + 2. * cos2SigmaM * cos2SigmaM)
			- bb / 6. * cos2SigmaM * (-3. + 4. * sinSigma * sinSigma) * (-3. + 4. * cos2SigmaM * cos2SigmaM)));

		//ellipsoidal distance between the two points [m]
		final double distance = EARTH_POLAR_RADIUS * aa * (sigma - deltaSigma);
		//angular separation between points [°]
		final double angularSeparation = Math.toDegrees(sigma);
		//forward azimuths at the points [°]
//		final double initialBearing = Math.toDegrees(
//			reduce0_2pi(StrictMath.atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda))
//		);
		//[°]
//		final double finalBearing = Math.toDegrees(
//			reduce0_2pi(StrictMath.atan2(cosU1 * sinLambda, -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda))
//		);

		return distance;
	}

	@Override
	public double distance(final Coordinate point, final LineString lineString){
		//TODO
		return 0.;
	}

	private static double reduce0_2pi(final double angle){
		return (angle + 2. * Math.PI) % (2. * Math.PI);
	}


//	//[km]
//	private static final double EARTH_RADIUS = 6371.;
//
//
//	public static double distance(final Coordinates standPoint, final Coordinates endPoint){
//		var startLatitude = standPoint.getLatitude();
//		final var startLongitude = standPoint.getLongitude();
//		var endLatitude = endPoint.getLatitude();
//		final var endLongitude = endPoint.getLongitude();
//
//		final var deltaLatitude = Math.toRadians(endLatitude - startLatitude);
//		final var deltaLongitude = Math.toRadians(endLongitude - startLongitude);
//
//		startLatitude = Math.toRadians(startLatitude);
//		endLatitude = Math.toRadians(endLatitude);
//
//		final var a = haversine(deltaLatitude) + StrictMath.cos(startLatitude) * StrictMath.cos(endLatitude) * haversine(deltaLongitude);
//		final var c = 2. * StrictMath.atan2(Math.sqrt(a), Math.sqrt(1. - a));
//
//		return EARTH_RADIUS * c;
//	}
//
//	private static double haversine(final double val){
//		final var arg = StrictMath.sin(val / 2.);
//		return arg * arg;
//	}

}
