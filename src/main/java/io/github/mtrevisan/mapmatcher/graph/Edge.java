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
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.Collection;
import java.util.Objects;


public class Edge{

	public static final int POINT_FROM = 0;
	public static final int POINT_TO = 1;

	private String id;
	protected final Node from;
	protected final Node to;

	private double weight;


	public static Edge createDirectEdge(final Node from, final Node to){
		return new Edge(from, to);
	}

	public static Edge createSelfEdge(final Node node){
		return new Edge(node, node);
	}

	private Edge(final Node from, final Node to){
		if(from == null)
			throw new IllegalArgumentException("`from` node cannot be null");
		if(to == null)
			throw new IllegalArgumentException("`to` node cannot be null");

		id = Objects.requireNonNullElse(from.getID(), "<null>")
			+ "-" + Objects.requireNonNullElse(to.getID(), "<null>");
		this.from = from;
		this.to = to;
	}

	public String getID(){
		return id;
	}

	void setID(final String id){
		if(id == null || id.length() == 0)
			throw new IllegalArgumentException("`id` cannot be null or empty");

		this.id = id;
	}

	public Point getPoint(final int index){
		return (index == POINT_FROM? from: to)
			.getPoint();
	}

	public Polyline getPolyline(){
		return from.getPoint().getFactory().createPolyline(from.getPoint(), to.getPoint());
	}

	public Node getFrom(){
		return from;
	}

	public Node getTo(){
		return to;
	}

	public Collection<Edge> getOutEdges(){
		return to.getOutEdges();
	}

	public double getWeight(){
		return weight;
	}

	public void setWeight(final double weight){
		this.weight = weight;
	}

	public Edge reversed(){
		return createDirectEdge(to, from);
	}

	@Override
	public boolean equals(final Object obj){
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;

		final Edge other = (Edge)obj;
		return (Objects.equals(from, other.from) && Objects.equals(to, other.to) && Objects.equals(weight, other.weight));
	}

	@Override
	public int hashCode(){
		return Objects.hash(from, to, weight);
	}

	@Override
	public String toString(){
		return "Edge{id = " + id + ", from = " + from + ", to = " + to + ", weight = " + weight + "}";
	}

}
