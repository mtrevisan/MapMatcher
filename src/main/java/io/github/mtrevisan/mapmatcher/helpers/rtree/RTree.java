/**
 * Copyright (c) 2023 Mauro Trevisan
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mtrevisan.mapmatcher.helpers.rtree;

import io.github.mtrevisan.mapmatcher.helpers.RegionTree;
import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * @see <a href="https://github.com/TheDeathFar/HilbertTree/blob/main/src/ru/vsu/css/vorobcov_i_a/RTree.java">RTree.java</a>
 */
public class RTree implements RegionTree<RTreeOptions>{

	private RNode root;


	public static RTree create(){
		return new RTree();
	}


	private RTree(){}


	@Override
	public boolean isEmpty(){
		return (root == null);
	}


	@Override
	public void insert(final Region region, final RTreeOptions options){
		final RNode node = RNode.createLeaf(region);
		if(isEmpty())
			root = node;
		else{
			final RNode parent = chooseLeaf(node.region);
			parent.children.add(node);
			node.parent = parent;

			if(parent.children.size() <= options.maxObjects)
				adjustRegionsUpToRoot(parent);
			else{
				final RNode[] splits = splitNode(parent, options.minObjects);
				adjustTree(splits[0], splits[1], options);
			}
		}
	}

	/**
	 * Find leaf that needs the least enlargement with the region.
	 */
	private RNode chooseLeaf(final Region region){
		RNode current = root;
		while(!current.leaf){
			//choose child which region enlarges the less with current record's region
			double minAreaIncrement = Double.MAX_VALUE;
			RNode minAreaNode = current.children.get(0);
			for(final RNode child : current.children){
				final double nonIntersectingArea = region.nonIntersectingArea(child.region);
				if(nonIntersectingArea < minAreaIncrement){
					minAreaIncrement = nonIntersectingArea;
					minAreaNode = child;
				}
				else if(nonIntersectingArea == minAreaIncrement){
					//choose the node with the smallest area
					final double childArea = child.region.euclideanArea();
					final double nextArea = minAreaNode.region.euclideanArea();
					if(childArea < nextArea)
						minAreaNode = child;
				}
			}

			current = minAreaNode;
		}
		return current;
	}

	private void adjustRegionsUpToRoot(final RNode node){
		RNode currentNode = node;
		while(currentNode != null){
			tightenRegion(currentNode);
			currentNode = currentNode.parent;
		}
	}

	private void adjustTree(final RNode node, RNode newNode, final RTreeOptions options){
		RNode currentNode = node;
		while(currentNode != root){
			tightenRegion(currentNode);
			tightenRegion(newNode);
			if(currentNode.parent.children.size() <= options.maxObjects)
				break;

			final RNode[] splits = splitNode(currentNode.parent, options.minObjects);
			currentNode = splits[0];
			newNode = splits[1];
		}

		final double coordinate = Math.sqrt(Double.MAX_VALUE);
		final double dimension = -2. * Math.sqrt(Double.MAX_VALUE);
		final Region region = Region.of(coordinate, coordinate, coordinate + dimension, coordinate + dimension);
		root = RNode.createInternal(region);

		root.children.add(currentNode);
		currentNode.parent = root;
		root.children.add(newNode);
		newNode.parent = root;

		tightenRegion(root);
	}

