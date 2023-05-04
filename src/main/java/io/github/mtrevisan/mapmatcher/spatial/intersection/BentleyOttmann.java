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

import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.topologies.TopologyCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;


/**
 * @see <a href="https://en.wikipedia.org/wiki/Bentley%E2%80%93Ottmann_algorithm">Bentley–Ottmann algorithm</a>
 * @see <a href="https://apps.dtic.mil/sti/pdfs/ADA058768.pdf">Algorithms for reporting and counting geometric intersections</a>
 * @see <a href="https://core.ac.uk/download/pdf/38922445.pdf">asdf</a>
 */
public class BentleyOttmann{

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
	private final SweepLineTreeSet sweepLine = new SweepLineTreeSet();
	private final List<Point> intersections = new ArrayList<>();

	private final TopologyCalculator topologyCalculator;


	public BentleyOttmann(final TopologyCalculator topologyCalculator){
		this.topologyCalculator = topologyCalculator;
	}

	public void addPoints(final Collection<Point> points){
		for(final Point point : points){
			final SweepSegment ss = new SweepSegment(point, topologyCalculator);
			eventQueue.add(ss.getLeftEvent());
			eventQueue.add(ss.getRightEvent());
		}
	}

	public void addPolylines(final Collection<Polyline> polylines){
		for(final Polyline polyline : polylines)
			addPolyline(polyline);
	}

	public void addPolyline(final Polyline polyline){
		final SweepSegment ss = new SweepSegment(polyline, topologyCalculator);
		eventQueue.add(ss.getLeftEvent());
		eventQueue.add(ss.getRightEvent());
	}

	public void findIntersections(){
		findIntersections(null);
	}

	public void findIntersections(final OnIntersectionListener listener){
		while(!eventQueue.isEmpty()){
			final Event event = eventQueue.poll();
			if(event.type() == Event.Type.POINT_LEFT){
				sweepLine.updateYIndexes(event.point().getX());
				final SweepSegment segmentLeft = event.firstSegment();
				sweepLine.add(segmentLeft);

				final SweepSegment segmentAbove = sweepLine.above(segmentLeft);
				addEventIfIntersection(segmentLeft, segmentAbove, event);
				final SweepSegment segmentBelow = sweepLine.below(segmentLeft);
				addEventIfIntersection(segmentLeft, segmentBelow, event);
			}
			else if(event.type() == Event.Type.POINT_RIGHT){
				final SweepSegment segmentRight = event.firstSegment();
				sweepLine.remove(segmentRight);

				final SweepSegment segmentAbove = sweepLine.above(segmentRight);
				final SweepSegment segmentBelow = sweepLine.below(segmentRight);
				addEventIfIntersectionAndCheck(segmentAbove, segmentBelow, event);
			}
			else{
				intersections.add(event.point());

				final SweepSegment segmentIntersection1 = event.firstSegment();
				final SweepSegment segmentIntersection2 = event.secondSegment();

				if(listener != null)
					listener.onIntersection(segmentIntersection1.getGeometry(), segmentIntersection2.getGeometry(), event.point());

				sweepLine.swap(segmentIntersection1, segmentIntersection2);

				final SweepSegment segmentAbove = sweepLine.above(segmentIntersection2);
				addEventIfIntersectionAndCheck(segmentIntersection2, segmentAbove, event);
				final SweepSegment segmentBelow = sweepLine.below(segmentIntersection1);
				addEventIfIntersectionAndCheck(segmentIntersection1, segmentBelow, event);
			}
		}
	}

	private void addEventIfIntersection(final SweepSegment segment1, final SweepSegment segment2, final Event event){
		if(segment1 != null && segment2 != null){
			final List<Point> points = segment1.intersection(segment2);
			for(final Point point : points)
				if(point.getX() > event.point().getX())
					eventQueue.add(new Event(point, segment1, segment2, topologyCalculator));
		}
	}

	private void addEventIfIntersectionAndCheck(final SweepSegment segment1, final SweepSegment segment2, final Event event){
		if(segment1 != null && segment2 != null){
			final List<Point> points = segment1.intersection(segment2);
			for(final Point point : points)
				if(point.getX() > event.point().getX()){
					final Event e = new Event(point, segment1, segment2, topologyCalculator);
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
