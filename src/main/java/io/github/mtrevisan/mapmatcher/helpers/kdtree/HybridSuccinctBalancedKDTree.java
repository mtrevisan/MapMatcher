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
import io.github.mtrevisan.mapmatcher.helpers.quadtree.TreeOptions;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * @see <a href="https://www.researchgate.net/publication/339433736_A_Proposed_Hybrid_Spatial_Data_Structure_based_on_KD_Tree_and_Quad_Tree">A Proposed Hybrid Spatial Data Structure based onKD Tree and Quad Tree</a>
 * @see <a href="https://dl.acm.org/doi/pdf/10.1145/318898.318925">Multikey retrieval from K-d trees and quad-trees</a>
 * @see <a href="https://pdfs.semanticscholar.org/abb0/fdeebccbdc1d3d57933751e95434136fb16a.pdf">A Hybrid Spatial Indexing Structure of Massive Point Cloud Based on Octree and 3D R*-Tree</a>
 */
public class HybridSuccinctBalancedKDTree<O extends TreeOptions>{

	private final RegionTree<O> tree;


	public static <O extends TreeOptions> HybridSuccinctBalancedKDTree<O> create(final RegionTree<O> tree){
		return new HybridSuccinctBalancedKDTree<>(tree);
	}


	private HybridSuccinctBalancedKDTree(final RegionTree<O> tree){
		this.tree = tree;
	}


	public void insert(final Region region){
		tree.insert(region);
	}

	public void insert(final Map<Region, SuccinctBalancedKDTree> nodes, final Region region, final Point point) throws MaximumTreeDepthReachedException{
		final List<Region> regions = query(region);
		for(int i = 0; i < regions.size(); i ++){
			final Region queriedRegion = regions.get(i);

			if(queriedRegion.isBoundary()){
				final SuccinctBalancedKDTree tree = nodes.get(queriedRegion);
				final Collection<Point> currentPoints = tree.getData();
				final List<Point> newPoints = new ArrayList<>(currentPoints.size() + 1);
				newPoints.addAll(currentPoints);
				newPoints.add(point);
				final SuccinctBalancedKDTree newKDTree = SuccinctBalancedKDTree.ofPoints(newPoints);
				nodes.put(queriedRegion, newKDTree);
				return;
			}
		}

		//region is outside the tree
		region.setBoundary();
		nodes.put(region, SuccinctBalancedKDTree.ofPoints(Collections.singletonList(point)));
		tree.insert(region);
	}


	public List<Region> query(final Region region){
		return tree.query(region);
	}

	public boolean contains(final Map<Region, SuccinctBalancedKDTree> nodes, final Region region, final Point point){
		final List<Region> regions = query(region);
		for(int i = 0; i < regions.size(); i ++){
			final Region queriedRegion = regions.get(i);

			if(queriedRegion.isBoundary()){
				final SuccinctBalancedKDTree tree = nodes.get(queriedRegion);
				if(tree.contains(point))
					return true;
			}
		}
		return false;
	}


	public Point nearestNeighbor(final Map<Region, SuccinctBalancedKDTree> nodes, final Region region, final Point point){
		final List<Region> regions = query(region);
		for(int i = 0; i < regions.size(); i ++){
			final Region queriedRegion = regions.get(i);

			if(queriedRegion.isBoundary()){
				final SuccinctBalancedKDTree tree = nodes.get(queriedRegion);
				final Point nearestNeighbor = tree.nearestNeighbor(point);
				if(nearestNeighbor != null)
					return nearestNeighbor;
			}
		}
		return null;
	}

}