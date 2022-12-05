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
package io.github.mtrevisan.mapmatcher.spatial;

import io.github.mtrevisan.mapmatcher.spatial.distances.DistanceCalculator;


//TODO
//https://github.com/locationtech/jts/blob/master/modules/core/src/main/java/org/locationtech/jts/geom/GeometryFactory.java
public class GeometryFactory{

	final DistanceCalculator distanceCalculator;


	public GeometryFactory(final DistanceCalculator distanceCalculator){
		this.distanceCalculator = distanceCalculator;
	}

	/**
	 * Constructs a <code>Coordinate</code> at (x, y).
	 *
	 * @param x	The x-ordinate.
	 * @param y	The y-ordinate.
	 */
	public Coordinate createPoint(final double x, final double y){
		//TODO
//		return Coordinate.of(distanceCalculator, x, y);
		return null;
	}

	public Coordinate createPoint(final Coordinate coordinate){
		//TODO
//		return Coordinate.of(distanceCalculator, coordinate);
		return null;
	}


	public Polyline createPolyline(final Coordinate... coordinates){
		//TODO
//		return Polyline.of(distanceCalculator, coordinates);
		return null;
	}

}
