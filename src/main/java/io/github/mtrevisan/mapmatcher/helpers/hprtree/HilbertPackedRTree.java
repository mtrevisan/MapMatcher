/**
 * Copyright (c) 2022 Mauro Trevisan
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
package io.github.mtrevisan.mapmatcher.helpers.hprtree;

import io.github.mtrevisan.mapmatcher.spatial.Envelope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * A Hilbert-Packed R-Tree.
 * <p>
 * This is a static R-tree which is packed by using the Hilbert ordering of the tree items.
 * </p>
 * <p>
 * The tree is constructed by sorting the items by the Hilbert code of the midpoint of their envelope.<br/>
 * Then, a set of internal layers is created recursively as follows:
 * <ul>
 * 	<li>The items/nodes of the previous are partitioned into blocks of size <code>nodeCapacity</code>
 * 	<li>For each block a layer node is created with range equal to the envelope of the items/nodes in the block
 * </ul>
 * The internal layers are stored using an array to store the node bounds.<br/>
 * The link between a node and its children is stored implicitly in the indexes of the array.<br/>
 * For efficiency, the offsets to the layers within the node array are pre-computed and stored.
 * </p>
 * <p>
 * NOTE: Based on performance testing, the HPRtree is somewhat faster than the STRtree.<br/>
 * It should also be more memory-efficient, due to fewer object allocations.<br/>
 * </p>
 *
 * @see <a href="https://github.com/locationtech/jts/blob/master/modules/core/src/main/java/org/locationtech/jts/index/hprtree/HPRtree.java">HPRtree.java</a>
 * @see <a href="https://www.cs.cmu.edu/~christos/PUBLICATIONS.OLDER/vldb94.pdf">Hilbert R-tree: An improved R-tree using fractals</a>
 * @see <a href="https://en.wikipedia.org/wiki/Hilbert_R-tree">Hilbert R-tree</a>
 * @see <a href="https://web.cs.swarthmore.edu/~adanner/cs97/s08/pdf/prtreesigmod04.pdf">The Priority R-Tree: A Practically Efficient and Worst-Case Optimal R-Tree</a>
 */
public class HilbertPackedRTree<T>{

	private static final int ENV_SIZE = 4;
	private static final int HILBERT_LEVEL = 12;
	private static final int DEFAULT_NODE_CAPACITY = 16;

	private final List<Item<T>> items = new ArrayList<>(0);
	private final int nodeCapacity;
	private final Envelope totalExtent = Envelope.ofEmpty();
	private int[] layerStartIndex;
	private double[] nodeBounds;
	private boolean isBuilt;


	/**
	 * Creates a new tree with the default node capacity.
	 */
	public HilbertPackedRTree(){
		this(DEFAULT_NODE_CAPACITY);
	}

	/**
	 * Creates a new tree with the given node capacity.
	 *
	 * @param nodeCapacity	The node capacity to use.
	 */
	public HilbertPackedRTree(final int nodeCapacity){
		this.nodeCapacity = nodeCapacity;
	}


	/**
	 * Gets the number of items in the index.
	 *
	 * @return the number of items
	 */
	public int size(){
		return items.size();
	}

	public void insert(final Envelope itemEnvelope, final T item){
		if(isBuilt)
			throw new IllegalStateException("Cannot insert items after tree is built.");

		items.add(new Item<>(itemEnvelope, item));
		totalExtent.expandToInclude(itemEnvelope);
	}

	public boolean remove(final Envelope itemEnvelope, final T item){
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
		if(items.size() <= nodeCapacity)
			return;

		sortItems();

		layerStartIndex = computeLayerIndices(items.size(), nodeCapacity);
		//allocate storage
		final int nodeCount = layerStartIndex[layerStartIndex.length - 1] / 4;
		nodeBounds = createBoundsArray(nodeCount);

		//compute tree nodes
		computeLeafNodes(layerStartIndex[1]);
		for(int i = 1; i < layerStartIndex.length - 1; i ++)
			computeLayerNodes(i);
	}

	private void sortItems(){
		final ItemComparator<T> comp = new ItemComparator<>(new HilbertEncoder(HILBERT_LEVEL, totalExtent));
		items.sort(comp);
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
		final double[] a = new double[4 * size];
		for(int i = 0; i < size; i ++){
			final int index = 4 * i;
			a[index] = Double.MAX_VALUE;
			a[index + 1] = Double.MAX_VALUE;
			a[index + 2] = - Double.MAX_VALUE;
			a[index + 3] = - Double.MAX_VALUE;
		}
		return a;
	}

	private void computeLeafNodes(final int layerSize){
		for(int i = 0; i < layerSize; i += ENV_SIZE)
			computeLeafNodeBounds(i, nodeCapacity * i / 4);
	}

