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
package io.github.mtrevisan.mapmatcher.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class Graph{

	private final Map<Vertex, List<Edge>> adjacencyList;
	private final Map<Long, Vertex> vertices;
	private final ConcurrentHashMap<Graph, Graph> cache = new ConcurrentHashMap<>();

	Graph(Map<Vertex, List<Edge>> adjacencyList, Map<Long, Vertex> vertices){
		this.adjacencyList = deepImmutableCopy(adjacencyList);
		this.vertices = new HashMap<>(vertices);
	}

	public Collection<Edge> getVertexEdges(Vertex node){
		return adjacencyList.getOrDefault(node, Collections.emptyList());
	}

	private Map<Vertex, List<Edge>> deepImmutableCopy(Map<Vertex, List<Edge>> adjacencyList){
		return adjacencyList.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> List.copyOf(entry.getValue())));
	}

	public Graph reversed(){
		return cache.computeIfAbsent(this, Graph::computeReversedGraph);
	}

	private Graph computeReversedGraph(){
		var reversedAdjacencyList = new HashMap<Vertex, List<Edge>>();

		for(var entry : adjacencyList.entrySet()){
			for(var edge : entry.getValue()){
				reversedAdjacencyList.computeIfAbsent(edge.getTo(), (e) -> new ArrayList<>()).add(edge.reversed());
			}
		}

		return new Graph(reversedAdjacencyList, vertices);
	}

	public Collection<Vertex> vertices(){
		return vertices.values();
	}

	public Collection<Edge> edges(){
		return adjacencyList.values().stream().collect(ArrayList::new, List::addAll, List::addAll);
	}

	@Override
	public String toString(){
		return "Graph{" + "adjacencyList=" + adjacencyList + ", vertices=" + vertices + '}';
	}

}
