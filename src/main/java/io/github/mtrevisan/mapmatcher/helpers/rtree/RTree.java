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

import io.github.mtrevisan.mapmatcher.helpers.QuickSelect;
import io.github.mtrevisan.mapmatcher.helpers.RegionTree;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;


/**
 * @see <a href="https://en.wikipedia.org/wiki/R-tree">R-tree</a>
 * @see <a href="https://github.com/davidmoten/rtree2">RTree2</a>
 * @see <a href="https://en.wikipedia.org/wiki/R*-tree">R*-tree</a>
 * @see <a href="https://en.wikipedia.org/wiki/Hilbert_R-tree">Hilbert R-tree</a>
 * @see <a href="https://github.com/TheDeathFar/HilbertTree">HilberTree</a>
 * @see <a href="https://en.wikipedia.org/wiki/Hilbert_R-tree">Hilbert R-tree</a>
 * @see <a href="https://github.com/locationtech/jts/blob/master/modules/core/src/main/java/org/locationtech/jts/index/hprtree/HPRtree.java">HPRtree.java</a>
 * @see <a href="https://www.cs.cmu.edu/~christos/PUBLICATIONS.OLDER/vldb94.pdf">Hilbert R-tree: An improved R-tree using fractals</a>
 * @see <a href="https://web.cs.swarthmore.edu/~adanner/cs97/s08/pdf/prtreesigmod04.pdf">The Priority R-Tree: A Practically Efficient and Worst-Case Optimal R-Tree</a>
 * @see <a href="https://cdn.dal.ca/content/dam/dalhousie/pdf/faculty/computerscience/technical-reports/CS-2006-07.pdf">Compact Hilbert Indices</a>
 * @see <a href="https://github.com/Ya-hwon/hprtree/tree/master">hprtree</a>
 */
public class RTree implements RegionTree<RTreeOptions>{

	private static final int HILBERT_LEVEL = 12;


	private RNode root;

	private final RTreeOptions options;
	private final NodeSplitter splitter;
	private final NodeSelector selector;


	public static RTree create(final RTreeOptions options){
		return new RTree(options, LinearSplitter.create(options), MinimalAreaIncreaseSelector.create());
	}

	public static RTree createSTR(final List<Region> regions, final RTreeOptions options){
		final RTree tree = new RTree(options, LinearSplitter.create(options), MinimalAreaIncreaseSelector.create());

		final Comparator<Region>[] comparators = createSTRComparators();

		tree.buildSortTileRecursive(regions, comparators);

		return tree;
	}

	public static RTree createRStar(final List<Region> regions, final RTreeOptions options){
		final RTree tree = new RTree(options, RStarSplitter.create(options), RStarSelector.create());

		final Comparator<Region>[] comparators = createSTRComparators();

		tree.buildSortTileRecursive(regions, comparators);

		return tree;
	}

	private static Comparator<Region>[] createSTRComparators(){
		final int dimensions = 2;
		@SuppressWarnings("unchecked")
		final Comparator<Region>[] comparators = (Comparator<Region>[])Array.newInstance(Comparator.class, dimensions);
		for(int i = 0; i < dimensions; i ++)
			comparators[i] = new MidComparator(i);
		return comparators;
	}

	private static final class MidComparator implements Comparator<Region>{
		private final int axis;

		public MidComparator(final int axis){
			this.axis = axis;
		}

		@Override
		public int compare(final Region region1, final Region region2){
			return Double.compare(midAxis(region1), midAxis(region2));
		}

		private double midAxis(final Region region){
			return (axis == 0? region.getMidX(): region.getMidY());
		}
	}

	public static RTree createHilbert(final List<Region> regions, final RTreeOptions options){
		final RTree tree = new RTree(options, LinearSplitter.create(options), MinimalAreaIncreaseSelector.create());

		//compute total extent of the tree:
		final Region rootRegion = Region.ofEmpty();
		final int size = regions.size();
		for(int i = 0; i < size; i ++){
			final Region region = regions.get(i);

			rootRegion.expandToInclude(region);
		}

		final HilbertEncoder encoder = new HilbertEncoder(HILBERT_LEVEL, rootRegion);
		final Comparator<Region> comparator = (region1, region2) -> {
			final int hilbertCode1 = encoder.encode(region1);
			final int hilbertCode2 = encoder.encode(region2);
			return Integer.compare(hilbertCode1, hilbertCode2);
		};

		tree.buildHilbert(regions, comparator);

		return tree;
	}


