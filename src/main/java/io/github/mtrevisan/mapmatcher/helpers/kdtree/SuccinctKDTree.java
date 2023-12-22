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

import io.github.mtrevisan.mapmatcher.helpers.SpatialTree;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import org.agrona.collections.Long2ObjectHashMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
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
 * @see <a href="https://en.wikipedia.org/wiki/Binary_tree">Binary tree</a>
 * @see <a href="https://en.wikipedia.org/wiki/K-d_tree">H<em>k</em>-d tree</a>
 * @see <a href="https://yasenh.github.io/post/kd-tree/">KD-Tree</a>
 * @see <a href="https://github.com/mgruben/Kd-Trees">Kd-Trees</a>
 * @see <a href="https://github.com/amay12/SpatialSearch">SpatialSearch</a>
 * @see <a href="https://github.com/mgruben/Kd-Trees/blob/master/KdTree.java">KdTree</a>
 * @see <a href="https://core.ac.uk/download/pdf/19529722.pdf">Multicore construction of k-d trees with applications in graphics and vision</a>
 * @see <a href="https://www.ri.cmu.edu/pub_files/pub1/moore_andrew_1991_1/moore_andrew_1991_1.pdf">An introductory tutorial on kd-trees</a>
 * @see <a href="https://arxiv.org/ftp/arxiv/papers/2106/2106.03799.pdf">Deterministic iteratively build KD-Tree with KNN search for exact applications</a>
 * @see <a href="https://upcommons.upc.edu/bitstream/handle/2099.1/11309/MasterMercePons.pdf?sequence=1&isAllowed=y">Design, analysis and implementation of new variants of Kd-trees</a>
 * @see <a href="https://github.com/tzaeschke/tinspin-indexes/blob/master/src/main/java/org/tinspin/index/kdtree/KDTree.java">KDTree.java</a>
 *
 * @see <a href="https://www.sciencedirect.com/science/article/abs/pii/S0890540120300067?fr=RR-2&ref=pdf_download&rr=80c1eafc9fcbfc8d">Compact and Succinct Data Structures for Multidimensional Orthogonal Range Searching</a>
 * @see <a href="https://iq.opengenus.org/succinct-0-1-encoding-of-binary-tree/">Succinct (0-1) Encoding of Binary Tree</a>
 * How to store tree by serializing it:
 * @see <a href="https://opendsa-server.cs.vt.edu/ODSA/Books/CS3/html/SequentialRep.html">Sequential Tree Representations</a>
 * @see <a href="https://www.researchgate.net/publication/259479421_Tree_Compression_and_Optimization_with_Applications">Tree compression and optimization with applications</a>
 */
public class SuccinctKDTree implements SpatialTree{

	private static final long NO_NODE = -1l;
	private static final long ROOT_INDEX = 0l;
	private static final int STARTING_DIMENSION = 0;
	private static final int DIMENSIONS = 2;


	private final Long2ObjectHashMap<Point> data = new Long2ObjectHashMap<>();


	public static SuccinctKDTree create(){
		return new SuccinctKDTree();
	}


	private SuccinctKDTree(){}


