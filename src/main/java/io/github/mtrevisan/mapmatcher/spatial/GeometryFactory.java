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
package io.github.mtrevisan.mapmatcher.spatial;

import io.github.mtrevisan.mapmatcher.spatial.topologies.TopologyCalculator;


/**
 * Supplies a set of utility methods for building geometry objects from lists of coordinates.
 */
public class GeometryFactory{

	protected final TopologyCalculator topologyCalculator;


	public GeometryFactory(final TopologyCalculator topologyCalculator){
		this.topologyCalculator = topologyCalculator;
	}


	/**
	 * Constructs a point at <code>(x, y)</code>.
	 *
	 * @param x	The x-ordinate.
	 * @param y	The y-ordinate.
	 */
	public Point createPoint(final double x, final double y){
		return Point.of(this, x, y);
	}

	/**
	 * Creates a point using the given coordinate.
	 *
	 * @param point	A coordinate.
	 * @return	The created point.
	 */
	public Point createPoint(final Point point){
		if(point == null)
			throw new IllegalArgumentException("Point cannot be empty");

		return Point.of(this, point);
	}

	/**
	 * Creates a point using the given coordinate in <a href="https://it.wikipedia.org/wiki/Well-Known_Text">WKT</a> format.
	 *
	 * @param wkt	A string representation in <a href="https://it.wikipedia.org/wiki/Well-Known_Text">WKT</a> format of a point.
	 * @return	The created point.
	 */
	public Point createPoint(final String wkt){
		if(wkt == null || wkt.isBlank())
			throw new IllegalArgumentException("WKT string cannot be null or empty");

		return Point.of(this, wkt);
	}


	/**
	 * Creates a polyline using the given points.
	 *
	 * @param points	An array without <code>null</code> elements.
	 * @return	The created polyline.
	 */
	public Polyline createPolyline(final Point... points){
		if(points == null)
			throw new IllegalArgumentException("Points cannot be empty");

		return Polyline.of(this, points);
	}

	/**
	 * Creates a polyline using the given points in <a href="https://it.wikipedia.org/wiki/Well-Known_Text">WKT</a> format.
	 *
	 * @param wkt	A string representation in <a href="https://it.wikipedia.org/wiki/Well-Known_Text">WKT</a> format of a polyline.
	 * @return	The created polyline.
	 */
	public Polyline createPolyline(final String wkt){
		if(wkt == null)
			throw new IllegalArgumentException("WKT string cannot be null");

		return Polyline.of(this, wkt);
	}

	/**
	 * Creates an empty polyline.
	 */
	public Polyline createEmptyPolyline(){
		return Polyline.of(this);
	}

}
