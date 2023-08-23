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
package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import io.github.mtrevisan.mapmatcher.helpers.MathHelper;
import io.github.mtrevisan.mapmatcher.helpers.QuickSelect;
import io.github.mtrevisan.mapmatcher.helpers.Tree;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;


/**
 * A <em>k</em>-d Tree.
 * <p>
 * Algorithm	Average		Worst case
 * Space			O(n)			O(n)
 * Search		O(log(n))	O(n)
 * Insert		O(log(n))	O(n)
 * Delete		O(log(n))	O(n)
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/K-d_tree">H<em>k</em>-d tree</a>
 * @see <a href="https://github.com/mgruben/Kd-Trees">Kd-Trees</a>
 * @see <a href="https://github.com/amay12/SpatialSearch">SpatialSearch</a>
 * @see <a href="https://github.com/mgruben/Kd-Trees/blob/master/KdTree.java">KdTree</a>
 */
public class KDTree implements Tree{

	private KDNode root;


	public static KDTree ofEmpty(){
		return new KDTree();
	}

	public static KDTree of(final Collection<Point> points){
		return new KDTree(points);
	}


	private KDTree(){}

	private KDTree(final Collection<Point> points){
		root = makeTree(points, 0, points.size(), 0);
	}

	private KDNode makeTree(final Collection<Point> points, final int begin, final int end, final int depth){
		if(end <= begin || points.isEmpty())
			return null;

		final List<KDNode> nodes = new ArrayList<>(points.size());
		for(final Point point : points)
			nodes.add(new KDNode(point));

		//extract dimensions from first point
		int dimensions = -1;
		for(final Point point : points){
			dimensions = point.getDimensions();
			break;
		}

		KDNode root = null;
		final Stack<Range> stack = new Stack<>();
		stack.push(new Range(begin, end, depth));

		while(!stack.isEmpty()){
			final Range range = stack.pop();
			final int start = range.begin;
			final int stop = range.end;
			int currentDepth = range.depth;

			if(stop <= start)
				continue;

			final int n = start + (stop - start) / 2;
			final KDNode node = QuickSelect.select(nodes, start, stop - 1, n, new NodeComparator(currentDepth));
			currentDepth = (currentDepth + 1) % dimensions;
			node.left = null;
			node.right = null;

			if(root == null)
				root = node;
			else{
				KDNode parent = null;
				KDNode current = root;

				while(current != null){
					parent = current;
					if(node.point.getCoordinate(currentDepth) < current.point.getCoordinate(currentDepth))
						current = current.left;
					else
						current = current.right;
				}

				if(node.point.getCoordinate(currentDepth) < parent.point.getCoordinate(currentDepth))
					parent.left = node;
				else
					parent.right = node;
			}

			stack.push(new Range(start, n, currentDepth));
			stack.push(new Range(n + 1, stop, currentDepth));
		}

		return root;
	}

	private static class Range{
		int begin;
		int end;
		int depth;

		Range(final int begin, final int end, final int depth){
			this.begin = begin;
			this.end = end;
			this.depth = depth;
		}
	}

	private KDNode makeTree2(final Collection<Point> points, final int begin, final int end, int depth){
		if(end <= begin || points.isEmpty())
			return null;

		final List<KDNode> nodes = new ArrayList<>(points.size());
		for(final Point point : points)
			nodes.add(new KDNode(point));

		final int n = begin + (end - begin) / 2;
		final KDNode parentNode = QuickSelect.select(nodes, begin, end - 1, n, new NodeComparator(depth));
if(parentNode.point.equals(parentNode.point.getFactory().createPoint(14.483_012, 40.741_087), 0.000_001))
System.out.println();

		//extract dimensions from first point
		int dimensions = -1;
		for(final Point point : points){
			dimensions = point.getDimensions();
			break;
		}

		depth = (depth + 1) % dimensions;
		parentNode.left = makeTree(points, begin, n, depth);
		parentNode.right = makeTree(points, n + 1, end, depth);
		return parentNode;
	}

	private static class NodeComparator implements Comparator<KDNode>{

		private final int index;

		private NodeComparator(final int index){
			this.index = index;
		}

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
	public KDNode insert(final Point point){
		return insert(root, point, 0);
	}

	public KDNode insert(KDNode currentNode, final Point point, final int depth){
		KDNode parentNode = null;
		int currentDepth = depth;
		while(currentNode != null){
			parentNode = currentNode;

			//select axis based on depth so that axis cycles through all valid values
			final int currentDimension = currentDepth % point.getDimensions();

			if(point.getCoordinate(currentDimension) < currentNode.point.getCoordinate(currentDimension))
				currentNode = currentNode.left;
			else
				currentNode = currentNode.right;

			currentDepth ++;
		}

		final KDNode newNode = new KDNode(point);
		if(parentNode == null){
			if(isEmpty())
				root = newNode;

			return newNode;
		}
		else{
			final int currentDimension = (currentDepth - 1) % point.getDimensions();
			if(point.getCoordinate(currentDimension) < parentNode.point.getCoordinate(currentDimension))
				parentNode.left = newNode;
			else
				parentNode.right = newNode;

			return root;
		}
	}

	public boolean query(final Point point){
		return query(root, point, 0);
	}

	public boolean query(KDNode currentNode, final Point point, final int depth){
		if(isEmpty())
			return false;

		int currentDepth = depth;
		while(currentNode != null){
			if(currentNode.point.equals(point, 0.000_001))
				return true;

			final int currentDimension = currentDepth % point.getDimensions();
			if(point.getCoordinate(currentDimension) < currentNode.point.getCoordinate(currentDimension))
				currentNode = currentNode.left;
			else
				currentNode = currentNode.right;

			currentDepth ++;
		}

		return false;
	}


	public KDNode nearestNeighbour(final Point point){
		if(isEmpty())
			return null;

		KDNode currentNode = root;
		KDNode bestNode = root;
		final Stack<KDNode> nodeStack = new Stack<>();
		int currentDepth = 0;
		double minDistance = Double.MAX_VALUE;
		while(currentNode != null){
			final double distanceFromNode = currentNode.point.distance(point);
			if(distanceFromNode < minDistance){
				minDistance = distanceFromNode;
				bestNode = currentNode;
			}
			if(MathHelper.nearlyEqual(distanceFromNode, 0., 0.000_001))
				break;

			KDNode nextNode;
			if(currentNode.left == null)
				nextNode = currentNode.right;
			else if(currentNode.right == null)
				nextNode = currentNode.left;
			else{
				final int currentDimension = currentDepth % point.getDimensions();
				if(root.point.getCoordinate(currentDimension) > bestNode.point.getCoordinate(currentDimension))
					nextNode = currentNode.left;
				else
					nextNode = currentNode.right;
			}
			if(nextNode != null)
				nodeStack.push(nextNode);

			currentNode = (!nodeStack.isEmpty()? nodeStack.pop(): null);
			currentDepth ++;
		}

		return bestNode;
	}

}
