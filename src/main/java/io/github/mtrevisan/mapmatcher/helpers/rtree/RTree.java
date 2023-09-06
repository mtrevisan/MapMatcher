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
			final RNode parent = chooseLeaf(root, node);
			parent.children.add(node);
			node.parent = parent;
			if(parent.children.size() > options.maxObjects){
				final RNode[] splits = splitNode(parent, options.minObjects);
				adjustTree(splits[0], splits[1], options.minObjects, options.maxObjects);
			}
			else
				adjustTree(parent, null, options.minObjects, options.maxObjects);
		}
	}

	private static RNode chooseLeaf(final RNode parent, final RNode node){
		RNode current = parent;
		while(!current.leaf){
			double minAreaIncrement = Double.MAX_VALUE;
			RNode next = current.children.get(0);
			for(int i = 1; i < current.children.size(); i ++){
				final RNode child = current.children.get(i);
				final double nonIntersectingArea = calculateNonIntersectingArea(child.region, node.region);
				if(nonIntersectingArea < minAreaIncrement){
					minAreaIncrement = nonIntersectingArea;
					next = child;
				}
				else if(nonIntersectingArea == minAreaIncrement){
					//choose the node with the smallest area
					final double nextArea = next.region.euclideanArea();
					final double childArea = child.region.euclideanArea();
					if(childArea < nextArea)
						next = child;
				}
			}

			current = next;
		}
		return current;
	}

	private static double calculateNonIntersectingArea(final Region region1, final Region region2){
		//calculate intersection points
		final double x1 = Math.max(region1.getMinX(), region2.getMinX());
		final double y1 = Math.max(region1.getMinY(), region2.getMinY());
		final double x2 = Math.min(region1.getMaxX(), region2.getMaxX());
		final double y2 = Math.min(region1.getMaxY(), region2.getMaxY());
		//calculate area of intersection
		final double intersectionArea = Math.max(0., x2 - x1) * Math.max(0., y2 - y1);
		//calculate total area of the two regions
		final double totalArea = region1.euclideanArea() + region2.euclideanArea();
		//calculate intersection area
		return totalArea - intersectionArea;
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

			final double nia0 = calculateNonIntersectingArea(nodes[0].region, child.region);
			final double nia1 = calculateNonIntersectingArea(nodes[1].region, child.region);
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
				else{
					if(nodes[0].children.size() < nodes[1].children.size())
						preferred = nodes[0];
					else if(nodes[0].children.size() > nodes[1].children.size())
						preferred = nodes[1];
					else
						preferred = nodes[(int)Math.round(Math.random())];
				}
			}
			preferred.children.add(child);
		}
		tighten(nodes[0]);
		tighten(nodes[1]);
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

	private void adjustTree(final RNode rNode, final RNode nNode, final int minObjects, final int maxObjects){
		RNode currentNode = rNode;
		RNode newNode = nNode;

		while(true){
			if(currentNode == root){
				if(newNode != null){
					final double coordinate = Math.sqrt(Double.MAX_VALUE);
					final double dimension = -2. * Math.sqrt(Double.MAX_VALUE);
					final Region region = Region.of(coordinate, coordinate, coordinate + dimension, coordinate + dimension);
					root = RNode.createInternal(region);

					root.children.add(currentNode);
					currentNode.parent = root;
					root.children.add(newNode);
					newNode.parent = root;
				}

				tighten(root);
				break;
			}

			tighten(currentNode);

			if(newNode != null){
				tighten(newNode);
				if(currentNode.parent.children.size() > maxObjects){
					final RNode[] splits = splitNode(currentNode.parent, minObjects);
					currentNode = splits[0];
					newNode = splits[1];
					continue;
				}
			}
			else if(currentNode.parent != null){
				currentNode = currentNode.parent;
				continue;
			}

			break;
		}
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
				tighten(remove);

			remove = remove.parent;
		}
		for(final RNode eNode : reprocessedNodes)
			insert(eNode.region, options);
	}

	private static void tighten(final RNode parent){
		final double[] coordinates = new double[4];
		final double[] childCoordinates = new double[4];
		for(int i = 0; i < 2; i ++){
			coordinates[i] = Double.MAX_VALUE;
			coordinates[i + 2] = 0.;

			for(final RNode child : parent.children){
				child.parent = parent;
				childCoordinates[0] = child.region.getMinX();
				childCoordinates[1] = child.region.getMinY();
				childCoordinates[2] = child.region.getMaxX();
				childCoordinates[3] = child.region.getMaxY();
				if(childCoordinates[i] < coordinates[i])
					coordinates[i] = childCoordinates[i];
				if(childCoordinates[i + 2] > coordinates[i + 2])
					coordinates[i + 2] = childCoordinates[i + 2];
			}
		}
		parent.region = Region.of(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
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
