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
import org.agrona.collections.Int2ObjectHashMap;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;


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
 * @see <a href="https://github.com/tzaeschke/tinspin-indexes/blob/master/src/main/java/org/tinspin/index/kdtree/KDTree.java">KDTree.java</a>
 *
 * @see <a href="https://pdf.sciencedirectassets.com/272575/1-s2.0-S0890540120X00046/1-s2.0-S0890540120300067/am.pdf?X-Amz-Security-Token=IQoJb3JpZ2luX2VjEH0aCXVzLWVhc3QtMSJGMEQCID%2FOQkedFMLgQQeZU01sTNCf8n%2FD297moJAmvzCLtizwAiAYrrd6sDhQbtqtN9Oe90aExQaQqDJtoDGTej5S7sXLISqzBQhGEAUaDDA1OTAwMzU0Njg2NSIMIvVFfgUuL%2B9Y9fIZKpAFEZVTGJagjKn2Ty4zlUI8FhXQAJppk%2FO9sv2W1XdR0Tusv39Ung9VnuFMCKLUGvij0ZQMXNrRtcJ5ChzAOobLkYij5MzC2x18iMFLkqyNhsRVWHC%2BLgO9bSczj1iFDsBUE6CPMuy0RKqzQWZf7pZ31VcfNIPPXksN6CM%2Ba5tvgpWdk%2B0mo0uRRnI%2FFEh3Ua5ulU4Ouoe%2B3D9IRtCR9IiETYB3x8aYnf76vgQWoA3akV006aq%2Foq4Z%2FyAFHa7RL%2F446uaQ6UmKuLeIBuzEI3bliAuZ6RAByyM1L8YHry0xcyT0g3fDj6P%2BafW3DoAQ%2Fa7OH%2BFnbLYd2AZa6yc85O1S1PJhk3J7wpmIm8S1YWBFK6X1LCHq6aw9KctXjPk8NLJzfPELw2%2FHya86T3zoRowOgHsv3MYWQwh5LGWTArgkr7Wlwio%2BXhiKqTyOU4KkKP8%2BRN84is9mmK22Ymysawb%2B5ydTTJ4qti0cqLvWWBSMYxreuUY%2Bcalkrl%2BmWFq9kIrc6Mlxxm6D5lEOutttLSmHgSO6Fgg99Q7EBAcV85CvwvYdCJpKQpj%2Fz%2FfPqNgqApCM6Hv2ly4JwL%2BJDS5ynjpfpiWGL1y0u9TGGU55Iedqd0VSKTFt4gQhjzZCRfolM0WN2TLQUk3tLRWJa8RYO0F0mWZUK39CHvbt993NZhHYBJ07SMo7ZO2jdbMSTBU%2Ffqgf0%2BKqm4Rk3CXn5u4msPl3ySxjBXiATF8Tnjgdw9u3%2FLwvQKZ%2Fw4IMQJYCSDTqspCY7qEnOD8Xy8cslYXf3qrY%2ByXdmnYVLAecmqxiqNOYB%2BEe6mgSgI4mJ6Y%2BjQypprGM0k2p5oAsPLOjQr9Ounxo%2B3fuc9NIsxsqQwYxT0q7kccwhsOipwY6sgEW0xqMICCN%2F6cevXo24ByYqnnhrWL22RepDBbBIpbNrtW1cg6QWDv4FggkFeiGPYHiWVb0FzxXnEcmhbyCuUzikHnP0jfBmTqeRzE9JGnEwX7%2BQGSWPP78Ldf72AN6MF0ntOclo2zqQG1O5IdVf%2FlF2X7u66d42uDQjVx7%2FcHsbcDt512hGv4uM4rOyjJ0ZaeBWJlp0WOLcc0mUpeGndvCiS%2Fql9g09wjDVqCQ4Et8T9bL&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20230825T135930Z&X-Amz-SignedHeaders=host&X-Amz-Expires=300&X-Amz-Credential=ASIAQ3PHCVTYWQGM27NF%2F20230825%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=1a6b00ba45a9de7e809732eaf31400961e1df973c0f9e2c09ff3c0d57ab5b123&hash=d6a51e61d9eb7c337d50ffcf1e1de33523fb7ac2668570db1584362770054911&host=68042c943591013ac2b2430a89b270f6af2c76d8dfd086a07176afe7c76c2c61&pii=S0890540120300067&tid=pdf-c498830d-40cc-4814-9761-fd1012133215&sid=bfe742ce2a4129440d39b6e85d84a8e8bad6gxrqb&type=client">Compact and Succinct Data Structures for Multidimensional Orthogonal Range Searching</a>
 * @see <a href="https://iq.opengenus.org/succinct-0-1-encoding-of-binary-tree/">Succinct (0-1) Encoding of Binary Tree</a>
 */
