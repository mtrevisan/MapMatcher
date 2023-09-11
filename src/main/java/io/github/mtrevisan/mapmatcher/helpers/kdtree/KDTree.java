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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;


/**
 * A k-d Tree (short for k-dimensional Tree) is a space-partitioning data
 * structure for organizing points in a k-dimensional space.
 * <p>k-d trees are a useful data structure for several applications, such as
 * searches involving a multidimensional search key (e.g. range searches and
 * nearest neighbor searches). k-d trees are a special case of binary space
 * partitioning trees.
 * </p>
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
 */
public class KDTree implements SpatialTree{

	private static final int STARTING_DIMENSION = 0;


	private KDNode root;

	private Comparator<KDNode>[] comparators;


	public static KDTree ofDimensions(final int dimensions){
		return new KDTree(dimensions);
	}

	public static KDTree ofPoints(final Collection<Point> points){
		return new KDTree(points);
	}


	private KDTree(final int dimensions){
		if(dimensions < 1)
			throw new IllegalArgumentException ("K-D Tree must have at least dimension 1");

		createComparators(dimensions);
	}

	/**
	 * Constructor for creating a (balanced) median k-d tree.
	 *
	 * @param points	A collection of {@link Point}s.
	 */
	private KDTree(final Collection<Point> points){
		if(points.isEmpty())
			throw new IllegalArgumentException("List of points cannot be empty");

		//extract dimensions from first point
		for(final Point point : points){
			createComparators(point.getDimensions());
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

		final Deque<BuildTreeParams> stack = new ArrayDeque<>();
		stack.push(BuildTreeParams.asRoot(nodes.size()));

		while(!stack.isEmpty()){
			final BuildTreeParams params = stack.pop();
			final int begin = params.begin;
			final int end = params.end;
			int axis = params.axis;

			//extract the splitting node
			final int middle = begin + ((end - begin) >> 1);
			final KDNode median = QuickSelect.select(nodes, begin, end - 1, middle, comparators[axis]);

			axis = getNextAxis(axis, comparators.length);
			if(begin < middle)
				stack.push(new BuildTreeParams(begin, middle, axis, median, BuildTreeParams.Side.LEFT));
			if(middle + 1 < end)
				stack.push(new BuildTreeParams(middle + 1, end, axis, median, BuildTreeParams.Side.RIGHT));

			if(root == null)
				root = median;
			else if(params.side == BuildTreeParams.Side.LEFT)
				params.node.left = median;
			else
				params.node.right = median;
		}
	}

	private static class BuildTreeParams extends NodeAxisItem{
		private final int begin;
		private final int end;
		private final Side side;

		private enum Side{LEFT, RIGHT}

		private static BuildTreeParams asRoot(final int size){
			return new BuildTreeParams(0, size, 0, null, null);
		}

		private BuildTreeParams(final int begin, final int end, final int axis, final KDNode parent, final Side side){
			super(parent, axis);

			this.begin = begin;
			this.end = end;
			this.side = side;
		}
	}

	@SuppressWarnings("unchecked")
	private void createComparators(final int dimensions){
		comparators = (Comparator<KDNode>[])Array.newInstance(Comparator.class, dimensions);
		for(int i = 0; i < dimensions; i ++)
			comparators[i] = new NodeComparator(i);
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


	@Override
	public boolean isEmpty(){
		return (root == null);
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
	 * x-coordinate of the root node. Similarly, if the next point to be
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
	 * of (0.5, 0.9). Similarly, if we next add (0.4, 0.95), it will be added
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
		final int dimensions = comparators.length;
		if(point.getDimensions() < dimensions)
			throw new IllegalArgumentException("Point dimension are less than what specified constructing this tree");

		if(isEmpty())
			root = new KDNode(point);
		else
			insert(root, point, dimensions);
	}

	/** NOTE: used by {@link HybridKDTree}. */
	@SuppressWarnings("DataFlowIssue")
	static void insert(KDNode parent, final Point point, final int dimensions){
		//traverse the tree and find the parent node:
		KDNode parentNode = null;
		//start from first dimension
		int lastAxis = STARTING_DIMENSION;
		int axis = STARTING_DIMENSION;
		while(parent != null){
			parentNode = parent;
			lastAxis = axis;

			if(point.getCoordinate(axis) < parent.point.getCoordinate(axis))
				parent = parent.left;
			else
				parent = parent.right;

			axis = getNextAxis(axis, dimensions);
		}

		//add new loaf node to the tree
		final KDNode newNode = new KDNode(point);
		if(point.getCoordinate(lastAxis) < parentNode.point.getCoordinate(lastAxis))
			parentNode.left = newNode;
		else
			parentNode.right = newNode;
	}


	@Override
	public boolean contains(final Point point){
		if(point == null)
			return false;
		return contains(root, point, comparators.length);
	}

	/** NOTE: used by {@link HybridKDTree}. */
	static boolean contains(KDNode currentNode, final Point point, final int dimensions){
		final double precision = point.getDistanceCalculator()
			.getPrecision();

		int axis = STARTING_DIMENSION;
		while(currentNode != null){
			if(currentNode.point.equals(point, precision))
				return true;

			if(point.getCoordinate(axis) < currentNode.point.getCoordinate(axis))
				currentNode = currentNode.left;
			else
				currentNode = currentNode.right;

			axis = getNextAxis(axis, dimensions);
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
	public Point nearestNeighbor(final Point point){
		if(isEmpty() || point == null)
			return null;

		return nearestNeighbor(root, point, comparators.length);
	}

	/** NOTE: used by {@link HybridKDTree}. */
	static Point nearestNeighbor(final KDNode currentNode, final Point point, final int dimensions){
		KDNode bestNode = null;
		double bestDistanceSquare = Double.POSITIVE_INFINITY;
		double precisionSquare = point.getDistanceCalculator()
			.getPrecision();
		precisionSquare *= precisionSquare;

		final Deque<NodeAxisItem> stack = new ArrayDeque<>();
		stack.push(new NodeAxisItem(currentNode, 0));
		while(!stack.isEmpty()){
			final NodeAxisItem currentItem = stack.pop();
			final KDNode node = currentItem.node;

			final Point nodePoint = node.point;
			final double squaredDistance = euclideanSquaredDistance(point, nodePoint);
			if(squaredDistance < bestDistanceSquare){
				bestDistanceSquare = squaredDistance;
				bestNode = node;
			}
			if(bestDistanceSquare <= precisionSquare)
				break;

			final double coordinateDelta = euclideanAxisDistance(nodePoint, point, currentItem.axis);
			final int axis = getNextAxis(currentItem.axis, dimensions);
			if(coordinateDelta > 0. && node.left != null){
				stack.push(new NodeAxisItem(node.left, axis));
				if(coordinateDelta * coordinateDelta < bestDistanceSquare)
					stack.push(new NodeAxisItem(node.left, axis));
			}
			else if(coordinateDelta <= 0. && node.right != null){
				stack.push(new NodeAxisItem(node.right, axis));
				if(coordinateDelta * coordinateDelta < bestDistanceSquare)
					stack.push(new NodeAxisItem(node.right, axis));
			}
		}

		return (bestNode != null? bestNode.point: null);
	}

	/** Return squared distance between two points. */
	private static double euclideanSquaredDistance(final Point point1, final Point point2){
		double squaredDistance = 0.;
		for(int i = 0; i < point1.getDimensions(); i ++){
			final double delta = euclideanAxisDistance(point1, point2, i);
			squaredDistance += delta * delta;
		}
		return squaredDistance;
	}

	/** Return distance between one axis only. */
	private static double euclideanAxisDistance(final Point point1, final Point point2, final int axis){
		return point1.getCoordinate(axis) - point2.getCoordinate(axis);
	}

	private static class NodeAxisItem{
		final KDNode node;
		final int axis;

		private NodeAxisItem(final KDNode node, final int axis){
			this.node = node;
			this.axis = axis;
		}
	}


	@Override
	public Collection<Point> query(final Point rangeMin, final Point rangeMax){
		final Deque<Point> points = new ArrayDeque<>();
		if(isEmpty())
			return points;

		final Deque<NodeAxisItem> stack = new ArrayDeque<>();
		stack.push(new NodeAxisItem(root, STARTING_DIMENSION));
		while(!stack.isEmpty()){
			final NodeAxisItem item = stack.pop();
			final KDNode node = item.node;
			final int axis = item.axis;

			//add contained points to points stack if inside the region
			final Point point = node.point;
			if(inside(point, rangeMin, rangeMax))
				points.push(point);

			final int nextAxis = getNextAxis(axis, comparators.length);
			if(node.left != null && point.getCoordinate(axis) >= rangeMin.getCoordinate(axis))
				stack.push(new NodeAxisItem(node.left, nextAxis));
			if(node.right != null && point.getCoordinate(axis) <= rangeMax.getCoordinate(axis))
				stack.push(new NodeAxisItem(node.right, nextAxis));
		}
		return points;
	}

	/**
	 * Tests if the point intersects (lies inside) the region.
	 *
	 * @param point	The point to be tested.
	 * @param rangeMin	Minimum point of the searching rectangle.
	 * @param rangeMax	Maximum point of the searching rectangle.
	 * @return	Whether the point lies inside the rectangle.
	 */
	private static boolean inside(final Point point, final Point rangeMin, final Point rangeMax){
		for(int axis = 0; axis < point.getDimensions(); axis ++){
			final double pointCoordinate = point.getCoordinate(axis);
			if(pointCoordinate < rangeMin.getCoordinate(axis) || pointCoordinate > rangeMax.getCoordinate(axis))
				return false;
		}

		return true;
	}


	//FIXME: another way to choose the cutting dimension is to calculate the variance of all values in each dimension and the largest one
	// will be chosen as the cutting dimension (knowledge of overall points is needed). The larger variance means data is more scatter on
	// the axis, so that we can split data better in this way.
	//FIXME: squarish k-d trees - When a rectangle is split by a newly inserted point, the longest side of the rectangle is cut (knowledge
	// of local BB is needed). Better performance for range search.
	private static int getNextAxis(final int currentAxis, final int dimensions){
		return (currentAxis + 1) % dimensions;
	}

}
