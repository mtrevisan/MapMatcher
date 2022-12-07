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

import io.github.mtrevisan.mapmatcher.helpers.MathHelper;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class Event implements Comparable<Event>{

	protected enum Type{
		POINT_LEFT, POINT_RIGHT, INTERSECTION
	}

	private static final double EPSILON = 1.e-9;

	private Type type;
	private final Point point;
	private final List<SweepSegment> segments = new ArrayList<>();


	Event(final Point point, final SweepSegment segment, final Type type){
		this.point = point;
		this.type = type;

		segments.add(segment);
	}

	Event(final Point point, final SweepSegment segment1, final SweepSegment segment2){
		this(point, segment1, Type.INTERSECTION);

		segments.add(segment2);

		//ensure segment1 is always above segment2
		if(!(segments.get(0).position() > segments.get(1).position()))
			Collections.swap(segments, 0, 1);
	}

	void setType(final Type type){
		this.type = type;
	}

	Type type(){
		return type;
	}

	Point point(){
		return point;
	}

	SweepSegment firstSegment(){
		return segments.get(0);
	}

	SweepSegment secondSegment(){
		return segments.get(1);
	}

	boolean nearlyEqual(final Event event){
		return (MathHelper.nearlyEqual(point().getX(), event.point().getX(), EPSILON)
			&& MathHelper.nearlyEqual(point().getY(), event.point().getY(), EPSILON));
	}

	@Override
	public int compareTo(final Event event){
		final double epx = event.point().getX();
		final double epy = event.point().getY();
		final double px = point().getX();
		final double py = point().getY();
		if(epx < px || MathHelper.nearlyEqual(epx, px, EPSILON) && epy < py)
			return 1;
		if(epx > px || MathHelper.nearlyEqual(epx, px, EPSILON) && epy > py)
			return -1;
		return 0;
	}

	@Override
	public String toString(){
		return String.format(Locale.getDefault(), "[%s, %s]", point().getX(), point().getY());
	}

}
