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
package io.github.mtrevisan.mapmatcher.spatial.intersection.calculators;

import io.github.mtrevisan.mapmatcher.helpers.MathHelper;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;


public class EuclideanSegmentCalculator implements IntersectionCalculator{

	private static final double EPSILON = 1.e-9;


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
	public Point intersection(final Polyline polyline1, final Polyline polyline2){
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
			return null;

		final double ta = ((y3 - y4) * (x1 - x3) + (x4 - x3) * (y1 - y3)) / v;
		final double tb = ((y1 - y2) * (x1 - x3) + (x2 - x1) * (y1 - y3)) / v;

		if(ta >= 0. && ta <= 1. && tb >= 0. && tb <= 1.){
			final double px = x1 + ta * (x2 - x1);
			final double py = y1 + ta * (y2 - y1);

			final GeometryFactory factory = pointLeft.getFactory();
			return factory.createPoint(px, py);
		}

		return null;
	}

}
