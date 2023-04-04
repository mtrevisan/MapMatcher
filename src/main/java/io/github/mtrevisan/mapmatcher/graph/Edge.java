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

import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.Collection;
import java.util.Objects;


public class Edge{

	private static final String EMPTY_STRING = "";


	private String id;

	protected final Node from;
	protected final Node to;
	protected final Polyline path;

	protected boolean offRoad;
	protected Edge fromProjected;
	protected Edge toProjected;


	public static Edge createDirectEdge(final Node from, final Node to){
		final GeometryFactory factory = from.getPoint().getFactory();
		return new Edge(from, to, factory.createPolyline(from.getPoint(), to.getPoint()));
	}

	public static Edge createDirectOffRoadEdge(final Node from, final Node to){
		final GeometryFactory factory = from.getPoint().getFactory();
		final Edge edge = new Edge(from, to, factory.createPolyline(from.getPoint(), to.getPoint()));
		edge.offRoad = true;
		return edge;
	}

	public static Edge createDirectEdge(final Node from, final Node to, final Polyline path){
		return new Edge(from, to, path);
	}

	private Edge(final Node from, final Node to, final Polyline path){
		if(from == null)
			throw new IllegalArgumentException("`from` node cannot be null");
		if(to == null)
			throw new IllegalArgumentException("`to` node cannot be null");
		if(path == null || path.isEmpty())
			throw new IllegalArgumentException("`path` node cannot be null or empty");

		id = Objects.requireNonNullElse(from.getID(), "<null>")
			+ "-"
			+ Objects.requireNonNullElse(to.getID(), "<null>");
		this.from = from;
		this.to = to;
		this.path = path;
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

	public Polyline getPath(){
		return path;
	}

	public Edge reversed(){
		return createDirectEdge(to, from, path.reverse());
	}

	public boolean isOffRoad(){
		return offRoad;
	}

	public Edge getFromProjected(){
		return fromProjected;
	}

	public Edge withFromProjected(final Edge fromProjected){
		if(fromProjected == null)
			throw new IllegalArgumentException("From-edge cannot be null");

		this.fromProjected = fromProjected;

		return this;
	}

	public Edge getToProjected(){
		return toProjected;
	}

	public Edge withToProjected(final Edge toProjected){
		if(toProjected == null)
			throw new IllegalArgumentException("To-edge cannot be null");

		this.toProjected = toProjected;

		return this;
	}

	@Override
	public boolean equals(final Object obj){
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;

		final Edge other = (Edge)obj;
		return (Objects.equals(from, other.from)
			&& Objects.equals(to, other.to)
			&& Objects.equals(path, other.path));
	}

	@Override
	public int hashCode(){
		return Objects.hash(from, to, path);
	}

	@Override
	public String toString(){
		return "Edge{id = " + id + ", from = " + from + ", to = " + to
			+ (path.size() > 1? ", path = " + path: EMPTY_STRING)
			+ "}";
	}

}
