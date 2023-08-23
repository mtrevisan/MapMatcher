package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import io.github.mtrevisan.mapmatcher.helpers.SpatialNode;
import io.github.mtrevisan.mapmatcher.spatial.Point;


public class KDNode implements SpatialNode{

	public double[] point;
	public final static int DIMENSION = 2;

	public KDNode left;
	public KDNode right;


	public KDNode(final double[] point){
		this.point = new double[DIMENSION];
		System.arraycopy(point, 0, this.point, 0, point.length);
		left = null;
		right = null;
	}

	public KDNode(final Point point){
		this.point = new double[]{point.getX(), point.getY()};
		left = null;
		right = null;
	}

}
