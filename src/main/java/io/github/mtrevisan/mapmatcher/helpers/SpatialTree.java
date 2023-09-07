/**
 * Copyright (c) 2023 Mauro Trevisan
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
package io.github.mtrevisan.mapmatcher.helpers;

import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.Collection;


public interface SpatialTree{

	boolean isEmpty();


	/**
	 * Add the point to the tree (if it is not already in the tree).
	 *
	 * @param point	The point to add.
	 */
	void insert(final Point point);

	/**
	 * Assess the given point is inside the tree.
	 *
	 * @param point	The point to check.
	 * @return	Whether the point is contained into the tree.
	 */
	boolean contains(final Point point);

	/**
	 * Return the nearest neighbor to the given point.
	 *
	 * @param point	The point to query.
	 * @return	The point that is the nearest neighbor.
	 */
	Point nearestNeighbor(final Point point);

	/**
	 * Query the tree returning all the points that lies inside the given rectangle.
	 *
	 * @param rangeMin	Minimum coordinate of the rectangle.
	 * @param rangeMax	Maximum coordinate of the rectangle.
	 * @return	The list of points that lies inside the given rectangle.
	 */
	Collection<Point> query(final Point rangeMin, final Point rangeMax);

}
