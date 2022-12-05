package io.github.mtrevisan.mapmatcher.helpers.spatial.other;

import io.github.mtrevisan.mapmatcher.helpers.spatial.Coordinate;
import io.github.mtrevisan.mapmatcher.helpers.spatial.Polyline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;


public final class GrahamScan{

	/**
	 * An enum denoting a directional-turn between 3 Coordinates (vectors).
	 */
	protected enum Turn { CLOCKWISE, COUNTER_CLOCKWISE, COLLINEAR }


	/**
	 * Returns true iff all Coordinates in <code>Coordinates</code> are collinear.
	 *
	 * @param Coordinates the list of Coordinates.
	 * @return       true iff all Coordinates in <code>Coordinates</code> are collinear.
	 */
	protected static boolean areAllCollinear(List<Coordinate> Coordinates) {
		if(Coordinates.size() < 2)
			return true;

		final Coordinate a = Coordinates.get(0);
		final Coordinate b = Coordinates.get(1);

		for(int i = 2; i < Coordinates.size(); i++) {
			Coordinate c = Coordinates.get(i);
			if(getTurn(a, b, c) != Turn.COLLINEAR)
				return false;
		}

		return true;
	}

	/**
	 * Returns the convex hull of the Coordinates created from the list
	 * <code>Coordinates</code>. Note that the first and last Coordinate in the
	 * returned <code>List&lt;java.awt.Coordinate&gt;</code> are the same
	 * Coordinate.
	 *
	 * @param polyline the list of Coordinates.
	 * @return       the convex hull of the Coordinates created from the list
	 *               <code>Coordinates</code>.
	 * @throws IllegalArgumentException if all Coordinates are collinear or if there are less than 3 unique Coordinates present.
	 */
	public static Polyline getConvexHull(Polyline polyline) throws IllegalArgumentException{
		List<Coordinate> sorted = new ArrayList<>(getSortedCoordinateSet(polyline));

		if(sorted.size() <= 2)
			return Polyline.of(sorted.toArray(Coordinate[]::new));
//		if(areAllCollinear(sorted))
//			throw new IllegalArgumentException("cannot create a convex hull from collinear Coordinates");

		Stack<Coordinate> stack = new Stack<>();
		stack.push(sorted.get(0));
		stack.push(sorted.get(1));

		for(int i = 2; i < sorted.size(); i ++){
			Coordinate head = sorted.get(i);
			Coordinate middle = stack.pop();
			Coordinate tail = stack.peek();

			Turn turn = getTurn(tail, middle, head);

			switch(turn){
				case COUNTER_CLOCKWISE -> {
					stack.push(middle);
					stack.push(head);
				}
				case CLOCKWISE -> i --;
				case COLLINEAR -> stack.push(head);
			}
		}

		//close the hull
		if(stack.size() > 2)
			stack.push(sorted.get(0));

		return Polyline.of(stack.toArray(Coordinate[]::new));
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
	private static int getLowestPoint(final Coordinate[] points){
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
	 * Returns a sorted set of Coordinates from the list <code>Coordinates</code>. The
	 * set of Coordinates are sorted in increasing order of the angle they and the
	 * lowest Coordinate <tt>P</tt> make with the x-axis. If tow (or more) Coordinates
	 * form the same angle towards <tt>P</tt>, the one closest to <tt>P</tt>
	 * comes first.
	 *
	 * @param polyline the list of Coordinates to sort.
	 * @return       a sorted set of Coordinates from the list <code>Coordinates</code>.
	 * @see GrahamScan#getLowestPoint(Coordinate[])
	 */
	protected static Set<Coordinate> getSortedCoordinateSet(Polyline polyline) {
		final Coordinate[] coordinates = polyline.getCoordinates();
		final Coordinate lowest = coordinates[getLowestPoint(coordinates)];

		TreeSet<Coordinate> set = new TreeSet<>(new Comparator<Coordinate>(){
			@Override
			public int compare(Coordinate a, Coordinate b){
				if(a == b || a.equals(b))
					return 0;

				final double thetaA = StrictMath.atan2(a.getY() - lowest.getY(), a.getX() - lowest.getX());
				final double thetaB = StrictMath.atan2(b.getY() - lowest.getY(), b.getX() - lowest.getX());

				if(thetaA < thetaB)
					return -1;
				else if(thetaA > thetaB)
					return 1;
				else{
					//collinear with the 'lowest' coordinate, let the Coordinate closest to it come first
					final double distanceA = ((lowest.getX() - a.getX()) * (lowest.getX() - a.getX())) + ((lowest.getY() - a.getY()) * (lowest.getY() - a.getY()));
					final double distanceB = ((lowest.getX() - b.getX()) * (lowest.getX() - b.getX())) + ((lowest.getY() - b.getY()) * (lowest.getY() - b.getY()));
					return (distanceA < distanceB? -1: 1);
				}
			}
		});

		set.addAll(Arrays.asList(coordinates));

		return set;
	}

	/**
	 * Returns the GrahamScan#Turn formed by traversing through the
	 * ordered Coordinates <code>a</code>, <code>b</code> and <code>c</code>.
	 * More specifically, the cross product <tt>C</tt> between the
	 * 3 Coordinates (vectors) is calculated:
	 *
	 * <tt>(b.getX()-a.getX() * c.getY()-a.getY()) - (b.getY()-a.getY() * c.getX()-a.getX())</tt>
	 *
	 * and if <tt>C</tt> is less than 0, the turn is CLOCKWISE, if
	 * <tt>C</tt> is more than 0, the turn is COUNTER_CLOCKWISE, else
	 * the three Coordinates are COLLINEAR.
	 *
	 * @param a the starting Coordinate.
	 * @param b the second Coordinate.
	 * @param c the end Coordinate.
	 * @return the GrahamScan#Turn formed by traversing through the
	 *         ordered Coordinates <code>a</code>, <code>b</code> and
	 *         <code>c</code>.
	 */
	protected static Turn getTurn(Coordinate a, Coordinate b, Coordinate c) {
		double crossProduct = ((b.getX() - a.getX()) * (c.getY() - a.getY())) - ((b.getY() - a.getY()) * (c.getX() - a.getX()));

		if(crossProduct > 0.) {
			return Turn.COUNTER_CLOCKWISE;
		}
		else if(crossProduct < 0.) {
			return Turn.CLOCKWISE;
		}
		else {
			return Turn.COLLINEAR;
		}
	}

}
