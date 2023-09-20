/**
 * Copyright (c) 2022-2023 Mauro Trevisan
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
package io.github.mtrevisan.mapmatcher.helpers.hilbertrtree;

import io.github.mtrevisan.mapmatcher.helpers.RegionTree;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.TreeOptions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;


/**
 * A Hilbert-Packed R-Tree.
 * <p>
 * A static R-tree which is packed by using the Hilbert ordering of the tree items.
 * </p>
 * <p>
 * The tree is constructed by sorting the items by the Hilbert code of the midpoint of their region.<br/>
 * Then, a set of internal layers is created recursively as follows:
 * <ul>
 * 	<li>The items/nodes of the previous are partitioned into blocks of size <code>nodeCapacity</code>.
 * 	<li>For each block a layer node is created with range equal to the region of the items/nodes in the block.
 * </ul>
 * The internal layers are stored using an array to store the node bounds.<br/>
 * The link between a node and its children is stored implicitly in the indexes of the array.<br/>
 * For efficiency, the offsets to the layers within the node array are pre-computed and stored.
 * </p>
 * <p>
 * NOTE: Based on performance testing, the HPRtree is somewhat faster than the STR tree.<br/>
 * It should also be more memory-efficient, due to fewer object allocations.<br/>
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Hilbert_R-tree">Hilbert R-tree</a>
 * @see <a href="https://github.com/locationtech/jts/blob/master/modules/core/src/main/java/org/locationtech/jts/index/hprtree/HPRtree.java">HPRtree.java</a>
 * @see <a href="https://www.cs.cmu.edu/~christos/PUBLICATIONS.OLDER/vldb94.pdf">Hilbert R-tree: An improved R-tree using fractals</a>
 * @see <a href="https://web.cs.swarthmore.edu/~adanner/cs97/s08/pdf/prtreesigmod04.pdf">The Priority R-Tree: A Practically Efficient and Worst-Case Optimal R-Tree</a>
 * @see <a href="https://cdn.dal.ca/content/dam/dalhousie/pdf/faculty/computerscience/technical-reports/CS-2006-07.pdf">Compact Hilbert Indices</a>
 * @see <a href="https://github.com/Ya-hwon/hprtree/tree/master">hprtree</a>
 */
public class HilbertRTree implements RegionTree<TreeOptions>{

	private static final int ENV_SIZE = 4;
	private static final int HILBERT_LEVEL = 12;
	private static final int DEFAULT_NODE_CAPACITY = 16;

	private static final int NODE_BOUND_MIN_X_INDEX = 0;
	private static final int NODE_BOUND_MIN_Y_INDEX = 1;
	private static final int NODE_BOUND_MAX_X_INDEX = 2;
	private static final int NODE_BOUND_MAX_Y_INDEX = 3;

	private final List<Region> items = new ArrayList<>(0);
	private final int minChildren;
	private final Region totalExtent = Region.ofEmpty();
	private int[] layerStartIndex;
	private double[] nodeBounds;
	private boolean isBuilt;


	/**
	 * Creates a new tree with the default node capacity.
	 */
	public static HilbertRTree create(){
		return new HilbertRTree();
	}

	/**
	 * Creates a new tree with the given node capacity.
	 *
	 * @param nodeCapacity	The node capacity to use.
	 */
	public static HilbertRTree create(final int nodeCapacity){
		return new HilbertRTree(nodeCapacity);
	}


	private HilbertRTree(){
		this(DEFAULT_NODE_CAPACITY);
	}

	private HilbertRTree(final int minChildren){
		this.minChildren = minChildren;
	}


	@Override
	public boolean isEmpty(){
		return items.isEmpty();
	}

	/**
	 * Gets the number of items in the index.
	 *
	 * @return the number of items
	 */
	public int size(){
		return items.size();
	}


	@Override
	public void insert(final Region region){
		if(isBuilt)
			throw new IllegalStateException("Cannot insert a new region after tree is built.");

		items.add(region);
		totalExtent.expandToInclude(region);
	}


	@Override
	public boolean delete(final Region region){
		//TODO https://www.cs.cmu.edu/~christos/PUBLICATIONS.OLDER/vldb94.pdf
		//	find the host leaf (perform an exact match search to find the leaf node `L` that contain the given item)
		//	delete the item (remove the item from node `L`)
		//	if `L` underflow
		//		borrow some entries from `s` cooperating siblings
		//	if all the siblings are ready to underflow, merge `s+1` to `s` nodes; then adjust the resulting nodes
		//		adjust MBR and LHV in parent levels: form a set `S` that contains `L` and its cooperating siblings (if underflow has
		// 	occurred); then invoke `AdjustTree(s)`
		throw new UnsupportedOperationException();
	}


