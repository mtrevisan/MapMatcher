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

import io.github.mtrevisan.mapmatcher.helpers.GeodeticHelper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;


/**
 * @see <a href="https://github.com/grumlimited/geocalc/blob/master/src/main/java/com/grum/geocalc/EarthCalc.java">EarthCalc</a>
 */
public class GeodeticCalculator implements DistanceCalculator{

	@Override
	public double distance(final Coordinate startPoint, final Coordinate endPoint){
		return GeodeticHelper.distance(startPoint, endPoint).getDistance();
	}

	/**
	 * Calculate cross-track distance on a spherical geometry.
	 * TODO: to be checked!
	 *
	 * @param point	The point
	 * @param lineString	The list of track points.
	 * @return	The distance [m].
	 */
	@Override
	public double distance(final Coordinate point, final LineString lineString){
		double minimumDistance = Double.POSITIVE_INFINITY;
		final Coordinate[] trackPoints = lineString.getCoordinates();
		for(int i = 1; i < trackPoints.length; i ++){
			final GeodeticHelper.OrthodromicDistance distance0P = GeodeticHelper.distance(trackPoints[i - 1], point);
			final GeodeticHelper.OrthodromicDistance distance01 = GeodeticHelper.distance(trackPoints[i - 1], trackPoints[i]);

			//(angular) distance from start point to third point
			final double delta13 = distance0P.getAngularDistance();
			//(initial) bearing from start point to third point
			final double theta13 = distance0P.getInitialBearing();
			//(initial) bearing from start point to end point
			final double theta12 = distance01.getInitialBearing();
			final double distance = StrictMath.asin(StrictMath.sin(delta13) * StrictMath.sin(theta13 - theta12));

			if(Math.abs(distance) < Math.abs(minimumDistance))
				minimumDistance = distance;
		}
		return minimumDistance * GeodeticHelper.EARTH_POLAR_RADIUS;
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
