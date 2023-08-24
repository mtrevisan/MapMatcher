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
import io.github.mtrevisan.mapmatcher.helpers.Tree;
import io.github.mtrevisan.mapmatcher.spatial.Envelope;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;


/**
 * A k-dimensional Tree.
 * <p>
 * Algorithm		Average		Worst case
 * Space				O(n)			O(n)
 * Search			O(log(n))	O(n)
 * Range search	?				O(k · n^(1 - 1/k))
 * Insert			O(log(n))	O(n)
 * </p>
 * <p>
 * Note that it is not required to select the median point upon construction of the tree. In the case where median points are not selected,
 * there is no guarantee that the tree will be balanced.<br />
 * Having a balanced tree that evenly splits regions improves the search time.
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
 */
public class KDTree implements Tree{

	private static final double PRECISION = 1.e-8;

	private KDNode root;


	public static KDTree ofEmpty(){
		return new KDTree();
	}

	public static KDTree of(final Collection<Point> points){
		return new KDTree(points);
	}


	private KDTree(){}

	private KDTree(final Collection<Point> points){
		final List<KDNode> nodes = new ArrayList<>(points.size());
		for(final Point point : points){
			final KDNode node = new KDNode(point);
			nodes.add(node);
		}

		//FIXME
		root = buildTree(nodes, 0, nodes.size(), 0);
//		root = buildTree(nodes);
	}

	private KDNode buildTree(List<KDNode> nodes, int begin, int end, int index){
		if(begin >= end)
			return null;

		//extract dimensions from first point
		int dimensions = -1;
		for(final KDNode node : nodes){
			dimensions = node.point.getDimensions();
			break;
		}

		final int k = begin + (end - begin) / 2;
		final KDNode node = QuickSelect.select(nodes, begin, end - 1, k, new NodeComparator(index));

		index = getNextIndex(index, dimensions);
		node.left = buildTree(nodes, begin, k, index);
		node.right = buildTree(nodes, k + 1, end, index);

		return node;
	}

	//FIXME
//	private KDNode buildTree(final List<KDNode> nodes){
//		//extract dimensions from first point
//		int dimensions = - 1;
//		for(final KDNode node : nodes){
//			dimensions = node.point.getDimensions();
//			break;
//		}
//
//		KDNode root = null;
//		final Stack<BuildTreeParams> stack = new Stack<>();
//		stack.push(new BuildTreeParams(0, nodes.size(), 0));
//
//		while(!stack.isEmpty()){
//			final BuildTreeParams params = stack.pop();
//			final int start = params.begin;
//			final int stop = params.end;
//			int currentIndex = params.index;
//
//			if(start >= stop)
//				continue;
//
//			final int k = start + (stop - start) / 2;
//			final KDNode node = QuickSelect.select(nodes, start, stop - 1, k, new NodeComparator(currentIndex));
//
//			currentIndex = getNextIndex(currentIndex, dimensions);
//			node.left = null;
//			node.right = null;
//
//			if(root == null)
//				root = node;
//			else{
//				KDNode parent = null;
//				KDNode current = root;
//				while(current != null){
//					parent = current;
//					if(node.point.getCoordinate(currentIndex) < current.point.getCoordinate(currentIndex))
//						current = current.left;
//					else
//						current = current.right;
//				}
//
//				if(node.point.getCoordinate(currentIndex) < parent.point.getCoordinate(currentIndex))
//					parent.left = node;
//				else
//					parent.right = node;
//			}
//
//			stack.push(new BuildTreeParams(start, k, currentIndex));
//			stack.push(new BuildTreeParams(k + 1, stop, currentIndex));
//		}
//
//		return root;
//	}

	private static class BuildTreeParams{
		final int begin;
		final int end;
		final int index;

		BuildTreeParams(final int begin, final int end, final int index){
			this.begin = begin;
			this.end = end;
			this.index = index;
		}
	}

	private static class NodeComparator implements Comparator<KDNode>{

		private final int index;

		private NodeComparator(final int index){
			this.index = index;
		}

		@Override
		public int compare(final KDNode node1, final KDNode node2){
			return Double.compare(node1.point.getCoordinate(index), node2.point.getCoordinate(index));
		}
	}


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
	 * @throws NullPointerException	If {@code p} is {@code null}.
	 */
	public void insert(final Point point){
		insert(root, point);
	}

