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
package io.github.mtrevisan.mapmatcher.graph;

import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class Node{

	private String id;
	private Point point;

	private final Set<Edge> outEdges = new HashSet<>(0);


	public Node(final String id, final Point point){
		this.id = id;
		this.point = point;
	}

	public String getID(){
		return id;
	}

	public void setID(final String id){
		if(id == null || id.length() == 0)
			throw new IllegalArgumentException("`id` cannot be null or empty");

		this.id = id;
	}

	public Collection<Edge> getOutEdges(){
		return outEdges;
	}

	public Edge findOutEdges(final Node nodeTo){
		for(final Edge edge : getOutEdges())
			if(edge.getTo().equals(nodeTo))
				return edge;
		return null;
	}

	public void addOutEdge(final Edge edge){
		outEdges.add(edge);
	}

	public Point getPoint(){
		return point;
	}

	public void setPoint(final Point point){
		this.point = point;
	}

	@Override
	public boolean equals(final Object obj){
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;

		final Node other = (Node)obj;
		return Objects.equals(point, other.point);
	}

	@Override
	public int hashCode(){
		return Objects.hash(point);
	}

	@Override
	public String toString(){
		return "Node{id = " + id + ", point = " + point + "}";
	}

}
