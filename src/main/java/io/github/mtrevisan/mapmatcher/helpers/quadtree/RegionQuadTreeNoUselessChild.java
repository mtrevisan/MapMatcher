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
package io.github.mtrevisan.mapmatcher.helpers.quadtree;

import io.github.mtrevisan.mapmatcher.helpers.RegionTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;


/**
 * A Quadtree is a spatial index structure for efficient range querying
 * of items bounded by 2D rectangles.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Quadtree">Quadtree</a>
 * @see <a href="https://stackoverflow.com/questions/41946007/efficient-and-well-explained-implementation-of-a-quadtree-for-2d-collision-det">Efficient (and well explained) implementation of a Quadtree for 2D collision detection</a>
 * @see <a href="https://github.com/locationtech/jts/blob/master/modules/core/src/main/java/org/locationtech/jts/index/quadtree/Quadtree.java">Quadtree.java</a>
 * @see <a href="https://www.graphhopper.com/blog/2012/05/29/tricks-to-speed-up-neighbor-searches-of-quadtrees-geo-spatial-java/">Tricks to Speed up Neighbor Searches of Quadtrees</a>
 * @see <a href="https://www.baeldung.com/java-range-search">Range Search Algorithm in Java</a>
 * @see <a href="https://algs4.cs.princeton.edu/92search/QuadTree.java.html">QuadTree.java</a>
 * @see <a href="https://github.com/varunpant/Quadtree/blob/master/src/main/java/com/github/varunpant/quadtree/QuadTree.java">varunpant QuadTree</a>
 * @see <a href="https://hal.science/hal-01876579/file/paper.pdf">Packed-Memory Quadtree: a cache-oblivious data structure for visual exploration of streaming spatiotemporal big data</a>
 * @see <a href="https://github.com/ryanmpelletier/java-simple-quadtree">java-simple-quadtree</a>
 *
 * quad k-d
 * https://core.ac.uk/download/pdf/41827175.pdf
 */
public class RegionQuadTreeNoUselessChild implements RegionTree{

	private static final int INDEX_SELF = -1;
	private static final int INDEX_NORTH_WEST_CHILD = 0;
	private static final int INDEX_NORTH_EAST_CHILD = 1;
	private static final int INDEX_SOUTH_WEST_CHILD = 2;
	private static final int INDEX_SOUTH_EAST_CHILD = 3;

	/** The region covered by this node. */
	private final Region envelope;
	/** The list of children. */
	private RegionQuadTreeNoUselessChild[] children;
	/** The actual regions this node spans. */
	private final List<Region> regions;

	private final QuadTreeOptions options;


	public static RegionQuadTreeNoUselessChild create(final QuadTreeOptions options, final Region envelope){
		return new RegionQuadTreeNoUselessChild(options, envelope);
	}


	private RegionQuadTreeNoUselessChild(final QuadTreeOptions options, final Region envelope){
		this.envelope = envelope;
		regions = new ArrayList<>(options.maxRegionsPerNode);

		this.options = options;
	}


//	@Override
//	public void insert(final Region region){
//		insert(region, "");
//	}
//
//	private void insert(final Region region, final String code){
//		if(hasChildren()){
//			final int childIndex = getChildIndex(region);
//			if(childIndex != INDEX_SELF){
//				children[childIndex].insert(region, code + childIndex);
//				return;
//			}
//		}
//
//		region.setCode(code);
//		regions.add(region);
//
//		if(regions.size() > maxRegionsPerNode){
//			split();
//
//			//redistribute sub-regions to the right child
//			int i = 0;
//			while(i < regions.size()){
//				final int childIndex = getChildIndex(regions.get(i));
//				if(childIndex != INDEX_SELF)
//					children[childIndex].insert(regions.remove(i), code + childIndex);
//				else
//					i ++;
//			}
//		}
//	}

