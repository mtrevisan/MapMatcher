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

import io.github.mtrevisan.mapmatcher.helpers.RegionTree;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * @see <a href="https://en.wikipedia.org/wiki/R-tree">R-tree</a>
 * @see <a href="https://en.wikipedia.org/wiki/Hilbert_R-tree">Hilbert R-tree</a>
 * @see <a href="https://en.wikipedia.org/wiki/R*-tree">R*-tree</a>
 * @see <a href="https://github.com/TheDeathFar/HilbertTree">HilberTree</a>
 * @see <a href="https://github.com/davidmoten/rtree2">RTree2</a>
 */
public class RTree implements RegionTree<RTreeOptions>{

	private RNode root;

	private final RTreeOptions options;
	private final NodeSplitter splitter;
	private final NodeSelector selector;


	public static RTree create(final RTreeOptions options){
		return new RTree(options, LinearSplitter.create(options), MinimalAreaIncreaseSelector.create());
	}

	public static RTree create(final List<Region> regions, final RTreeOptions options){
		return new RTree(regions, options, LinearSplitter.create(options), MinimalAreaIncreaseSelector.create());
	}

	public static RTree createRStar(final RTreeOptions options){
		return new RTree(options, RStarSplitter.create(options), RStarSelector.create());
	}

	public static RTree createRStar(final List<Region> regions, final RTreeOptions options){
		return new RTree(regions, options, RStarSplitter.create(options), RStarSelector.create());
	}


	private RTree(final RTreeOptions options, final NodeSplitter splitter, final NodeSelector selector){
		this.options = options;
		this.splitter = splitter;
		this.selector = selector;
	}

	private RTree(final List<Region> regions, final RTreeOptions options, final NodeSplitter splitter, final NodeSelector selector){
		this(options, splitter, selector);

		build(regions);
	}

	/**
	 * Bulk load an R-tree using the Sort-Tile-Recursive (STR) algorithm.
	 *
	 * @param regions	The list of regions to be added.
	 *
	 * @see <a href="http://ieeexplore.ieee.org/abstract/document/582015/">STR: a simple and efficient algorithm for R-tree packing</a>
	 */
	private void build(final List<Region> regions){
		List<RNode> objects = regions.stream()
			.map(RNode::createInternal)
			.toList();
		packingSTR(new ArrayList<>(objects), true);

		final Deque<BuildItem> stack = new ArrayDeque<>();
		root = RNode.createLeaf(Region.ofEmpty());
		stack.push(new BuildItem(root, new ArrayList<>(regions)));

		boolean isLeaf = true;
		final int capacity = (int)Math.round(options.maxChildren * options.fillingFactor);
		while(!stack.isEmpty()){
			final BuildItem item = stack.pop();
			final RNode parent = item.parent;
			final List<Region> internalRegions = item.regions;
			final int regionCount = internalRegions.size();

			final int nodeCount = (int)Math.ceil((double)regionCount / capacity);
			if(nodeCount == 0)
				continue;
			if(nodeCount == 1){
				final Region newRegion = minimumBoundingRegion(internalRegions, 0, regionCount);
				if(parent == root && root.leaf)
					//reassign root
					root = RNode.createInternal(root.region);

				parent.addChild(RNode.createLeaf(newRegion));

				continue;
			}

			final int nodePerSlice = (int)Math.ceil(Math.sqrt(nodeCount));
			final int sliceCapacity = nodePerSlice * capacity;
			final int sliceCount = (int)Math.ceil((double)regionCount / sliceCapacity);
			internalRegions.sort(CMP_X);

			final List<Region> newRegions = new ArrayList<>(nodeCount);
			int fromIndex = 0;
			for(int i = 0; i < sliceCount; i ++){
				final List<Region> slice = internalRegions.subList(fromIndex, Math.min(fromIndex + sliceCapacity, regionCount));
				slice.sort(CMP_Y);

				if(parent == root && root.leaf)
					//reassign root
					root = RNode.createInternal(root.region);

				if(isLeaf){
					for(int j = 0; j < slice.size(); j += capacity){
						final Region newRegion = minimumBoundingRegion(slice, j, Math.min(slice.size(), j + capacity));
						parent.addChild(RNode.createLeaf(newRegion));
					}
				}
				else{
					for(int j = 0; j < slice.size(); j += capacity){
						final Region newRegion = minimumBoundingRegion(slice, j, Math.min(slice.size(), j + capacity));
						final RNode newParent = RNode.createInternal(newRegion);
						newRegions.add(newRegion);
						parent.addChild(newParent);

						stack.push(new BuildItem(newParent, newRegions));
					}
				}

				fromIndex += sliceCapacity;
			}

			isLeaf = false;
		}
	}

