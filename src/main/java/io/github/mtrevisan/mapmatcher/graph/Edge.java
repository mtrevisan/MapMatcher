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

import java.util.Objects;


public class Edge{

	private final Vertex from;
	private final Vertex to;
	private final int maxSpeed;


	public Edge(final Vertex from, final Vertex to, final int maxSpeed){
		this.from = from;
		this.to = to;
		this.maxSpeed = maxSpeed;
	}

	public Vertex getFrom(){
		return from;
	}

	public Vertex getTo(){
		return to;
	}

	public int getMaxSpeed(){
		return maxSpeed;
	}

	public Edge reversed(){
		return new Edge(to, from, maxSpeed);
	}

	@Override
	public boolean equals(final Object o){
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;

		final Edge edge = (Edge)o;
		return (Objects.equals(from, edge.from) && Objects.equals(to, edge.to) && Objects.equals(maxSpeed, edge.maxSpeed));
	}

	@Override
	public int hashCode(){
		return Objects.hash(from, to, maxSpeed);
	}

}
