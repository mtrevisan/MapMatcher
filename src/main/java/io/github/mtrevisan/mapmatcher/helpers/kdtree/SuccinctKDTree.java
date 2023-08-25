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

import io.github.mtrevisan.mapmatcher.helpers.QuickSelect;
import io.github.mtrevisan.mapmatcher.helpers.SpatialTree;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;


/**
 * A k-d Tree (short for k-dimensional Tree) is a space-partitioning data
 * structure for organizing points in a k-dimensional space. k-d trees are a
 * useful data structure for several applications, such as searches involving a
 * multidimensional search key (e.g. range searches and nearest neighbor
 * searches). k-d trees are a special case of binary space partitioning trees.
 * <p>
 * Algorithm		Average								Worst case
 * Space				O(n)									O(n)
 * Search			O(log(n))							O(n)
 * Range search	k(n) + Θ(n^alpha) + Θ(log(n))	O(k · n^(1 - 1/k)), (for
 * 	"small enough sides of the searching rectangle") where k(n) is the
 * 	expected number of matches, alpha depends on the variant of k-d tree.
 * Insert			O(log(n))							O(n)
 * </p>
 * <p>
 * Note that it is not required to select the median point upon construction of the tree. In the case where median points are not selected,
 * there is no guarantee that the tree will be balanced.<br />
 * Having a balanced tree that evenly splits regions improves nearest neighbor performance.
 * Having a squarish k-d trees improves range search performance.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/K-d_tree">H<em>k</em>-d tree</a>
 * @see <a href="https://yasenh.github.io/post/kd-tree/">KD-Tree</a>
 * @see <a href="https://github.com/mgruben/Kd-Trees">Kd-Trees</a>
 * @see <a href="https://github.com/amay12/SpatialSearch">SpatialSearch</a>
 * @see <a href="https://github.com/mgruben/Kd-Trees/blob/master/KdTree.java">KdTree</a>
 * @see <a href="https://core.ac.uk/download/pdf/19529722.pdf">Multicore construction of k-d trees with applications in graphics and vision</a>
 * @see <a href="https://www.ri.cmu.edu/pub_files/pub1/moore_andrew_1991_1/moore_andrew_1991_1.pdf">An introductory tutorial on kd-trees</a>
 * @see <a href="https://arxiv.org/ftp/arxiv/papers/2106/2106.03799.pdf">Deterministic iteratively build KD-Tree with KNN search for exact applications</a>
 * @see <a href="https://upcommons.upc.edu/bitstream/handle/2099.1/11309/MasterMercePons.pdf?sequence=1&isAllowed=y">Design, analysis and implementation of new variants of Kd-trees</a>
 *
 * @see <a href="https://pdf.sciencedirectassets.com/272575/1-s2.0-S0890540120X00046/1-s2.0-S0890540120300067/am.pdf?X-Amz-Security-Token=IQoJb3JpZ2luX2VjEH0aCXVzLWVhc3QtMSJGMEQCID%2FOQkedFMLgQQeZU01sTNCf8n%2FD297moJAmvzCLtizwAiAYrrd6sDhQbtqtN9Oe90aExQaQqDJtoDGTej5S7sXLISqzBQhGEAUaDDA1OTAwMzU0Njg2NSIMIvVFfgUuL%2B9Y9fIZKpAFEZVTGJagjKn2Ty4zlUI8FhXQAJppk%2FO9sv2W1XdR0Tusv39Ung9VnuFMCKLUGvij0ZQMXNrRtcJ5ChzAOobLkYij5MzC2x18iMFLkqyNhsRVWHC%2BLgO9bSczj1iFDsBUE6CPMuy0RKqzQWZf7pZ31VcfNIPPXksN6CM%2Ba5tvgpWdk%2B0mo0uRRnI%2FFEh3Ua5ulU4Ouoe%2B3D9IRtCR9IiETYB3x8aYnf76vgQWoA3akV006aq%2Foq4Z%2FyAFHa7RL%2F446uaQ6UmKuLeIBuzEI3bliAuZ6RAByyM1L8YHry0xcyT0g3fDj6P%2BafW3DoAQ%2Fa7OH%2BFnbLYd2AZa6yc85O1S1PJhk3J7wpmIm8S1YWBFK6X1LCHq6aw9KctXjPk8NLJzfPELw2%2FHya86T3zoRowOgHsv3MYWQwh5LGWTArgkr7Wlwio%2BXhiKqTyOU4KkKP8%2BRN84is9mmK22Ymysawb%2B5ydTTJ4qti0cqLvWWBSMYxreuUY%2Bcalkrl%2BmWFq9kIrc6Mlxxm6D5lEOutttLSmHgSO6Fgg99Q7EBAcV85CvwvYdCJpKQpj%2Fz%2FfPqNgqApCM6Hv2ly4JwL%2BJDS5ynjpfpiWGL1y0u9TGGU55Iedqd0VSKTFt4gQhjzZCRfolM0WN2TLQUk3tLRWJa8RYO0F0mWZUK39CHvbt993NZhHYBJ07SMo7ZO2jdbMSTBU%2Ffqgf0%2BKqm4Rk3CXn5u4msPl3ySxjBXiATF8Tnjgdw9u3%2FLwvQKZ%2Fw4IMQJYCSDTqspCY7qEnOD8Xy8cslYXf3qrY%2ByXdmnYVLAecmqxiqNOYB%2BEe6mgSgI4mJ6Y%2BjQypprGM0k2p5oAsPLOjQr9Ounxo%2B3fuc9NIsxsqQwYxT0q7kccwhsOipwY6sgEW0xqMICCN%2F6cevXo24ByYqnnhrWL22RepDBbBIpbNrtW1cg6QWDv4FggkFeiGPYHiWVb0FzxXnEcmhbyCuUzikHnP0jfBmTqeRzE9JGnEwX7%2BQGSWPP78Ldf72AN6MF0ntOclo2zqQG1O5IdVf%2FlF2X7u66d42uDQjVx7%2FcHsbcDt512hGv4uM4rOyjJ0ZaeBWJlp0WOLcc0mUpeGndvCiS%2Fql9g09wjDVqCQ4Et8T9bL&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20230825T135930Z&X-Amz-SignedHeaders=host&X-Amz-Expires=300&X-Amz-Credential=ASIAQ3PHCVTYWQGM27NF%2F20230825%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=1a6b00ba45a9de7e809732eaf31400961e1df973c0f9e2c09ff3c0d57ab5b123&hash=d6a51e61d9eb7c337d50ffcf1e1de33523fb7ac2668570db1584362770054911&host=68042c943591013ac2b2430a89b270f6af2c76d8dfd086a07176afe7c76c2c61&pii=S0890540120300067&tid=pdf-c498830d-40cc-4814-9761-fd1012133215&sid=bfe742ce2a4129440d39b6e85d84a8e8bad6gxrqb&type=client">Compact and Succinct Data Structures for Multidimensional Orthogonal Range Searching</a>
 * @see <a href="https://iq.opengenus.org/succinct-0-1-encoding-of-binary-tree/">Succinct (0-1) Encoding of Binary Tree</a>
 */
