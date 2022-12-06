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

import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;


public class BentleyOttmann{

	private final GeometryFactory factory;

	private final PriorityQueue<Event> eventQueue = new PriorityQueue<>(){
		@Override
		public boolean contains(final Object o){
			boolean result = false;
			for(final Event e : this)
				if(e.nearlyEqual((Event)o)){
					result = true;
					break;
				}
			return result;
		}
	};
	private final SweepLine sweepLine = new SweepLine();
	private final List<Point> intersections = new ArrayList<>();
	private OnIntersectionListener listener;


	public BentleyOttmann(final GeometryFactory factory){
		this.factory = factory;
	}

	public void addPolylines(final List<Polyline> polylines){
		for(final Polyline polyline : polylines){
			final SweepSegment ss = new SweepSegment(polyline);
			eventQueue.add(ss.leftEvent());
			eventQueue.add(ss.rightEvent());
		}
	}

	public void findIntersections(){
		while(!eventQueue.isEmpty()){
			final Event event = eventQueue.poll();
			if(event.type() == Event.Type.POINT_LEFT){
				final SweepSegment segE = event.firstSegment();

				sweepLine.updatePositions(event.point().getX());
				sweepLine.add(segE);

				final SweepSegment segA = sweepLine.above(segE);
				final SweepSegment segB = sweepLine.below(segE);

				//System.out.println("LEFT POINT -> seg: " + segE.segment() + ", above: " + (segA != null? segA.segment(): "") + ", below: " + (segB != null? segB.segment(): ""));

				addEventIfIntersection(segE, segA, event, false);
				addEventIfIntersection(segE, segB, event, false);
			}
			else if(event.type() == Event.Type.POINT_RIGHT){
				final SweepSegment segE = event.firstSegment();
				final SweepSegment segA = sweepLine.above(segE);
				final SweepSegment segB = sweepLine.below(segE);

				//System.out.println("RIGHT POINT -> seg: " + segE.segment() + ", above: " + (segA != null? segA.segment(): "") + ", below: " + (segB != null? segB.segment(): ""));

				sweepLine.remove(segE);

				addEventIfIntersection(segA, segB, event, true);
			}
			else{
				intersections.add(event.point());

				final SweepSegment segE1 = event.firstSegment();
				final SweepSegment segE2 = event.secondSegment();

				if(listener != null)
					listener.onIntersection(segE1.segment(), segE2.segment(), event.point());

				sweepLine.swap(segE1, segE2);

				final SweepSegment segA = sweepLine.above(segE2);
				final SweepSegment segB = sweepLine.below(segE1);

				//System.out.println("INTERSECTION POINT -> seg1: " + segE1.segment() + ", seg2: " + segE2.segment() + ", above " + segE2.segment() + ": " + (segA != null? segA.segment(): "") + ", below " + segE1.segment() + ": " + (segB != null? segB.segment(): ""));

				addEventIfIntersection(segE2, segA, event, true);
				addEventIfIntersection(segE1, segB, event, true);
			}
		}
	}

	public List<Point> intersections(){
		return Collections.unmodifiableList(intersections);
	}

	public void setListener(final OnIntersectionListener listener){
		this.listener = listener;
	}

	public void reset(){
		intersections.clear();
		eventQueue.clear();
		sweepLine.clear();
	}

	private void addEventIfIntersection(final SweepSegment s1, final SweepSegment s2, final Event event, final boolean check){
		if(s1 != null && s2 != null){
			final Point i = SweepSegment.intersection(s1, s2, factory);
			if(i != null && i.getX() > event.point().getX()){
				final Event e = new Event(i, s1, s2);
				if(check)
					if(eventQueue.contains(e))
						return;

				eventQueue.add(e);
			}
		}
	}

}
