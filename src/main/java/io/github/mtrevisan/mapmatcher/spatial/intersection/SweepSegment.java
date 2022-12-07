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
package io.github.mtrevisan.mapmatcher.spatial.intersection;

import io.github.mtrevisan.mapmatcher.spatial.Geometry;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.intersection.calculators.IntersectionCalculator;


class SweepSegment{

	private final Event event1;
	private final Event event2;
	private final Geometry geometry;

	private double yIndex;

	private final IntersectionCalculator calculator;


	SweepSegment(final Point point, final IntersectionCalculator calculator){
		this.calculator = calculator;

		this.geometry = point;

		this.event1 = new Event(point, this, Event.Type.POINT_LEFT, calculator);
		this.event2 = new Event(point, this, Event.Type.POINT_RIGHT, calculator);
	}

	SweepSegment(final Polyline polyline, final IntersectionCalculator calculator){
		this.calculator = calculator;

		this.geometry = polyline;

		final Point startPoint = polyline.getStartPoint();
		final Point endPoint = polyline.getEndPoint();
		final int order = calculator.compare(endPoint, startPoint);
		this.event1 = new Event((order == 1? startPoint: endPoint), this, Event.Type.POINT_LEFT, calculator);
		this.event2 = new Event((order == 1? endPoint: startPoint), this, Event.Type.POINT_RIGHT, calculator);

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

	//FIXME
	boolean isNearlyEqual(final SweepSegment segment){
		return (segment.getLeftEvent().nearlyEqual(getLeftEvent())
			&& segment.getRightEvent().nearlyEqual(getRightEvent()));
	}

	void updateYIndex(final double x){
		//FIXME
		final double y = calculator.calculateYIndex(getLeftEvent().point(), getRightEvent().point(), x);
		this.setYIndex(y);
	}

	Point intersection(final SweepSegment segment){
		return calculator.intersection((Polyline)geometry, (Polyline)segment.geometry);
	}

	@Override
	public String toString(){
		return "SweepSegment{left = " + getLeftEvent() + ", right = " + getRightEvent() + "}";
	}

}