	/**
	 * Builds the index, if not already built.
	 */
	private void build(){
		//skip if already built
		if(isBuilt)
			return;

		isBuilt = true;
		//don't need to build an empty or very small tree
		if(items.size() <= minChildren)
			return;

		sortItems();

		layerStartIndex = computeLayerIndices(items.size(), minChildren);
		//allocate storage
		final int nodeCount = layerStartIndex[layerStartIndex.length - 1] >> 2;
		nodeBounds = createBoundsArray(nodeCount);

		//compute tree nodes
		computeLeafNodes(layerStartIndex[1]);
		for(int i = 1; i < layerStartIndex.length - 1; i ++)
			computeLayerNodes(i);
	}

	private void sortItems(){
		final RegionComparator comp = new RegionComparator(new HilbertEncoder(HILBERT_LEVEL, totalExtent));
		items.sort(comp);
	}

	private static class RegionComparator implements Comparator<Region>{

		private final HilbertEncoder encoder;


		RegionComparator(final HilbertEncoder encoder){
			this.encoder = encoder;
		}

		@Override
		public int compare(final Region region1, final Region region2){
			final int hilbertCode1 = encoder.encode(region1);
			final int hilbertCode2 = encoder.encode(region2);
			return Integer.compare(hilbertCode1, hilbertCode2);
		}

	}

	private static int[] computeLayerIndices(final int itemSize, final int nodeCapacity){
		final List<Integer> layerIndexList = new ArrayList<>(0);
		int layerSize = itemSize;
		int index = 0;
		do{
			layerIndexList.add(index);
			layerSize = numberOfNodesToCover(layerSize, nodeCapacity);
			index += ENV_SIZE * layerSize;
		}while(layerSize > 1);
		return toIntArray(layerIndexList);
	}

	/**
	 * Computes the number of blocks (nodes) required to cover a given number of children.
	 *
	 * @param numberOfChildren	The number of children.
	 * @param nodeCapacity	The node capacity.
	 * @return	The number of nodes needed to cover the children.
	 */
	private static int numberOfNodesToCover(final int numberOfChildren, final int nodeCapacity){
		final int multiplier = numberOfChildren / nodeCapacity;
		final int total = multiplier * nodeCapacity;
		return (total == numberOfChildren? multiplier: multiplier + 1);
	}

	private static int[] toIntArray(final List<Integer> list){
		final int[] array = new int[list.size()];
		for(int i = 0; i < array.length; i ++)
			array[i] = list.get(i);
		return array;
	}

	private static double[] createBoundsArray(final int size){
		final double[] a = new double[size << 2];
		for(int i = 0; i < size; i ++){
			final int index = i << 2;
			a[index + NODE_BOUND_MIN_X_INDEX] = Double.NaN;
			a[index + NODE_BOUND_MIN_Y_INDEX] = Double.NaN;
			a[index + NODE_BOUND_MAX_X_INDEX] = Double.NaN;
			a[index + NODE_BOUND_MAX_Y_INDEX] = Double.NaN;
		}
		return a;
	}

	private void computeLeafNodes(final int layerSize){
		for(int i = 0; i < layerSize; i += ENV_SIZE)
			computeLeafNodeBounds(i, (minChildren * i) >> 2);
	}

