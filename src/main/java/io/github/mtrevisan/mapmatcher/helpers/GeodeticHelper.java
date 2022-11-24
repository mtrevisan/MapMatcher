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

import org.locationtech.jts.geom.Coordinate;


public class GeodeticHelper{

	//flattening of the ellipsoid, in WGS84 reference (f = 1 - EARTH_POLAR_RADIUS/EARTH_EQUATORIAL_RADIUS)
	private static final double EARTH_FLATTENING = 1. / 298.257_223_563;
	//length of semi-major axis of the ellipsoid (radius at the equator), in WGS84 reference [m]
	private static final double EARTH_EQUATORIAL_RADIUS = 6_378_137.;
	//length of semi-minor axis of the ellipsoid (radius at the poles) [m]
	private static final double EARTH_POLAR_RADIUS = (1. - EARTH_FLATTENING) * EARTH_EQUATORIAL_RADIUS;
	//ff = 1 / (1 - f)
	private static final double FF = EARTH_EQUATORIAL_RADIUS / EARTH_POLAR_RADIUS;

	//this corresponds to an accuracy of approximately 0.1 m
	private static final double CONVERGENCE_THRESHOLD = 1.e-8;
	private static final double EPSILON_COINCIDENT_POINT = 1.e-16;
	private static final int ITERATION_LIMIT = 5;


	public static class OrthodromicDistance{
		private Coordinate destination;

		//[rad]
		private double angularDistance;
		//ellipsoidal distance between the two points [m]
		private double distance;
		//forward azimuths at the points [rad]
		private double initialBearing;
		//[rad]
		private double finalBearing;


		public Coordinate getDestination(){
			return destination;
		}

		public double getAngularDistance(){
			return angularDistance;
		}

		public double getDistance(){
			return distance;
		}

		public double getInitialBearing(){
			return initialBearing;
		}

		public double getFinalBearing(){
			return finalBearing;
		}
	}


	/**
	 * Calculate orthodromic distance, (azimuth) bearing and final bearing between two points using inverse Vincenty formula.
	 *
	 * @param startPoint	The start point.
	 * @param endPoint	The end point.
	 * @return	The distance [m].
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Vincenty%27s_formulae">Vincenty's formulae</a>
	 */
	public static OrthodromicDistance distance(final Coordinate startPoint, final Coordinate endPoint){
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
		//σ is the angular distance on the sphere
		double sigma, sinSigma, cosSigma;
		//σₘ is the angular distance on the sphere from the equator to the midpoint of the line
		double cos2SigmaM;
		//α is the forward azimuth of the geodesic at the equator, if it were extended that far
		double cos2Alpha;
		int iteration = ITERATION_LIMIT;
		do{
			sinLambda = StrictMath.sin(lambda);
			cosLambda = StrictMath.cos(lambda);
			final double tmp1 = cosU2 * sinLambda;
			final double tmp2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
			sinSigma = Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2);
			//this corresponds to an accuracy of approximately 0.1 m
			if(Math.abs(sinSigma) < EPSILON_COINCIDENT_POINT)
				//points are coincident
				return new OrthodromicDistance();

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

		final double u2 = cos2Alpha * (FF * FF - 1.);
		final double aa = 1. + u2 / 16384. * (4096. + u2 * (-768. + u2 * (320. - 175. * u2)));
		final double bb = u2 / 1024. * (256. + u2 * (-128. + u2 * (74. - 47. * u2)));
		final double deltaSigma = bb * sinSigma * (cos2SigmaM
			+ bb / 4. * (cosSigma * (-1. + 2. * cos2SigmaM * cos2SigmaM)
			- bb / 6. * cos2SigmaM * (-3. + 4. * sinSigma * sinSigma) * (-3. + 4. * cos2SigmaM * cos2SigmaM)));

		//angular distance on the sphere between points [rad]
		final double angularDistance = sigma - deltaSigma;
		//ellipsoidal distance between the two points [m]
		final double distance = EARTH_POLAR_RADIUS * angularDistance * aa;
		//forward azimuths at the points [rad]
		//Note the special handling of exactly antipodal points where sin²σ = 0 (due to discontinuity `atan2(0, 0) = 0` but
		// `atan2(ε, 0) = π/2`) - in which case bearing is always meridional, due north (or due south!)
		final double initialBearing = (Math.abs(sinSigma) >= EPSILON_COINCIDENT_POINT
			? reduce0_2pi(StrictMath.atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda))
			: 0.
		);
		//[rad]
		final double finalBearing = (Math.abs(sinSigma) >= EPSILON_COINCIDENT_POINT
			? reduce0_2pi(StrictMath.atan2(cosU1 * sinLambda, -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda))
			: Math.PI
		);

