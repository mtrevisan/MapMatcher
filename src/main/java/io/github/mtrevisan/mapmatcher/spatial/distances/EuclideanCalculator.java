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


public class EuclideanCalculator implements DistanceCalculator{

	@Override
	public double distance(final Coordinate startPoint, final Coordinate endPoint){
		final double dx = startPoint.getX() - endPoint.getX();
		final double dy = startPoint.getY() - endPoint.getY();
		return Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	public double distance(final Coordinate point, final Polyline polyline){
		double minNearestPointDistance = Double.MAX_VALUE;
		final Coordinate[] coordinates = polyline.getCoordinates();
		for(int i = 1; i < coordinates.length; i ++){
			final Coordinate startPoint = coordinates[i - 1];
			final Coordinate endPoint = coordinates[i];
			final double distance = Math.abs(distance(startPoint, endPoint));
			if(distance < minNearestPointDistance)
				minNearestPointDistance = distance;
		}
		return minNearestPointDistance;
	}


	@Override
	public double initialBearing(final Coordinate startPoint, final Coordinate endPoint){
		final double dx = endPoint.getX() - startPoint.getX();
		final double dy = endPoint.getY() - startPoint.getY();
		final double angle = Math.toDegrees(StrictMath.atan2(dx, dy));
		return (angle < 0.? angle + 360.: angle);
	}

	@Override
	public Coordinate onTrackClosestPoint(final Coordinate startPoint, final Coordinate endPoint, final Coordinate point){
		//TODO
		return null;
	}

	@Override
	public double alongTrackDistance(final Coordinate startPoint, final Coordinate endPoint, final Coordinate point){
		final double dx21 = endPoint.getX() - startPoint.getX();
		final double dy21 = endPoint.getY() - startPoint.getY();
		return Math.abs(dx21 * (startPoint.getY() - point.getY()) - (startPoint.getX() - point.getX()) * dy21)
			/ Math.sqrt(dx21 * dx21 + dy21 * dy21);
	}

}
