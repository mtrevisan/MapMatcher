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

import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.Objects;


class SweepSegment{

	private final Event event1;
	private final Event event2;
	private final Polyline polyline;
	private double position;


	SweepSegment(final Polyline polyline){
		this.polyline = polyline;

		Event event1 = new Event(polyline.getStartPoint(), this, Event.Type.POINT_LEFT);
		Event event2 = new Event(polyline.getEndPoint(), this, Event.Type.POINT_RIGHT);
		if(!(Objects.compare(event2, event1, Event::compareTo) == 1)){
			final Event swap = event1;
			event1 = event2;
			event2 = swap;
			event1.setType(Event.Type.POINT_LEFT);
			event2.setType(Event.Type.POINT_RIGHT);
		}

		this.event1 = event1;
		this.event2 = event2;

		updatePosition(leftEvent().point().getX());
	}

	double position(){
		return position;
	}

	void setPosition(final double position){
		this.position = position;
	}

	Event leftEvent(){
		return event1;
	}

	Event rightEvent(){
		return event2;
	}

	Polyline segment(){
		return polyline;
	}

	boolean nearlyEqual(final SweepSegment segment){
		return segment.leftEvent().nearlyEqual(leftEvent()) && segment.rightEvent().nearlyEqual(rightEvent());
	}

	void updatePosition(final double x){
		final double x1 = leftEvent().point().getX();
		final double y1 = leftEvent().point().getY();
		final double x2 = rightEvent().point().getX();
		final double y2 = rightEvent().point().getY();

		final double y = y1 + (((y2 - y1) * (x - x1)) / (x2 - x1));
		this.setPosition(y);
	}

	//TODO
	static Point intersection(final SweepSegment segment1, final SweepSegment segment2, final GeometryFactory factory){
		final double x1 = segment1.leftEvent().point().getX();
		final double y1 = segment1.leftEvent().point().getY();
		final double x2 = segment1.rightEvent().point().getX();
		final double y2 = segment1.rightEvent().point().getY();

		final double x3 = segment2.leftEvent().point().getX();
		final double y3 = segment2.leftEvent().point().getY();
		final double x4 = segment2.rightEvent().point().getX();
		final double y4 = segment2.rightEvent().point().getY();

		final double v = (x4 - x3) * (y1 - y2) - (x1 - x2) * (y4 - y3);
		if(v == 0.)
			return null;

		final double ta = ((y3 - y4) * (x1 - x3) + (x4 - x3) * (y1 - y3)) / v;
		final double tb = ((y1 - y2) * (x1 - x3) + (x2 - x1) * (y1 - y3)) / v;

		if(ta >= 0. && ta <= 1. && tb >= 0. && tb <= 1.){
			final double px = x1 + ta * (x2 - x1);
			final double py = y1 + ta * (y2 - y1);

			return factory.createPoint(px, py);
		}

		return null;
	}

	@Override
	public String toString(){
		return "SweepSegment{left = " + leftEvent() + ", right = " + rightEvent() + "}";
	}

}
