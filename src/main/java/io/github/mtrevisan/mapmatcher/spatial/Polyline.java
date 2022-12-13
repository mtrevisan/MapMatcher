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

import io.github.mtrevisan.mapmatcher.spatial.topologies.TopologyCalculator;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.StringJoiner;


public class Polyline extends Geometry implements Comparable<Polyline>, Serializable{

	@Serial
	private static final long serialVersionUID = -2848807287557631823L;

	private static final String SPACE = " ";


	private final Point[] points;


	public static Polyline of(final GeometryFactory factory, final Point... points){
		return new Polyline(factory, points);
	}

	private Polyline(final GeometryFactory factory, final Point... points){
		super(factory);

		if(points != null)
			this.points = points;
		else
			this.points = new Point[0];
	}

	public boolean isEmpty() {
		return (points.length == 0);
	}

	public Point getStartPoint(){
		return (isEmpty()? null: points[0]);
	}

	public Point getEndPoint(){
		return (isEmpty()? null: points[points.length - 1]);
	}

	public Point[] getPoints(){
		return points;
	}

	public int indexOf(final Point point){
		for(int i = 0; i < points.length; i ++)
			if(points[i].equals(point))
				return i;
		return -1;
	}

	public int size(){
		return points.length;
	}

	public Envelope getBoundingBox(){
		double minLatitude = Double.POSITIVE_INFINITY;
		double maxLatitude = Double.NEGATIVE_INFINITY;
		double minLongitude = Double.POSITIVE_INFINITY;
		double maxLongitude = Double.NEGATIVE_INFINITY;
		if(points.length > 0){
			minLatitude = points[0].getY();
			maxLatitude = points[0].getY();
			minLongitude = points[0].getX();
			maxLongitude = points[0].getX();
		}
		for(int i = 1; i < points.length; i ++){
			final Point point = points[i];
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

	public boolean isClosed(){
		return (points != null && points[0].equals(points[points.length - 1]));
	}

	public Polyline reverse(){
		final Point[] reversedPoints = Arrays.copyOf(points, points.length);
		reverse(reversedPoints);
		return of(factory, reversedPoints);
	}

	/**
	 * Reverses the order of the given array.
	 *
	 * @param array	The array to reverse in place.
	 */
	private static void reverse(final Point[] array){
		int i = 0;
		int j = array.length - 1;
		Point tmp;
		while(j > i){
			tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;

			j --;
			i ++;
		}
	}


	public Point onTrackClosestPoint(final Point point){
		double xtd = -1.;
		double minClosestPointDistance = Double.MAX_VALUE;
		Point minClosestPoint = null;
		final TopologyCalculator topologyCalculator = point.factory.topologyCalculator;
		for(int i = 1; xtd != 0. && i < points.length; i ++){
			final Point startPoint = points[i - 1];
			final Point endPoint = points[i];
			final Point closestPoint = topologyCalculator.onTrackClosestPoint(startPoint, endPoint, point);
			xtd = point.distance(closestPoint);
			if(xtd < minClosestPointDistance){
				minClosestPointDistance = xtd;
				minClosestPoint = closestPoint;
			}
		}
		return minClosestPoint;
	}

	public double alongTrackDistance(final Point point){
		double atd = 0.;
		double xtd = -1.;
		double minClosestPointDistance = Double.MAX_VALUE;
		double cumulativeDistance = 0.;
		final TopologyCalculator topologyCalculator = point.factory.topologyCalculator;
		for(int i = 1; xtd != 0. && i < points.length; i ++){
			final Point startPoint = points[i - 1];
			final Point endPoint = points[i];
			final Point closestPoint = topologyCalculator.onTrackClosestPoint(startPoint, endPoint, point);

			final double onTrackDistance = startPoint.distance(closestPoint);
			cumulativeDistance += onTrackDistance;

			xtd = point.distance(closestPoint);
			if(xtd < minClosestPointDistance){
				minClosestPointDistance = xtd;

				atd += cumulativeDistance;
				cumulativeDistance = 0.;
			}
		}
		return (xtd >= 0.? atd: -1.);
	}


	@Override
	public boolean equals(final Object obj){
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;

		final Polyline other = (Polyline)obj;
		return Arrays.equals(points, other.points);
	}

	@Override
	public int hashCode(){
		return Arrays.hashCode(points);
	}

	@Override
	public String toString(){
		final StringJoiner sj = new StringJoiner(", ", "LINESTRING (", ")");
		for(final Point point : points)
			sj.add(point.getX() + SPACE + point.getY());
		return sj.toString();
	}

	@Override
	public int compareTo(final Polyline other){
		return Arrays.compare(points, other.points);
	}

}
