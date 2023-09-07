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

	private final NodeSplitter splitter;


	public static RTree create(final NodeSplitter splitter){
		return new RTree(splitter);
	}


	private RTree(final NodeSplitter splitter){
		this.splitter = splitter;
	}


	@Override
	public boolean isEmpty(){
		return (root == null);
	}


	@Override
	public void insert(final Region region, final RTreeOptions options){
		final RNode newNode = RNode.createLeaf(region);
		if(isEmpty())
			root = newNode;
		else{
			final RNode parent = chooseLeaf(newNode.region);
			parent.addChild(newNode);

			if(parent.children.size() <= options.maxObjects)
				adjustRegionsUpToRoot(parent);
			else
				splitAndAdjust(parent, newNode, options);
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

	private void splitAndAdjust(RNode parent, final RNode overflowNode, final RTreeOptions options){
		//FIXME
		while(true){
//			RNode[] splits = splitter.splitNode(parent, overflowNode);
			RNode[] splits = splitter.splitNode(parent);
			RNode currentNode = splits[0];
			RNode newNode = splits[1];

			if(parent == root){
				//assign new root
				root = RNode.createInternal(Region.ofEmpty());
				root.addChild(currentNode);
				root.addChild(newNode);
				tightenRegion(root);

				break;
			}

			tightenRegion(currentNode);
			tightenRegion(newNode);
			if(currentNode.parent.children.size() <= options.maxObjects)
				break;

			parent = currentNode.parent;
		}
	}


	@Override
	public boolean delete(final Region region, final RTreeOptions options){
		boolean deleted = false;
		final RNode leaf = findLeaf(root, region);
		if(leaf != null){
			condenseTree(leaf, options);

			//reassign root if it has only one child
			if(root.children.size() == 1)
				root = RNode.createLeaf(root.children.get(0).region);

			deleted = true;
		}
		return deleted;
	}

	private static RNode findLeaf(final RNode parent, final Region region){
		final Deque<RNode> stack = new ArrayDeque<>();
		stack.push(parent);
		while(!stack.isEmpty()){
			final RNode currentNode = stack.pop();

			if(currentNode.leaf){
				for(final RNode child : currentNode.children)
					if(child.region.intersects(region))
						return currentNode;
			}
			else{
				for(final RNode child : currentNode.children)
					if(child.region.intersects(region))
						stack.push(child);
			}
		}
		return null;
	}

	private void condenseTree(RNode remove, final RTreeOptions options){
		final Set<RNode> removedNodes = new HashSet<>();
		while(remove != root){
			//node has underflow of children
			if(remove.children.size() < options.minObjects){
				if(remove.leaf)
					removedNodes.addAll(remove.children);
				else{
					final LinkedList<RNode> toVisit = new LinkedList<>(remove.children);
					while(!toVisit.isEmpty()){
						final RNode node = toVisit.pop();
						if(node.leaf)
							removedNodes.addAll(node.children);
						else
							toVisit.addAll(node.children);
					}
				}

				remove.parent.children.remove(remove);
			}
			else
				tightenRegion(remove);

			final RNode oldRemove = remove;
			remove = remove.parent;
			oldRemove.parent = null;
		}

		//reinsert temporarily deleted nodes
		for(final RNode node : removedNodes)
			insert(node.region, options);
	}

	private void tightenRegion(final RNode node){
		final double[] coordinates = new double[4];
		coordinates[0] = Double.MAX_VALUE;
		coordinates[1] = Double.MAX_VALUE;
		coordinates[2] = -Double.MAX_VALUE;
		coordinates[3] = -Double.MAX_VALUE;
		for(final RNode child : node.children){
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
		return (findLeaf(root, region) != null);
	}

	@Override
	public boolean contains(final Region region){
		final Deque<RNode> stack = new ArrayDeque<>();
		stack.push(root);
		while(!stack.isEmpty()){
			final RNode current = stack.pop();
			if(current.leaf){
				for(final RNode child : current.children)
					if(child.region.contains(region))
						return true;
			}
			else{
				for(final RNode child : current.children)
					if(child.region.contains(region))
						stack.push(child);
			}
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
