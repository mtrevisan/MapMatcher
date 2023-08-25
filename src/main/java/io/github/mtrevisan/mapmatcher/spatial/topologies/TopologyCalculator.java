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
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.List;


public interface TopologyCalculator{

	/** The tolerance distance for considering two points equal. */
	double getPrecision();


	double distance(Point startPoint, Point endPoint);

	double distance(Point point, Polyline polyline);

	double initialBearing(Point startPoint, Point endPoint);

	Point destination(Point startPoint, double initialBearing, double distance);


	Point onTrackClosestPoint(Point startPoint, Point endPoint, Point point);

	double alongTrackDistance(Point startPoint, Point endPoint, Point point);


	Point leftmostPoint(Polyline polyline);

	Point rightmostPoint(Polyline polyline);

	default double calculateYIndex(final Point pointLeft, final Point pointRight, final double x){
		final double x1 = pointLeft.getX();
		final double y1 = pointLeft.getY();
		final double x2 = pointRight.getX();
		final double y2 = pointRight.getY();
		//equation of line passing through two points
		return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
	}


	default int compare(final Point point1, final Point point2){
		final double p1x = point1.getX();
		final double p1y = point1.getY();
		final double p2x = point2.getX();
		final double p2y = point2.getY();
		if(p1x > p2x || MathHelper.nearlyEqual(p1x, p2x, getPrecision()) && p1y > p2y)
			return 1;
		if(p1x < p2x || MathHelper.nearlyEqual(p1x, p2x, getPrecision()) && p1y < p2y)
			return -1;
		return 0;
	}

	List<Point> intersection(Polyline polyline1, Polyline polyline2);

}
