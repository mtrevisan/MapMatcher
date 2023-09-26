package io.github.mtrevisan.mapmatcher.helpers.rtree;

import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;

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
	public RNode[] splitNode(final RNode node){
		final RNode newNode = (node.leaf
			? RNode.createLeaf(node.region)
			: RNode.createInternal(node.region));
		newNode.parent = node.parent;
		if(node.parent != null)
			node.parent.children.add(newNode);

		//find the two nodes that maximizes the space waste, and assign them to a node:
		final LinkedList<RNode> seeds = new LinkedList<>(node.children);
		node.children.clear();

		final RNode[] seedNodes = pickSeeds(seeds);
		node.children.add(seedNodes[0]);
		newNode.children.add(seedNodes[1]);

		//examine remaining entries and add them to either `node` or `newNode` with the least enlargement criteria
		final RNode[] nodes = new RNode[]{node, newNode};
		while(!seeds.isEmpty()){
			if(node.children.size() >= minObjects && newNode.children.size() + seeds.size() == minObjects){
				newNode.children.addAll(seeds);
				return nodes;
			}
			if(newNode.children.size() >= minObjects && node.children.size() + seeds.size() == minObjects){
				node.children.addAll(seeds);
				return nodes;
			}

			//add the next record to the node which will require the least enlargement:
			final RNode child = seeds.pop();
			final RNode preferred = pickNext(child.region, node, newNode);
			preferred.children.add(child);
		}

		return nodes;
	}

	/** Find the two nodes that maximizes the space waste. */
	private static RNode[] pickSeeds(final List<RNode> nodes){
		RNode[] bestPair = null;
		double bestSeparation = 0.;
		double dimLowerBound = Double.POSITIVE_INFINITY;
		double dimMinUpperBound = Double.POSITIVE_INFINITY;
		double dimUpperBound = Double.NEGATIVE_INFINITY;
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
			if(separation >= bestSeparation){
				bestPair = new RNode[]{nodeMaxLowerBound, nodeMinUpperBound};
				bestSeparation = separation;
			}
		}
		if(bestPair != null){
			nodes.remove(bestPair[0]);
			nodes.remove(bestPair[1]);
		}
		return bestPair;
	}

	private static RNode pickNext(final Region childRegion, final RNode node1, final RNode node2){
		RNode preferred;
		final double nia0 = childRegion.nonIntersectingArea(node1.region);
		final double nia1 = childRegion.nonIntersectingArea(node2.region);
		if(nia0 < nia1)
			preferred = node1;
		else if(nia0 > nia1)
			preferred = node2;
		else{
			final double area0 = node1.region.euclideanArea();
			final double area1 = node2.region.euclideanArea();
			if(area0 < area1)
				preferred = node1;
			else if(nia0 > area1)
				preferred = node2;
			else
				preferred = (node1.children.size() <= node2.children.size()? node1: node2);
		}
		return preferred;
	}

}
