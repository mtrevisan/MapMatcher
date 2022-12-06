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
package io.github.mtrevisan.mapmatcher.spatial.bentleyottmann;

import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class Event implements Comparable<Event>{

	enum Type{
		POINT_LEFT, POINT_RIGHT, INTERSECTION
	}

	private static final double EPSILON = 1E-9;

	private Type type;
	private final Point point;
	private final List<SweepSegment> segments = new ArrayList<>();


	Event(final Point p, final SweepSegment s1, final Type type){
		point = p;
		this.type = type;
		segments.add(s1);
	}

	Event(final Point p, final SweepSegment s1, final SweepSegment s2){
		this(p, s1, Type.INTERSECTION);

		segments.add(s2);

		// Ensure s1 is always above s2
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

	@Override
	public String toString(){
		return String.format(Locale.getDefault(), "[%s, %s]", point().getX(), point().getY());
	}

	@Override
	public int compareTo(final Event e){
		if(e.point().getX() < point().getX() || (nearlyEqual(e.point().getX(), point().getX()) && e.point().getY() < point().getY()))
			return 1;
		if(e.point().getX() > point().getX() || (nearlyEqual(e.point().getX(), point().getX()) && e.point().getY() > point().getY()))
			return -1;
		return 0;
	}

	boolean nearlyEqual(final Event e){
		return nearlyEqual(point().getX(), e.point().getX()) && nearlyEqual(point().getY(), e.point().getY());
	}

	// Taken from: https://floating-point-gui.de/errors/comparison/
	private static boolean nearlyEqual(final double a, final double b){
		final double absA = Math.abs(a);
		final double absB = Math.abs(b);
		final double diff = Math.abs(a - b);

		//shortcut, handles infinities
		if(a == b)
			return true;

		//a or b is zero or both are extremely close to it
		//relative error is less meaningful here
		if(a == 0 || b == 0 || (absA + absB < Double.MIN_NORMAL))
			return diff < (Event.EPSILON * Double.MIN_NORMAL);

		//use relative error
		return diff / Math.min((absA + absB), Double.MAX_VALUE) < Event.EPSILON;
	}

}
