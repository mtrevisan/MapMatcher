package io.github.mtrevisan.mapmatcher.helpers.rtree;

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
		final RNode[] nodes = new RNode[]{
			node,
			(node.leaf? RNode.createLeaf(node.region): RNode.createInternal(node.region))
		};
		nodes[1].parent = node.parent;
		if(nodes[1].parent != null)
			nodes[1].parent.children.add(nodes[1]);

		//find the two nodes that maximizes the space waste, and assign them to a node:
		final LinkedList<RNode> seeds = new LinkedList<>(node.children);
		node.children.clear();

		final RNode[] seedNodes = pickSeeds(seeds);
		nodes[0].children.add(seedNodes[0]);
		nodes[1].children.add(seedNodes[1]);

		//examine remaining entries and add them to either `nodes[0]` or `nodes[1]` with the least enlargement criteria
		while(!seeds.isEmpty()){
			if(nodes[0].children.size() >= minObjects && nodes[1].children.size() + seeds.size() == minObjects){
				nodes[1].children.addAll(seeds);
				return nodes;
			}
			if(nodes[1].children.size() >= minObjects && nodes[1].children.size() + seeds.size() == minObjects){
				nodes[0].children.addAll(seeds);
				return nodes;
			}

			//add the next record to the node which will require the least enlargement:
			final RNode child = seeds.pop();
			final RNode preferred = pickNext(child, nodes[0], nodes[1]);
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

			if(node.region.getMinX() < dimLowerBound)
				dimLowerBound = node.region.getMinX();
			if(node.region.getMinY() < dimLowerBound)
				dimLowerBound = node.region.getMinY();
			if(node.region.getMaxX() > dimUpperBound)
				dimUpperBound = node.region.getMaxX();
			if(node.region.getMaxY() > dimUpperBound)
				dimUpperBound = node.region.getMaxY();

			if(node.region.getMinX() > dimMaxLowerBound){
				dimMaxLowerBound = node.region.getMinX();
				nodeMaxLowerBound = node;
			}
			if(node.region.getMinY() > dimMaxLowerBound){
				dimMaxLowerBound = node.region.getMinY();
				nodeMaxLowerBound = node;
			}
			if(node.region.getMaxX() < dimMinUpperBound){
				dimMinUpperBound = node.region.getMaxX();
				nodeMinUpperBound = node;
			}
			if(node.region.getMaxY() < dimMinUpperBound){
				dimMinUpperBound = node.region.getMaxY();
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

	private static RNode pickNext(final RNode child, final RNode node1, final RNode node2){
		RNode preferred;
		final double nia0 = child.region.nonIntersectingArea(node1.region);
		final double nia1 = child.region.nonIntersectingArea(node2.region);
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
