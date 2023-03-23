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
import io.github.mtrevisan.mapmatcher.spatial.ArrayHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class PathSummaryCreator{

	public Edge[] createUnidirectionalPath(final Node start, final Node end,
			final Map<Node, Edge> predecessorTree){
		final Edge[] fromEndToStart = reconstructPathFromPredecessorTree(end, start, predecessorTree);

		ArrayHelper.reverse(fromEndToStart);

		return fromEndToStart;
	}

	public Edge[] createBidirectionalPath(final Node start, final Node middle, final Node end,
			final Map<Node, Edge> predecessorTreeStart, final Map<Node, Edge> predecessorTreeEnd){
		final Edge[] fromMidToStart = reconstructPathFromPredecessorTree(middle, start, predecessorTreeStart);
		final Edge[] fromEndToMid = reconstructPathFromPredecessorTree(middle, end, predecessorTreeEnd);

		if((start != middle && fromMidToStart.length == 0) || (end != middle && fromEndToMid.length == 0))
			return new Edge[0];

		final Edge[] fromStartToEnd = new Edge[fromMidToStart.length + fromEndToMid.length];
		int size = 0;
		for(int i = fromMidToStart.length - 1; i >= 0; i --)
			fromStartToEnd[size ++] = fromMidToStart[i];
		for(int i = fromEndToMid.length - 1; i >= 0; i --)
			fromStartToEnd[size ++] = fromEndToMid[i];
		return fromStartToEnd;
	}

	private Edge[] reconstructPathFromPredecessorTree(final Node from, final Node to, final Map<Node, Edge> predecessorTree){
		final List<Edge> result = new ArrayList<>(0);
		var currentNode = from;
		while(predecessorTree.containsKey(currentNode) && !Objects.equals(currentNode, to)){
			final Edge edge = predecessorTree.get(currentNode);
			result.add(edge);
			currentNode = edge.getFrom();
		}
		return (Objects.equals(currentNode, to)? result.toArray(Edge[]::new): new Edge[0]);
	}


	public static List<Node> simplePath(final List<Edge> path){
		if(path.isEmpty())
			return Collections.emptyList();

		final List<Node> withoutLast = new ArrayList<>(path.size() + 1);
		for(final Edge edge : path)
			withoutLast.add(edge.getFrom());
		final Node nodeTo = path.get(path.size() - 1).getTo();
		final Node lastNode = withoutLast.get(withoutLast.size() - 1);
		if(!lastNode.equals(nodeTo))
			withoutLast.add(nodeTo);
		return withoutLast;
	}

}
