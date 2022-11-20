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
package io.github.mtrevisan.mapmatcher.graph;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.Objects;


public class Coordinates{

	private static final PrecisionModel PRECISION_MODEL = new PrecisionModel(PrecisionModel.FLOATING);
	private static final int SRID_WGS84 = 4326;
	private static final GeometryFactory FACTORY = new GeometryFactory(PRECISION_MODEL, SRID_WGS84);


	private final Point point;


	public static Coordinates of(final double latitude, final double longitude){
		return new Coordinates(latitude, longitude);
	}

	private Coordinates(final double latitude, final double longitude){
		if(latitude < -90. || latitude > 90.)
			throw new IllegalArgumentException("Latitude must be between -90 and 90 inclusive");
		if(longitude < -180. || longitude > 180.)
			throw new IllegalArgumentException("Longitude must be between -180 and 180 inclusive");

		point = FACTORY.createPoint(new Coordinate(longitude, latitude));
	}

	public double getLatitude(){
		return point.getY();
	}

	public double getLongitude(){
		return point.getX();
	}

	public Point getPoint(){
		return point;
	}

	@Override
	public boolean equals(final Object o){
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;

		final Coordinates other = (Coordinates)o;
		return (Double.compare(other.getLatitude(), getLatitude()) == 0 && Double.compare(other.getLongitude(), getLongitude()) == 0);
	}

	@Override
	public int hashCode(){
		return Objects.hash(getLatitude(), getLongitude());
	}

	@Override
	public String toString(){
		return "Coordinates{" + "φ = " + getLatitude() + ", λ = " + getLongitude() + '}';
	}

}
