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
package io.github.mtrevisan.mapmatcher.helpers.quadtree;


public class QuadTreeOptions{

	public static final int MAX_LEVELS_UNLIMITED = -1;


	/**
	 * The maximum number of regions for this node before splitting (coverage-based splitting if 1, density-based splitting if greater than
	 * 1).
	 */
	int maxRegionsPerNode = 10;
	/** The maximum number of levels. */
	int maxLevels = MAX_LEVELS_UNLIMITED;


	public static QuadTreeOptions withDefault(){
		return new QuadTreeOptions();
	}

	private QuadTreeOptions(){}


	public QuadTreeOptions withMaxRegionsPerNode(final int maxRegionsPerNode){
		if(maxRegionsPerNode < 1)
			throw new IllegalArgumentException("Maximum number of regions for this node must be greater than zero");

		this.maxRegionsPerNode = maxRegionsPerNode;

		return this;
	}

	public QuadTreeOptions withMaxLevels(final int maxLevels){
		this.maxLevels = (maxLevels < 0? MAX_LEVELS_UNLIMITED: maxLevels);

		return this;
	}

}
