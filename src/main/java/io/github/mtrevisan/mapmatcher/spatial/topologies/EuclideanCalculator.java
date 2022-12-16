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
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.Collections;
import java.util.List;


public class EuclideanCalculator implements TopologyCalculator{

	private static final double EPSILON = 1.e-9;


	@Override
	public double distance(final Point startPoint, final Point endPoint){
		final double dx = startPoint.getX() - endPoint.getX();
		final double dy = startPoint.getY() - endPoint.getY();
		return Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	public double distance(final Point point, final Polyline polyline){
		final Point[] points = polyline.getPoints();
		double minNearestPointDistance = distance(point, points[0]);
		for(int i = 1; i < points.length; i ++){
			final Point startPoint = points[i - 1];
			final Point endPoint = points[i];
			final double distance = Math.abs(distance(point, startPoint, endPoint));
			if(distance < minNearestPointDistance)
				minNearestPointDistance = distance;
		}
		return minNearestPointDistance;
	}

	private double distance(final Point point, final Point startPoint, final Point endPoint){
		//if start is the same as end, then just compute distance to one of the endpoints
		if(startPoint.getX() == endPoint.getX() && startPoint.getY() == endPoint.getY())
			return distance(point, startPoint);

		/*
		 * (1) r = AC dot AB
		 *         ---------
		 *         ||AB||^2
		 *
		 * r has the following meaning:
		 *   r=0 P = A
		 *   r=1 P = B
		 *   r<0 P is on the backward extension of AB
		 *   r>1 P is on the forward extension of AB
		 *   0<r<1 P is interior to AB
		 */
		final double len2 = (endPoint.getX() - startPoint.getX()) * (endPoint.getX() - startPoint.getX())
			+ (endPoint.getY() - startPoint.getY()) * (endPoint.getY() - startPoint.getY());
		final double r = ((point.getX() - startPoint.getX()) * (endPoint.getX() - startPoint.getX())
			+ (point.getY() - startPoint.getY()) * (endPoint.getY() - startPoint.getY())) / len2;

		if(r <= 0.)
			return distance(point, startPoint);
		if(r >= 1.)
			return distance(point, endPoint);

		/*
		 * (2) s = (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay)
		 *         -----------------------------
		 *                    L^2
		 *
		 * Then the distance from C to P = |s|*L.
		 */
		final double s = ((startPoint.getY() - point.getY()) * (endPoint.getX() - startPoint.getX())
			- (startPoint.getX() - point.getX()) * (endPoint.getY() - startPoint.getY())) / len2;
		return Math.abs(s) * Math.sqrt(len2);
	}


	@Override
	public double initialBearing(final Point startPoint, final Point endPoint){
		final double dx = endPoint.getX() - startPoint.getX();
		final double dy = endPoint.getY() - startPoint.getY();
		final double angle = Math.toDegrees(StrictMath.atan2(dx, dy));
		return (angle < 0.? angle + 360.: angle);
	}

	@Override
	public Point destination(final Point startPoint, final double initialBearing, final double distance){
		final double dx = distance * StrictMath.cos(Math.toRadians(initialBearing));
		final double dy = distance * StrictMath.sin(Math.toRadians(initialBearing));
		final GeometryFactory factory = startPoint.getFactory();
		return factory.createPoint(startPoint.getX() + dx, startPoint.getY() + dy);
	}

	@Override
	public Point onTrackClosestPoint(final Point startPoint, final Point endPoint, final Point point){
		final double vx = endPoint.getX() - startPoint.getX();
		final double vy = endPoint.getY() - startPoint.getY();
		final double ux = startPoint.getX() - point.getX();
		final double uy = startPoint.getY() - point.getY();
		final double vu = vx * ux + vy * uy;
		final double vv = vx * vx + vy * vy;
		final double t = - vu / vv;
		if(t >= 0 && t <= 1){
			final GeometryFactory factory = startPoint.getFactory();
			return vectorToSegment(t, factory.createPoint(0., 0.), startPoint, endPoint);
		}

		final double g0 = norm2(vectorToSegment(0., point, startPoint, endPoint));
		final double g1 = norm2(vectorToSegment(1., point, startPoint, endPoint));
		return (g0 <= g1? startPoint: endPoint);
	}

	private Point vectorToSegment(final double t, final Point point, final Point startPoint, final Point endPoint){
		final GeometryFactory factory = startPoint.getFactory();
		return factory.createPoint(
			(1. - t) * startPoint.getX() + t * endPoint.getX() - point.getX(),
			(1. - t) * startPoint.getY() + t * endPoint.getY() - point.getY()
		);
	}

	private double norm2(final Point point) { return point.getX() * point.getX() + point.getY() * point.getY(); }


	@Override
	public double alongTrackDistance(final Point startPoint, final Point endPoint, final Point point){
		final double dx21 = endPoint.getX() - startPoint.getX();
		final double dy21 = endPoint.getY() - startPoint.getY();
		return Math.abs(dx21 * (startPoint.getY() - point.getY()) - (startPoint.getX() - point.getX()) * dy21)
			/ Math.sqrt(dx21 * dx21 + dy21 * dy21);
	}


	@Override
	public Point leftmostPoint(final Polyline polyline){
		final Point startPoint = polyline.getStartPoint();
		final Point endPoint = polyline.getEndPoint();
		final int order = compare(endPoint, startPoint);
		return (order == 1? startPoint: endPoint);
	}

	@Override
	public Point rightmostPoint(final Polyline polyline){
		final Point startPoint = polyline.getStartPoint();
		final Point endPoint = polyline.getEndPoint();
		final int order = compare(endPoint, startPoint);
		return (order == 1? endPoint: startPoint);
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
		final Point pointLeft = leftmostPoint(polyline1);
		final Point pointRight = rightmostPoint(polyline1);
		final double x1 = pointLeft.getX();
		final double y1 = pointLeft.getY();
		final double x2 = pointRight.getX();
		final double y2 = pointRight.getY();

		final Point segmentPointLeft = leftmostPoint(polyline2);
		final Point segmentPointRight = rightmostPoint(polyline2);
		final double x3 = segmentPointLeft.getX();
		final double y3 = segmentPointLeft.getY();
		final double x4 = segmentPointRight.getX();
		final double y4 = segmentPointRight.getY();

		final double v = (x4 - x3) * (y1 - y2) - (x1 - x2) * (y4 - y3);
		if(v == 0.)
			return Collections.emptyList();

		final double ta = ((y3 - y4) * (x1 - x3) + (x4 - x3) * (y1 - y3)) / v;
		final double tb = ((y1 - y2) * (x1 - x3) + (x2 - x1) * (y1 - y3)) / v;

		if(ta >= 0. && ta <= 1. && tb >= 0. && tb <= 1.){
			final double px = x1 + ta * (x2 - x1);
			final double py = y1 + ta * (y2 - y1);

			final GeometryFactory factory = pointLeft.getFactory();
			return Collections.singletonList(factory.createPoint(px, py));
		}

		return Collections.emptyList();
	}

}
