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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.Objects;


public class Edge{

	private static final PrecisionModel PRECISION_MODEL = new PrecisionModel(PrecisionModel.FLOATING);
	private static final int SRID_WGS84 = 4326;
	private static final GeometryFactory FACTORY = new GeometryFactory(PRECISION_MODEL, SRID_WGS84);


	private final Vertex from;
	private final Vertex to;
	private LineString geometry;
	private double weight;


	public Edge(final Vertex from, final Vertex to, final double weight){
		if(from == null)
			throw new IllegalArgumentException("`from` vertex cannot be null");
		if(to == null)
			throw new IllegalArgumentException("`to` vertex cannot be null");

		this.from = from;
		this.to = to;
		this.weight = weight;
	}

	public Edge(final Vertex from, final Vertex to, final LineString geometry, final double weight){
		if(from == null)
			throw new IllegalArgumentException("`from` vertex cannot be null");
		if(to == null)
			throw new IllegalArgumentException("`to` vertex cannot be null");
		if(geometry == null)
			throw new IllegalArgumentException("geometry cannot be null");

		this.from = from;
		this.to = to;
		this.geometry = geometry;
		this.weight = weight;
	}

	public Vertex getFrom(){
		return from;
	}

	public Vertex getTo(){
		return to;
	}

	public LineString getLineString(){
		if(geometry == null)
			geometry = FACTORY.createLineString(new Coordinate[]{from.getCoordinate(), to.getCoordinate()});
		return geometry;
	}

	public double getWeight(){
		return weight;
	}

	public void setWeight(final double weight){
		this.weight = weight;
	}

	public Edge reversed(){
		return (geometry != null
			? new Edge(to, from, geometry.reverse(), weight)
			: new Edge(to, from, weight)
		);
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

}