	private RTree packingSTR(List<RNode> objects, boolean isLeaf){
		int capacity = (int)Math.round(options.maxChildren * options.fillingFactor);
		int nodeCount = (int)Math.ceil((double)objects.size() / capacity);

		if(nodeCount == 0)
			return create(options);
		if(nodeCount == 1){
			RNode root;
			if(isLeaf)
				root = RNode.createLeaf(minimumBoundingRegion2(objects, 0, objects.size()));
			else
				root = RNode.createInternal(minimumBoundingRegion2(objects, 0, objects.size()));
			RTree tree = RTree.create(options);
			tree.root = root;
			return tree;
		}

		int nodePerSlice = (int)Math.ceil(Math.sqrt(nodeCount));
		int sliceCapacity = nodePerSlice * capacity;
		int sliceCount = (int)Math.ceil((double)objects.size() / sliceCapacity);
		objects.sort(new MidComparator2(0));

		List<RNode> nodes = new ArrayList<>(nodeCount);
		for(int s = 0; s < sliceCount; s++){
			List<RNode> slice = objects.subList(s * sliceCapacity, Math.min((s + 1) * sliceCapacity, objects.size()));
			slice.sort(new MidComparator2((short)1));

			for(int i = 0; i < slice.size(); i += capacity){
				if(isLeaf){
					List<RNode> entries = slice.subList(i, Math.min(slice.size(), i + capacity));
					RNode leaf = RNode.createLeaf(minimumBoundingRegion2(entries, 0, entries.size()));
					nodes.add(leaf);
				}
				else{
					List<RNode> children = slice.subList(i, Math.min(slice.size(), i + capacity));
					RNode nonLeaf = RNode.createInternal(minimumBoundingRegion2(children, 0, children.size()));
					nodes.add(nonLeaf);
				}
			}
		}
		return packingSTR(nodes, false);
	}

	private static final class BuildItem{
		RNode parent;
		List<Region> regions;

		BuildItem(final RNode parent, final List<Region> regions){
			this.parent = parent;
			this.regions = regions;
		}
	}
	private static final MidComparator CMP_X = new MidComparator(0);
	private static final MidComparator CMP_Y = new MidComparator(1);
	private static final class MidComparator implements Comparator<Region>{
		private final int axis;

		public MidComparator(final int axis){
			this.axis = axis;
		}

		@Override
		public int compare(final Region region1, final Region region2){
			return Double.compare(mid(region1), mid(region2));
		}

		private double mid(final Region region){
			return (axis == 0? region.getMidX(): region.getMidY());
		}
	}
	/** Returns the minimum bounding region of a number of items. */
	private Region minimumBoundingRegion(final List<Region> regions, final int fromIndexInclusive, final int toIndexExclusive){
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for(int i = fromIndexInclusive; i < toIndexExclusive; i ++){
			final Region region = regions.get(i);
			if(region.getMinX() < minX)
				minX = region.getMinX();
			if(region.getMinY() < minY)
				minY = region.getMinY();
			if(region.getMaxX() > maxX)
				maxX = region.getMaxX();
			if(region.getMaxY() > maxY)
				maxY = region.getMaxY();
		}
		return Region.of(minX, minY, maxX, maxY);
	}
	private static final class MidComparator2 implements Comparator<RNode>{
		private final int axis;

		public MidComparator2(final int axis){
			this.axis = axis;
		}

		@Override
		public int compare(final RNode region1, final RNode region2){
			return Double.compare(mid(region1.region), mid(region2.region));
		}

		private double mid(final Region region){
			return (axis == 0? region.getMidX(): region.getMidY());
		}
	}
	private Region minimumBoundingRegion2(final List<RNode> nodes, final int fromIndexInclusive, final int toIndexExclusive){
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for(int i = fromIndexInclusive; i < toIndexExclusive; i ++){
			final Region region = nodes.get(i)
				.region;
			if(region.getMinX() < minX)
				minX = region.getMinX();
			if(region.getMinY() < minY)
				minY = region.getMinY();
			if(region.getMaxX() > maxX)
				maxX = region.getMaxX();
			if(region.getMaxY() > maxY)
				maxY = region.getMaxY();
		}
		return Region.of(minX, minY, maxX, maxY);
	}


	@Override
	public boolean isEmpty(){
		return (root == null);
	}


