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
package io.github.mtrevisan.mapmatcher.helpers.rtree;

import io.github.mtrevisan.mapmatcher.helpers.quadtree.TreeOptions;


public class RTreeOptions implements TreeOptions{

	/**
	 * According to <a href="https://infolab.usc.edu/csci599/Fall2001/paper/rstar-tree.pdf">The R*-tree: an efficient and robust access method for points and rectangles</a>,
	 * the best filling ratio is 0.4.
	 */
	public static final double DEFAULT_FILLING_FACTOR = 0.4;


	double fillingFactor = DEFAULT_FILLING_FACTOR;
	int minChildren = 1;
	int maxChildren = 4;


	public static RTreeOptions create(){
		return new RTreeOptions();
	}


	private RTreeOptions(){}


	public RTreeOptions setFillingFactor(double fillingFactor){
		if(fillingFactor <= 0. || fillingFactor > 1.)
			throw new IllegalArgumentException("Filling factor must be greater than zero and less or equal to one");

		this.fillingFactor = fillingFactor;

		return this;
	}

	/**
	 * Default is {@link #withMaxChildren(int)} times {@link #DEFAULT_FILLING_FACTOR}.
	 *
	 * @param minChildren	The minimum number of children.
	 * @return	This instance.
	 */
	public RTreeOptions withMinChildren(final int minChildren){
		if(minChildren < 1)
			throw new IllegalArgumentException("Minimum number of children for this node must be greater than zero");

		this.minChildren = minChildren;

		return this;
	}

	public RTreeOptions withMaxChildren(final int maxChildren){
		if(maxChildren < 1)
			throw new IllegalArgumentException("Maximum number of children for this node must be greater than zero");

		this.maxChildren = maxChildren;

		return this;
	}

}