public class SuccinctKDTree implements SpatialTree{

	private BitSet structure = new BitSet();
	private final List<Point> data;

	private Comparator<KDNode>[] comparators;


	public static SuccinctKDTree ofEmpty(final int dimensions){
		return new SuccinctKDTree(dimensions);
	}

	public static SuccinctKDTree of(final Collection<Point> points){
		return new SuccinctKDTree(points);
	}


	private SuccinctKDTree(final int dimensions){
		if(dimensions < 1)
			throw new IllegalArgumentException ("K-D Tree must have at least dimension 1");

		data = new ArrayList<>();

		setDimensions(dimensions);
	}

	/**
	 * Constructor for creating a (balanced) median k-d tree.
	 *
	 * @param points	A collection of {@link Point}s.
	 */
	private SuccinctKDTree(final Collection<Point> points){
		if(points.isEmpty())
			throw new IllegalArgumentException("List of points cannot be empty");

		data = new ArrayList<>(points.size());

		//extract dimensions from first point
		for(final Point point : points){
			setDimensions(point.getDimensions());
			break;
		}

		buildTree(points);
	}

	private void buildTree(final Collection<Point> points){
		final List<KDNode> nodes = new ArrayList<>(points.size());
		for(final Point point : points){
			final KDNode node = new KDNode(point);
			nodes.add(node);
		}

		final Stack<BuildTreeParams> stack = new Stack<>();
		stack.push(BuildTreeParams.asRoot(nodes.size()));

		while(!stack.isEmpty()){
			final BuildTreeParams params = stack.pop();
			final int begin = params.begin;
			final int end = params.end;
			final int parentIndex = params.nodeIndex;
			int axis = params.axis;

			//extract the splitting node
			final int k = begin + ((end - begin) >> 1);
			final KDNode median = QuickSelect.select(nodes, begin, end - 1, k, comparators[axis]);

			axis = getNextAxis(axis);
			if(begin < k)
				stack.push(new BuildTreeParams(begin, k, axis, parentIndex, BuildTreeParams.Side.LEFT));
			if(k + 1 < end)
				stack.push(new BuildTreeParams(k + 1, end, axis, parentIndex, BuildTreeParams.Side.RIGHT));

			if(isEmpty())
				addNode(0, median.point);
			else if(params.side == BuildTreeParams.Side.LEFT)
				addNode(leftIndex(params.nodeIndex), median.point);
			else if(params.side == BuildTreeParams.Side.RIGHT)
				addNode(rightIndex(params.nodeIndex), median.point);
		}
	}

