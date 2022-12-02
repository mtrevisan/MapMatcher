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

import io.github.mtrevisan.mapmatcher.helpers.Polyline;

import java.util.Collection;
import java.util.Objects;


public class Edge{

	private String id;
	protected final Node from;
	protected final Node to;
	protected final Polyline polyline;

	private double weight;


	public static Edge createDirectEdge(final Node from, final Node to, final Polyline polyline){
		return new Edge(from, to, polyline);
	}

	public static Edge createSelfEdge(final Node node){
		final Polyline polyline = Polyline.of(node.getCoordinate(), node.getCoordinate());
		return new Edge(node, node, polyline);
	}

	private Edge(final Node from, final Node to, final Polyline polyline){
		if(from == null)
			throw new IllegalArgumentException("`from` node cannot be null");
		if(to == null)
			throw new IllegalArgumentException("`to` node cannot be null");
		if(polyline == null)
			throw new IllegalArgumentException("`geometry` cannot be null");

		id = "E-" + Objects.requireNonNullElse(from.getID(), "<null>")
			+ "-" + Objects.requireNonNullElse(to.getID(), "<null>");
		this.from = from;
		this.to = to;
		this.polyline = polyline;
	}

	public String getID(){
		return id;
	}

	void setID(final String id){
		if(id == null || id.length() == 0)
			throw new IllegalArgumentException("`id` cannot be null or empty");

		this.id = id;
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

	public Polyline getPolyline(){
		return polyline;
	}

	public double getWeight(){
		return weight;
	}

	public void setWeight(final double weight){
		this.weight = weight;
	}

	public Edge reversed(){
		return createDirectEdge(to, from, polyline.reverse());
	}

	@Override
	public boolean equals(final Object obj){
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;

		final Edge edge = (Edge)obj;
		return (Objects.equals(from, edge.from) && Objects.equals(to, edge.to) && Objects.equals(weight, edge.weight));
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
