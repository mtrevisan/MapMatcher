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
		final Coordinate[] points = Arrays.stream(coordinates)
			.distinct()
			.toArray(Coordinate[]::new);

		// use heuristic to reduce points, if large
		//https://github.com/locationtech/jts/blob/master/modules/core/src/main/java/org/locationtech/jts/algorithm/ConvexHull.java#L203
//		if(points.length > 50)
//			points = reduce(points);

		if(points.length <= 2)
			return of(points);

		//sort points for Graham scan
		final Coordinate[] sortedCoordinates = preSort(points);

		//use Graham scan to find convex hull
		final Stack<Coordinate> convexHull = grahamScan(sortedCoordinates);

		//convert stack to an array
		return of(convexHull.toArray(Coordinate[]::new));
	}

	private Coordinate[] preSort(final Coordinate[] points){
		//find the lowest point in the set; if two or more points have the same minimum Y coordinate choose the one with the minimum X
		//(this focal point is put in array location points[0])
		final int pivotIndex = getLowestPoint(points);
		final Coordinate pivot = points[pivotIndex];
		points[pivotIndex] = points[0];
		points[0] = pivot;

		//sort the points radially around the focal point
		Arrays.sort(points, 1, points.length, new RadialComparator(pivot));

		return points;
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
			if(tmp.getY() < lowest.getY() || tmp.getY() == lowest.getY() && tmp.getX() < lowest.getX()){
				lowestIndex = i;
			}
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
		//find a point not collinear with the previous two
		int i = 2;
		for( ; i < coordinates.length; i ++)
			if(RadialComparator.orientation(coordinates[0], coordinates[1], coordinates[i]) != 0){
				hull.push(coordinates[i]);
				break;
			}
		for( ; i < coordinates.length; i ++){
			final Coordinate head = coordinates[i];
			Coordinate middle = hull.pop();
			final Coordinate tail = hull.peek();

			//loop while stack is not empty and the three points are clockwise or collinear w.r.t. head
			while(RadialComparator.orientation(tail, middle, head) <= 0)
				hull.pop();

			hull.push(middle);
			hull.push(head);
		}

		//close the hull
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


	private static class RadialComparator implements Comparator<Coordinate>{

		/** A value that indicates an orientation of clockwise, or a right turn. */
		private static final int ORIENTATION_CLOCKWISE = -1;
		/** A value that indicates an orientation of counterclockwise, or a left turn. */
		private static final int ORIENTATION_COUNTER_CLOCKWISE = 1;
		/** A value that indicates an orientation of collinear, or no turn (straight). */
		private static final int ORIENTATION_COLLINEAR = 0;
		/** An enum denoting a directional-turn between 3 points (vectors) */

		/** A value which is safely greater than the relative round-off error in double-precision numbers. */
		private static final double DOUBLE_SAFE_EPSILON = 1.e-15;


		private final Coordinate origin;


		/**
		 * Creates a new comparator using a given origin.
		 * <p>
		 * The origin must be lower in Y and then X to all compared points.
		 * </p>
		 *
		 * @param origin	The origin of the radial comparison.
		 */
		RadialComparator(final Coordinate origin){
			this.origin = origin;
		}

		@Override
		public int compare(final Coordinate p1, final Coordinate p2){
			return polarCompare(origin, p1, p2);
		}

		/**
		 * Given two points `p` and `q` compare them with respect to their radial ordering about point `reference`.
		 * <p>
		 * First checks radial ordering using a CCW orientation.<br/>
		 * If the points are collinear, the comparison is based on their distance to the origin.
		 * </p>
		 * </p>
		 * p < q iff
		 * <ul>
		 * 	<li>ang(o-p) < ang(o-q) (e.g. o-p-q is CCW)
		 * 	<li>or ang(o-p) == ang(o-q) && dist(o,p) < dist(o,q)
		 * </ul>
		 *
		 * @param p	A point.
		 * @param q	Another point.
		 * @param reference	The origin.
		 * @return	<code>-1</code>, <code>0</code> or <code>1</code> depending on whether `p` is less than, equal to or greater than `q`.
		 */
		private static int polarCompare(final Coordinate reference, final Coordinate p, final Coordinate q){
			final int orientation = orientation(p, q, reference);
			if(orientation == ORIENTATION_COUNTER_CLOCKWISE)
				return ORIENTATION_COUNTER_CLOCKWISE;
			if(orientation == ORIENTATION_CLOCKWISE)
				return ORIENTATION_CLOCKWISE;

			//the points are collinear, so compare based on distance from the origin
			//the points `p` and `q` are greater than or equals to the origin, so they lie in the closed half-plane above the origin
			//if they are not in a horizontal line, the Y ordinate can be tested to determine distance
			//this is more robust than computing the distance explicitly
			if(p.getY() > q.getY())
				return ORIENTATION_COUNTER_CLOCKWISE;
			if(p.getY() < q.getY())
				return ORIENTATION_CLOCKWISE;

			//the points lie in a horizontal line, which should also contain the origin (since they are collinear)
			//also, they must be above the origin.
			//use the X ordinate to determine distance
			return Double.compare(p.getX(), q.getX());
		}

		/**
		 * Returns whether if <code>a → b → c</code> is a clockwise/counterclockwise turn, or they are collinear.
		 *
		 * @param a	First point.
		 * @param b	Second point.
		 * @param c	Third point.
		 * @return {-1, 0, +1} if <code>a → b → c</code> is a {clockwise, collinear; counterclockwise} turn.
		 */
		private static int ccw(final Coordinate a, final Coordinate b, final Coordinate c){
			final double area2 = (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX());
			if(area2 < 0)
				return -1;
			else if(area2 > 0)
				return 1;
			else
				return 0;
		}

		static int orientation(final Coordinate p, final Coordinate q, final Coordinate reference){
			//fast filter for orientation index
			//avoids use of slow extended-precision arithmetic in many cases
			final double px = p.getX();
			final double py = p.getY();
			final double qx = q.getX();
			final double qy = q.getY();
			final double rx = reference.getX();
			final double ry = reference.getY();
			final int index = orientationIndexFilter(px, py, qx, qy, rx, ry);
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
		 * @param pax	A coordinate.
		 * @param pay	A coordinate.
		 * @param pbx	B coordinate.
		 * @param pby	B coordinate.
		 * @param pcx	C coordinate.
		 * @param pcy	C coordinate.
		 * @return	The orientation index if it can be computed safely (<code>i > 1</code> if the orientation index cannot be computed
		 * 	safely)
		 */
		private static int orientationIndexFilter(final double pax, final double pay, final double pbx, final double pby,
				final double pcx, final double pcy){
			final double detLeft = (pax - pcx) * (pby - pcy);
			final double detRight = (pay - pcy) * (pbx - pcx);
			final double det = detLeft - detRight;

			double detSum;
			if(detLeft > 0.){
				if(detRight <= 0.)
					return (int)Math.signum(det);
				else
					detSum = detLeft + detRight;
			}
			else if(detLeft < 0.){
				if(detRight >= 0.)
					return (int)Math.signum(det);
				else
					detSum = -detLeft - detRight;
			}
			else
				return (int)Math.signum(det);

			final double errorBound = DOUBLE_SAFE_EPSILON * detSum;
			if(det <= -errorBound || det >= errorBound)
				return (int)Math.signum(det);
			return 2;
		}
	}

}