	private static class BuildTreeParams extends NodeIndexAxisItem{
		private final int begin;
		private final int end;
		private final Side side;

		private enum Side{LEFT, RIGHT}

		private static BuildTreeParams asRoot(final int size){
			return new BuildTreeParams(0, size, 0, 0, null);
		}

		private BuildTreeParams(final int begin, final int end, final int axis, final int parentIndex, final Side side){
			super(parentIndex, axis);

			this.begin = begin;
			this.end = end;
			this.side = side;
		}
	}

	private static class NodeComparator implements Comparator<KDNode>{

		private final int axis;

		private NodeComparator(final int axis){
			this.axis = axis;
		}

		@Override
		public int compare(final KDNode node1, final KDNode node2){
			return Double.compare(node1.point.getCoordinate(axis), node2.point.getCoordinate(axis));
		}
	}

	@SuppressWarnings("unchecked")
	private void setDimensions(final int dimensions){
		comparators = (Comparator<KDNode>[])Array.newInstance(Comparator.class, dimensions);
		for(int i = 0; i < dimensions; i ++)
			comparators[i] = new NodeComparator(i);
	}


	@Override
	public void clear(){
		structure.clear();
		data.clear();
	}

	@Override
	public boolean isEmpty(){
		return (structure.cardinality() == 0);
	}