	@Override
	public void insert(final Region region){
		final Stack<InsertItem> stack = new Stack<>();
		stack.push(new InsertItem(this, BitCode.ofEmpty(), region));
		while(!stack.isEmpty()){
			final InsertItem item = stack.pop();
			final RegionQuadTreeNoUselessChild itemNode = item.node;
			final BitCode itemCode = item.code;
			final Region itemRegion = item.region;

			if(itemNode.hasChildren()){
				final int childIndex = itemNode.getChildIndex(itemRegion);
				//if it doesn't belong to the current node, let one of the children find where to put it
				if(childIndex != INDEX_SELF){
					if(itemNode.children[childIndex] == null)
						createChild(itemNode, childIndex);

					final BitCode newItemCode = itemCode.clone()
						.append(childIndex, 2);
					stack.push(new InsertItem(itemNode.children[childIndex], newItemCode, itemRegion));
					continue;
				}
			}

			//store linear region quadtree location code:
			//	NW is 0, NE is 1, SW is 2, SE is 3
			//	the number of bytes halved is the number of level the region is
			itemRegion.setCode(itemCode);
			//add the region to the list of regions of the current node
			itemNode.regions.add(itemRegion);

			//if number of regions is greater than the maximum, split the node
			if(itemNode.regions.size() > options.maxRegionsPerNode
					&& (options.maxLevels < 0 || itemCode.getLevel() < options.maxLevels)){
				//redistribute sub-regions to the right child where it belongs
				int i = 0;
				while(i < itemNode.regions.size()){
					final Region nodeRegion = itemNode.regions.get(i);
					final int childIndex = itemNode.getChildIndex(nodeRegion);
					if(childIndex != INDEX_SELF){
						if(itemNode.children == null)
							itemNode.children = new RegionQuadTreeNoUselessChild[4];
						if(itemNode.children[childIndex] == null)
							createChild(itemNode, childIndex);

						final BitCode newItemCode = itemCode.clone()
							.append(childIndex, 2);
						stack.push(new InsertItem(itemNode.children[childIndex], newItemCode, nodeRegion));
						itemNode.regions.remove(i);
					}
					else
						i ++;
				}
			}
		}
	}

	private void createChild(final RegionQuadTreeNoUselessChild node, final int childIndex){
		final double x = node.envelope.getX();
		final double y = node.envelope.getY();
		final double width = node.envelope.getWidth() / 2.;
		final double height = node.envelope.getHeight() / 2.;
		//FIXME ge xé na manièra de kavar sto "Region.of"?
		final Region region = Region.of(
			x + ((childIndex & 0x01) != 0x00? width: 0),
			y + ((childIndex & 0x10) != 0x00? height: 0),
			width,
			height);
		node.children[childIndex] = create(options, region);
	}

	private static class InsertItem{
		final RegionQuadTreeNoUselessChild node;
		final BitCode code;
		final Region region;

		private InsertItem(final RegionQuadTreeNoUselessChild node, final BitCode code, final Region region){
			this.node = node;
			this.code = code;
			this.region = region;
		}
	}

	private int getChildIndex(final Region region){
		final double x = region.getX();
		final double y = region.getY();
		final double width = region.getWidth();
		final double height = region.getHeight();
		final double midX = envelope.getX() + envelope.getWidth() / 2.;
		final double midY = envelope.getY() + envelope.getHeight() / 2.;
		final boolean northSide = (y < midY && height + y < midY);
		final boolean southSide = (y > midY);
		final boolean westSide = (x < midX && x + width < midX);
		final boolean eastSide = (x > midX);

		int index = INDEX_SELF;
		if(eastSide){
			if(northSide)
				index = INDEX_NORTH_EAST_CHILD;
			else if(southSide)
				index = INDEX_SOUTH_EAST_CHILD;
		}
		else if(westSide){
			if(northSide)
				index = INDEX_NORTH_WEST_CHILD;
			else if(southSide)
				index = INDEX_SOUTH_WEST_CHILD;
		}
		return index;
	}

	private boolean hasChildren(){
		if(children != null)
			for(int i = 0; i < 4; i ++)
				if(children[i] != null)
					return true;
		return false;
	}


//	@Override
//	public boolean delete(final Region region){
//		final int index = getChildIndex(region);
//		if(index == INDEX_SELF || children[index] == null){
//			for(int i = 0; i < regions.size(); i ++){
//				if(regions.get(i).equals(region)){
//					regions.remove(i)
//						.setCode(null);
//					return true;
//				}
//			}
//		}
//		else{
//			children[index].delete(region);
//			return true;
//		}
//
//		return false;
//	}

