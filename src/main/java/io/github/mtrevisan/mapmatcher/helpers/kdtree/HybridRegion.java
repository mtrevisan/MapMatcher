package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import io.github.mtrevisan.mapmatcher.helpers.SpatialNode;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;


public class HybridRegion extends Region{

	private SpatialNode node;
	private boolean boundary;


	HybridRegion(final Region region){
		super(region);
	}

	public SpatialNode getNode(){
		return node;
	}

	public void setNode(final SpatialNode node){
		this.node = node;
	}

	public boolean isBoundary(){
		return boundary;
	}

	public void setBoundary(){
		boundary = true;
	}

}
