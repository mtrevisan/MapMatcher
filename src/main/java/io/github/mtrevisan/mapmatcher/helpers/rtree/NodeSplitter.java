package io.github.mtrevisan.mapmatcher.helpers.rtree;


interface NodeSplitter{

	/**
	 * Splits the children of a node into two.
	 */
	RNode[] splitNode(RNode node);

}
