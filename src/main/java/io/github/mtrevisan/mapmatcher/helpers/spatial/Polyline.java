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
import java.util.Comparator;
import java.util.Stack;
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
	 * Returns a {@link Polyline} that represents the convex hull of this polyline.
	 * <p>
	 * The returned geometry contains the minimal number of points needed to represent the convex hull.<br/>
	 * In particular, no more than two consecutive points will be collinear.
	 * </p>
	 *
	 * @return	The convex hull.
	 */
	public Polyline getConvexHull(){
		if(coordinates.length <= 2)
			return of(coordinates);

		final Coordinate[] points = Arrays.copyOf(coordinates, coordinates.length);
		polarSort(points);

		// use heuristic to reduce points, if large
		//https://github.com/locationtech/jts/blob/master/modules/core/src/main/java/org/locationtech/jts/algorithm/ConvexHull.java#L203
//		if(points.length > 50)
//			points = reduce(points);

		//use Graham scan to find convex hull
		final Stack<Coordinate> convexHull = grahamScan(points);

		//convert stack to an array
		return of(convexHull.toArray(Coordinate[]::new));
	}

	private void polarSort(final Coordinate[] points){
		//find the lowest point in the set; if two or more points have the same minimum Y coordinate choose the one with the minimum X
		//(this focal point is put in array location points[0])
		final int pivotIndex = getLowestPoint(points);
		final Coordinate pivot = points[pivotIndex];
		points[pivotIndex] = points[0];
		points[0] = pivot;

		//sort points by polar angle with pivot (if several points have the same polar angle then only keep the farthest)
		Arrays.sort(points, 1, points.length, new PolarAngleComparator(pivot));
	}

	/**
	 * Returns the point with the lowest Y coordinate.
	 * <p>
	 * In case more than one such point exists, the one with the lowest X coordinate is returned.
	 * </p>
	 *
	 * @param points	The list of points to return the lowest point from.
	 * @return	The index of the point with the lowest Y (or X) coordinate.
	 */
	private int getLowestPoint(final Coordinate[] points){
		int lowestIndex = 0;
		for(int i = 1; i < points.length; i ++){
			final Coordinate tmp = points[i];
			final Coordinate lowest = points[lowestIndex];
			if(tmp.getY() < lowest.getY() || tmp.getY() == lowest.getY() && tmp.getX() < lowest.getX())
				lowestIndex = i;
		}
		return lowestIndex;
	}

	/**
	 * Uses the Graham Scan algorithm to compute the convex hull vertices.
	 *
	 * @param coordinates	A list of points, with at least 3 entries.
	 * @return	A stack containing the ordered points of the convex hull ring.
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Graham_scan">Graham scan</a>
	 */
	private Stack<Coordinate> grahamScan(final Coordinate[] coordinates){
		final Stack<Coordinate> hull = new Stack<>();
		hull.push(coordinates[0]);
		hull.push(coordinates[1]);
		for(int i = 2; hull.size() > 1 && i < coordinates.length; i ++){
			final Coordinate head = coordinates[i];
			final Coordinate middle = hull.pop();
			final Coordinate tail = hull.peek();

			final int turnType = PolarAngleComparator.orientation(head, tail, middle);
			switch(turnType){
				case PolarAngleComparator.COUNTER_CLOCKWISE -> {
					hull.push(middle);
					hull.push(head);
				}
				case PolarAngleComparator.CLOCKWISE -> i --;
				case PolarAngleComparator.COLLINEAR -> hull.push(head);
			}
		}

		//close the hull
		if(hull.size() > 2)
			hull.push(coordinates[0]);

		return hull;
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


	static class PolarAngleComparator implements Comparator<Coordinate>{

		/** A value that indicates an orientation of clockwise, or a right turn. */
		static final int CLOCKWISE = -1;
		/** A value that indicates an orientation of counterclockwise, or a left turn. */
		static final int COUNTER_CLOCKWISE = 1;
		/** A value that indicates an orientation of collinear, or no turn (straight). */
		static final int COLLINEAR = 0;
		/** An enum denoting a directional-turn between 3 points (vectors) */

		/** A value which is safely greater than the relative round-off error in double-precision numbers. */
		private static final double DOUBLE_SAFE_EPSILON = 1.e-15;


		private final Coordinate reference;


		/**
		 * Creates a new comparator using a given reference.
		 * <p>
		 * The reference must be lower in Y and then X to all compared points.
		 * </p>
		 *
		 * @param reference	The reference of the polar comparison.
		 */
		PolarAngleComparator(final Coordinate reference){
			this.reference = reference;
		}

		@Override
		public int compare(final Coordinate p1, final Coordinate p2){
			return polarCompare(reference, p1, p2);
		}

		/**
		 * Given two points `p` and `q` compare them with respect to their polar ordering about point `r`.
		 * <p>
		 * First checks polar ordering using a CCW orientation (in the "right–handed" coordinate system, if the result is <code>0</code>,
		 * the points are collinear; if it is positive, the three points constitute a positive angle of rotation around <code>r</code> from
		 * <code>p</code> to <code>q</code>, otherwise a negative angle).<br/>
		 * </p>
		 * <p>
		 * If the points are collinear, the comparison is based on their distance to the reference.<br/>
		 * <code>p < q</code> (that is <code>r–p–q</code> is CCW) <code>iff ang(r–p) < ang(r–q)</code>, or
		 * <code>ang(r–p) == ang(r–q) && dist(r, p) < dist(r, q)</code>.
		 * </p>
		 *
		 * @param r	The reference point.
		 * @param p	A point.
		 * @param q	Another point.
		 * @return	<code>-1</code>, <code>0</code> or <code>1</code> depending on whether `p` is less than, equal to or greater than `q`
		 * 	with respect to `r`.
		 */
		static int polarCompare(final Coordinate r, final Coordinate p, final Coordinate q){
			final int orientation = orientation(r, p, q);
			if(orientation != COLLINEAR)
				return orientation;

			//the points are collinear, so compare based on distance from the reference
			if(p.getY() > q.getY())
				return COUNTER_CLOCKWISE;
			if(p.getY() < q.getY())
				return CLOCKWISE;

			//the points lie in a horizontal line, which should also contain the reference (since they are collinear)
			return Double.compare(p.getX(), q.getX());
		}

		static int orientation(final Coordinate r, final Coordinate p, final Coordinate q){
			//fast filter for orientation index (avoids use of slow extended-precision arithmetic in many cases)
			final double rx = r.getX();
			final double ry = r.getY();
			final double px = p.getX();
			final double py = p.getY();
			final double qx = q.getX();
			final double qy = q.getY();
			final int index = orientationIndexFilter(r, p, q);
			if(index <= 1)
				return index;

			//normalize coordinates
			final double dx1 = qx - px;
			final double dy1 = qy - py;
			final double dx2 = rx - qx;
			final double dy2 = ry - qy;

			//sign of determinant
			return (int)Math.signum(dx1 * dy2 - dy1 * dx2);
		}

		/**
		 * A filter for computing the orientation index of three coordinates.
		 * <p>
		 * If the orientation can be computed safely using standard DP arithmetic, this routine returns the orientation index.<br/>
		 * Otherwise, a value i > 1 is returned.<br/>
		 * In this case the orientation index must be computed using some other more robust method.<br/>
		 * The filter is fast to compute, so can be used to avoid the use of slower robust methods except when they are really needed,
		 * thus providing better average performance.
		 * </p>
		 * <p>
		 * Uses an approach due to Jonathan Shewchuk, which is in the public domain.
		 * </p>
		 *
		 * @param r	C coordinate.
		 * @param p	A coordinate.
		 * @param q	B coordinate.
		 * @return The orientation index if it can be computed safely (<code>i > 1</code> if the orientation index cannot be computed
		 * 	safely)
		 */
		private static int orientationIndexFilter(final Coordinate r, final Coordinate p, final Coordinate q){
			final double detLeft = (p.getX() - r.getX()) * (q.getY() - r.getY());
			final double detRight = (p.getY() - r.getY()) * (q.getX() - r.getX());
			final double det = detLeft - detRight;

			if(detLeft == 0.)
				return (int)Math.signum(det);

			double detSum = detLeft + detRight;
			if(detLeft < 0.){
				if(detRight >= 0.)
					return (int)Math.signum(det);
				else
					detSum = -detSum;
			}
			else if(detRight <= 0.)
				return (int)Math.signum(det);

			final double errorBound = DOUBLE_SAFE_EPSILON * detSum;
			if(det <= -errorBound || det >= errorBound)
				return (int)Math.signum(det);
			return 2;
		}
	}

}
