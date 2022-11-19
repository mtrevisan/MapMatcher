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
package io.github.mtrevisan.mapmatcher.path;

import io.github.mtrevisan.mapmatcher.convexhull.AndrewMonotoneChainConvexHullCalculator;
import io.github.mtrevisan.mapmatcher.convexhull.ConvexHullCalculator;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.pathfinding.PathSummary;
import io.github.mtrevisan.mapmatcher.weight.DistanceEdgeWeightCalculator;
import io.github.mtrevisan.mapmatcher.weight.DurationEdgeWeightCalculator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class BidirectionalPathSummary implements PathSummary{

	private static final DistanceEdgeWeightCalculator DISTANCE_CALCULATOR = new DistanceEdgeWeightCalculator();
	private static final DurationEdgeWeightCalculator DURATION_CALCULATOR = new DurationEdgeWeightCalculator();
	private static final ConvexHullCalculator CONVEX_HULL_CALCULATOR = new AndrewMonotoneChainConvexHullCalculator();

	private final List<Edge> path;
	private final Set<Vertex> searchedVerticesFromStart;
	private final Set<Vertex> searchedVerticesFromEnd;


	BidirectionalPathSummary(final List<Edge> path, final Set<Vertex> searchedVerticesFromStart, final Set<Vertex> searchedVerticesFromEnd){
		this.path = path;
		this.searchedVerticesFromStart = searchedVerticesFromStart;
		this.searchedVerticesFromEnd = searchedVerticesFromEnd;
	}

	@Override
	public List<Vertex> simplePath(){
		if(!isFound())
			return Collections.emptyList();

		final var withoutLast = path.stream().map(Edge::getFrom).collect(Collectors.toList());
		withoutLast.add(path.get(path.size() - 1).getTo());
		return withoutLast;
	}

	@Override
	public int numberOfVertices(){
		return (isFound()? path.size() + 1: 0);
	}

	@Override
	public int totalVisitedVertices(){
		return searchedVerticesFromStart.size() + searchedVerticesFromEnd.size();
	}

	@Override
	public double totalDistance(){
		return path.stream()
			.mapToDouble(DISTANCE_CALCULATOR::calculateWeight)
			.sum();
	}

	@Override
	public double totalDuration(){
		return path.stream()
			.mapToDouble(DURATION_CALCULATOR::calculateWeight)
			.sum();
	}

	@Override
	public Collection<List<Vertex>> searchBoundaries(){
		return List.of(
			CONVEX_HULL_CALCULATOR.calculateConvexHull(searchedVerticesFromStart),
			CONVEX_HULL_CALCULATOR.calculateConvexHull(searchedVerticesFromEnd)
		);
	}

	@Override
	public boolean isFound(){
		return !path.isEmpty();
	}

}
