/**
 * Copyright (c) 2021 Mauro Trevisan
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

import io.github.mtrevisan.mapmatcher.helpers.Envelope;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * A Hilbert-Packed R-tree.  This is a static R-tree
 * which is packed by using the Hilbert ordering
 * of the tree items.
 * <p>
 * The tree is constructed by sorting the items
 * by the Hilbert code of the midpoint of their envelope.
 * Then, a set of internal layers is created recursively
 * as follows:
 * <ul>
 * <li>The items/nodes of the previous are partitioned into blocks
 * of size <code>nodeCapacity</code>
 * <li>For each block a layer node is created with range
 * equal to the envelope of the items/nodess in the block
 * </ul>
 * The internal layers are stored using an array to
 * store the node bounds.
 * The link between a node and its children is
 * stored implicitly in the indexes of the array.
 * For efficiency, the offsets to the layers
 * within the node array are pre-computed and stored.
 * <p>
 * NOTE: Based on performance testing,
 * the HPRtree is somewhat faster than the STRtree.
 * It should also be more memory-efficent,
 * due to fewer object allocations.
 * However, it is not clear whether this
 * will produce a significant improvement
 * for use in JTS operations.
 *
 * @author Martin Davis
 */
public class HPRtree<T>{

	private static final int ENV_SIZE = 4;

	private static final int HILBERT_LEVEL = 12;

	private static final int DEFAULT_NODE_CAPACITY = 16;


	private final List<Item<T>> items = new ArrayList<>();

	private final int nodeCapacity;

	private final Envelope totalExtent = Envelope.ofEmpty();

	private int[] layerStartIndex;

	private double[] nodeBounds;

	private boolean isBuilt = false;

	//public int nodeIntersectsCount;


	/**
	 * Creates a new index with the default node capacity.
	 */
	public HPRtree(){
		this(DEFAULT_NODE_CAPACITY);
	}

