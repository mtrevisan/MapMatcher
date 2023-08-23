package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import io.github.mtrevisan.mapmatcher.helpers.SpatialNode;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;


public class KDNode implements SpatialNode{

	public Point point;

	public KDNode left;
	public KDNode right;


	public KDNode(final double[] point, final GeometryFactory factory){
		this.point = Point.of(factory, point[1], point[0]);
	}

	public KDNode(final Point point){
		this.point = point;
	}

}
