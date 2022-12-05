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
package io.github.mtrevisan.mapmatcher.helpers.spatial;

import io.github.mtrevisan.mapmatcher.distances.DistanceCalculator;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.StringJoiner;


public class Polyline implements Comparable<Polyline>, Serializable{

	@Serial
	private static final long serialVersionUID = -2848807287557631823L;

	private static final String SPACE = " ";

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
		if(coordinates != null)
			this.coordinates = Arrays.copyOf(coordinates, coordinates.length);
		else
			this.coordinates = new Coordinate[0];
	}

	public boolean isEmpty() {
		return (coordinates.length == 0);
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

	public Envelope getBoundingBox(){
		double minLatitude = Double.POSITIVE_INFINITY;
		double maxLatitude = Double.NEGATIVE_INFINITY;
		double minLongitude = Double.POSITIVE_INFINITY;
		double maxLongitude = Double.NEGATIVE_INFINITY;
		if(coordinates.length > 0){
			minLatitude = coordinates[0].getY();
			maxLatitude = coordinates[0].getY();
			minLongitude = coordinates[0].getX();
			maxLongitude = coordinates[0].getX();
		}
		for(int i = 1; i < coordinates.length; i ++){
			final Coordinate point = coordinates[i];
			if(point.getX() < minLongitude)
				minLongitude = point.getX();
			else if(point.getX() > maxLongitude)
				maxLongitude = point.getX();

			if(point.getY() < minLatitude)
				minLatitude = point.getY();
			else if(point.getY() > maxLatitude)
				maxLatitude = point.getY();
		}
		return Envelope.of(minLongitude, maxLongitude, minLatitude, maxLatitude);
	}

	/**
	 * Returns a (closed) {@link Polyline} that represents the convex hull of this polyline.
	 * <p>
	 * The returned geometry contains the minimal number of points needed to represent the convex hull.<br/>
	 * In particular, no more than two consecutive points will be collinear.
	 * </p>
	 *
	 * @return	The convex hull.
	 */
	public Polyline getConvexHull(){
		return GrahamScan.getConvexHull(this);
	}

	public boolean isClosed(){
		return (coordinates != null && coordinates[0].equals(coordinates[coordinates.length - 1]));
	}

	public Polyline reverse(){
		final Coordinate[] reversedCoordinates = Arrays.copyOf(coordinates, coordinates.length);
		reverse(reversedCoordinates);
		return of(reversedCoordinates);
	}

	/**
	 * Reverses the order of the given array.
	 *
	 * @param array	The array to reverse in place.
	 */
	private static void reverse(final Coordinate[] array){
		int i = 0;
		int j = array.length - 1;
		Coordinate tmp;
		while(j > i){
			tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;

			j --;
			i ++;
		}
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
			sj.add(coordinate.getX() + SPACE + coordinate.getY());
		return sj.toString();
	}

	@Override
	public int compareTo(final Polyline other){
		return Arrays.compare(coordinates, other.coordinates);
	}

}