	private void computeLeafNodeBounds(final int nodeIndex, final int blockStart){
		for(int i = 0; i <= nodeCapacity; i ++){
			final int itemIndex = blockStart + i;
			if(itemIndex >= items.size())
				break;

			final Envelope env = items.get(itemIndex).getEnvelope();
			updateNodeBounds(nodeIndex, env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
		}
	}

	private void computeLayerNodes(final int layerIndex){
		final int layerStart = layerStartIndex[layerIndex];
		final int childLayerStart = layerStartIndex[layerIndex - 1];
		final int layerSize = layerSize(layerIndex);
		final int childLayerEnd = layerStart;
		for(int i = 0; i < layerSize; i += ENV_SIZE){
			final int childStart = childLayerStart + nodeCapacity * i;
			computeNodeBounds(layerStart + i, childStart, childLayerEnd);
		}
	}

	private void computeNodeBounds(final int nodeIndex, final int blockStart, final int nodeMaxIndex){
		for(int i = 0; i <= nodeCapacity; i ++){
			final int index = blockStart + 4 * i;
			if(index >= nodeMaxIndex)
				break;

			updateNodeBounds(nodeIndex, nodeBounds[index], nodeBounds[index + 1], nodeBounds[index + 2], nodeBounds[index + 3]);
		}
	}

	private void updateNodeBounds(final int nodeIndex, final double minX, final double minY, final double maxX, final double maxY){
		if(minX < nodeBounds[nodeIndex])
			nodeBounds[nodeIndex] = minX;
		if(minY < nodeBounds[nodeIndex + 1])
			nodeBounds[nodeIndex + 1] = minY;
		if(maxX > nodeBounds[nodeIndex + 2])
			nodeBounds[nodeIndex + 2] = maxX;
		if(maxY > nodeBounds[nodeIndex + 3])
			nodeBounds[nodeIndex + 3] = maxY;
	}


	public Collection<T> nodes(){
		final List<T> list = new ArrayList<>(items.size());
		for(final Item<T> item : items)
			list.add(item.getItem());
		return list;
	}


	public List<T> query(final Envelope searchEnvelope){
		build();

		if(!totalExtent.intersects(searchEnvelope))
			return Collections.emptyList();

		final ArrayListVisitor<T> visitor = new ArrayListVisitor<>();
		query(searchEnvelope, visitor);
		return visitor.getItems();
	}

	private void query(final Envelope searchEnvelope, final ItemVisitor<T> visitor){
		if(layerStartIndex == null)
			queryItems(0, searchEnvelope, visitor);
		else
			queryTopLayer(searchEnvelope, visitor);
	}

	private void queryItems(final int blockStart, final Envelope searchEnvelope, final ItemVisitor<T> visitor){
		for(int i = 0; i < nodeCapacity; i ++){
			final int itemIndex = blockStart + i;
			//don't query past end of items
			if(itemIndex >= items.size())
				break;

			//visit the item if its envelope intersects search env
			final Item<T> item = items.get(itemIndex);
			if(intersects(item.getEnvelope(), searchEnvelope))
				visitor.visitItem(item.getItem());
		}
	}

	/**
	 * Tests whether two envelopes intersect.
	 * Avoids the null check in {@link Envelope#intersects(Envelope)}.
	 *
	 * @param envelope1 an envelope
	 * @param envelope2 an envelope
	 * @return true if the envelopes intersect
	 */
	private static boolean intersects(final Envelope envelope1, final Envelope envelope2){
		return !(envelope2.getMinX() > envelope1.getMaxX()
			|| envelope2.getMaxX() < envelope1.getMinX()
			|| envelope2.getMinY() > envelope1.getMaxY()
			|| envelope2.getMaxY() < envelope1.getMinY());
	}

	private void queryTopLayer(final Envelope searchEnvelope, final ItemVisitor<T> visitor){
		final int layerIndex = layerStartIndex.length - 2;
		final int layerSize = layerSize(layerIndex);
		//query each node in layer
		for(int i = 0; i < layerSize; i += ENV_SIZE)
			queryNode(layerIndex, i, searchEnvelope, visitor);
	}

	private int layerSize(final int layerIndex){
		final int layerStart = layerStartIndex[layerIndex];
		final int layerEnd = layerStartIndex[layerIndex + 1];
		return layerEnd - layerStart;
	}

	private void queryNode(final int layerIndex, final int nodeOffset, final Envelope searchEnvelope, final ItemVisitor<T> visitor){
		final int layerStart = layerStartIndex[layerIndex];
		final int nodeIndex = layerStart + nodeOffset;
		if(!intersects(nodeIndex, searchEnvelope))
			return;

		if(layerIndex == 0){
			final int childNodesOffset = nodeOffset / ENV_SIZE * nodeCapacity;
			queryItems(childNodesOffset, searchEnvelope, visitor);
		}
		else{
			final int childNodesOffset = nodeOffset * nodeCapacity;
			queryNodeChildren(layerIndex - 1, childNodesOffset, searchEnvelope, visitor);
		}
	}

	private boolean intersects(final int nodeIndex, final Envelope env){
		final boolean isBeyond = (
			env.getMaxX() < nodeBounds[nodeIndex]
			|| env.getMaxY() < nodeBounds[nodeIndex + 1]
			|| env.getMinX() > nodeBounds[nodeIndex + 2]
			|| env.getMinY() > nodeBounds[nodeIndex + 3]);
		return !isBeyond;
	}

	private void queryNodeChildren(final int layerIndex, final int blockOffset, final Envelope searchEnvelope, final ItemVisitor<T> visitor){
		final int layerStart = layerStartIndex[layerIndex];
		final int layerEnd = layerStartIndex[layerIndex + 1];
		for(int i = 0; i < nodeCapacity; i ++){
			final int nodeOffset = blockOffset + ENV_SIZE * i;
			//don't query past layer end
			if(layerStart + nodeOffset >= layerEnd)
				break;

			queryNode(layerIndex, nodeOffset, searchEnvelope, visitor);
		}
	}


	/**
	 * Gets the extents of the internal index nodes,
	 *
	 * @return	A list of the internal node extents.
	 */
	public Envelope[] getBounds(){
		final int numNodes = nodeBounds.length / 4;
		final Envelope[] bounds = new Envelope[numNodes];
		//create from largest to smallest
		for(int i = numNodes - 1; i >= 0; i --){
			final int boundIndex = 4 * i;
			bounds[i] = Envelope.of(nodeBounds[boundIndex], nodeBounds[boundIndex + 1], nodeBounds[boundIndex + 2], nodeBounds[boundIndex + 3]);
		}
		return bounds;
	}

}
