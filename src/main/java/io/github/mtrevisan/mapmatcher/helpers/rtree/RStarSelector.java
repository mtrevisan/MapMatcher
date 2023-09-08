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

import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;


class RStarSelector implements NodeSelector{

	private static final MinimalAreaIncreaseSelector baseSelector = MinimalAreaIncreaseSelector.create();


	static RStarSelector create(){
		return new RStarSelector();
	}


	private RStarSelector(){}


	/**
	 * Find leaf that uses minimal overlap area selector for leaf nodes and minimal area increase selector for non-leaf nodes.
	 */
	@Override
	public RNode select(final RNode parent, final Region region){
		if(!parent.leaf)
			return baseSelector.select(parent, region);


		//choose child which region enlarges the less with current record's region
		double minOverlapArea = Double.POSITIVE_INFINITY;
		double minAreaIncrement = Double.POSITIVE_INFINITY;
		RNode minAreaNode = parent;
		for(final RNode child : parent.children){
			//first order by minimum intersecting area
			final double overlapArea = region.intersectingArea(child.region);
			if(overlapArea < minOverlapArea){
				minOverlapArea = overlapArea;
				minAreaNode = child;
			}
			else if(overlapArea == minOverlapArea){
				//then order by minimum non-intersecting area
				final double nonIntersectingArea = region.nonIntersectingArea(child.region);
				if(nonIntersectingArea < minAreaIncrement){
					minAreaIncrement = nonIntersectingArea;
					minAreaNode = child;
				}
				else if(nonIntersectingArea == minAreaIncrement){
					//then order by smallest area
					final double childArea = child.region.euclideanArea();
					final double nextArea = minAreaNode.region.euclideanArea();
					if(childArea < nextArea)
						minAreaNode = child;
				}
			}
		}
		return minAreaNode;
	}

}
