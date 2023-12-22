package io.github.mtrevisan.mapmatcher.helpers.rtree;


interface NodeSplitter{

	/**
	 * Splits the children of a node into two.
	 */
	Object[] pivotNode(RNode node);

}
