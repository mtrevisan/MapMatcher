/**
 * Copyright (c) 2022-2023 Mauro Trevisan
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
package io.github.mtrevisan.mapmatcher.spatial;

import io.github.mtrevisan.mapmatcher.helpers.MathHelper;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Pattern;


public class Point extends Geometry implements Comparable<Point>, Serializable{

	@Serial
	private static final long serialVersionUID = 3422386613349753773L;


	private static final Pattern PATTERN_POINT = Pattern.compile("\\s*POINT\\s*\\((.*)\\)\\s*");

	private static final String SPACE = " ";


	private final double x;
	private final double y;


	/**
	 * Constructs a point at <code>(x, y)</code>.
	 *
	 * @param x	The x-ordinate.
	 * @param y	The y-ordinate.
	 */
	public static Point of(final GeometryFactory factory, final double x, final double y){
		return new Point(factory, x, y);
	}

	public static Point of(final GeometryFactory factory, final Point point){
		return new Point(factory, point.x, point.y);
	}

	public static Point of(final GeometryFactory factory, final String wkt){
		return new Point(factory, wkt);
	}

	protected Point(final GeometryFactory factory, final double x, final double y){
		super(factory);

		this.x = x;
		this.y = y;
	}

	protected Point(final GeometryFactory factory, String wkt){
		super(factory);

		//clean input string
		wkt = PATTERN_POINT.matcher(wkt).replaceAll("$1")
			.trim();

		final int separatorIndex = wkt.indexOf(SPACE, 1);
		if(separatorIndex < 0)
			throw new IllegalArgumentException("WKT input is invalid: " + wkt);

		this.x = Double.parseDouble(wkt.substring(0, separatorIndex).trim());
		this.y = Double.parseDouble(wkt.substring(separatorIndex + 1).trim());
	}

	public double getX(){
		return x;
	}

	public double getY(){
		return y;
	}

	public double getCoordinate(final int dimension){
		return (dimension == 0? x: y);
	}

	public int getDimensions(){
		return 2;
	}


	public double distance(final Point point){
		return factory.topologyCalculator.distance(this, point);
	}

	public double distance(final Polyline polyline){
		return factory.topologyCalculator.distance(this, polyline);
	}

	public double initialBearing(final Point point){
		return factory.topologyCalculator.initialBearing(this, point);
	}

	public Point destination(final double initialBearing, final double distance){
		return factory.topologyCalculator.destination(this, initialBearing, distance);
	}


	@Override
	public boolean equals(final Object obj){
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;

		final Point other = (Point)obj;
		return (Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x)
			&& Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y));
	}

	public boolean equals(final Point other, final double precision){
		if(this == other)
			return true;

		return (MathHelper.nearlyEqual(x, other.x, precision)
			&& MathHelper.nearlyEqual(y, other.y, precision));
	}

	@Override
	public int hashCode(){
		return Objects.hash(x, y);
	}

	@Override
	public String toString(){
		final StringJoiner sj = new StringJoiner(", ", "POINT (", ")");
		sj.add(x + SPACE + y);
		return sj.toString();
	}

	public String toSimpleString(){
		final StringJoiner sj = new StringJoiner(", ");
		sj.add(x + SPACE + y);
		return sj.toString();
	}

	@Override
	public int compareTo(final Point other){
		if(x < other.x)
			return -1;
		if(x > other.x)
			return 1;
		return Double.compare(y, other.y);
	}

}
