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

import io.github.mtrevisan.mapmatcher.helpers.GeodeticHelper;
import io.github.mtrevisan.mapmatcher.helpers.JTSGeometryHelper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;


/**
 * @see <a href="https://github.com/grumlimited/geocalc/blob/master/src/main/java/com/grum/geocalc/EarthCalc.java">EarthCalc</a>
 */
public class GeodeticCalculator implements DistanceCalculator{

	@Override
	public double distance(final Coordinate startPoint, final Coordinate endPoint){
		return GeodeticHelper.distance(startPoint, endPoint)
			.getDistance();
	}

	/**
	 * Calculate cross-track distance on a spherical geometry.
	 *
	 * @param point	The point
	 * @param lineString	The list of track points.
	 * @return	The distance [m].
	 */
	@Override
	public double distance(final Coordinate point, final LineString lineString){
		final Coordinate nearestPoint = JTSGeometryHelper.onTrackClosestPoint(lineString, point);
		return GeodeticHelper.distance(point, nearestPoint)
			.getDistance();
	}

}
