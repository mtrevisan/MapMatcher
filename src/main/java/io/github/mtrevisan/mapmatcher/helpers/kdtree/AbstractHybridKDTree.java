package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import io.github.mtrevisan.mapmatcher.helpers.RegionTree;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.Collection;


//TODO
public class AbstractHybridKDTree{

	public void insert(final RegionTree tree, final Region region){
		tree.insert(region);
	}

	public void insert(final RegionTree tree, final Region region, final Point point){
		final Collection<Region> regions = regionsInRange(tree, region);

		if(!regions.isEmpty())
			for(final Region queriedRegion : regions)
				if(queriedRegion.isBoundary()){
					final KDTree terminalTree = KDTree.ofEmpty(2);
					final KDNode parentNode = (KDNode)queriedRegion.getNode();
					terminalTree.insert(parentNode, point);
					return;
				}

		region.setBoundary();
		region.setNode(new KDNode(point));
		tree.insert(region);
	}

//	public void insert(final RegionTree tree, final Region region, final List<Point> points){
//		final Collection<Region> regions = regionsInRange(tree, region);
//
//		if(!regions.isEmpty())
//			for(final Region queriedRegion : regions)
//				if(queriedRegion.isBoundary()){
//					final KDNode parentNode = (KDNode)queriedRegion.getNode();
//					final KDTree terminalTree = KDTree.of(parentNode, points);
//					return;
//				}
//
//		region.setBoundary();
//		region.setNode(new KDNode(points));
//		tree.insert(region);
//	}


	public Collection<Region> regionsInRange(final RegionTree tree, final Region region){
		return tree.query(region);
	}

	public boolean contains(final RegionTree tree, final Region region, final Point point){
		final Collection<Region> regions = regionsInRange(tree, region);

		if(!regions.isEmpty())
			for(final Region queriedRegion : regions)
				if(queriedRegion.isBoundary()){
					final KDTree kdTree = KDTree.ofEmpty(2);
					final KDNode kdNode = (KDNode)queriedRegion.getNode();
					if(kdTree.contains(kdNode, point))
						return true;
				}

		return false;
	}

}