	private RTree(final RTreeOptions options, final NodeSplitter splitter, final NodeSelector selector){
		this.options = options;
		this.splitter = splitter;
		this.selector = selector;
	}


	/**
	 * Bulk load an R-tree using the Sort-Tile-Recursive (STR) algorithm.
	 *
	 * @param regions	The list of regions to be added.
	 *
	 * @see <a href="http://ieeexplore.ieee.org/abstract/document/582015/">STR: a simple and efficient algorithm for R-tree packing</a>
	 */
	private void buildSortTileRecursive(final List<Region> regions, final Comparator<Region>[] comparators){
		if(regions.isEmpty())
			return;

		//sort the regions based on the mid-coordinates of the given axis
		final List<Region> sortedRegions = new ArrayList<>(regions);
		sortedRegions.sort(comparators[0]);

		final Deque<BuildItem> stack = new ArrayDeque<>();
		stack.push(new BuildItem(sortedRegions));
		while(!stack.isEmpty()){
			final BuildItem item = stack.pop();

			buildNode(item, comparators[1], stack);
		}

		//update the parent region for all nodes
		if(!isEmpty())
			updateRegions();
	}

	private void buildNode(final BuildItem item, final Comparator<Region> comparator, final Deque<BuildItem> stack){
		final List<Region> currentRegions = item.regions;
		final int begin = item.begin;
		final int end = item.end;
		final RNode parentNode = item.parent;

		//calculate the midpoint
		final int middle = begin + ((end - begin) >> 1);
		final Region median = QuickSelect.select(currentRegions, begin, end, middle, comparator);

		//create a new node with the region corresponding to the midpoint
		final RNode node = (begin == end
			? RNode.createLeaf(median)
			: RNode.createInternal(median));

		//add the node to the root if it is the first created node
		if(isEmpty())
			root = node;
		else
			//add the node as a child of the parent
			parentNode.addChild(node);

		//add the left and right subtrees to the stack
		if(begin < middle)
			stack.push(new BuildItem(currentRegions, begin, middle - 1, node));
		if(middle < end)
			stack.push(new BuildItem(currentRegions, middle + 1, end, node));
	}

	private static final class BuildItem{
		private final List<Region> regions;
		private final int begin;
		private final int end;
		private final RNode parent;

		BuildItem(final List<Region> regions){
			this(regions, 0, regions.size() - 1, null);
		}

		BuildItem(final List<Region> regions, final int begin, final int end, final RNode parent){
			this.regions = regions;
			this.begin = begin;
			this.end = end;
			this.parent = parent;
		}
	}

	private void updateRegions(){
		final Deque<RNode> stack = new ArrayDeque<>();
		stack.push(root);
		while(!stack.isEmpty()){
			final RNode current = stack.pop();

			if(current.leaf)
				current.tightenRegion();
			else
				stack.addAll(current.children);
		}
	}


	/**
	 * Bulk load an R-tree using Hilbert packing algorithm.
	 *
	 * @param regions	The list of regions to be added.
	 */
	private void buildHilbert(final List<Region> regions, final Comparator<Region> comparator){
		if(regions.isEmpty())
			return;

		//sort the regions based on the hilbert code
		final List<Region> sortedRegions = new ArrayList<>(regions);
		sortedRegions.sort(comparator);


		final Deque<BuildItem> stack = new ArrayDeque<>();
		stack.push(new BuildItem(sortedRegions));
		while(!stack.isEmpty()){
			final BuildItem item = stack.pop();

			buildNode(item, comparator, stack);
		}

		//update the parent region for all nodes
		if(!isEmpty())
			updateRegions();
	}


	@Override
	public boolean isEmpty(){
		return (root == null);
	}


	@Override
	public void insert(final Region region){
		final RNode newNode = RNode.createLeaf(region);
		if(isEmpty())
			root = newNode;
		else{
			final RNode parent = selector.select(root, newNode.region);
			parent.addChild(newNode);

			if(parent.children.size() <= options.maxChildren)
				adjustRegionsUpToRoot(parent);
			else
				splitAndAdjust(parent);
		}
	}

	private void adjustRegionsUpToRoot(RNode node){
		while(node != root && node.parent != null){
			node.tightenRegion();

			node = node.parent;
		}

		if(node == root)
			root.tightenRegion();
	}

