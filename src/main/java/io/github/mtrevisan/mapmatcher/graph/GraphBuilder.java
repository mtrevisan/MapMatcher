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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GraphBuilder{

	private final Map<Long, Vertex> vertices = new HashMap<>();
	private final Map<Vertex, List<Edge>> adjacencyList = new HashMap<>();

	public GraphBuilder addVertex(Vertex vertex){
		adjacencyList.put(vertex, new ArrayList<>());
		vertices.put(vertex.getId(), vertex);
		return this;
	}

	public GraphBuilder connect(Vertex from, Vertex to, int maxSpeed){
		if(! vertices.containsKey(from.getId())){
			throw new VertexNotPresentException(from.getId());
		}
		if(! vertices.containsKey(to.getId())){
			throw new VertexNotPresentException(to.getId());
		}

		connectVertices(from, to, maxSpeed);
		return this;
	}

	private void connectVertices(Vertex from, Vertex to, int maxSpeed){
		adjacencyList.computeIfAbsent(from, (e) -> new ArrayList<>()).add(new Edge(from, to, maxSpeed));
	}

	public GraphBuilder connectByIds(long fromId, long toId, int maxSpeed){
		if(! vertices.containsKey(fromId)){
			throw new VertexNotPresentException(fromId);
		}
		if(! vertices.containsKey(toId)){
			throw new VertexNotPresentException(toId);
		}
		return connect(vertices.get(fromId), vertices.get(toId), maxSpeed);
	}

	public Graph asGraph(){
		return new Graph(adjacencyList, vertices);
	}

}
