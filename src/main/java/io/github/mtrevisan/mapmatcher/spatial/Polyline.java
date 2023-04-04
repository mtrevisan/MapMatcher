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

	private enum CutType{
		HARD, SOFT
	}


	private final Point[] points;


	public static Polyline of(final GeometryFactory factory, final Point... points){
		return new Polyline(factory, points);
	}

	private Polyline(final GeometryFactory factory, final Point... points){
		super(factory);

		if(points != null)
			this.points = removeConsecutiveDuplicates(points);
		else
			this.points = new Point[0];
	}

	private static Point[] removeConsecutiveDuplicates(final Point[] input){
		int distinctIndex = 0;
		int removedCount = 0;
		for(int i = 1; i < input.length; i ++){
			if(input[i].equals(input[distinctIndex]))
				removedCount ++;
			else{
				distinctIndex = i;
				if(removedCount > 0)
					input[i - removedCount] = input[i];
			}
		}
		return (removedCount > 0? Arrays.copyOfRange(input, 0, input.length - removedCount): input);
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

	public Polyline prepend(final Polyline polyline){
		return prepend(polyline.points);
	}

	public Polyline prepend(final Point... points){
		final int oldSize = points.length;
		final int skip = (this.points[0].equals(points[oldSize - 1])? 1: 0);
		final Point[] newPoints = Arrays.copyOf(points, oldSize + this.points.length - skip);
		System.arraycopy(this.points, skip, newPoints, oldSize, this.points.length - skip);
		return of(factory, newPoints);
	}

	public Polyline append(final Polyline polyline){
		return append(polyline.points);
	}

	public Polyline append(final Point... points){
		final int oldSize = this.points.length;
		final int skip = (this.points[this.points.length - 1].equals(points[0])? 1: 0);
		final Point[] newPoints = Arrays.copyOf(this.points, oldSize + points.length - skip);
		System.arraycopy(points, skip, newPoints, oldSize, points.length - skip);
		return of(factory, newPoints);
	}

	public Polyline reverse(){
		final Point[] reversedPoints = Arrays.copyOf(points, points.length);
		ArrayHelper.reverse(reversedPoints);
		return of(factory, reversedPoints);
	}

	/**
	 * Cut the polyline on the closest node on polyline.
	 *
	 * @param point	The cut point.
	 * @return	An array of polyline {@link Point}s, the first before the cut, the second after the cut. The polylines have the cut point
	 * 	in common.
	 */
	public Point[][] cutOnNode(final Point point){
		return cut(point, CutType.SOFT);
	}

	/**
	 * Cut the polyline exactly on the closest point on polyline, possibly creating a new node.
	 *
	 * @param point	The cut point.
	 * @return	An array of polyline {@link Point}s, the first before the cut, the second after the cut. The polylines have the cut point
	 * 	in common.
	 */
	public Point[][] cutHard(final Point point){
		return cut(point, CutType.HARD);
	}

	//FIXME ugliness...
	private Point[][] cut(final Point point, final CutType cutType){
		if(points.length < 2)
			return new Point[][]{points, new Point[0]};

		final int hardCutSize = (cutType == CutType.HARD? 1: 0);
		final Point onTrackClosestPoint = (hardCutSize == 1
			? onTrackClosestPoint(point)
			: onTrackClosestNode(point));
		final double atdToPoint = alongTrackDistance(onTrackClosestPoint);
		boolean cutPointOnNode = (atdToPoint == 0.);
		double cumulativeDistance = 0.;
		int cutPointBeforeOrOnIndex = 0;
		for(int i = 1; !cutPointOnNode && i < points.length; i ++){
			final double onTrackDistance = points[i - 1].distance(points[i]);
			cumulativeDistance += onTrackDistance;
			if(cumulativeDistance > atdToPoint)
				break;

			cutPointBeforeOrOnIndex = i;

			if(cumulativeDistance == atdToPoint)
				cutPointOnNode = true;
		}


		final int beforeSize = cutPointBeforeOrOnIndex + 1;
		final int afterSize = points.length - cutPointBeforeOrOnIndex - (cutPointOnNode? 0: 1);
		final Point[] before = new Point[beforeSize + hardCutSize];
		final Point[] after = new Point[afterSize + hardCutSize];
		System.arraycopy(points, 0, before, 0, beforeSize);
		System.arraycopy(points, cutPointBeforeOrOnIndex + (cutPointOnNode? 0: 1), after, hardCutSize, afterSize);
		if(hardCutSize == 1){
			before[beforeSize] = onTrackClosestPoint;
			after[0] = onTrackClosestPoint;
		}
		return new Point[][]{
			before,
			after
		};
	}


	//https://caseymuratori.com/blog_0003
	//https://dyn4j.org/2010/04/gjk-gilbert-johnson-keerthi/
	//https://github.com/dyn4j/dyn4j/blob/master/src/main/java/org/dyn4j/collision/narrowphase/Gjk.java
	//https://www.researchgate.net/publication/224108603_A_Fast_Geometric_Algorithm_for_Finding_the_Minimum_Distance_Between_Two_Convex_Hulls
	public Point onTrackClosestPoint(final Point point){
		double minClosestPointDistance = Double.MAX_VALUE;
		Point minClosestPoint = points[0];
		final TopologyCalculator topologyCalculator = point.factory.topologyCalculator;
		for(int i = 1; i < points.length; i ++){
			final Point closestPoint = topologyCalculator.onTrackClosestPoint(points[i - 1], points[i], point);
			final double xtd = point.distance(closestPoint);
			if(xtd <= minClosestPointDistance){
				minClosestPointDistance = xtd;
				minClosestPoint = closestPoint;
			}
		}
		return minClosestPoint;
	}

	public Point onTrackClosestNode(final Point point){
		return points[onTrackClosestNodeIndex(point)];
	}

	public int onTrackClosestNodeIndex(final Point point){
		double minClosestPointDistance = Double.MAX_VALUE;
		int minClosestPointIndex = 0;
		final TopologyCalculator topologyCalculator = point.factory.topologyCalculator;
		for(int i = 1; i < points.length; i ++){
			final Point closestPoint = topologyCalculator.onTrackClosestPoint(points[i - 1], points[i], point);
			final double xtd = point.distance(closestPoint);
			if(xtd <= minClosestPointDistance){
				minClosestPointDistance = xtd;
				minClosestPointIndex = i;
			}
		}
		final double distancePointToCurrent = point.distance(points[minClosestPointIndex]);
		if(minClosestPointIndex < points.length - 2 && point.distance(points[minClosestPointIndex + 1]) < distancePointToCurrent)
			minClosestPointIndex = minClosestPointIndex + 1;
		if(minClosestPointIndex > 0 && point.distance(points[minClosestPointIndex - 1]) < distancePointToCurrent)
			minClosestPointIndex = minClosestPointIndex - 1;
		return minClosestPointIndex;
	}

	public double alongTrackDistance(final Point point){
		double cumulativeDistance = Double.NaN;
		if(points.length > 0){
			double minClosestPointDistance = Double.MAX_VALUE;
			//on or before
			int minClosestPointIndex = 0;
			Point minClosestPoint = points[0];
			final TopologyCalculator topologyCalculator = point.factory.topologyCalculator;
			for(int i = 1; i < points.length; i ++){
				final Point closestPoint = topologyCalculator.onTrackClosestPoint(points[i - 1], points[i], point);
				final double xtd = point.distance(closestPoint);
				if(xtd <= minClosestPointDistance){
					minClosestPointDistance = xtd;
					minClosestPointIndex = i - 1;
					minClosestPoint = closestPoint;
				}
			}

			cumulativeDistance = 0.;
			for(int i = 1; i <= minClosestPointIndex; i ++)
				cumulativeDistance += points[i - 1].distance(points[i]);
			if(minClosestPointIndex >= 0)
				cumulativeDistance += points[minClosestPointIndex].distance(minClosestPoint);
		}
		return cumulativeDistance;
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
			sj.add(point != null? point.getX() + SPACE + point.getY(): "<null>");
		return sj.toString();
	}

	public String toSimpleString(){
		final StringJoiner sj = new StringJoiner(", ");
		for(final Point point : points)
			sj.add(point != null? point.getX() + SPACE + point.getY(): "<null>");
		return sj.toString();
	}

	@Override
	public int compareTo(final Polyline other){
		return Arrays.compare(points, other.points);
	}

}
