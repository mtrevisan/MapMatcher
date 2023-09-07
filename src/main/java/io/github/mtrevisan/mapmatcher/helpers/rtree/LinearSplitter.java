package io.github.mtrevisan.mapmatcher.helpers.rtree;

import java.util.LinkedList;
import java.util.List;


//https://github.com/TheDeathFar/HilbertTree/blob/main/src/ru/vsu/css/vorobcov_i_a/LinearSplitter.java#L17
public class LinearSplitter extends NodeSplitter{

	private final int minObjects;


	LinearSplitter(final int minObjects){
		this.minObjects = minObjects;
	}


	@Override
	RNode[] splitNode(final RNode node, final int minObjects){
		final RNode[] nodes = new RNode[]{
			node,
			(node.leaf? RNode.createLeaf(node.region): RNode.createInternal(node.region))
		};
		nodes[1].parent = node.parent;
		if(nodes[1].parent != null)
			nodes[1].parent.children.add(nodes[1]);

		final LinkedList<RNode> children = new LinkedList<>(node.children);
		node.children.clear();
		final RNode[] seedNodes = pickSeeds(children);
		nodes[0].children.add(seedNodes[0]);
		nodes[1].children.add(seedNodes[1]);

		while(!children.isEmpty()){
			if((nodes[0].children.size() >= minObjects) && (nodes[1].children.size() + children.size() == minObjects)){
				nodes[1].children.addAll(children);
				children.clear();
				return nodes;
			}
			else if((nodes[1].children.size() >= minObjects) && (nodes[1].children.size() + children.size() == minObjects)){
				nodes[0].children.addAll(children);
				children.clear();
				return nodes;
			}
			final RNode child = children.pop();
			RNode preferred;

			final double nia0 = child.region.nonIntersectingArea(nodes[0].region);
			final double nia1 = child.region.nonIntersectingArea(nodes[1].region);
			if(nia0 < nia1)
				preferred = nodes[0];
			else if(nia0 > nia1)
				preferred = nodes[1];
			else{
				final double area0 = nodes[0].region.euclideanArea();
				final double area1 = nodes[1].region.euclideanArea();
				if(area0 < area1)
					preferred = nodes[0];
				else if(nia0 > area1)
					preferred = nodes[1];
				else
					preferred = nodes[nodes[0].children.size() <= nodes[1].children.size()? 0: 1];
			}
			preferred.children.add(child);
		}
		tightenRegion(nodes[0]);
		tightenRegion(nodes[1]);
		return nodes;
	}

	@Override
	RNode[] pickSeeds(final List<RNode> nodes){
		RNode[] bestPair = null;
		double bestSeparation = 0.;
		double dimLowerBound = Double.MAX_VALUE;
		double dimMinUpperBound = Double.MAX_VALUE;
		double dimUpperBound = -Double.MAX_VALUE;
		double dimMaxLowerBound = -Double.MAX_VALUE;
		RNode nodeMaxLowerBound = null;
		RNode nodeMinUpperBound = null;
		for(final RNode node : nodes){
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

	@Override
	void pickNext(final List<RNode> nodes, final RNode node1, final RNode node2){
//		RNode chosenEntry = null;
//		double maxDifference = 0;
//		//get the max difference between area enlargements
//		for(final RNode entry : nodes){
//			double enlargementL1 = node1.region.calculateEnlargement(entry.region);
//			double enlargementL2 = node2.region.calculateEnlargement(entry.region);
//			double maxEnlargementDifference = Math.abs(enlargementL1 - enlargementL2);
//			if(maxEnlargementDifference >= maxDifference){
//				chosenEntry = entry;
//				maxDifference = maxEnlargementDifference;
//			}
//		}
//		//selecting group to which we add the selected entry
//		resolveTies(node1, node2, chosenEntry);
//		//remove chosenRecord from records
//		records.remove(chosenEntry);
	}

}