	@Override
	public boolean delete(final Region region){
		RegionQuadTreeNoUselessChild currentNode = this;
		while(currentNode != null){
			final int index = currentNode.getChildIndex(region);
			if(index == INDEX_SELF || currentNode.children == null || currentNode.children[index] == null){
				final List<Region> nodeRegions = currentNode.regions;
				for(int i = 0; i < nodeRegions.size(); i ++){
					final Region nodeRegion = nodeRegions.get(i);
					if(nodeRegion.equals(region)){
						nodeRegions.remove(i)
							.setCode(null);

						//FIXME re-balance the tree
//						if(currentNode.regions.isEmpty()){
//							final List<Region> descendants = getAllDescendants(currentNode);
//							clear(currentNode);
//							for(final Region descendant : descendants)
//								currentNode.insert(descendant);
//						}

						return true;
					}
				}
				break;
			}
			else
				currentNode = currentNode.children[index];
		}

		return false;
	}

	private static List<Region> getAllDescendants(final RegionQuadTreeNoUselessChild node){
		final List<Region> descendants = new ArrayList<>();
		final Stack<RegionQuadTreeNoUselessChild> stack = new Stack<>();
		stack.push(node);
		while(!stack.isEmpty()){
			final RegionQuadTreeNoUselessChild current = stack.pop();

			descendants.addAll(current.regions);
			if(current.hasChildren())
				for(final RegionQuadTreeNoUselessChild child : current.children)
					if(child != null)
						stack.push(child);
		}
		return descendants;
	}

	private static void clear(final RegionQuadTreeNoUselessChild node){
		final Stack<RegionQuadTreeNoUselessChild> stack = new Stack<>();
		stack.push(node);
		while(!stack.isEmpty()){
			final RegionQuadTreeNoUselessChild current = stack.pop();

			current.regions.clear();

			if(current.hasChildren())
				for(int i = 0; i < 4; i ++)
					if(current.children[i] != null){
						stack.push(current.children[i]);
						current.children[i] = null;
					}
		}
	}


	@Override
	public boolean contains(final Region region){
		final Stack<RegionQuadTreeNoUselessChild> stack = new Stack<>();
		stack.push(this);
		while(!stack.isEmpty()){
			final RegionQuadTreeNoUselessChild node = stack.pop();

			final int index = node.getChildIndex(region);
			if(index == INDEX_SELF || !node.hasChildren()){
				if(node.hasChildren())
					for(final RegionQuadTreeNoUselessChild child : node.children)
						if(child != null && region.intersects(child.envelope))
							stack.push(child);
			}
			else if(node.children[index] != null)
				//the search area is in one of the children totally, but it is still not possible to exclude the objects on this node, because
				//that search area could include one
				stack.push(node.children[index]);

			for(final Region nodeRegion : node.regions)
				if(nodeRegion.intersects(region))
					return true;
		}

		return false;
	}

	@Override
	public Collection<Region> query(final Region region){
		final List<Region> returnList = new ArrayList<>();

		final Stack<RegionQuadTreeNoUselessChild> stack = new Stack<>();
		stack.push(this);
		while(!stack.isEmpty()){
			final RegionQuadTreeNoUselessChild node = stack.pop();

			final int index = node.getChildIndex(region);
			if(index == INDEX_SELF || !node.hasChildren()){
				if(node.hasChildren())
					for(final RegionQuadTreeNoUselessChild child : node.children)
						if(child != null && region.intersects(child.envelope))
							stack.push(child);
			}
			else if(node.children[index] != null)
				//the search area is in one of the children totally, but it is still not possible to exclude the objects on this node, because
				//that search area could include one
				stack.push(node.children[index]);

			for(final Region nodeRegion : node.regions)
				if(nodeRegion.intersects(region))
					returnList.add(nodeRegion);
		}

		return returnList;
	}


	@Override
	public boolean isEmpty(){
		return regions.isEmpty();
	}

}
