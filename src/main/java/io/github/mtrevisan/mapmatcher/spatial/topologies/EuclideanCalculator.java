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

import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.Collections;
import java.util.List;


public class EuclideanCalculator implements TopologyCalculator{

	/** The tolerance distance for considering two points equal. */
	private static final double PRECISION = 1.e-6;


	private final double precision;


	/**
	 * Construct a new calculator instance with the given precision.
	 *
	 * @param precision	The tolerance distance for considering two points equal.
	 */
	public static EuclideanCalculator withPrecision(final double precision){
		return new EuclideanCalculator(precision);
	}


	public EuclideanCalculator(){
		precision = PRECISION;
	}

	protected EuclideanCalculator(final double precision){
		this.precision = precision;
	}


	@Override
	public double getPrecision(){
		return precision;
	}


	@Override
	public double distance(final Point startPoint, final Point endPoint){
		final double dx = startPoint.getX() - endPoint.getX();
		final double dy = startPoint.getY() - endPoint.getY();
		return Math.hypot(dx, dy);
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
		 * r = (AC dot AB) / ||AB||^2
		 * r has the following meaning:
		 *   r = 0 P = A
		 *   r = 1 P = B
		 *   r < 0 P is on the backward extension of AB
		 *   r > 1 P is on the forward extension of AB
		 *   0 < r < 1 P is interior to AB
		 */
		final double dx = endPoint.getX() - startPoint.getX();
		final double dy = endPoint.getY() - startPoint.getY();
		final double rr = (point.getX() - startPoint.getX()) * dx + (point.getY() - startPoint.getY()) * dy;
		if(rr <= 0.)
			return distance(point, startPoint);
		if(rr >= dx * dx + dy * dy)
			return distance(point, endPoint);

		/*
		 * s = ((Ay - Cy) * (Bx - Ax) - (Ax - Cx) * (By - Ay)) / L^2
		 *
		 * Then the distance from C to P is |s| * L.
		 */
		return Math.abs((startPoint.getY() - point.getY()) * dx - (startPoint.getX() - point.getX()) * dy) / StrictMath.hypot(dx, dy);
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
		final double t = -vu / vv;
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
		final double hyp = Math.hypot(dx21, dy21);
		return (Math.abs(hyp) >= Double.MIN_VALUE
			? Math.abs(dx21 * (startPoint.getY() - point.getY()) - (startPoint.getX() - point.getX()) * dy21) / hyp
			: Double.NaN);
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
