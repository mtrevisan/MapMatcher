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
package io.github.mtrevisan.mapmatcher.pathfinding.path;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.DistanceCalculator;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class BidirectionalPathSummary implements PathSummary{

	private final List<Edge> path;
	private final Set<Node> searchedVerticesFromStart;
	private final Set<Node> searchedVerticesFromEnd;


	public static BidirectionalPathSummary ofNode(final Node node){
		final Set<Node> searchedVertices = Collections.singleton(node);
		return new BidirectionalPathSummary(Collections.singletonList(Edge.createSelfEdge(node)), searchedVertices, searchedVertices);
	}

	public static BidirectionalPathSummary ofPath(final List<Edge> path, final Set<Node> searchedVerticesFromStart,
			final Set<Node> searchedVerticesFromEnd){
		return new BidirectionalPathSummary(path, searchedVerticesFromStart, searchedVerticesFromEnd);
	}

	private BidirectionalPathSummary(final List<Edge> path, final Set<Node> searchedVerticesFromStart,
			final Set<Node> searchedVerticesFromEnd){
		this.path = path;
		this.searchedVerticesFromStart = searchedVerticesFromStart;
		this.searchedVerticesFromEnd = searchedVerticesFromEnd;
	}

	@Override
	public List<Node> simplePath(){
		if(!isFound())
			return Collections.emptyList();

		final var withoutLast = path.stream()
			.map(Edge::getFrom)
			.collect(Collectors.toList());
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
	public double totalDistance(final DistanceCalculator distanceCalculator){
		return path.stream()
			.mapToDouble(edge -> distanceCalculator.calculateWeight(edge.getFrom().getPoint(), edge.getTo().getPoint()))
			.sum();
	}

	@Override
	public boolean isFound(){
		return !path.isEmpty();
	}

}