	/**
	 * Add the point to the set (if it is not already in the set).
	 * <p>
	 * At the root (and every second level thereafter), the x-coordinate is
	 * used as the key.
	 * </p>
	 * <p>
	 * This means that if (0.7, 0.2) is the root, then (0.5, 0.9) will be
	 * added to the left, since its x-coordinate is smaller than the
	 * x-coordinate of the root node.  Similarly, if the next point to be
	 * added is (0.8, 0.1), that point will be added to the right of root,
	 * since its x-coordinate is larger than the x-coordinate of the root node.
	 * </p>
	 * <p>
	 * So, visually, we would have:
	 *       (0.7, 0.2)
	 *      /          \
	 * (0.5, 0.9)   (0.8, 0.1)
	 * </p>
	 * <p>
	 * At one level below the root (and every second level thereafter), the
	 * y-coordinate is used as the key.
	 * </p>
	 * <p>
	 * This means that if we next add (0.6, 0.8), it will be added to the left
	 * of (0.5, 0.9).  Similarly, if we next add (0.4, 0.95), it will be added
	 * to the right of (0.5, 0.9).
	 * </p>
	 * <p>
	 * So, visually, we would have:
	 *              (0.7, 0.2)
	 *             /          \
	 *        (0.5, 0.9)   (0.8, 0.1)
	 *       /          \
	 * (0.6, 0.8)   (0.4, 0.95)
	 * </p>
	 *
	 * @param point	The point to add.
	 */
	@Override
	public void insert(final Point point){
		if(point.getDimensions() < comparators.length)
			throw new IllegalArgumentException("Point dimension are less than what specified constructing this tree");

		int parentIndex = (isEmpty()? -1: 0);
		//find parent node
		int parentNodeIndex = -1;
		int axis = getNextAxis(comparators.length - 1);
		while(parentIndex >= 0 && parentIndex < structure.length()){
			parentNodeIndex = parentIndex;

			if(point.getCoordinate(axis) < data(parentIndex).getCoordinate(axis))
				parentIndex = leftIndex(parentIndex);
			else
				parentIndex = rightIndex(parentIndex);

			axis = getNextAxis(axis);
		}

		//insert point in the right place
		if(parentNodeIndex == -1){
			if(isEmpty())
				addNode(0, point);
		}
		else{
			axis = getNextAxis(axis);
			if(point.getCoordinate(axis) < data(parentNodeIndex).getCoordinate(axis))
				addNode(leftIndex(parentNodeIndex), point);
			else
				addNode(rightIndex(parentNodeIndex), point);
		}
	}


	@Override
	public boolean contains(final Point point){
		if(isEmpty() || point == null)
			return false;

		final double precision = point.getDistanceCalculator().getPrecision();

		int axis = 0;
		int currentNodeIndex = 0;
		while(currentNodeIndex >= 0 && currentNodeIndex < structure.length()){
			if(data(currentNodeIndex).equals(point, precision))
				return true;

			if(point.getCoordinate(axis) < data(currentNodeIndex).getCoordinate(axis))
				currentNodeIndex = leftIndex(currentNodeIndex);
			else
				currentNodeIndex = rightIndex(currentNodeIndex);

			axis = getNextAxis(axis);
		}

		return false;
	}


	/**
	 * Find the nearest point in the k-d tree to the given point.
	 * <p>
	 * Only returns null if the tree was initially empty. Otherwise, must
	 * return some point that belongs to the tree.
	 * <p>
	 * If tree is empty or if the target is <code>null</code> then
	 * <code>null</code> is returned.
	 *
	 * @param point	The target of the search.
	 * @return Closest {@link Point} object in the tree to the target.
	 */
	@Override
	public Point nearestNeighbour(final Point point){
		if(isEmpty() || point == null)
			return null;

		int bestNodeIndex = -1;
		double bestDistanceSquare = Double.POSITIVE_INFINITY;
		final double precision = point.getDistanceCalculator().getPrecision();

		final Stack<NodeIndexAxisItem> stack = new Stack<>();
		stack.push(new NodeIndexAxisItem(0, 0));
		while(!stack.isEmpty()){
			final NodeIndexAxisItem currentItem = stack.pop();
			final int currentNodeIndex = currentItem.nodeIndex;

			final Point currentNodeData = data(currentNodeIndex);
			final double distanceSquare = (currentNodeData.getX() - point.getX()) * (currentNodeData.getX() - point.getX())
				+ (currentNodeData.getY() - point.getY()) * (currentNodeData.getY() - point.getY());

			if(bestNodeIndex == -1 || distanceSquare < bestDistanceSquare){
				bestDistanceSquare = distanceSquare;
				bestNodeIndex = currentNodeIndex;
			}
			if(bestDistanceSquare <= precision * precision)
				break;

			int axis = currentItem.axis;
			final double coordinateDelta = currentNodeData.getCoordinate(axis) - point.getCoordinate(axis);
			axis = getNextAxis(axis);
			final int currentNodeLeftIndex = leftIndex(currentNodeIndex);
			if(coordinateDelta > 0. && currentNodeLeftIndex >= 0 && currentNodeLeftIndex < structure.length()){
				stack.push(new NodeIndexAxisItem(currentNodeLeftIndex, axis));
				if(coordinateDelta * coordinateDelta < bestDistanceSquare)
					stack.push(new NodeIndexAxisItem(currentNodeLeftIndex, axis));
			}
			else{
				final int currentNodeRightIndex = rightIndex(currentNodeIndex);
				if(coordinateDelta <= 0. && currentNodeRightIndex >= 0 && currentNodeRightIndex < structure.length()){
					stack.push(new NodeIndexAxisItem(currentNodeRightIndex, axis));
					if(coordinateDelta * coordinateDelta < bestDistanceSquare)
						stack.push(new NodeIndexAxisItem(currentNodeRightIndex, axis));
				}
			}
		}

		return (bestNodeIndex >= 0? data(bestNodeIndex): null);
	}

