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
package io.github.mtrevisan.mapmatcher.distances;

import io.github.mtrevisan.mapmatcher.helpers.Coordinate;
import io.github.mtrevisan.mapmatcher.helpers.GeodeticHelper;
import io.github.mtrevisan.mapmatcher.helpers.Polyline;


/**
 * @see <a href="https://github.com/grumlimited/geocalc/blob/master/src/main/java/com/grum/geocalc/EarthCalc.java">EarthCalc</a>
 */
public class GeodeticCalculator implements DistanceCalculator{

	/**
	 * Calculate orthodromic distance, (azimuth) bearing and final bearing between two points using inverse Vincenty formula.
	 *
	 * @param startPoint	The start point.
	 * @param endPoint	The end point.
	 * @return	The distance.
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Vincenty%27s_formulae">Vincenty's formulae</a>
	 */
	@Override
	public double distance(final Coordinate startPoint, final Coordinate endPoint){
		return GeodeticHelper.orthodromicDistance(startPoint, endPoint);
	}

	/**
	 * Calculate cross-track distance.
	 *
	 * @param point	The point.
	 * @param lineString The list of track points.
	 * @return The distance [m].
	 */
	@Override
	public double distance(final Coordinate point, final Polyline lineString){
		double minNearestPointDistance = Double.MAX_VALUE;
		final Coordinate[] coordinates = lineString.getCoordinates();
		for(int i = 1; i < coordinates.length; i ++){
			final Coordinate startPoint = coordinates[i - 1];
			final Coordinate endPoint = coordinates[i];
			final Coordinate nearestPoint = GeodeticHelper.onTrackClosestPoint(startPoint, endPoint, point);
			final double distance = Math.abs(GeodeticHelper.orthodromicDistance(nearestPoint, point));
			if(distance < minNearestPointDistance)
				minNearestPointDistance = distance;
		}
		return minNearestPointDistance;
	}

	@Override
	public double alongTrackDistance(final Coordinate startPoint, final Coordinate endPoint, final Coordinate point){
		return GeodeticHelper.orthodromicDistance(point, GeodeticHelper.onTrackClosestPoint(startPoint, endPoint, point));
	}

}
