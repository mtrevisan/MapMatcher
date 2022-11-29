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
package io.github.mtrevisan.mapmatcher.helpers;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.linearref.LengthLocationMap;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.util.GeometricShapeFactory;


public class JTSGeometryHelper{

	private static final PrecisionModel PRECISION_MODEL = new PrecisionModel(PrecisionModel.FLOATING);
	private static final int SRID_WGS84 = 4326;
	private static final GeometryFactory FACTORY = new GeometryFactory(PRECISION_MODEL, SRID_WGS84);

	private static final WKTReader WKT_READER = new WKTReader(FACTORY);


	public static WKTReader getWktReader(){
		return WKT_READER;
	}

	public static Point createPoint(final double latitude, final double longitude){
		return createPoint(new Coordinate(longitude, latitude));
	}

	public static Point createPoint(final Coordinate coordinate){
		return FACTORY.createPoint(coordinate);
	}

	public static LineString createLineString(final Coordinate[] coordinates){
		return FACTORY.createLineString(coordinates);
	}

	public static LineString createSimplifiedLineString(final Coordinate[] coordinates, final double distanceTolerance){
		final LineString lineString = createLineString(coordinates);
		final DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(lineString);
		simplifier.setDistanceTolerance(distanceTolerance);
		return (LineString)simplifier.getResultGeometry();
	}

	public static Polygon createCircle(final Coordinate origin, final double radius){
		final double phi = Math.toRadians(origin.getY());
		//precision is within 1 cm [m/°]
		final double metersPerDegreeInLatitude = 111_132.954 - 559.822 * StrictMath.cos(2. * phi)
			+ 1.175 * StrictMath.cos(4. * phi);
		final double metersPerDegreesInLongitude = 111_132.954 * StrictMath.cos(phi);

		final GeometricShapeFactory gsf = new GeometricShapeFactory(FACTORY);
		gsf.setWidth(radius * 2. / metersPerDegreesInLongitude);
		gsf.setHeight(radius * 2. / metersPerDegreeInLatitude);
		gsf.setCentre(origin);
		return gsf.createEllipse();
	}


	public static double alongTrackDistance(final LineString line, final Coordinate coordinate){
		//projection of point onto line
		final LocationIndexedLine locationIndexedLine = new LocationIndexedLine(line);
		final LinearLocation point = locationIndexedLine.project(coordinate);
		return new LengthLocationMap(line)
			.getLength(point);
	}

	public static double crossTrackDistance(final LineString line, final Coordinate coordinate){
		final Coordinate nearestPoint = onTrackClosestPoint(line, coordinate);
		return nearestPoint.distance(coordinate);
	}

	public static Coordinate onTrackClosestPoint(final LineString line, final Coordinate coordinate){
		return DistanceOp.nearestPoints(line, createPoint(coordinate))[0];
	}

	public static double distanceClosestPointsOnLineString(final LineString line, final Coordinate coordinate1,
			final Coordinate coordinate2){
		//projection of point onto line
		final LocationIndexedLine locationIndexedLine = new LocationIndexedLine(line);
		final LinearLocation point1 = locationIndexedLine.project(coordinate1);
		final LinearLocation point2 = locationIndexedLine.project(coordinate2);

		final LengthLocationMap lengthLocationMap = new LengthLocationMap(line);
		return Math.abs(lengthLocationMap.getLength(point1) - lengthLocationMap.getLength(point2));
	}

}