	@Override
	public void insert(final Region region, final RTreeOptions options){
		final RNode newNode = RNode.createLeaf(region);
		if(isEmpty())
			root = newNode;
		else{
			final RNode parent = selector.select(root, newNode.region);
			parent.addChild(newNode);

			if(parent.children.size() <= options.maxChildren)
				adjustRegionsUpToRoot(parent);
			else
				splitAndAdjust(parent, options);
		}
	}

	private void adjustRegionsUpToRoot(final RNode node){
		RNode currentNode = node;
		while(currentNode != null){
			tightenParentRegion(currentNode);

			currentNode = currentNode.parent;
		}
	}

	private void splitAndAdjust(RNode parent, final RTreeOptions options){
		while(true){
			RNode[] splits = splitter.splitNode(parent);
			RNode currentNode = splits[0];
			RNode newNode = splits[1];

			if(parent == root){
				//assign new root
				root = RNode.createInternal(Region.ofEmpty());
				root.addChild(currentNode);
				root.addChild(newNode);
				tightenParentRegion(root);

				break;
			}

			tightenParentRegion(currentNode);
			tightenParentRegion(newNode);
			if(currentNode.parent.children.size() <= options.maxChildren)
				break;

			parent = currentNode.parent;
		}
	}


	@Override
	public boolean delete(final Region region, final RTreeOptions options){
		boolean deleted = false;
		final RNode leaf = findLeaf(root, region);
		if(leaf != null){
			condenseTree(leaf, options);

			//reassign root if it has only one child
			if(root.children.size() == 1)
				root = RNode.createLeaf(root.children.get(0).region);

			deleted = true;
		}
		return deleted;
	}

	private static RNode findLeaf(final RNode parent, final Region region){
		final Deque<RNode> stack = new ArrayDeque<>();
		stack.push(parent);
		while(!stack.isEmpty()){
			final RNode currentNode = stack.pop();

			if(currentNode.leaf){
				for(final RNode child : currentNode.children)
					if(child.region.intersects(region))
						return currentNode;
			}
			else{
				for(final RNode child : currentNode.children)
					if(child.region.intersects(region))
						stack.push(child);
			}
		}
		return null;
	}

	private void condenseTree(RNode remove, final RTreeOptions options){
		final Set<RNode> removedNodes = new HashSet<>();
		while(remove != root){
			//node has underflow of children
			if(remove.children.size() < options.minChildren){
				if(remove.leaf)
					removedNodes.addAll(remove.children);
				else{
					final LinkedList<RNode> toVisit = new LinkedList<>(remove.children);
					while(!toVisit.isEmpty()){
						final RNode node = toVisit.pop();
						if(node.leaf)
							removedNodes.addAll(node.children);
						else
							toVisit.addAll(node.children);
					}
				}

				remove.parent.children.remove(remove);
			}
			else
				tightenParentRegion(remove);

			final RNode oldRemove = remove;
			remove = remove.parent;
			oldRemove.parent = null;
		}

		//reinsert temporarily deleted nodes
		for(final RNode node : removedNodes)
			insert(node.region, options);
	}

	private void tightenParentRegion(final RNode node){
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for(final RNode child : node.children){
			if(child.region.getMinX() < minX)
				minX = child.region.getMinX();
			if(child.region.getMinY() < minY)
				minY = child.region.getMinY();
			if(child.region.getMaxX() > maxX)
				maxX = child.region.getMaxX();
			if(child.region.getMaxY() > maxY)
				maxY = child.region.getMaxY();
		}

		node.parent.region = Region.of(minX, minY, maxX, maxY);
	}


	@Override
	public boolean intersects(final Region region){
		return (findLeaf(root, region) != null);
	}

	@Override
	public boolean contains(final Region region){
		final Deque<RNode> stack = new ArrayDeque<>();
		stack.push(root);
		while(!stack.isEmpty()){
			final RNode current = stack.pop();

			if(current.leaf){
				for(final RNode child : current.children)
					if(child.region.contains(region))
						return true;
			}
			else{
				for(final RNode child : current.children)
					if(child.region.contains(region))
						stack.push(child);
			}
		}
		return false;
	}

	@Override
	public Collection<Region> query(final Region region){
		final List<Region> results = new LinkedList<>();
		final Deque<RNode> stack = new ArrayDeque<>();
		stack.push(root);
		while(!stack.isEmpty()){
			final RNode current = stack.pop();
			if(current.leaf){
				for(final RNode e : current.children)
					if(region.intersects(e.region))
						results.add(e.region);
			}
			else{
				for(final RNode c : current.children)
					if(region.intersects(c.region))
						stack.push(c);
			}
		}
		return results;
	}

}
