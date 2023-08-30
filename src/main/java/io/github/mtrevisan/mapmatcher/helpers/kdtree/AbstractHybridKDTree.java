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
package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import io.github.mtrevisan.mapmatcher.helpers.RegionTree;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.Collection;


public class AbstractHybridKDTree{

	public void insert(final RegionTree tree, final Region region){
		tree.insert(region);
	}

	public void insert(final RegionTree tree, final Region region, final Point point){
		final Collection<Region> regions = query(tree, region);

		if(!regions.isEmpty())
			for(final Region queriedRegion : regions)
				if(queriedRegion.isBoundary()){
					final KDTree terminalTree = KDTree.ofEmpty(point.getDimensions());
					final KDNode parentNode = (KDNode)queriedRegion.getNode();
					terminalTree.insert(parentNode, point);
					return;
				}

		//TODO extends Region, parké kusita 'l fà senso (?)
		//	ge xé un poblèma su RegionQuadTree.split(), kuando ke se krea un Region...
		region.setBoundary();
		region.setNode(new KDNode(point));
		tree.insert(region);
	}


	public Collection<Region> query(final RegionTree tree, final Region region){
		return tree.query(region);
	}

	public boolean contains(final RegionTree tree, final Region region, final Point point){
		final Collection<Region> regions = query(tree, region);

		if(!regions.isEmpty())
			for(final Region queriedRegion : regions)
				if(queriedRegion.isBoundary()){
					final KDTree terminalTree = KDTree.ofEmpty(point.getDimensions());
					final KDNode kdNode = (KDNode)queriedRegion.getNode();
					if(terminalTree.contains(kdNode, point))
						return true;
				}
		return false;
	}

}
