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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.util.GeometricShapeFactory;


public class WGS84GeometryHelper{

	private static final PrecisionModel PRECISION_MODEL = new PrecisionModel(PrecisionModel.FLOATING);
	private static final int SRID_WGS84 = 4326;
	private static final GeometryFactory FACTORY = new GeometryFactory(PRECISION_MODEL, SRID_WGS84);


	public static Point createPoint(final double latitude, final double longitude){
		return createPoint(new Coordinate(longitude, latitude));
	}

	public static Point createPoint(final Coordinate coordinate){
		return FACTORY.createPoint(coordinate);
	}

	public static LineString createLineString(final Coordinate[] coordinates){
		return FACTORY.createLineString(coordinates);
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

}