	private void splitAndAdjust(RNode parent){
		while(true){
			final RNode[] splits = splitter.splitNode(parent);
			final RNode currentNode = splits[0];
			final RNode newNode = splits[1];

			if(currentNode == root){
				//assign new root
				root = RNode.createInternal(Region.ofEmpty());
				root.addChild(currentNode);
				root.addChild(newNode);
				root.tightenRegion();

				break;
			}

			currentNode.tightenRegion();
			newNode.tightenRegion();
			if(currentNode.parent.children.size() <= options.maxChildren)
				break;

			parent = currentNode.parent;
		}
	}


	@Override
	public boolean delete(final Region region){
		boolean deleted = false;
		final RNode leaf = findLeaf(region);
		if(leaf != null){
			final int size = leaf.children.size();
			for(int i = 0; i < size; i ++){
				final RNode node = leaf.children.get(i);

				if(node.region.equals(region)){
					condenseTree(leaf);

					//reassign root if it has only one child
					if(root.children.size() == 1)
						root = RNode.createLeaf(root.children.get(0).region);

					deleted = true;
					break;
				}
			}
		}
		return deleted;
	}

	private RNode findLeaf(final Region region){
		if(root.region.intersects(region)){
			final Deque<RNode> stack = new ArrayDeque<>();
			stack.push(root);
			while(!stack.isEmpty()){
				final RNode current = stack.pop();

				if(current.leaf){
					final int size = current.children.size();
					for(int i = 0; i < size; i ++){
						final RNode child = current.children.get(i);

						if(child.region.intersects(region))
							return current;
					}
				}
				else{
					final int size = current.children.size();
					for(int i = 0; i < size; i ++){
						final RNode child = current.children.get(i);

						if(child.region.intersects(region))
							stack.push(child);
					}
				}
			}
		}
		return null;
	}

	private void condenseTree(RNode remove){
		final List<RNode> removedNodes = new ArrayList<>();
		while(remove != root){
			if(remove.children.size() >= options.minChildren)
				remove.tightenRegion();
			//node has underflow of children
			else if(remove.leaf){
				removedNodes.addAll(remove.children);
				remove.parent.children.remove(remove);
			}
			else{
				final LinkedList<RNode> toVisit = new LinkedList<>(remove.children);
				while(!toVisit.isEmpty()){
					final RNode node = toVisit.pop();

					if(node.leaf)
						removedNodes.addAll(node.children);
					else
						toVisit.addAll(node.children);
				}

				remove.parent.children.remove(remove);
			}

			final RNode oldRemove = remove;
			remove = remove.parent;
			oldRemove.parent = null;
		}

		//reinsert temporarily deleted nodes
		final int size = removedNodes.size();
		for(int i = 0; i < size; i ++){
			final RNode node = removedNodes.get(i);

			insert(node.region);
		}
	}


	@Override
	public boolean intersects(final Region region){
		return (findLeaf(region) != null);
	}

	@Override
	public boolean contains(final Region region){
		final Deque<RNode> stack = new ArrayDeque<>();
		stack.push(root);
		while(!stack.isEmpty()){
			final RNode current = stack.pop();

			if(current.leaf){
				final int size = current.children.size();
				for(int i = 0; i < size; i ++){
					final RNode child = current.children.get(i);

					if(child.region.contains(region))
						return true;
				}
			}
			else{
				final int size = current.children.size();
				for(int i = 0; i < size; i ++){
					final RNode child = current.children.get(i);

					if(child.region.contains(region))
						stack.push(child);
				}
			}
		}
		return false;
	}

	@Override
	public List<Region> query(final Region region){
		final List<Region> results = new ArrayList<>();
		final Deque<RNode> stack = new ArrayDeque<>();
		if(region.intersects(root.region))
			stack.push(root);
		while(!stack.isEmpty()){
			final RNode current = stack.pop();

			final int size = current.children.size();
			if(current.leaf){
				for(int i = 0; i < size; i ++){
					final RNode child = current.children.get(i);

					if(region.intersects(child.region))
						results.add(child.region);
				}
			}
			else{
				for(int i = 0; i < size; i ++){
					final RNode child = current.children.get(i);

					if(region.intersects(child.region))
						stack.push(child);
				}
			}
		}
		return results;
	}

}
