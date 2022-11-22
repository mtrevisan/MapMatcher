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

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.pathfinding.PathSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class PathSummaryCreator{

	public PathSummary createUnidirectionalPath(final Vertex start, final Vertex end,
			final Map<Vertex, Edge> predecessorTree){
		final var fromEndToStart = reconstructPathFromPredecessorTree(end, start, predecessorTree);

		Collections.reverse(fromEndToStart);

		return new SingleDirectionalPathSummary(fromEndToStart, predecessorTree.keySet());
	}

	public PathSummary createBidirectionalPath(final Vertex start, final Vertex middle, final Vertex end,
			final Map<Vertex, Edge> predecessorTreeStart, final Map<Vertex, Edge> predecessorTreeEnd){
		final var fromMidToStart = reconstructPathFromPredecessorTree(middle, start, predecessorTreeStart);
		Collections.reverse(fromMidToStart);

		final var fromEndToMid = reconstructPathFromPredecessorTree(middle, end, predecessorTreeEnd);

		if((start != middle && fromMidToStart.isEmpty()) || (end != middle && fromEndToMid.isEmpty()))
			return new BidirectionalPathSummary(Collections.emptyList(), predecessorTreeStart.keySet(), predecessorTreeEnd.keySet());

		fromMidToStart.addAll(fromEndToMid.stream().map(Edge::reversed).toList());
		return new BidirectionalPathSummary(fromMidToStart, predecessorTreeStart.keySet(), predecessorTreeEnd.keySet());
	}

	private List<Edge> reconstructPathFromPredecessorTree(final Vertex from, final Vertex to, final Map<Vertex, Edge> predecessorTree){
		final var result = new ArrayList<Edge>();
		var currentNode = from;
		while(predecessorTree.containsKey(currentNode) && !currentNode.equals(to)){
			final var edge = predecessorTree.get(currentNode);
			result.add(edge);
			currentNode = edge.getFrom();
		}
		return (currentNode.equals(to)? result: new ArrayList<>());
	}

}
