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

import io.github.mtrevisan.mapmatcher.spatial.Coordinate;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;


public interface DistanceCalculator{

	double distance(Coordinate startPoint, Coordinate endPoint);

	double distance(Coordinate point, Polyline polyline);


	double initialBearing(Coordinate startPoint, Coordinate endPoint);

	default Coordinate onTrackClosestPoint(final Coordinate point, final Polyline polyline){
		double minClosestPointDistance = Double.MAX_VALUE;
		Coordinate minClosestPoint = null;
		final Coordinate[] coordinates = polyline.getCoordinates();
		for(int i = 1; i < coordinates.length; i ++){
			final Coordinate startPoint = coordinates[i - 1];
			final Coordinate endPoint = coordinates[i];
			final Coordinate closestPoint = onTrackClosestPoint(startPoint, endPoint, point);
			final double distance = distance(closestPoint, startPoint, endPoint);
			if(distance < minClosestPointDistance){
				minClosestPointDistance = distance;
				minClosestPoint = closestPoint;
			}
		}
		return minClosestPoint;
	}

	Coordinate onTrackClosestPoint(Coordinate startPoint, Coordinate endPoint, Coordinate point);

	default double alongTrackDistance(final Coordinate point, final Polyline polyline){
		double nearestPointDistance = 0.;
		double cumulativeDistance = 0.;
		double minNearestPointDistance = Double.MAX_VALUE;
		final Coordinate[] coordinates = polyline.getCoordinates();
		for(int i = 1; i < coordinates.length; i ++){
			final Coordinate startPoint = coordinates[i - 1];
			final Coordinate endPoint = coordinates[i];
			final double distance = Math.abs(alongTrackDistance(startPoint, endPoint, point));
			cumulativeDistance += distance;
			if(distance < minNearestPointDistance){
				nearestPointDistance += cumulativeDistance;
				cumulativeDistance = 0.;
			}
		}
		return nearestPointDistance;
	}

	double alongTrackDistance(Coordinate startPoint, Coordinate endPoint, Coordinate point);

}
