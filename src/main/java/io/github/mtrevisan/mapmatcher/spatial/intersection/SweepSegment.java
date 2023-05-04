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
package io.github.mtrevisan.mapmatcher.spatial.intersection;

import io.github.mtrevisan.mapmatcher.spatial.Geometry;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.topologies.TopologyCalculator;

import java.util.List;


class SweepSegment{

	private final Event event1;
	private final Event event2;
	private final Geometry geometry;

	private double yIndex;

	private final TopologyCalculator topologyCalculator;


	SweepSegment(final Point point, final TopologyCalculator topologyCalculator){
		if(point == null)
			throw new IllegalArgumentException("`point` cannot be null");

		this.topologyCalculator = topologyCalculator;

		this.geometry = point;

		this.event1 = new Event(point, this, Event.Type.POINT_LEFT, topologyCalculator);
		this.event2 = new Event(point, this, Event.Type.POINT_RIGHT, topologyCalculator);
	}

	SweepSegment(final Polyline polyline, final TopologyCalculator topologyCalculator){
		if(polyline == null || polyline.isEmpty())
			throw new IllegalArgumentException("`polyline` cannot be null or empty");

		this.topologyCalculator = topologyCalculator;

		this.geometry = polyline;

		final Point leftmostPoint = topologyCalculator.leftmostPoint(polyline);
		final Point rightPoint = topologyCalculator.rightmostPoint(polyline);
		this.event1 = new Event(leftmostPoint, this, Event.Type.POINT_LEFT, topologyCalculator);
		this.event2 = new Event(rightPoint, this, Event.Type.POINT_RIGHT, topologyCalculator);

		updateYIndex(getLeftEvent().point().getX());
	}

	double getYIndex(){
		return yIndex;
	}

	void setYIndex(final double yIndex){
		this.yIndex = yIndex;
	}

	Event getLeftEvent(){
		return event1;
	}

	Event getRightEvent(){
		return event2;
	}

	Geometry getGeometry(){
		return geometry;
	}

	boolean isNearlyEqual(final SweepSegment segment){
		return (segment.getLeftEvent().nearlyEqual(getLeftEvent())
			&& segment.getRightEvent().nearlyEqual(getRightEvent()));
	}

	void updateYIndex(final double x){
		final double y = topologyCalculator.calculateYIndex(getLeftEvent().point(), getRightEvent().point(), x);
		this.setYIndex(y);
	}

	List<Point> intersection(final SweepSegment segment){
		return topologyCalculator.intersection((Polyline)geometry, (Polyline)segment.geometry);
	}

	@Override
	public String toString(){
		return "SweepSegment{left = " + getLeftEvent() + ", right = " + getRightEvent() + "}";
	}

}
