/**
 * Copyright (c) 2022 Mauro Trevisan
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
package io.github.mtrevisan.mapmatcher.spatial.topologies;

import io.github.mtrevisan.mapmatcher.helpers.MathHelper;
import io.github.mtrevisan.mapmatcher.spatial.GeodeticHelper;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.intersection.BentleyOttmann;

import java.util.ArrayList;
import java.util.List;


/**
 * @see <a href="https://github.com/grumlimited/geocalc/blob/master/src/main/java/com/grum/geocalc/EarthCalc.java">EarthCalc</a>
 */
public class GeoidalCalculator implements TopologyCalculator{

	private static final double EPSILON = 1.e-9;


	/**
	 * Calculate orthodromic distance, (azimuth) bearing and final bearing between two points using inverse Vincenty formula.
	 *
	 * @param startPoint	The start point.
	 * @param endPoint	The end point.
	 * @return	The distance.
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Vincenty%27s_formulae">Vincenty's formulae</a>
	 */
	@Override
	public double distance(final Point startPoint, final Point endPoint){
		return GeodeticHelper.orthodromicDistance(startPoint, endPoint);
	}

	/**
	 * Calculate cross-track distance.
	 *
	 * @param point	The point.
	 * @param lineString The list of track points.
	 * @return The distance [m].
	 */
	@Override
	public double distance(final Point point, final Polyline lineString){
		double minNearestPointDistance = Double.MAX_VALUE;
		final Point[] points = lineString.getPoints();
		for(int i = 1; i < points.length; i ++){
			final Point startPoint = points[i - 1];
			final Point endPoint = points[i];
			final Point nearestPoint = GeodeticHelper.onTrackClosestPoint(startPoint, endPoint, point);
			final double distance = Math.abs(GeodeticHelper.orthodromicDistance(nearestPoint, point));
			if(distance < minNearestPointDistance)
				minNearestPointDistance = distance;
		}
		return minNearestPointDistance;
	}

	@Override
	public double initialBearing(final Point startPoint, final Point endPoint){
		return GeodeticHelper.initialBearing(startPoint, endPoint);
	}

	@Override
	public Point destination(final Point startPoint, final double initialBearing, final double distance){
		return GeodeticHelper.destination(startPoint, initialBearing, distance);
	}


	@Override
	public Point onTrackClosestPoint(final Point startPoint, final Point endPoint, final Point point){
		return GeodeticHelper.onTrackClosestPoint(startPoint, endPoint, point);
	}

	@Override
	public double alongTrackDistance(final Point startPoint, final Point endPoint, final Point point){
		final Point onTrackClosestPoint = onTrackClosestPoint(startPoint, endPoint, point);
		return GeodeticHelper.orthodromicDistance(startPoint, onTrackClosestPoint);
	}


	@Override
	public Point leftmostPoint(final Polyline polyline){
		Point leftmostPoint = null;
		for(final Point point : polyline.getPoints())
			if(leftmostPoint == null || compare(leftmostPoint, point) == 1)
				leftmostPoint = point;
		return leftmostPoint;
	}

	@Override
	public Point rightmostPoint(final Polyline polyline){
		Point leftmostPoint = null;
		for(final Point point : polyline.getPoints())
			if(leftmostPoint == null || compare(leftmostPoint, point) == 1)
				leftmostPoint = point;
		return leftmostPoint;
	}

	@Override
	public double calculateYIndex(final Point pointLeft, final Point pointRight, final double x){
		final double x1 = pointLeft.getX();
		final double y1 = pointLeft.getY();
		final double x2 = pointRight.getX();
		final double y2 = pointRight.getY();
		//equation of line passing through two points
		return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
	}

	@Override
	public int compare(final Point point1, final Point point2){
		final double p1x = point1.getX();
		final double p1y = point1.getY();
		final double p2x = point2.getX();
		final double p2y = point2.getY();
		if(p1x > p2x || MathHelper.nearlyEqual(p1x, p2x, EPSILON) && p1y > p2y)
			return 1;
		if(p1x < p2x || MathHelper.nearlyEqual(p1x, p2x, EPSILON) && p1y < p2y)
			return -1;
		return 0;
	}

	@Override
	public List<Point> intersection(final Polyline polyline1, final Polyline polyline2){
		final GeometryFactory factory = polyline1.getFactory();
		final TopologyCalculator topologyCalculator = polyline1.getDistanceCalculator();
		final BentleyOttmann bentleyOttmann = new BentleyOttmann(topologyCalculator);
		final Point[] polyline1Points = polyline1.getPoints();
		for(int i = 1; i < polyline1Points.length; i ++){
			final Point startPoint = polyline1Points[i - 1];
			final Point endPoint = polyline1Points[i];
			bentleyOttmann.addPolyline(factory.createPolyline(startPoint, endPoint));
		}

		final List<Point> intersections = new ArrayList<>();
		bentleyOttmann.findIntersections((p1, p2, intersection) -> intersections.add(intersection));
		return intersections;
	}

}
