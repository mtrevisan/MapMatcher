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
package io.github.mtrevisan.mapmatcher.spatial;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;


public class Coordinate extends GeometryAbstract implements Comparable<Coordinate>, Serializable{

	@Serial
	private static final long serialVersionUID = 3422386613349753773L;

	private final double x;
	private final double y;


	/**
	 * Constructs a <code>Coordinate</code> at (x, y).
	 *
	 * @param x	The x-ordinate.
	 * @param y	The y-ordinate.
	 */
	public static Coordinate of(final GeometryFactory factory, final double x, final double y){
		return new Coordinate(factory, x, y);
	}

	public static Coordinate of(final GeometryFactory factory, final Coordinate coordinate){
		return new Coordinate(factory, coordinate.x, coordinate.y);
	}

	protected Coordinate(final GeometryFactory factory, final double x, final double y){
		super(factory);

		this.x = x;
		this.y = y;
	}

	public double getX(){
		return x;
	}

	public double getY(){
		return y;
	}


	public double initialBearing(final Coordinate point){
		return factory.distanceCalculator.initialBearing(this, point);
	}

	public double distance(final Coordinate point){
		return factory.distanceCalculator.distance(this, point);
	}

	public double distance(final Polyline polyline){
		return factory.distanceCalculator.distance(this, polyline);
	}


	@Override
	public boolean equals(final Object obj){
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;

		final Coordinate other = (Coordinate)obj;
		return (Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x)
			&& Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y));
	}

	@Override
	public int hashCode(){
		return Objects.hash(x, y);
	}

	@Override
	public String toString(){
		return "(" + x + ", " + y + ")";
	}

	@Override
	public int compareTo(final Coordinate other){
		if(x < other.x)
			return -1;
		if(x > other.x)
			return 1;
		return Double.compare(y, other.y);
	}

}
