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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;


public interface DistanceCalculator{

	double distance(Coordinate startPoint, Coordinate endPoint);

	double distance(Coordinate point, LineString lineString);

	double alongTrackDistance(Coordinate startPoint, Coordinate endPoint, Coordinate point);

	default double alongTrackDistance(Coordinate point, LineString lineString){
		double minNearestPointDistance = Double.MAX_VALUE;
		final Coordinate[] coordinates = lineString.getCoordinates();
		for(int i = 1; i < coordinates.length; i ++){
			final Coordinate startPoint = coordinates[i - 1];
			final Coordinate endPoint = coordinates[i];
			final double distance = Math.abs(alongTrackDistance(startPoint, endPoint, point));
			if(distance < minNearestPointDistance)
				minNearestPointDistance = distance;
		}
		return minNearestPointDistance;
	}

}