	KDNode insert(KDNode parent, final Point point){
		KDNode parentNode = null;
		final int dimensions = point.getDimensions();
		int index = getNextIndex(dimensions - 1, dimensions);
		while(parent != null){
			parentNode = parent;

			if(point.getCoordinate(index) < parent.point.getCoordinate(index))
				parent = parent.left;
			else
				parent = parent.right;

			index = getNextIndex(index, dimensions);
		}

		final KDNode newNode = new KDNode(point);
		if(parentNode == null){
			if(isEmpty())
				root = newNode;

			return newNode;
		}
		else{
			index = getNextIndex(index, dimensions);
			if(point.getCoordinate(index) < parentNode.point.getCoordinate(index))
				parentNode.left = newNode;
			else
				parentNode.right = newNode;

			return root;
		}
	}


	public boolean contains(final Point point){
		return contains(root, point);
	}

	boolean contains(KDNode currentNode, final Point point){
		if(root == null)
			throw new IllegalStateException("Tree is empty");
		if(isEmpty())
			return false;

		final int dimensions = point.getDimensions();
		int index = 0;
		while(currentNode != null){
			if(currentNode.point.equals(point, PRECISION))
				return true;

			if(point.getCoordinate(index) < currentNode.point.getCoordinate(index))
				currentNode = currentNode.left;
			else
				currentNode = currentNode.right;

			index = getNextIndex(index, dimensions);
		}

		return false;
	}


	public Point nearestNeighbour(final Point point){
		if(root == null)
			throw new IllegalStateException("Tree is empty");
		if(isEmpty())
			return null;

		KDNode bestNode = null;
		double bestSquareDistance = 0.;
		final int dimensions = point.getDimensions();

		final Stack<NodeIndexItem> stack = new Stack<>();
		stack.push(new NodeIndexItem(root, 0));
		while(!stack.isEmpty()){
			final NodeIndexItem currentItem = stack.pop();
			final KDNode currentNode = currentItem.node;

			final double squareDistance = (currentNode.point.getX() - point.getX()) * (currentNode.point.getX() - point.getX())
				+ (currentNode.point.getY() - point.getY()) * (currentNode.point.getY() - point.getY());

			if(bestNode == null || squareDistance < bestSquareDistance){
				bestSquareDistance = squareDistance;
				bestNode = currentNode;
			}
			if(bestSquareDistance <= PRECISION * PRECISION)
				break;

			final double coordinateDelta = currentNode.point.getCoordinate(currentItem.index) - point.getCoordinate(currentItem.index);
			final int index = getNextIndex(currentItem.index, dimensions);
			if(coordinateDelta > 0. && currentNode.left != null){
				stack.push(new NodeIndexItem(currentNode.left, index));
				if(coordinateDelta * coordinateDelta < bestSquareDistance)
					stack.push(new NodeIndexItem(currentNode.left, index));
			}
			else if(coordinateDelta <= 0. && currentNode.right != null){
				stack.push(new NodeIndexItem(currentNode.right, index));
				if(coordinateDelta * coordinateDelta < bestSquareDistance)
					stack.push(new NodeIndexItem(currentNode.right, index));
			}
		}

		return (bestNode != null? bestNode.point: null);
	}

	private static class NodeIndexItem{
		final KDNode node;
		final int index;

		NodeIndexItem(final KDNode node, final int index){
			this.node = node;
			this.index = index;
		}
	}


	public Collection<Point> pointsInRange(final Envelope envelope){
		final Stack<Point> points = new Stack<>();
		if(isEmpty())
			return points;

		final Point rangeMin = root.point.getFactory().createPoint(envelope.getMinX(), envelope.getMinY());
		final Point rangeMax = root.point.getFactory().createPoint(envelope.getMaxX(), envelope.getMaxY());
		final int dimensions = root.point.getDimensions();
		int index = 0;

		final Stack<KDNode> nodes = new Stack<>();
		nodes.push(root);
		while(!nodes.isEmpty()){
			final KDNode node = nodes.pop();

			//add contained points to points stack
			if(envelope.intersects(node.point))
				points.push(node.point);

			if(node.left != null && node.point.getCoordinate(index) >= rangeMin.getCoordinate(index))
				nodes.push(node.left);
			if(node.right != null && node.point.getCoordinate(index) <= rangeMax.getCoordinate(index))
				nodes.push(node.right);

			index = getNextIndex(index, dimensions);
		}
		return points;
	}


	//FIXME: another way to choose the cutting dimension is to calculate the variance of all values in each dimension and the largest one
	// will be chosen as the cutting dimension. The larger variance means data is more scatter on the axis, so that we can split data better
	// in this way.
	private static int getNextIndex(final int currentIndex, final int dimensions){
		return (currentIndex + 1) % dimensions;
	}

}