	private RNode[] splitNode(final RNode node, final int minObjects){
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

	private static RNode[] pickSeeds(final List<RNode> nodes){
		RNode[] bestPair = null;
		double bestSeparation = 0.;
		for(int i = 0; i < 2; i ++){
			double dimLowerBound = Double.MAX_VALUE;
			double dimMinUpperBound = Double.MAX_VALUE;
			double dimUpperBound = -Double.MAX_VALUE;
			double dimMaxLowerBound = -Double.MAX_VALUE;
			RNode nodeMaxLowerBound = null;
			RNode nodeMinUpperBound = null;
			for(final RNode node : nodes){
				final double[] coordinates = new double[]{node.region.getMinX(), node.region.getMinY(),
					node.region.getMaxX(), node.region.getMaxY()};
				if(coordinates[i] < dimLowerBound)
					dimLowerBound = coordinates[i];
				if(coordinates[i + 2] > dimUpperBound)
					dimUpperBound = coordinates[i + 2];
				if(coordinates[i] > dimMaxLowerBound){
					dimMaxLowerBound = coordinates[i];
					nodeMaxLowerBound = node;
				}
				if(coordinates[i + 2] < dimMinUpperBound){
					dimMinUpperBound = coordinates[i + 2];
					nodeMinUpperBound = node;
				}
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
	public boolean delete(final Region region, final RTreeOptions options){
		final RNode leaf = findLeaf(root, region);
		boolean isDeleted = false;
		if(leaf != null){
			for(final RNode node : leaf.children)
				if(node.region.equals(region)){
					isDeleted = true;
					break;
				}
			if(isDeleted)
				condenseTree(leaf, options);
		}
		return isDeleted;
	}

	private static RNode findLeaf(final RNode parent, final Region region){
		if(parent.leaf){
			for(final RNode child : parent.children)
				if(child.region.intersects(region))
					return parent;
		}
		else
			for(final RNode child : parent.children)
				if(child.region.intersects(region)){
					final RNode result = findLeaf(child, region);
					if(result != null)
						return result;
				}
		return null;
	}

	private void condenseTree(RNode remove, final RTreeOptions options){
		final Set<RNode> reprocessedNodes = new HashSet<>();
		while(remove != root){
			if(remove.leaf && remove.children.size() < options.minObjects){
				reprocessedNodes.addAll(remove.children);
				remove.parent.children.remove(remove);
			}
			else if(!remove.leaf && remove.children.size() < options.minObjects){
				final LinkedList<RNode> toVisit = new LinkedList<>(remove.children);
				while(!toVisit.isEmpty()){
					final RNode node = toVisit.pop();
					if(node.leaf)
						reprocessedNodes.addAll(node.children);
					else
						toVisit.addAll(node.children);
				}
				remove.parent.children.remove(remove);
			}
			else
				tightenRegion(remove);

			remove = remove.parent;
		}
		for(final RNode eNode : reprocessedNodes)
			insert(eNode.region, options);
	}

	private static void tightenRegion(final RNode node){
		final double[] coordinates = new double[4];
		coordinates[0] = Double.MAX_VALUE;
		coordinates[1] = Double.MAX_VALUE;
		coordinates[2] = -Double.MAX_VALUE;
		coordinates[3] = -Double.MAX_VALUE;
		for(final RNode child : node.children){
			child.parent = node;
			if(child.region.getMinX() < coordinates[0])
				coordinates[0] = child.region.getMinX();
			if(child.region.getMinY() < coordinates[1])
				coordinates[1] = child.region.getMinY();
			if(child.region.getMaxX() > coordinates[2])
				coordinates[2] = child.region.getMaxX();
			if(child.region.getMaxY() > coordinates[3])
				coordinates[3] = child.region.getMaxY();
		}

		node.region = Region.of(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
	}


	@Override
	public boolean intersects(final Region region){
		final Deque<RNode> stack = new ArrayDeque<>();
		stack.push(root);
		while(!stack.isEmpty()){
			final RNode current = stack.pop();
			if(current.leaf){
				for(final RNode e : current.children)
					if(e.region.intersects(region))
						return true;
			}
			else
				for(final RNode c : current.children)
					if(c.region.intersects(region))
						stack.push(c);
		}
		return false;
	}

	@Override
	public boolean contains(final Region region){
		final Deque<RNode> stack = new ArrayDeque<>();
		stack.push(root);
		while(!stack.isEmpty()){
			final RNode current = stack.pop();
			if(current.leaf){
				for(final RNode e : current.children)
					if(e.region.contains(region))
						return true;
			}
			else
				for(final RNode c : current.children)
					if(c.region.contains(region))
						stack.push(c);
		}
		return false;
	}

	@Override
	public Collection<Region> query(final Region region){
		final List<Region> results = new LinkedList<>();
		final Deque<RNode> stack = new ArrayDeque<>();
		stack.push(root);
		while(!stack.isEmpty()){
			final RNode current = stack.pop();
			if(current.leaf){
				for(final RNode e : current.children)
					if(region.intersects(e.region))
						results.add(e.region);
			}
			else
				for(final RNode c : current.children)
					if(region.intersects(c.region))
						stack.push(c);
		}
		return results;
	}

}