public class SuccinctKDTree implements SpatialTree{

	private static final int ROOT_INDEX = 0;
	private static final int STARTING_DIMENSION = 0;


	//(min-max) 0-33 bit/node vs 64-256 bit/node, that is 12.9% wrt simple k-d tree
	private final BitSet structure = new BitSet();
	//the maximum number of nodes in a binary tree of height `h` is `2^h – 1`, therefore in order to contain at least `n` nodes, the
	//tree has to have at least h = log2(n + 1)
	//final int h = (int)Math.ceil(Math.log(maxSize + 1) / LOG2);
	//data = new Int2ObjectHashMap<>(1 << h, (float)maxSize / (1 << h));
	private final Int2ObjectHashMap<Point> data = new Int2ObjectHashMap<>();

	private Comparator<Point>[] comparators;


	public static SuccinctKDTree ofDimensions(final int dimensions){
		return new SuccinctKDTree(dimensions);
	}

	public static SuccinctKDTree ofPoints(final List<Point> points){
		return new SuccinctKDTree(points);
	}


	private SuccinctKDTree(final int dimensions){
		if(dimensions < 1)
			throw new IllegalArgumentException ("K-D Tree must have at least dimension 1");

		createComparators(dimensions);
	}

	/**
	 * Constructor for creating a (balanced) median k-d tree.
	 *
	 * @param points	A collection of {@link Point}s.
	 */
	private SuccinctKDTree(final List<Point> points){
		if(points.isEmpty())
			throw new IllegalArgumentException("List of points cannot be empty");

		buildTree(points);
	}

	private void buildTree(final List<Point> points){
		//extract dimensions from first point
		for(final Point point : points){
			createComparators(point.getDimensions());
			break;
		}

		final Deque<BuildTreeParams> stack = new ArrayDeque<>();
		if(!points.isEmpty())
			stack.push(BuildTreeParams.asRoot(points.size()));
		while(!stack.isEmpty()){
			final BuildTreeParams params = stack.pop();
			final int parent = params.parent;
			final int begin = params.begin;
			final int end = params.end;
			int axis = params.axis;

			//extract the splitting node
			final int middle = begin + ((end - begin) >> 1);
			final Point median = QuickSelect.select(points, begin, end - 1, middle, comparators[axis]);

			axis = getNextAxis(axis, comparators.length);
			if(begin < middle)
				stack.push(new BuildTreeParams(leftIndex(parent), axis, begin, middle));
			if(middle + 1 < end)
				stack.push(new BuildTreeParams(rightIndex(parent), axis, middle + 1, end));

			addNode(parent, median, structure, data);
		}
	}

	private static class BuildTreeParams{
		private final int parent;
		private final int axis;
		private final int begin;
		private final int end;

		private static BuildTreeParams asRoot(final int size){
			return new BuildTreeParams(ROOT_INDEX, STARTING_DIMENSION, 0, size);
		}

		private BuildTreeParams(final int parent, final int axis, final int begin, final int end){
			this.parent = parent;
			this.axis = axis;
			this.begin = begin;
			this.end = end;
		}
	}

	@SuppressWarnings("unchecked")
	private void createComparators(final int dimensions){
		comparators = (Comparator<Point>[])Array.newInstance(Comparator.class, dimensions);
		for(int i = 0; i < dimensions; i ++)
			comparators[i] = new NodeComparator(i);
	}

	private static class NodeComparator implements Comparator<Point>{
		private final int axis;

		private NodeComparator(final int axis){
			this.axis = axis;
		}

		@Override
		public int compare(final Point point1, final Point point2){
			return Double.compare(point1.getCoordinate(axis), point2.getCoordinate(axis));
		}
	}

