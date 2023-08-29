package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import io.github.mtrevisan.mapmatcher.helpers.RegionTree;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.Collection;
import java.util.List;


public class AbstractHybridKDTree{

	public void insert(final RegionTree tree, final Region region){
		tree.insert(region);
	}

	public void insert(final RegionTree tree, final Region region, final Point point){
		final Collection<Region> regions = query(tree, region);

		if(!regions.isEmpty())
			for(final Region queriedRegion : regions)
				if(queriedRegion.isBoundary()){
					final KDTree terminalTree = KDTree.ofEmpty(point.getDimensions());
					final KDNode parentNode = (KDNode)queriedRegion.getNode();
					terminalTree.insert(parentNode, point);
					return;
				}

		//TODO extends Region, parké kusita 'l fà senso (?)
		//	ge xé un poblèma su RegionQuadTree.split(), kuando ke se krea un Region...
		region.setBoundary();
		region.setNode(new KDNode(point));
		tree.insert(region);
	}


	public Collection<Region> query(final RegionTree tree, final Region region){
		return tree.query(region);
	}

	public boolean contains(final RegionTree tree, final Region region, final Point point){
		final Collection<Region> regions = query(tree, region);

		if(!regions.isEmpty())
			for(final Region queriedRegion : regions)
				if(queriedRegion.isBoundary()){
					final KDTree terminalTree = KDTree.ofEmpty(point.getDimensions());
					final KDNode kdNode = (KDNode)queriedRegion.getNode();
					if(terminalTree.contains(kdNode, point))
						return true;
				}
		return false;
	}

}