		final OrthodromicDistance od = new OrthodromicDistance();
		od.angularDistance = angularDistance;
		od.distance = distance;
		od.initialBearing = initialBearing;
		od.finalBearing = finalBearing;
		return od;
	}

	/**
	 * Destination given distance & bearing from start point (direct solution).
	 *
	 * @param origin	Origin position.
	 * @param initialBearing	Azimuth of the geodesic [°].
	 * @param distance	Length of the geodesic along the surface of the ellipsoid [m].
	 * @return	The final position.
	 */
	public static OrthodromicDistance destination(final Coordinate origin, final double initialBearing, final double distance){
		final double phi1 = Math.toRadians(origin.getY());
		final double lambda1 = Math.toRadians(origin.getX());

		final double tanU1 = (1. - EARTH_FLATTENING) * StrictMath.tan(phi1);
		final double cosU1 = 1. / Math.sqrt(1. + tanU1 * tanU1);
		final double sinU1 = tanU1 * cosU1;
		final double sinAlpha1 = StrictMath.sin(Math.toRadians(initialBearing));
		final double cosAlpha1 = StrictMath.cos(Math.toRadians(initialBearing));
		//angular distance on the sphere from the equator to the first point
		double angularDistance = StrictMath.atan2(tanU1, cosAlpha1);
		//α is the azimuth of the geodesic at the equator
		final double sinAlpha = cosU1 * sinAlpha1;
		final double cos2Alpha = 1. - sinAlpha * sinAlpha;
		final double u2 = cos2Alpha * (FF * FF - 1.);
		final double aa = 1. + u2 / 16384. * (4096. + u2 * (-768. + u2 * (320. - 175. * u2)));
		final double bb = u2 / 1024. * (256. + u2 * (-128. + u2 * (74. - 47. * u2)));

		//σ is the angular distance on the sphere
		double sigma = distance / (EARTH_POLAR_RADIUS * aa);
		double sigmaPrime, sinSigma, cosSigma;
		//σₘ is the angular distance on the sphere from the equator to the midpoint of the line
		double cos2SigmaM;
		int iteration = ITERATION_LIMIT;
		do{
			sinSigma = StrictMath.sin(sigma);
			cosSigma = StrictMath.cos(sigma);
			cos2SigmaM = StrictMath.cos(2. * angularDistance + sigma);
			final double deltaSigma = bb * sinSigma * (cos2SigmaM + bb / 4. * (cosSigma * (-1. + 2. * cos2SigmaM * cos2SigmaM)
				- bb / 6. * cos2SigmaM * (-3. + 4. * sinSigma * sinSigma) * (-3. + 4. * cos2SigmaM * cos2SigmaM)));
			sigmaPrime = sigma;
			sigma = distance / (EARTH_POLAR_RADIUS * aa) + deltaSigma;
		}while(Math.abs(sigma - sigmaPrime) > CONVERGENCE_THRESHOLD && -- iteration > 0);
		if(iteration == 100)
			throw new IllegalStateException("Formula failed to converge");

		final double x = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1;
		final double phi2 = StrictMath.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1,
			(1. - EARTH_FLATTENING) * Math.sqrt(sinAlpha * sinAlpha + x * x));
		final double lambda = StrictMath.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
		final double cc = EARTH_FLATTENING / 16. * cos2Alpha * (4. + EARTH_FLATTENING * (4. - 3. * cos2Alpha));
		final double ll = lambda - (1. - cc) * EARTH_FLATTENING * sinAlpha * (sigma + cc * sinSigma * (cos2SigmaM
			+ cc * cosSigma * (-1. + 2. * cos2SigmaM * cos2SigmaM)));
		final double lambda2 = lambda1 + ll;
		//[rad]
		final double finalBearing = StrictMath.atan2(sinAlpha, -x);

		final OrthodromicDistance od = new OrthodromicDistance();
		od.destination = new Coordinate(Math.toDegrees(lambda2), Math.toDegrees(phi2));
		od.angularDistance = sigma;
		od.distance = distance;
		od.initialBearing = initialBearing;
		od.finalBearing = finalBearing;
		return od;
	}


	private static double reduce0_2pi(final double angle){
		return (angle + 2. * Math.PI) % (2. * Math.PI);
	}

}
