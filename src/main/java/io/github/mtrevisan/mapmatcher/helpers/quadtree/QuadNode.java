package io.github.mtrevisan.mapmatcher.helpers.quadtree;

import io.github.mtrevisan.mapmatcher.helpers.kdtree.Region;


public class QuadNode extends Region{

	QuadNode(final Region region){
		super(region.getX(), region.getY(), region.getWidth(), region.getHeight());
	}

}