	private static class NodeIndexAxisItem{
		final int nodeIndex;
		final int axis;

		private NodeIndexAxisItem(final int nodeIndex, final int axis){
			this.nodeIndex = nodeIndex;
			this.axis = axis;
		}
	}


	/**
	 * Locate all points within the tree that fall within the given rectangle.
	 *
	 * @param rangeMin	Minimum point of the searching rectangle.
	 * @param rangeMax	Maximum point of the searching rectangle.
	 * @return	Collection of {@link Point}s that fall within the given envelope.
	 */
	@Override
	public Collection<Point> pointsInRange(final Point rangeMin, final Point rangeMax){
		if(rangeMin.getX() > rangeMax.getX() || rangeMin.getY() > rangeMax.getY())
			throw new IllegalArgumentException("Unfeasible search rectangle boundaries");

		final Stack<Point> points = new Stack<>();
		if(isEmpty())
			return points;

		int axis = 0;

		final Stack<Integer> nodes = new Stack<>();
		nodes.push(0);
		while(!nodes.isEmpty()){
			final int nodeIndex = nodes.pop();
			final Point point = data(nodeIndex);

			//add contained points to points stack if inside the region
			if(inside(point, rangeMin, rangeMax))
				points.push(point);

			final int nodeLeftIndex = leftIndex(nodeIndex);
			if(nodeLeftIndex >= 0 && nodeLeftIndex < structure.length() && point.getCoordinate(axis) >= rangeMin.getCoordinate(axis))
				nodes.push(nodeLeftIndex);
			final int nodeRightIndex = rightIndex(nodeIndex);
			if(nodeRightIndex >= 0 && nodeRightIndex < structure.length() && point.getCoordinate(axis) <= rangeMax.getCoordinate(axis))
				nodes.push(nodeRightIndex);

			axis = getNextAxis(axis);
		}
		return points;
	}

	/**
	 * Locate all points within the tree that fall within the given circle.
	 *
	 * @param center	Center point of the searching circle.
	 * @param radius	Radius of the searching circle.
	 * @return	Collection of {@link Point}s that fall within the given envelope.
	 */
	@Override
	public Collection<Point> pointsInRange(final Point center, final double radius){
		final Stack<Point> points = new Stack<>();
		if(isEmpty())
			return points;

		int axis = 0;

		final Stack<Integer> nodes = new Stack<>();
		nodes.push(0);
		while(!nodes.isEmpty()){
			final int nodeIndex = nodes.pop();
			final Point point = data(nodeIndex);

			//add contained points to points stack if inside the region
			if(inside(point, center, radius))
				points.push(point);

			final int nodeLeftIndex = leftIndex(nodeIndex);
			if(nodeLeftIndex >= 0 && nodeLeftIndex < structure.length() && point.getCoordinate(axis) >= center.getCoordinate(axis))
				nodes.push(nodeLeftIndex);
			final int nodeRightIndex = rightIndex(nodeIndex);
			if(nodeRightIndex >= 0 && nodeRightIndex < structure.length() && point.getCoordinate(axis) <= center.getCoordinate(axis))
				nodes.push(nodeRightIndex);

			axis = getNextAxis(axis);
		}
		return points;
	}

