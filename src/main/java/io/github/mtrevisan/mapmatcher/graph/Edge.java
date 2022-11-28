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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.util.Collection;
import java.util.Objects;


public class Edge{

	private String id;
	protected final Node from;
	protected final Node to;
	protected final LineString geometry;

	private double weight;


	public static Edge createDirectEdge(final Node from, final Node to, final LineString geometry){
		return new Edge(from, to, geometry);
	}

	private Edge(final String id){
		this.id = id;
		from = null;
		to = null;
		geometry = null;
	}

	private Edge(final Node from, final Node to, final LineString geometry){
		if(from == null)
			throw new IllegalArgumentException("`from` node cannot be null");
		if(to == null)
			throw new IllegalArgumentException("`to` node cannot be null");
		if(geometry == null)
			throw new IllegalArgumentException("`geometry` cannot be null");

		id = "E-" + from.getID() + "-" + to.getID();
		this.from = from;
		this.to = to;
		this.geometry = geometry;
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

	public Collection<Edge> geOutEdges(){
		return to.geOutEdges();
	}

	public LineString getLineString(){
		return geometry;
	}

	public double getWeight(){
		return weight;
	}

	public void setWeight(final double weight){
		this.weight = weight;
	}

	public Edge reversed(){
		return createDirectEdge(to, from, geometry.reverse());
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
