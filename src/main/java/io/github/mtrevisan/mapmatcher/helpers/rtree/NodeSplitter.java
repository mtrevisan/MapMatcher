package io.github.mtrevisan.mapmatcher.helpers.rtree;

import java.util.List;


public abstract class NodeSplitter{

	abstract RNode[] splitNode(RNode node);

	/** Find the two nodes that maximizes the space waste. */
	abstract RNode[] pickSeeds(List<RNode> nodes);

	abstract void pickNext(List<RNode> nodes, RNode node1, RNode node2);

}
