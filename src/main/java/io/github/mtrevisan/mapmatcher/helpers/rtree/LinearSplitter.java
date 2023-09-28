package io.github.mtrevisan.mapmatcher.helpers.rtree;

import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Least enlargement splitter using Guttman's linear split.
 */
class LinearSplitter implements NodeSplitter{

	private final int minObjects;


	public static LinearSplitter create(final RTreeOptions options){
		return new LinearSplitter(options);
	}


	private LinearSplitter(final RTreeOptions options){
		this.minObjects = options.minChildren;
	}


	@Override
	public Object[] pivotNode(final RNode node){
		//find the two nodes that maximizes the space waste, and assign them to a node:
		final LinkedList<RNode> seeds = new LinkedList<>(node.children);

		final int childrenCount = node.children.size();
		final List<RNode> childrenLesser = new ArrayList<>(childrenCount);
		final List<RNode> childrenGreater = new ArrayList<>(childrenCount);

		final RNode[] seedNodes = pickSeeds(seeds);
		if(seedNodes == null)
			return null;
		childrenLesser.add(seedNodes[0]);
		childrenGreater.add(seedNodes[1]);
		seeds.remove(seedNodes[0]);
		seeds.remove(seedNodes[1]);

		//examine remaining entries and add them to either `node` or `newNode` with the least enlargement criteria
		while(!seeds.isEmpty()){
			if(childrenLesser.size() + seeds.size() == minObjects){
				childrenLesser.addAll(seeds);

				break;
			}
			if(childrenGreater.size() + seeds.size() == minObjects){
				childrenGreater.addAll(seeds);

				break;
			}
//			if(childrenLesser.size() >= minObjects && childrenGreater.size() + seeds.size() == minObjects){
//				childrenGreater.addAll(seeds);
//
//				break;
//			}
//			if(childrenGreater.size() >= minObjects && childrenLesser.size() + seeds.size() == minObjects){
//				childrenLesser.addAll(seeds);
//
//				break;
//			}

			//add the next record to the node which will require the least enlargement:
			final RNode child = seeds.pop();
			final List<RNode> preferred = pickNext(child.region, childrenLesser, childrenGreater);
			preferred.add(child);
		}

		return new Object[]{childrenLesser, childrenGreater};
	}

	/** Find the two nodes that maximizes the space waste. */
	private static RNode[] pickSeeds(final List<RNode> nodes){
		RNode[] bestPair = null;
		double bestSeparation = 0.;
		double dimLowerBound = Double.POSITIVE_INFINITY;
		double dimUpperBound = Double.NEGATIVE_INFINITY;
		double dimMinUpperBound = Double.POSITIVE_INFINITY;
		double dimMaxLowerBound = Double.NEGATIVE_INFINITY;
		RNode nodeMaxLowerBound = null;
		RNode nodeMinUpperBound = null;
		final int size = nodes.size();
		for(int i = 0; i < size; i ++){
			final RNode node = nodes.get(i);

			final Region nodeRegion = node.region;
			if(nodeRegion.getMinX() < dimLowerBound)
				dimLowerBound = nodeRegion.getMinX();
			if(nodeRegion.getMinY() < dimLowerBound)
				dimLowerBound = nodeRegion.getMinY();
			if(nodeRegion.getMaxX() > dimUpperBound)
				dimUpperBound = nodeRegion.getMaxX();
			if(nodeRegion.getMaxY() > dimUpperBound)
				dimUpperBound = nodeRegion.getMaxY();

			if(nodeRegion.getMinX() > dimMaxLowerBound){
				dimMaxLowerBound = nodeRegion.getMinX();
				nodeMaxLowerBound = node;
			}
			if(nodeRegion.getMinY() > dimMaxLowerBound){
				dimMaxLowerBound = nodeRegion.getMinY();
				nodeMaxLowerBound = node;
			}
			if(nodeRegion.getMaxX() < dimMinUpperBound){
				dimMinUpperBound = nodeRegion.getMaxX();
				nodeMinUpperBound = node;
			}
			if(nodeRegion.getMaxY() < dimMinUpperBound){
				dimMinUpperBound = nodeRegion.getMaxY();
				nodeMinUpperBound = node;
			}

			final double separation = Math.abs((dimMinUpperBound - dimMaxLowerBound) / (dimUpperBound - dimLowerBound));
			if(separation > bestSeparation){
				bestPair = new RNode[]{nodeMaxLowerBound, nodeMinUpperBound};
				bestSeparation = separation;
			}
		}
		return bestPair;
	}

	private static List<RNode> pickNext(final Region childRegion, final List<RNode> children1, final List<RNode> children2){
		List<RNode> preferred;
		final Region region1 = boundingRegion(children1);
		final Region region2 = boundingRegion(children2);
		final double nia0 = childRegion.nonIntersectingArea(region1);
		final double nia1 = childRegion.nonIntersectingArea(region2);
		if(nia0 < nia1)
			preferred = children1;
		else if(nia0 > nia1)
			preferred = children2;
		else{
			final double area0 = region1.euclideanArea();
			final double area1 = region2.euclideanArea();
			if(area0 < area1)
				preferred = children1;
			else if(nia0 > area1)
				preferred = children2;
			else
				preferred = (children1.size() <= children2.size()? children1: children2);
		}
		return preferred;
	}

	private static Region boundingRegion(final List<RNode> children){
		final Region region = Region.ofEmpty();
		final int size = children.size();
		for(int i = 0; i < size; i ++){
			final RNode child = children.get(i);

			region.expandToInclude(child.region);
		}
		return region;
	}

}
