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

import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;

import java.util.Collection;


/**
 * @param <R>	The type of {@link Region} to use.
 */
public interface RegionTree<R extends Region>{

	boolean isEmpty();


	/**
	 * Add the region to the tree (if it is not already in the tree).
	 *
	 * @param region	The region to add.
	 */
	void insert(R region);

	/**
	 * Delete the region from the tree.
	 *
	 * @param region	The region to delete.
	 * @return	Whether the region was deleted.
	 */
	boolean delete(R region);

	/**
	 * Assess the given region is inside the tree.
	 *
	 * @param region	The region to check.
	 * @return	Whether the region is contained into the tree.
	 */
	boolean contains(R region);

	/**
	 * Query the tree returning all the regions that lies inside the given region.
	 *
	 * @param region	The region used to filter.
	 * @return	The list of regions contained into the given one.
	 */
	Collection<R> query(R region);

}
