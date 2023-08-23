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
import io.github.mtrevisan.mapmatcher.helpers.NodeItem;
import io.github.mtrevisan.mapmatcher.helpers.SpatialNode;
import io.github.mtrevisan.mapmatcher.helpers.Tree;
import io.github.mtrevisan.mapmatcher.spatial.Envelope;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.List;
import java.util.Objects;


/**
 * A <em>k</em>-d Tree.
 *
 * Algorithm	Average		Worst case
 * Space			O(n)			O(n)
 * Search		O(log(n))	O(n)
 * Insert		O(log(n))	O(n)
 * Delete		O(log(n))	O(n)
 *
 * @see <a href="https://en.wikipedia.org/wiki/K-d_tree">H<em>k</em>-d tree</a>
 * @see <a href="https://github.com/mgruben/Kd-Trees">Kd-Trees</a>
 * @see <a href="https://github.com/amay12/SpatialSearch">SpatialSearch</a>
 */
public class KDTree implements Tree{

	public KDNode insert(final KDNode root, final Point point){
		return insertNode(root, new double[]{point.getX(), point.getY()}, 0);
	}

	private static KDNode insertNode(final KDNode root, final double[] dataPoint, final int depth){
		if(root == null)
			return new KDNode(dataPoint);

		final int currentDimension = depth % KDNode.DIMENSION;

		if(dataPoint[currentDimension] < (root.point[currentDimension]))
			root.left = insertNode(root.left, dataPoint, depth + 1);
		else
			root.right = insertNode(root.right, dataPoint, depth + 1);
		return root;
	}


	public boolean query(final KDNode root, final Point point){
		return searchNode(root, new double[]{point.getX(), point.getY()}, 0);
	}

	private static boolean searchNode(final KDNode root, final double[] point, final int depth){
		if(Objects.isNull(root))
			return false;
		if(isEqual(root.point, point))
			return true;

		final int currentDimension = depth % KDNode.DIMENSION;
		if(point[currentDimension] < root.point[currentDimension])
			return searchNode(root.left, point, depth + 1);
		else
			return searchNode(root.right, point, depth + 1);
	}


	public KDNode nearestNeighbour(final KDNode root, final Point point){
		return searchNearestNeighbour(root, new double[]{point.getX(), point.getY()}, Integer.MAX_VALUE, root);
	}

	private static KDNode searchNearestNeighbour(final KDNode root, final double[] dataPoint, double minDist, KDNode bestNode){
		if(root == null)
			return bestNode;

		final double distanceFromNode = euclideanDistance(root.point, dataPoint);
		if(euclideanDistance(root.point, dataPoint) < minDist){
			minDist = distanceFromNode;
			bestNode = root;
		}

		if(Objects.isNull(root.left))
			return searchNearestNeighbour(root.right, dataPoint, minDist, bestNode);

		if(Objects.isNull(root.right))
			return searchNearestNeighbour(root.left, dataPoint, minDist, bestNode);

		if(euclideanDistance(root.left.point, dataPoint) < euclideanDistance(root.right.point, dataPoint))
			bestNode = searchNearestNeighbour(root.left, dataPoint, minDist, bestNode);
		else
			bestNode = searchNearestNeighbour(root.right, dataPoint, minDist, bestNode);

		return bestNode;
	}

	private static double euclideanDistance(final double[] a, final double[] b){
		if(a == null || b == null)
			return Integer.MAX_VALUE;

		return Math.hypot(b[0] - a[0], b[1] - a[1]);
	}


	private static boolean isEqual(final double[] point1, double[] point2){
		for(int i = 0; i < KDNode.DIMENSION; i ++)
			if(!MathHelper.nearlyEqual(point1[i], point2[i], 0.000_001))
				return false;
		return true;
	}

}
