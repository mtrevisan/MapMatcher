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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;


public class GrahamScan{

	private GrahamScan(){}


	/**
	 * Returns a (closed) {@link Polyline} that represents the convex hull of this polyline.
	 * <p>
	 * The returned geometry contains the minimal number of points needed to represent the convex hull.<br/>
	 * In particular, no more than two consecutive points will be collinear.
	 * </p>
	 *
	 * @return	The convex hull.
	 */
	public static Polyline getConvexHull(final Polyline polyline){
		final Point[] points = polyline.getPoints();
		if(points.length <= 2)
			return polyline.factory.createPolyline(points);

		final Point[] sortedPoints = Arrays.copyOf(points, points.length);
		polarSort(sortedPoints);

		//use Graham scan to find convex hull
		final Stack<Point> convexHull = grahamScan(sortedPoints);

		//convert stack to an array
		return polyline.factory.createPolyline(convexHull.toArray(Point[]::new));
	}

	static void polarSort(final Point[] points){
		//find the lowest point in the set; if two or more points have the same minimum Y coordinate choose the one with the minimum X
		//(this focal point is put in array location points[0])
		final int pivotIndex = getLowestPoint(points);
		final Point pivot = points[pivotIndex];
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
	static int getLowestPoint(final Point[] points){
		int lowestIndex = 0;
		for(int i = 1; i < points.length; i ++){
			final Point tmp = points[i];
			final Point lowest = points[lowestIndex];
			if(tmp.getY() < lowest.getY() || tmp.getY() == lowest.getY() && tmp.getX() < lowest.getX())
				lowestIndex = i;
		}
		return lowestIndex;
	}

	/**
	 * Uses the Graham Scan algorithm to compute the convex hull vertices.
	 *
	 * @param points	A list of points, with at least 3 entries.
	 * @return	A stack containing the ordered points of the convex hull ring.
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Graham_scan">Graham scan</a>
	 */
	private static Stack<Point> grahamScan(final Point[] points){
		final Stack<Point> hull = new Stack<>();
		hull.push(points[0]);
		hull.push(points[1]);
		for(int i = 2; hull.size() > 1 && i < points.length; i ++){
			final Point head = points[i];
			final Point middle = hull.pop();
			final Point tail = hull.peek();

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
			hull.push(points[0]);

		return hull;
	}


	static class PolarAngleComparator implements Comparator<Point>{

		/** A value that indicates an orientation of clockwise, or a right turn. */
		static final int CLOCKWISE = -1;
		/** A value that indicates an orientation of counterclockwise, or a left turn. */
		static final int COUNTER_CLOCKWISE = 1;
		/** A value that indicates an orientation of collinear, or no turn (straight). */
		static final int COLLINEAR = 0;
		/** An enum denoting a directional-turn between 3 points (vectors) */

		/** A value which is safely greater than the relative round-off error in double-precision numbers. */
		private static final double DOUBLE_SAFE_EPSILON = 1.e-15;


		private final Point reference;


		/**
		 * Creates a new comparator using a given reference.
		 * <p>
		 * The reference must be lower in Y and then X to all compared points.
		 * </p>
		 *
		 * @param reference	The reference of the polar comparison.
		 */
		PolarAngleComparator(final Point reference){
			this.reference = reference;
		}

		@Override
		public int compare(final Point p1, final Point p2){
			return -polarCompare(reference, p1, p2);
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
		 * @return	<code>-1</code>, <code>0</code>, or <code>1</code> depending on whether `p` is less than, equal to, or greater than `q`
		 * 	with respect to `r` respectively.
		 */
		static int polarCompare(final Point r, final Point p, final Point q){
			int orientation = orientation(r, p, q);
			if(orientation != COLLINEAR)
				return orientation;

			//the points are collinear, so compare based on distance from the reference
			orientation = Double.compare(Math.abs(q.getY() - r.getY()), Math.abs(p.getY() - r.getY()));
			if(orientation != COLLINEAR)
				return orientation;

			//the points lie in a horizontal line, which should also contain the reference (since they are collinear)
			return Double.compare(Math.abs(q.getX() - r.getX()), Math.abs(p.getX() - r.getX()));
		}

		static int orientation(final Point r, final Point p, final Point q){
			//fast filter for orientation index (avoids use of slow extended-precision arithmetic in many cases)
			final int index = orientationIndexFilter(r, p, q);
			if(index <= 1)
				return index;

			//normalize coordinates
			final double dx1 = q.getX() - p.getX();
			final double dy1 = q.getY() - p.getY();
			final double dx2 = r.getX() - q.getX();
			final double dy2 = r.getY() - q.getY();

			//sign of determinant
			return (int)Math.signum(dx1 * dy2 - dy1 * dx2);
		}

		/**
		 * A filter for computing the orientation index of three points.
		 * <p>
		 * If the orientation can be computed safely using standard DP arithmetic, this routine returns the orientation index.<br/>
		 * Otherwise, a value i > 1 is returned.<br/>
		 * In this case the orientation index must be computed using some other more robust method.<br/>
		 * The filter is fast to compute, so can be used to avoid the use of slower robust methods except when they are really needed,
		 * thus providing better average performance.
		 * </p>
		 * <p>
		 * Uses an approach due to Jonathan Shewchuk.
		 * </p>
		 *
		 * @param r	The reference point.
		 * @param p	The first point.
		 * @param q	The second point.
		 * @return The orientation index if it can be computed safely (<code>i > 1</code> if the orientation index cannot be computed
		 * 	safely)
		 */
		private static int orientationIndexFilter(final Point r, final Point p, final Point q){
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