	public void rebalance(){
		//extract all points
		final List<Point> points = new ArrayList<>(data.values());

		structure.clear();
		data.clear();

		//reinsert
		buildTree(points);
	}


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
	public void insert(final Point point){
		final int dimensions = comparators.length;
		if(point.getDimensions() < dimensions)
			throw new IllegalArgumentException("Point dimensions are less than what is specified constructing this tree");

		if(isEmpty())
			addNode(ROOT_INDEX, point, structure, data);
		else{
			int parent = ROOT_INDEX;
			//traverse the tree and find the parent node:
			int parentNode = -1;
			int axis = STARTING_DIMENSION;
			boolean goLeft = false;
			Point currentPoint;
			while((currentPoint = data.get(parent)) != null){
				parentNode = parent;

				goLeft = (point.getCoordinate(axis) < currentPoint.getCoordinate(axis));
				parent = (goLeft? leftIndex(parent): rightIndex(parent));

				axis = getNextAxis(axis, dimensions);
			}

			//add new leaf node to the tree
			final int newNode = (goLeft
				? leftIndex(parentNode)
				: rightIndex(parentNode));
			//Note: if `newNode < 0`, then add point to `parentNode` (max size of structure is reached)
			//TODO manage multi-point per node
			addNode(newNode, point, structure, data);
		}
	}


	@Override
	public boolean contains(final Point point){
		if(point == null)
			return false;
		final int dimensions = comparators.length;
		if(point.getDimensions() < dimensions)
			throw new IllegalArgumentException("Point dimensions are less than what is specified constructing this tree");

		final double precision = point.getDistanceCalculator()
			.getPrecision();

		int currentNode = ROOT_INDEX;
		int axis = STARTING_DIMENSION;
		Point currentPoint;
		while((currentPoint = data.get(currentNode)) != null){
			if(currentPoint.equals(point, precision))
				return true;

			currentNode = (point.getCoordinate(axis) < currentPoint.getCoordinate(axis)
				? leftIndex(currentNode)
				: rightIndex(currentNode));

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

		final int dimensions = comparators.length;;
		if(point.getDimensions() < dimensions)
			throw new IllegalArgumentException("Point dimensions are less than what is specified constructing this tree");

		int bestNode = -1;
		double bestSquaredDistance = Double.POSITIVE_INFINITY;
		double squaredPrecision = point.getDistanceCalculator()
			.getPrecision();
		squaredPrecision *= squaredPrecision;

		final Deque<Integer> stack = new ArrayDeque<>();
		stack.push(STARTING_DIMENSION);
		stack.push(SuccinctKDTree.ROOT_INDEX);
		while(!stack.isEmpty()){
			final int node = stack.pop();
			final int axis = stack.pop();

			final Point nodePoint = data.get(node);
			final double squaredDistance = euclideanSquaredDistance(point, nodePoint);
			if(squaredDistance < bestSquaredDistance){
				bestSquaredDistance = squaredDistance;
				bestNode = node;

				//early exit
				if(bestSquaredDistance <= squaredPrecision)
					break;
			}

			final double coordinateDelta = euclideanAxisDistance(nodePoint, point, axis);
			final int nextAxis = getNextAxis(axis, dimensions);
			if(coordinateDelta > 0. && structure.get(leftIndex(node))){
				stack.push(nextAxis);
				stack.push(leftIndex(node));
			}
			else if(coordinateDelta <= 0. && structure.get(rightIndex(node))){
				stack.push(nextAxis);
				stack.push(rightIndex(node));
			}
		}

		return (bestNode >= 0? data.get(bestNode): null);
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

		final Deque<Integer> stack = new ArrayDeque<>();
		stack.push(ROOT_INDEX);
		stack.push(STARTING_DIMENSION);
		while(!stack.isEmpty()){
			final int node = stack.pop();
			final int axis = stack.pop();

			//add contained points to points stack if inside the region
			final Point point = data.get(node);
			if(inside(point, rangeMin, rangeMax))
				points.add(point);

			final int nextAxis = getNextAxis(axis, comparators.length);
			if(structure.get(leftIndex(node)) && point.getCoordinate(axis) >= rangeMin.getCoordinate(axis)){
				stack.push(leftIndex(node));
				stack.push(nextAxis);
			}
			if(structure.get(rightIndex(node)) && point.getCoordinate(axis) <= rangeMax.getCoordinate(axis)){
				stack.push(rightIndex(node));
				stack.push(nextAxis);
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
	private static int getNextAxis(final int currentAxis, final int dimensions){
		return (currentAxis + 1) % dimensions;
	}


	private static int leftIndex(final int parentIndex){
		return (parentIndex << 1) + 1;
	}

	private static int rightIndex(final int parentIndex){
		return (parentIndex << 1) + 2;
	}

	private static void addNode(final int index, final Point point, final BitSet structure, final Map<Integer, Point> data){
		structure.set(index);
		addPoint(index, point, data);
	}

	private static void addPoint(final int index, final Point point, final Map<Integer, Point> data){
		data.put(index, point);
	}

}
