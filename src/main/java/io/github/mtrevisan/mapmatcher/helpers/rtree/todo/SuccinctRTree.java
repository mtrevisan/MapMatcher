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
package io.github.mtrevisan.mapmatcher.helpers.rtree.todo;

import io.github.mtrevisan.mapmatcher.helpers.QuickSelect;
import io.github.mtrevisan.mapmatcher.helpers.RegionTree;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;
import io.github.mtrevisan.mapmatcher.helpers.rtree.HilbertEncoder;
import io.github.mtrevisan.mapmatcher.helpers.rtree.LinearSplitter;
import io.github.mtrevisan.mapmatcher.helpers.rtree.MinimalAreaIncreaseSelector;
import io.github.mtrevisan.mapmatcher.helpers.rtree.NodeSelector;
import io.github.mtrevisan.mapmatcher.helpers.rtree.NodeSplitter;
import io.github.mtrevisan.mapmatcher.helpers.rtree.RStarSelector;
import io.github.mtrevisan.mapmatcher.helpers.rtree.RStarSplitter;
import io.github.mtrevisan.mapmatcher.helpers.rtree.RTreeOptions;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;


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
public class SuccinctRTree implements RegionTree<RTreeOptions>{

	private static final int HILBERT_LEVEL = 12;


	//TODO succinct tree structure
	//https://sux.di.unimi.it/
	//https://github.com/vigna/Sux4J/blob/master/src/it/unimi/dsi/sux4j/bits/Select.java
	//
	//LOUDS representation? http://www.cs.cmu.edu/~huanche1/slides/FST.pdf
	//child(i) = select(S, rank(HC, i) + 1)
	//parent(i) = select(S, rank(S, i) - 1)
	//value(i) = i - rank(HC, i)
	//
	//https://dukespace.lib.duke.edu/dspace/bitstream/handle/10161/434/A.Gupta%20thesis%20revision.pdf?sequence=3
	//http://groups.di.unipi.it/~ottavian/files/phd_thesis.pdf
	//https://vigna.di.unimi.it/ftp/papers/Broadword.pdf
	//firstChild(i) = select0(rank1(i) + 1)
	//nextSibling(i) = i + 1
	//parent(i) = select1(rank0(i))
	//
	//https://www.researchgate.net/publication/221131620_Engineering_the_LOUDS_Succinct_Tree_Representation
	private final BitSet louds = new BitSet();
	//data in level-order traversal
	private Region[] data;

	private final RTreeOptions options;
	private final NodeSplitter splitter;
	private final NodeSelector selector;


	public static SuccinctRTree create(final RTreeOptions options){
		return new SuccinctRTree(options, LinearSplitter.create(options), MinimalAreaIncreaseSelector.create());
	}

	public static SuccinctRTree createSTR(final List<Region> regions, final RTreeOptions options){
		final SuccinctRTree tree = new SuccinctRTree(options, LinearSplitter.create(options), MinimalAreaIncreaseSelector.create());

		final Comparator<Region>[] comparators = createSTRComparators();

		tree.buildSortTileRecursive(regions, comparators);

		return tree;
	}