	/**
	 * .Tests if the point intersects (lies inside) the region.
	 *
	 * @param point	The point to be tested.
	 * @param rangeMin	Minimum point of the searching rectangle.
	 * @param rangeMax	Maximum point of the searching rectangle.
	 * @return	Whether the point lies inside the rectangle.
	 */
	private static boolean inside(final Point point, final Point rangeMin, final Point rangeMax){
		return (rangeMin.getX() <= point.getX() && point.getX() <= rangeMax.getX()
			&& rangeMin.getY() <= point.getY() && point.getY() <= rangeMax.getY());
	}

	/**
	 * .Tests if the point intersects (lies inside) the region.
	 *
	 * @param point	The point to be tested.
	 * @param center	Center point of the searching circle.
	 * @param radius	Radius of the searching circle.
	 * @return	Whether the point lies inside the circle.
	 */
	private static boolean inside(final Point point, final Point center, final double radius){
		return (point.distance(center) <= radius);
	}


	//FIXME: another way to choose the cutting dimension is to calculate the variance of all values in each dimension and the largest one
	// will be chosen as the cutting dimension (knowledge of overall points is needed). The larger variance means data is more scatter on
	// the axis, so that we can split data better in this way.
	//FIXME: squarish k-d trees - When a rectangle is split by a newly inserted point, the longest side of the rectangle is cut (knowledge
	// of overall BB is needed). Better performance for range search.
	private int getNextAxis(final int currentAxis){
		return (currentAxis + 1) % comparators.length;
	}


	private static int leftIndex(final int parentIndex){
		return (parentIndex << 1) + 1;
	}

	private static int rightIndex(final int parentIndex){
		return (parentIndex << 1) + 2;
	}

	private void addNode(final int index, final Point point){
		if(index >= structure.length())
			structure = insertBit(structure, index, true);

		structure.set(index);
		data.add(point);
	}

//	public void encodeSuccinct(){
//		if(isEmpty())
//			return;
//
//		data.add(root.point);
//		encodeSuccinct(root.left, structure, data, 1);
//		encodeSuccinct(root.right, structure, data, 1);
//	}
//
//	private void encodeSuccinct(final KDNode parent, final BitSet structure, final List<Point> data, final int index){
//		if(parent != null){
//			addNode(index, parent.point);
//			encodeSuccinct(parent.left, structure, data, index + 1);
//			encodeSuccinct(parent.right, structure, data, index + 1);
//		}
//	}
//
//	public void decodeSuccinct(final BitSet structure, final List<Point> data){
//		if(structure.isEmpty() || data.isEmpty())
//			return;
//
//		root = new KDNode(data.get(0));
//		root.left = decodeSuccinct(structure, data, 1);
//		root.right = decodeSuccinct(structure, data, 1);
//	}
//
//	private KDNode decodeSuccinct(final BitSet structure, final List<Point> data, final int index){
//		if(index < structure.length() && structure.get(index)){
//			final KDNode root = new KDNode(data.get(index));
//			root.left = decodeSuccinct(structure, data, index + 1);
//			root.right = decodeSuccinct(structure, data, index + 1);
//			return root;
//		}
//		return null;
//	}

	private Point data(final int index){
		return data.get(countSetBits(index) - 1);
	}

	private int countSetBits(final int index){
//FIXME can structure.cardinality() be used?
		int count = 0;
		int i = -1;
		while(++ i <= index){
			i = structure.nextSetBit(i);
			if(i < 0 || i == Integer.MAX_VALUE)
				break;

			count ++;
		}
		return count;
	}

	//FIXME to be tested!
	private BitSet insertBit(final BitSet structure, final int insertBeforeIndex, final boolean value){
		final BitSet newSet = new BitSet(structure.length() + 1);
		int index = -1;
		while(++ index < insertBeforeIndex){
			index = structure.nextSetBit(index);
			if(index < 0 || index == Integer.MAX_VALUE)
				break;

			newSet.set(index);
		}
		if(value)
			newSet.set(insertBeforeIndex);
		index = insertBeforeIndex - 1;
		while(++ index < structure.length()){
			index = structure.nextSetBit(index);
			if(index < 0 || index == Integer.MAX_VALUE)
				break;

			newSet.set(index + 1);
		}
		return newSet;
	}

}