	/**
	 * Creates a new index with the given node capacity.
	 *
	 * @param nodeCapacity the node capacity to use
	 */
	public HPRtree(int nodeCapacity){
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

//	@Override
	public void insert(Envelope itemEnv, T item){
		if(isBuilt){
			throw new IllegalStateException("Cannot insert items after tree is built.");
		}
		items.add(new Item<T>(itemEnv, item));
		totalExtent.expandToInclude(itemEnv);
	}

//	@Override
	public List<T> query(Envelope searchEnv){
		build();

		if(! totalExtent.intersects(searchEnv))
			return new ArrayList<>();

		ArrayListVisitor<T> visitor = new ArrayListVisitor<>();
		query(searchEnv, visitor);
		return visitor.getItems();
	}

//	@Override
	public void query(Envelope searchEnv, ItemVisitor<T> visitor){
		build();
		if(! totalExtent.intersects(searchEnv))
			return;
		if(layerStartIndex == null){
			queryItems(0, searchEnv, visitor);
		}
		else{
			queryTopLayer(searchEnv, visitor);
		}
	}

	private void queryTopLayer(Envelope searchEnv, ItemVisitor<T> visitor){
		int layerIndex = layerStartIndex.length - 2;
		int layerSize = layerSize(layerIndex);
		// query each node in layer
		for(int i = 0; i < layerSize; i += ENV_SIZE){
			queryNode(layerIndex, i, searchEnv, visitor);
		}
	}

	private void queryNode(int layerIndex, int nodeOffset, Envelope searchEnv, ItemVisitor<T> visitor){
		int layerStart = layerStartIndex[layerIndex];
		int nodeIndex = layerStart + nodeOffset;
		if(! intersects(nodeIndex, searchEnv))
			return;
		if(layerIndex == 0){
			int childNodesOffset = nodeOffset / ENV_SIZE * nodeCapacity;
			queryItems(childNodesOffset, searchEnv, visitor);
		}
		else{
			int childNodesOffset = nodeOffset * nodeCapacity;
			queryNodeChildren(layerIndex - 1, childNodesOffset, searchEnv, visitor);
		}
	}

	private boolean intersects(int nodeIndex, Envelope env){
		//nodeIntersectsCount++;
		boolean isBeyond = (env.getMaxX() < nodeBounds[nodeIndex]) || (env.getMaxY() < nodeBounds[nodeIndex + 1]) || (env.getMinX() > nodeBounds[nodeIndex + 2]) || (env.getMinY() > nodeBounds[nodeIndex + 3]);
		return ! isBeyond;
	}

	private void queryNodeChildren(int layerIndex, int blockOffset, Envelope searchEnv, ItemVisitor<T> visitor){
		int layerStart = layerStartIndex[layerIndex];
		int layerEnd = layerStartIndex[layerIndex + 1];
		for(int i = 0; i < nodeCapacity; i++){
			int nodeOffset = blockOffset + ENV_SIZE * i;
			// don't query past layer end
			if(layerStart + nodeOffset >= layerEnd)
				break;

			queryNode(layerIndex, nodeOffset, searchEnv, visitor);
		}
	}

	private void queryItems(int blockStart, Envelope searchEnv, ItemVisitor<T> visitor){
		for(int i = 0; i < nodeCapacity; i++){
			int itemIndex = blockStart + i;
			// don't query past end of items
			if(itemIndex >= items.size())
				break;

			// visit the item if its envelope intersects search env
			Item<T> item = items.get(itemIndex);
			//nodeIntersectsCount++;
			if(intersects(item.getEnvelope(), searchEnv)){
				//if (item.getEnvelope().intersects(searchEnv)) {
				visitor.visitItem(item.getItem());
			}
		}
	}

	/**
	 * Tests whether two envelopes intersect.
	 * Avoids the null check in {@link Envelope#intersects(Envelope)}.
	 *
	 * @param env1 an envelope
	 * @param env2 an envelope
	 * @return true if the envelopes intersect
	 */
	private static boolean intersects(Envelope env1, Envelope env2){
		return ! (env2.getMinX() > env1.getMaxX() || env2.getMaxX() < env1.getMinX() || env2.getMinY() > env1.getMaxY() || env2.getMaxY() < env1.getMinY());
	}

	private int layerSize(int layerIndex){
		int layerStart = layerStartIndex[layerIndex];
		int layerEnd = layerStartIndex[layerIndex + 1];
		return layerEnd - layerStart;
	}

//	@Override
	public boolean remove(Envelope itemEnv, Object item){
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Builds the index, if not already built.
	 */
	public synchronized void build(){
		// skip if already built
		if(isBuilt)
			return;
		isBuilt = true;
		// don't need to build an empty or very small tree
		if(items.size() <= nodeCapacity)
			return;

		sortItems();
		//dumpItems(items);

		layerStartIndex = computeLayerIndices(items.size(), nodeCapacity);
		// allocate storage
		int nodeCount = layerStartIndex[layerStartIndex.length - 1] / 4;
		nodeBounds = createBoundsArray(nodeCount);

		// compute tree nodes
		computeLeafNodes(layerStartIndex[1]);
		for(int i = 1; i < layerStartIndex.length - 1; i++){
			computeLayerNodes(i);
		}
		//dumpNodes();
	}

	private static double[] createBoundsArray(int size){
		double[] a = new double[4 * size];
		for(int i = 0; i < size; i++){
			int index = 4 * i;
			a[index] = Double.MAX_VALUE;
			a[index + 1] = Double.MAX_VALUE;
			a[index + 2] = - Double.MAX_VALUE;
			a[index + 3] = - Double.MAX_VALUE;
		}
		return a;
	}

	private void computeLayerNodes(int layerIndex){
		int layerStart = layerStartIndex[layerIndex];
		int childLayerStart = layerStartIndex[layerIndex - 1];
		int layerSize = layerSize(layerIndex);
		int childLayerEnd = layerStart;
		for(int i = 0; i < layerSize; i += ENV_SIZE){
			int childStart = childLayerStart + nodeCapacity * i;
			computeNodeBounds(layerStart + i, childStart, childLayerEnd);
			//System.out.println("Layer: " + layerIndex + " node: " + i + " - " + getNodeEnvelope(layerStart + i));
		}
	}

	private void computeNodeBounds(int nodeIndex, int blockStart, int nodeMaxIndex){
		for(int i = 0; i <= nodeCapacity; i++){
			int index = blockStart + 4 * i;
			if(index >= nodeMaxIndex)
				break;
			updateNodeBounds(nodeIndex, nodeBounds[index], nodeBounds[index + 1], nodeBounds[index + 2], nodeBounds[index + 3]);
		}
	}

	private void computeLeafNodes(int layerSize){
		for(int i = 0; i < layerSize; i += ENV_SIZE){
			computeLeafNodeBounds(i, nodeCapacity * i / 4);
		}
	}

	private void computeLeafNodeBounds(int nodeIndex, int blockStart){
		for(int i = 0; i <= nodeCapacity; i++){
			int itemIndex = blockStart + i;
			if(itemIndex >= items.size())
				break;
			Envelope env = items.get(itemIndex).getEnvelope();
			updateNodeBounds(nodeIndex, env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
		}
	}

	private void updateNodeBounds(int nodeIndex, double minX, double minY, double maxX, double maxY){
		if(minX < nodeBounds[nodeIndex])
			nodeBounds[nodeIndex] = minX;
		if(minY < nodeBounds[nodeIndex + 1])
			nodeBounds[nodeIndex + 1] = minY;
		if(maxX > nodeBounds[nodeIndex + 2])
			nodeBounds[nodeIndex + 2] = maxX;
		if(maxY > nodeBounds[nodeIndex + 3])
			nodeBounds[nodeIndex + 3] = maxY;
	}

	private Envelope getNodeEnvelope(int i){
		return Envelope.of(nodeBounds[i], nodeBounds[i + 1], nodeBounds[i + 2], nodeBounds[i + 3]);
	}

	private static int[] computeLayerIndices(int itemSize, int nodeCapacity){
		List<Integer> layerIndexList = new ArrayList<Integer>();
		int layerSize = itemSize;
		int index = 0;
		do{
			layerIndexList.add(index);
			layerSize = numNodesToCover(layerSize, nodeCapacity);
			index += ENV_SIZE * layerSize;
		}
		while(layerSize > 1);
		return toIntArray(layerIndexList);
	}

	/**
	 * Computes the number of blocks (nodes) required to
	 * cover a given number of children.
	 *
	 * @param nChild
	 * @param nodeCapacity
	 * @return the number of nodes needed to cover the children
	 */
	private static int numNodesToCover(int nChild, int nodeCapacity){
		int mult = nChild / nodeCapacity;
		int total = mult * nodeCapacity;
		if(total == nChild)
			return mult;
		return mult + 1;
	}

	private static int[] toIntArray(List<Integer> list){
		int[] array = new int[list.size()];
		for(int i = 0; i < array.length; i++){
			array[i] = list.get(i);
		}
		return array;
	}

	/**
	 * Gets the extents of the internal index nodes,
	 *
	 * @return	A list of the internal node extents.
	 */
	public Envelope[] getBounds(){
		int numNodes = nodeBounds.length / 4;
		Envelope[] bounds = new Envelope[numNodes];
		// create from largest to smallest
		for(int i = numNodes - 1; i >= 0; i--){
			int boundIndex = 4 * i;
			bounds[i] = Envelope.of(nodeBounds[boundIndex], nodeBounds[boundIndex + 2], nodeBounds[boundIndex + 1], nodeBounds[boundIndex + 3]);
		}
		return bounds;
	}

	private void sortItems(){
		ItemComparator comp = new ItemComparator(new HilbertEncoder(HILBERT_LEVEL, totalExtent));
		items.sort(comp);
	}


	class ItemComparator implements Comparator<Item<T>>{

		private final HilbertEncoder encoder;

		public ItemComparator(HilbertEncoder encoder){
			this.encoder = encoder;
		}

		@Override
		public int compare(Item item1, Item item2){
			int hCode1 = encoder.encode(item1.getEnvelope());
			int hCode2 = encoder.encode(item2.getEnvelope());
			return Integer.compare(hCode1, hCode2);
		}
	}

}