	public static SuccinctRTree createRStar(final List<Region> regions, final RTreeOptions options){
		final SuccinctRTree tree = new SuccinctRTree(options, RStarSplitter.create(options), RStarSelector.create());

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

	public static SuccinctRTree createHilbert(final List<Region> regions, final RTreeOptions options){
		final SuccinctRTree tree = new SuccinctRTree(options, LinearSplitter.create(options), MinimalAreaIncreaseSelector.create());

		//compute total extent of the tree:
		final Region rootRegion = Region.ofEmpty();
		for(final Region region : regions)
			rootRegion.expandToInclude(region);

		final HilbertEncoder encoder = new HilbertEncoder(HILBERT_LEVEL, rootRegion);
		final Comparator<Region> comparator = (region1, region2) -> {
			final int hilbertCode1 = encoder.encode(region1);
			final int hilbertCode2 = encoder.encode(region2);
			return Integer.compare(hilbertCode1, hilbertCode2);
		};

		tree.buildHilbert(regions, comparator);

		return tree;
	}


	private SuccinctRTree(final RTreeOptions options, final NodeSplitter splitter, final NodeSelector selector){
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
		final int parentNode = item.parent;

		//calculate the midpoint
		final int middle = begin + ((end - begin) >> 1);
		final Region median = QuickSelect.select(currentRegions, begin, end, middle, comparator);

		//add the node to the root if it is the first created node
		if(isEmpty())
			addChild(0, median);
		else
			//add the node as a child of the parent
			addChild(parentNode, median);

		//add the left and right subtrees to the stack
		if(begin < middle)
			stack.push(new BuildItem(currentRegions, begin, middle - 1, middle));
		if(middle < end)
			stack.push(new BuildItem(currentRegions, middle + 1, end, middle));
	}

	private static final class BuildItem{
		private final List<Region> regions;
		private final int begin;
		private final int end;
		private final int parent;

		BuildItem(final List<Region> regions){
			this(regions, 0, regions.size()  - 1, -1);
		}

		BuildItem(final List<Region> regions, final int begin, final int end, final int parent){
			this.regions = regions;
			this.begin = begin;
			this.end = end;
			this.parent = parent;
		}
	}

	private void updateRegions(){
		final Deque<Integer> stack = new ArrayDeque<>();
		stack.push(0);
		while(!stack.isEmpty()){
			final int current = stack.pop();

			if(isLeaf1(current))
				tightenRegion(current);
			else
				stack.addAll(children(current));
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
		return (louds.cardinality() == 0);
	}


	@Override
	public void insert(final Region region){
		if(isEmpty())
			addChild(0, region);
		else{
			final int parent = selector.select(0, region);
			addChild(parent, region);

			if(childCount1(parent) <= options.maxChildren)
				adjustRegionsUpToRoot(parent);
			else
				splitAndAdjust(parent);
		}
	}

	private void adjustRegionsUpToRoot(int node){
		while(node > 0 && parent1(node) >= 0){
			tightenRegion(node);

			node = parent1(node);
		}

		if(node == 0)
			tightenRegion(0);
	}

	private void splitAndAdjust(int parent){
		while(true){
			final int[] splits = splitter.splitNode(parent);
			final int currentNode = splits[0];
			final int newNode = splits[1];

			if(currentNode == 0){
				//assign new root
				setData(0, Region.ofEmpty());
				addChild(0, data(currentNode));
				addChild(0, data(newNode));
				tightenRegion(0);

				break;
			}

			tightenRegion(currentNode);
			tightenRegion(newNode);
			if(childCount1(parent1(currentNode)) <= options.maxChildren)
				break;

			parent = parent1(currentNode);
		}
	}


	@Override
	public boolean delete(final Region region){
		boolean deleted = false;
		final int leaf = findLeaf(region);
		if(leaf >= 0)
			for(final int node : children(leaf))
				if(data(node).equals(region)){
					condenseTree(leaf);

					//reassign root if it has only one child
					if(childCount1(0) == 1)
						setData(0, data(firstChild1(0)));

					deleted = true;
					break;
				}
		return deleted;
	}

	private int findLeaf(final Region region){
		if(data(0).intersects(region)){
			final Deque<Integer> stack = new ArrayDeque<>();
			stack.push(0);
			while(!stack.isEmpty()){
				final int currentNode = stack.pop();

				if(isLeaf1(currentNode)){
					for(final int child : children(currentNode))
						if(data(child).intersects(region))
							return currentNode;
				}
				else{
					for(final int child : children(currentNode))
						if(data(child).intersects(region))
							stack.push(child);
				}
			}
		}
		return -1;
	}

	private void condenseTree(int remove){
		final Set<Integer> removedNodes = new HashSet<>();
		while(remove != 0){
			if(childCount1(remove) >= options.minChildren)
				tightenRegion(remove);
			//node has underflow of children
			else if(isLeaf1(remove)){
				removedNodes.addAll(children(remove));
				removeChild(parent1(remove), remove);
			}
			else{
				final LinkedList<Integer> toVisit = new LinkedList<>(children(remove));
				while(!toVisit.isEmpty()){
					final int node = toVisit.pop();
					if(isLeaf1(node))
						removedNodes.addAll(children(node));
					else
						toVisit.addAll(children(node));
				}

				removeChild(parent1(remove), remove);
			}

			remove = parent1(remove);
		}

		//reinsert temporarily deleted nodes
		for(final int node : removedNodes)
			insert(data(node));
	}

	void tightenRegion(final int node){
		for(final int child : children(node))
			data(node).expandToInclude(data(child));
	}


	@Override
	public boolean intersects(final Region region){
		return (findLeaf(region) >= 0);
	}

	@Override
	public boolean contains(final Region region){
		final Deque<Integer> stack = new ArrayDeque<>();
		stack.push(0);
		while(!stack.isEmpty()){
			final int current = stack.pop();

			if(isLeaf1(current)){
				for(final int child : children(current))
					if(data(child).contains(region))
						return true;
			}
			else{
				for(final int child : children(current))
					if(data(child).contains(region))
						stack.push(child);
			}
		}
		return false;
	}

	@Override
	public Collection<Region> query(final Region region){
		final List<Region> results = new LinkedList<>();
		final Deque<Integer> stack = new ArrayDeque<>();
		stack.push(0);
		while(!stack.isEmpty()){
			final int current = stack.pop();
			if(isLeaf1(current)){
				for(final int child : children(current)){
					final Region childRegion = data(child);
					if(region.intersects(childRegion))
						results.add(childRegion);
				}
			}
			else{
				for(final int child : children(current))
					if(region.intersects(data(child)))
						stack.push(child);
			}
		}
		return results;
	}


	public int rank1(final int x){
		return louds.get(0, x + 1)
			.cardinality();
	}

	public int select1(final int x){
		int count = 0;
		int index = -1;
		while(count < x){
			index = louds.nextSetBit(index + 1);
			if(index < 0)
				return -1;

			count ++;
		}
		return index;
	}

	public int parent1(final int x){
		return select1(rank0(x));
	}

	//double-numbering LOUDS (Level-Order Unary Degree Sequence)
	//x	the level-order of the node
	//y	the position in LOUDS for the node
	public int[] parent1(final int x, final int y){
		final int r = y - x;
		final int newY = select1(r);
		final int newX = newY - r;
		return new int[]{newX, newY};
	}

	public int degree1(final int x){
		return lastChild1(x) - firstChild1(x) + 1;
	}

	//LOUDS++
	//if there are runs of 0s of length l1,l2,...,lz in the LBS, then the bit-string R0 is simply 0^(l1−1)10^(l2−1)1...0^(lz−1)1
	//if there are runs of 1s of length l1,l2,...,lz in the LBS, then the bit-string R1 is simply 1^(l1−1)01^(l2−1)0...1^(lz−1)0

	public boolean isLeaf1(final int x){
		return (firstChild1(x) < 0);
	}

	public int firstChild1(final int x){
		final int y = select0(rank1(x)) + 1;
		return (louds.get(y)? y: -1);
	}

	public int lastChild1(final int x){
		final int y = select0(rank1(x) + 1) - 1;
		return (louds.get(y)? y: -1);
	}

	public int childCount1(final int x){
		final int fc = firstChild1(x);
		final int lc = lastChild1(x);
		return (fc >= 0 && lc >= 0? lc - fc + 1: 0);
	}

	public int sibling1(final int x){
		return (louds.get(x + 1)? x + 1: -1);
	}

	//FIXME?
	public int nextSibling1(final int x){
		final int select1 = select1(rank1(x) + 1);
		final int select0 = select0(rank0(x) + 1);
		return Math.min(select1, select0);
	}

	private Collection<Integer> children(final int x){
		final int fc = firstChild1(x);
		final int lc = lastChild1(x);
		return IntStream.rangeClosed(fc, lc)
			.boxed()
			.toList();
	}

	private void addChild(final int x, final Region region){
		//TODO
		louds.set(x);
		data[x] = region;
	}

	private void removeChild(final int x, final int child){
		//TODO
	}

	public Region data(final int x){
		return data[rank1(x)];
	}

	public void setData(final int x, final Region region){
		data[rank1(x)] = region;
	}


	public  int rank0(final int k){
		return k + 1 - rank1(k);
	}

	public int select0(final int k){
		int count = 0;
		int index = 0;
		while(count < k){
			index = louds.nextClearBit(index + 1);
			if(index < 0)
				return -1;

			count ++;
		}
		return index;
	}

}