	private void computeLeafNodeBounds(final int nodeIndex, final int blockStart){
		for(int i = 0; i <= minChildren; i ++){
			final int itemIndex = blockStart + i;
			if(itemIndex >= items.size())
				break;

			final Region env = items.get(itemIndex);
			updateNodeBounds(nodeIndex, env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
		}
	}

	private void computeLayerNodes(final int layerIndex){
		final int layerStart = layerStartIndex[layerIndex];
		final int childLayerStart = layerStartIndex[layerIndex - 1];
		final int layerSize = layerSize(layerIndex);
		final int childLayerEnd = layerStart;
		for(int i = 0; i < layerSize; i += ENV_SIZE){
			final int childStart = childLayerStart + minChildren * i;
			computeNodeBounds(layerStart + i, childStart, childLayerEnd);
		}
	}

	private void computeNodeBounds(final int nodeIndex, final int blockStart, final int nodeMaxIndex){
		for(int i = 0; i <= minChildren; i ++){
			final int index = blockStart + (i << 2);
			if(index >= nodeMaxIndex)
				break;

			updateNodeBounds(nodeIndex,
				nodeBounds[index + NODE_BOUND_MIN_X_INDEX], nodeBounds[index + NODE_BOUND_MIN_Y_INDEX],
				nodeBounds[index + NODE_BOUND_MAX_X_INDEX], nodeBounds[index + NODE_BOUND_MAX_Y_INDEX]);
		}
	}

	private void updateNodeBounds(final int nodeIndex, final double minX, final double minY, final double maxX, final double maxY){
		if(Double.isNaN(nodeBounds[nodeIndex + NODE_BOUND_MIN_X_INDEX])){
			nodeBounds[nodeIndex + NODE_BOUND_MIN_X_INDEX] = minX;
			nodeBounds[nodeIndex + NODE_BOUND_MIN_Y_INDEX] = minY;
			nodeBounds[nodeIndex + NODE_BOUND_MAX_X_INDEX] = maxX;
			nodeBounds[nodeIndex + NODE_BOUND_MAX_Y_INDEX] = maxY;
		}
		else{
			final double newLeft = Math.min(nodeBounds[nodeIndex + NODE_BOUND_MIN_X_INDEX], minX);
			final double newTop = Math.min(nodeBounds[nodeIndex + NODE_BOUND_MIN_Y_INDEX], minY);
			final double newRight = Math.max(nodeBounds[nodeIndex + NODE_BOUND_MAX_X_INDEX], maxX);
			final double newBottom = Math.max(nodeBounds[nodeIndex + NODE_BOUND_MAX_Y_INDEX], maxY);
			nodeBounds[nodeIndex + NODE_BOUND_MIN_X_INDEX] = newLeft;
			nodeBounds[nodeIndex + NODE_BOUND_MIN_Y_INDEX] = newTop;
			nodeBounds[nodeIndex + NODE_BOUND_MAX_X_INDEX] = newRight;
			nodeBounds[nodeIndex + NODE_BOUND_MAX_Y_INDEX] = newBottom;
		}
	}


	@Override
	public List<Region> query(final Region region){
		build();

		if(!totalExtent.intersects(region))
			return Collections.emptyList();

		final List<Region> visitor = new ArrayList<>();
		query(region, visitor);
		return visitor;
	}

	private void query(final Region region, final Collection<Region> visitor){
		if(layerStartIndex == null)
			queryItems(0, region, visitor);
		else
			queryTopLayer(region, visitor);
	}

	private void queryItems(final int blockStart, final Region region, final Collection<Region> visitor){
		for(int i = 0; i < minChildren; i ++){
			final int itemIndex = blockStart + i;
			//don't query past end of items
			if(itemIndex >= items.size())
				break;

			//visit the item if its region intersects search region
			final Region item = items.get(itemIndex);
			if(item.intersects(region))
				visitor.add(item);
		}
	}

	private void queryTopLayer(final Region region, final Collection<Region> visitor){
		final int layerIndex = layerStartIndex.length - 2;
		final int layerSize = layerSize(layerIndex);
		//query each node in layer
		for(int i = 0; i < layerSize; i += ENV_SIZE)
			queryNode(layerIndex, i, region, visitor);
	}

	private int layerSize(final int layerIndex){
		final int layerStart = layerStartIndex[layerIndex];
		final int layerEnd = layerStartIndex[layerIndex + 1];
		return layerEnd - layerStart;
	}

	private void queryNode(final int layerIndex, final int nodeOffset, final Region region, final Collection<Region> visitor){
		int layerStart = layerStartIndex[layerIndex];
		final int nodeIndex = layerStart + nodeOffset;
		if(!intersects(nodeIndex, region))
			return;

		final Deque<Integer> stack = new ArrayDeque<>();
		stack.push(layerIndex);
		stack.push(nodeOffset);
		while(!stack.isEmpty()){
			final int currentOffset = stack.pop();
			final int currentIndex = stack.pop();

			if(currentIndex == 0){
				final int childNodesOffset = currentOffset / ENV_SIZE * minChildren;
				queryItems(childNodesOffset, region, visitor);
			}
			else{
				final int childNodesOffset = currentOffset * minChildren;
				//query node children
				layerStart = layerStartIndex[currentIndex - 1];
				final int layerEnd = layerStartIndex[currentIndex];
				for(int i = 0; i < minChildren; i ++){
					final int childNodeOffset = childNodesOffset + ENV_SIZE * i;
					//don't query past layer end
					if(layerStart + childNodeOffset >= layerEnd)
						break;

					stack.push(currentIndex - 1);
					stack.push(childNodeOffset);
				}
			}
		}
	}

	private boolean intersects(final int nodeIndex, final Region region){
		return !(region.getMaxX() < nodeBounds[nodeIndex + NODE_BOUND_MIN_X_INDEX]
			|| region.getMaxY() < nodeBounds[nodeIndex + NODE_BOUND_MIN_Y_INDEX]
			|| region.getMinX() > nodeBounds[nodeIndex + NODE_BOUND_MAX_X_INDEX]
			|| region.getMinY() > nodeBounds[nodeIndex + NODE_BOUND_MAX_Y_INDEX]);

	}


	@Override
	public boolean intersects(final Region region){
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean contains(final Region region){
		throw new UnsupportedOperationException();
	}


//	/**
//	 * Gets the extents of the internal index nodes,
//	 *
//	 * @return	A list of the internal node extents.
//	 */
//	public Region[] getBounds(){
//		final int numNodes = nodeBounds.length >> 2;
//		final Region[] bounds = new Region[numNodes];
//		//create from largest to smallest
//		for(int i = numNodes - 1; i >= 0; i --){
//			final int boundIndex = i << 2;
//			bounds[i] = Region.of(nodeBounds[boundIndex + NODE_BOUND_MIN_X_INDEX], nodeBounds[boundIndex + NODE_BOUND_MIN_Y_INDEX],
//				nodeBounds[boundIndex + NODE_BOUND_MAX_X_INDEX], nodeBounds[boundIndex + NODE_BOUND_MAX_Y_INDEX]);
//		}
//		return bounds;
//	}

}