	@Override
	public boolean isEmpty(){
		return data.isEmpty();
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
	public void insert(final Point point) throws MaximumTreeDepthReachedException{
		if(isEmpty())
			addNode(ROOT_INDEX, point);
		else{
			long parent = ROOT_INDEX;
			//traverse the tree and find the parent node:
			long parentNode = NO_NODE;
			int axis = STARTING_DIMENSION;
			boolean goLeft = false;
			Point currentPoint;
			while((currentPoint = getData(parent)) != null){
				parentNode = parent;

				goLeft = (euclideanAxisDistance(point, currentPoint, axis) < 0.);
				parent = (goLeft? leftIndex(parent): rightIndex(parent));

				axis = getNextAxis(axis);
			}

			//add new leaf node to the tree
			final long newNode = (goLeft
				? leftIndex(parentNode)
				: rightIndex(parentNode));
			if(newNode < 0l)
				throw MaximumTreeDepthReachedException.create();
			addNode(newNode, point);
		}
	}


	@Override
	public boolean contains(final Point point){
		if(point == null)
			return false;

		final double precision = point.getDistanceCalculator()
			.getPrecision();

		long currentNode = ROOT_INDEX;
		int axis = STARTING_DIMENSION;
		Point currentPoint;
		while((currentPoint = getData(currentNode)) != null){
			if(currentPoint.equals(point, precision))
				return true;

			currentNode = (euclideanAxisDistance(point, currentPoint, axis) < 0.
				? leftIndex(currentNode)
				: rightIndex(currentNode));

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
	public Point nearestNeighbor(final Point point){
		if(isEmpty() || point == null)
			return null;

		long bestNode = NO_NODE;
		double bestSquaredDistance = Double.POSITIVE_INFINITY;
		double squaredPrecision = point.getDistanceCalculator()
			.getPrecision();
		squaredPrecision *= squaredPrecision;

		final Deque<Long> stack = new ArrayDeque<>();
		stack.push((long)STARTING_DIMENSION);
		stack.push(ROOT_INDEX);
		while(!stack.isEmpty()){
			final long node = stack.pop();
			final int axis = stack.pop()
				.intValue();

			//find closest node
			final Point nodePoint = getData(node);
			final double squaredDistance = euclideanSquaredDistance(point, nodePoint);
			if(squaredDistance < bestSquaredDistance){
				bestSquaredDistance = squaredDistance;
				bestNode = node;

				//early exit
				if(bestSquaredDistance <= squaredPrecision)
					break;
			}

			final double coordinateDelta = euclideanAxisDistance(nodePoint, point, axis);
			if(coordinateDelta > 0.){
				final long leftIndex = leftIndex(node);
				if(hasNode(leftIndex)){
					stack.push((long)getNextAxis(axis));
					stack.push(leftIndex);
				}
			}
			else{
				final long rightIndex = rightIndex(node);
				if(hasNode(rightIndex)){
					stack.push((long)getNextAxis(axis));
					stack.push(rightIndex);
				}
			}
		}

		return getData(bestNode);
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


	@Override
	public Collection<Point> query(final Point rangeMin, final Point rangeMax){
		final List<Point> points = new ArrayList<>();
		if(isEmpty())
			return points;

		final Deque<Long> stack = new ArrayDeque<>();
		stack.push((long)STARTING_DIMENSION);
		stack.push(ROOT_INDEX);
		while(!stack.isEmpty()){
			final long node = stack.pop();
			final int axis = stack.pop()
				.intValue();

			//add contained points to points stack if inside the region
			final Point point = getData(node);
			if(inside(point, rangeMin, rangeMax))
				points.add(point);

			final int nextAxis = getNextAxis(axis);
			final long leftIndex = leftIndex(node);
			if(hasNode(leftIndex) && euclideanAxisDistance(point, rangeMin, axis) >= 0.){
				stack.push((long)nextAxis);
				stack.push(leftIndex);
			}
			final long rightIndex = rightIndex(node);
			if(hasNode(rightIndex) && euclideanAxisDistance(point, rangeMax, axis) <= 0.){
				stack.push((long)nextAxis);
				stack.push(rightIndex);
			}
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
	private static int getNextAxis(final int currentAxis){
		return (currentAxis + 1) % DIMENSIONS;
	}


	private static long leftIndex(final long parentIndex){
		return (parentIndex << 1) + 1;
	}

	private static long rightIndex(final long parentIndex){
		return (parentIndex << 1) + 2;
	}

	private boolean hasNode(final long index){
		return data.containsKey(index);
	}

	private Point getData(final long index){
		return data.get(index);
	}

	private void addNode(final long index, final Point point){
		data.put(index, point);
	}

}
