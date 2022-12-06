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
package io.github.mtrevisan.mapmatcher.spatial.distances;

import io.github.mtrevisan.mapmatcher.spatial.GeodeticHelper;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;


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
	public double distance(final Point startPoint, final Point endPoint){
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
	public double distance(final Point point, final Polyline lineString){
		double minNearestPointDistance = Double.MAX_VALUE;
		final Point[] points = lineString.getPoints();
		for(int i = 1; i < points.length; i ++){
			final Point startPoint = points[i - 1];
			final Point endPoint = points[i];
			final Point nearestPoint = GeodeticHelper.onTrackClosestPoint(startPoint, endPoint, point);
			final double distance = Math.abs(GeodeticHelper.orthodromicDistance(nearestPoint, point));
			if(distance < minNearestPointDistance)
				minNearestPointDistance = distance;
		}
		return minNearestPointDistance;
	}

	@Override
	public double initialBearing(final Point startPoint, final Point endPoint){
		return GeodeticHelper.initialBearing(startPoint, endPoint);
	}


	@Override
	public Point onTrackClosestPoint(final Point startPoint, final Point endPoint, final Point point){
		return GeodeticHelper.onTrackClosestPoint(startPoint, endPoint, point);
	}

	@Override
	public double alongTrackDistance(final Point startPoint, final Point endPoint, final Point point){
		final Point onTrackClosestPoint = onTrackClosestPoint(startPoint, endPoint, point);
		return GeodeticHelper.orthodromicDistance(startPoint, onTrackClosestPoint);
	}

}
