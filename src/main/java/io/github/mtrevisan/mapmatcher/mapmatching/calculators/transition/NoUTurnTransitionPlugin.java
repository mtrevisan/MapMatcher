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
package io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.HashSet;
import java.util.Set;


public class NoUTurnTransitionPlugin implements TransitionProbabilityPlugin{

	@Override
	public double factor(final Edge fromSegment, final Edge toSegment, final Point previousObservation, final Point currentObservation,
			final Polyline path){
		//penalize u-turns: make then unreachable
		final Point[] points = path.getPoints();
		final Set<Point> seenPoints = new HashSet<>(points.length);
		for(int i = points.length - 1; i >= 0; i --)
			if(!seenPoints.add(points[i]))
				return Double.POSITIVE_INFINITY;
		return 0.;


//		boolean segmentsReversed = PathHelper.isSegmentsReversed(fromSegment, toSegment);
//
//		if(path != null && !isSegmentsTheSame(fromSegment, toSegment))
//			//disallow U-turn along multiple edges
//			segmentsReversed = PathHelper.hasMixedDirections(path, fromSegment, toSegment);
//
//		return (segmentsReversed? Double.POSITIVE_INFINITY: 0.);


//		final Edge[] pathEdges = PathHelper.extractPathAsEdges(path);
//		Set<String> froms = new HashSet<>(pathEdges.length + 2);
//		froms.add(fromSegment.getFrom().getID());
//		if(froms.contains(fromSegment.getTo().getID()))
//			return Double.POSITIVE_INFINITY;
//		for(final Edge edge : pathEdges)
//			if(!froms.add(edge.getFrom().getID()) || froms.contains(edge.getTo().getID()))
//				return Double.POSITIVE_INFINITY;
//		if(!froms.add(toSegment.getFrom().getID()) || froms.contains(toSegment.getTo().getID()))
//			return Double.POSITIVE_INFINITY;
//		return 0.;
	}

}
