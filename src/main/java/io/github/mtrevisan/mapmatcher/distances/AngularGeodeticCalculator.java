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
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LengthLocationMap;
import org.locationtech.jts.linearref.LocationIndexedLine;


//FIXME to be removed
/**
 * @see <a href="https://github.com/grumlimited/geocalc/blob/master/src/main/java/com/grum/geocalc/EarthCalc.java">EarthCalc</a>
 */
public class AngularGeodeticCalculator implements DistanceCalculator{

	@Override
	public double distance(final Coordinate startPoint, final Coordinate endPoint){
		return GeodeticHelper.distance(startPoint, endPoint);
	}

	//FIXME
	@Override
	public double distance(final Coordinate point, final LineString lineString){
		final Point start = JTSGeometryHelper.createPoint(point);
		final double distance = start.distance(lineString);
		final double radius = GeodeticHelper.meanRadiusOfCurvature((lineString.getStartPoint().getCoordinate().getY() + lineString.getEndPoint().getCoordinate().getY()) / 2.);
		final double factor = (Math.PI * radius) / 180.;
//		final double factor = 10.;

		double minNearestPointDistance = Double.MAX_VALUE;
		final Coordinate[] coordinates = lineString.getCoordinates();
		for(int i = 1; i < coordinates.length; i ++){
			final Coordinate startPoint = coordinates[i - 1];
			final Coordinate endPoint = coordinates[i];
			final Coordinate nearestPoint = GeodeticHelper.onTrackClosestPoint(startPoint, endPoint, point);
			final double dist = Math.abs(GeodeticHelper.distance(nearestPoint, point));
			if(dist < minNearestPointDistance)
				minNearestPointDistance = dist;
		}

		return distance * factor;
	}

	//FIXME
	@Override
	public double alongTrackDistance(Coordinate startPoint, Coordinate endPoint, Coordinate point){
		final LineString lineString = JTSGeometryHelper.createLineString(new Coordinate[]{startPoint, endPoint});
		//projection of point onto line
		final LocationIndexedLine locationIndexedLine = new LocationIndexedLine(lineString);
		final double distance = new LengthLocationMap(lineString)
			.getLength(locationIndexedLine.project(point));
		final double radius = GeodeticHelper.meanRadiusOfCurvature((startPoint.getY() + endPoint.getY()) / 2.);
		final double factor = (Math.PI * radius) / 180.;
		return distance * factor;
	}

}
