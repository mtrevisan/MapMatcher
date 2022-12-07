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

import java.util.ArrayList;
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
		findIntersections(null);
	}

	public void findIntersections(final OnIntersectionListener listener){
		while(!eventQueue.isEmpty()){
			final Event event = eventQueue.poll();
			if(event.type() == Event.Type.POINT_LEFT){
				final SweepSegment segmentLeft = event.firstSegment();

				sweepLine.updatePositions(event.point().getX());
				sweepLine.add(segmentLeft);

				final SweepSegment segmentAbove = sweepLine.above(segmentLeft);
				final SweepSegment segmentBelow = sweepLine.below(segmentLeft);

				addEventIfIntersection(segmentLeft, segmentAbove, event);
				addEventIfIntersection(segmentLeft, segmentBelow, event);
			}
			else if(event.type() == Event.Type.POINT_RIGHT){
				final SweepSegment segmentRight = event.firstSegment();
				final SweepSegment segmentAbove = sweepLine.above(segmentRight);
				final SweepSegment segmentBelow = sweepLine.below(segmentRight);

				sweepLine.remove(segmentRight);

				addEventIfIntersectionAndCheck(segmentAbove, segmentBelow, event);
			}
			else{
				intersections.add(event.point());

				final SweepSegment segmentIntersection1 = event.firstSegment();
				final SweepSegment segmentIntersection2 = event.secondSegment();

				if(listener != null)
					listener.onIntersection(segmentIntersection1.segment(), segmentIntersection2.segment(), event.point());

				sweepLine.swap(segmentIntersection1, segmentIntersection2);

				final SweepSegment segmentAbove = sweepLine.above(segmentIntersection2);
				final SweepSegment segmentBelow = sweepLine.below(segmentIntersection1);

				addEventIfIntersectionAndCheck(segmentIntersection2, segmentAbove, event);
				addEventIfIntersectionAndCheck(segmentIntersection1, segmentBelow, event);
			}
		}
	}

	private void addEventIfIntersection(final SweepSegment segment1, final SweepSegment segment2, final Event event){
		if(segment1 != null && segment2 != null){
			final Point point = SweepSegment.intersection(segment1, segment2, factory);
			if(point != null && point.getX() > event.point().getX())
				eventQueue.add(new Event(point, segment1, segment2));
		}
	}

	private void addEventIfIntersectionAndCheck(final SweepSegment segment1, final SweepSegment segment2, final Event event){
		if(segment1 != null && segment2 != null){
			final Point point = SweepSegment.intersection(segment1, segment2, factory);
			if(point != null && point.getX() > event.point().getX()){
				final Event e = new Event(point, segment1, segment2);
				if(!eventQueue.contains(e))
					eventQueue.add(e);
			}
		}
	}

	public List<Point> intersections(){
		return intersections;
	}

	public void reset(){
		intersections.clear();
		eventQueue.clear();
		sweepLine.clear();
	}

}
