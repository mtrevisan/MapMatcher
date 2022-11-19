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
package io.github.mtrevisan.mapmatcher.path;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.pathfinding.PathSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PathSummaryCreator{

	private List<Edge> reconstructPathFromPredecessorTree(Vertex from, Vertex to, Map<Vertex, Edge> predecessorTree){
		var result = new ArrayList<Edge>();

		var currNode = from;

		while(predecessorTree.containsKey(currNode) && ! currNode.equals(to)){
			var edge = predecessorTree.get(currNode);
			result.add(edge);
			currNode = edge.getFrom();
		}

		if(! currNode.equals(to)){
			return new ArrayList<>();
		}

		return result;
	}

	public PathSummary createUnidirectionalPath(Vertex start, Vertex end, Map<Vertex, Edge> predecessorTree){
		var fromEndToStart = reconstructPathFromPredecessorTree(end, start, predecessorTree);

		Collections.reverse(fromEndToStart);

		return new SingleDirectionalPathSummary(fromEndToStart, predecessorTree.keySet());
	}

	public PathSummary createBidirectionalPath(Vertex start, Vertex mid, Vertex end, Map<Vertex, Edge> predecessorTreeStart, Map<Vertex, Edge> predecessorTreeEnd){
		var fromMidToStart = reconstructPathFromPredecessorTree(mid, start, predecessorTreeStart);
		Collections.reverse(fromMidToStart);

		var fromEndToMid = reconstructPathFromPredecessorTree(mid, end, predecessorTreeEnd);

		if((start != mid && fromMidToStart.isEmpty()) || (end != mid && fromEndToMid.isEmpty())){
			return new BidirectionalPathSummary(Collections.emptyList(), predecessorTreeStart.keySet(), predecessorTreeEnd.keySet());
		}

		fromMidToStart.addAll(fromEndToMid.stream().map(Edge::reversed).collect(Collectors.toList()));

		return new BidirectionalPathSummary(fromMidToStart, predecessorTreeStart.keySet(), predecessorTreeEnd.keySet());
	}

}
