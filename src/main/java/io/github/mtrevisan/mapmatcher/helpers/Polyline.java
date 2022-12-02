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

import io.github.mtrevisan.mapmatcher.distances.DistanceCalculator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.StringJoiner;


public class Polyline implements Comparable<Polyline>, Serializable{

	@Serial
	private static final long serialVersionUID = -2848807287557631823L;

	private final Coordinate[] coordinates;


	public static Polyline of(final Coordinate... coordinates){
		return new Polyline(coordinates);
	}

	public static Polyline ofSimplified(final DistanceCalculator distanceCalculator, final double distanceTolerance,
			final Coordinate... coordinates){
		final RamerDouglasPeuckerSimplifier simplifier = new RamerDouglasPeuckerSimplifier(coordinates, distanceCalculator);
		simplifier.setDistanceTolerance(distanceTolerance);
		return of(simplifier.simplify());
	}

	private Polyline(final Coordinate... coordinates){
		this.coordinates = Arrays.copyOf(coordinates, coordinates.length);
	}

	public boolean isEmpty() {
		return (coordinates == null || coordinates.length == 0);
	}

	public Coordinate getStartCoordinate(){
		return (isEmpty()? null: coordinates[0]);
	}

	public Coordinate getEndCoordinate(){
		return (isEmpty()? null: coordinates[coordinates.length - 1]);
	}

	public Coordinate[] getCoordinates(){
		return coordinates;
	}

	public int size(){
		return coordinates.length;
	}

	public Polyline reverse(){
		final Coordinate[] reversedCoordinates = Arrays.copyOf(coordinates, coordinates.length);
		ArrayUtils.reverse(reversedCoordinates);
		return of(reversedCoordinates);
	}


	@Override
	public boolean equals(final Object obj){
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;

		final Polyline other = (Polyline)obj;
		return Arrays.equals(coordinates, other.coordinates);
	}

	@Override
	public int hashCode(){
		return Arrays.hashCode(coordinates);
	}

	@Override
	public String toString(){
		final StringJoiner sj = new StringJoiner(", ", "LINESTRING (", ")");
		for(final Coordinate coordinate : coordinates)
			sj.add(coordinate.getX() + StringUtils.SPACE + coordinate.getY());
		return sj.toString();
	}

	@Override
	public int compareTo(final Polyline other){
		return Arrays.compare(coordinates, other.coordinates);
	}

}