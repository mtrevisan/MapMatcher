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
 * @see <a href="http://www.cs.umd.edu/%7Ehjs/pubs/bulkload.pdf">Speeding Up Construction of Quadtrees for Spatial Indexing</a>
 * @see <a href="https://ruc.udc.es/dspace/bitstream/handle/2183/13769/BernardoRoca_Guillermode_TD_2014.pdf">New data structures and algorithms for the efficient management of large spatial datasets</a>
 * @see <a href="https://www.ifi.uzh.ch/dam/jcr:ffffffff-96c1-007c-ffff-fffff2d50548/ReportWolfensbergerFA.pdf">Improving the Performance of Region Quadtrees</a>
 * @see <a href="https://github.com/Octopus773/QuadTree">QuadTree</a>
 *
 * quad k-d
 * https://core.ac.uk/download/pdf/41827175.pdf
 */
public class RegionQuadTree implements RegionTree{

	public static final int MAX_LEVELS_UNLIMITED = -1;

	private static final int INDEX_SELF = -1;
	private static final int INDEX_NORTH_WEST_CHILD = 0;
	private static final int INDEX_NORTH_EAST_CHILD = 1;
	private static final int INDEX_SOUTH_WEST_CHILD = 2;
	private static final int INDEX_SOUTH_EAST_CHILD = 3;

	/** The region covered by this node. */
	private final Region envelope;
	/** The list of children. */
	private final RegionQuadTree[] children;
	/** The actual regions this node contains. */
	private List<Region> regions;


	/**
	 * Creator of a node.
	 *
	 * @param envelope	The region envelope defining the node.
	 * @return	The node.
	 */
	public static RegionQuadTree create(final Region envelope){
		return new RegionQuadTree(envelope);
	}


	private RegionQuadTree(final Region envelope){
		this.envelope = envelope;
		children = new RegionQuadTree[4];
	}


	@Override
	public boolean isEmpty(){
		return (regions == null || regions.isEmpty());
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
	public void insert(final Region region, final int maxRegionsPerNode){
		insert(region, maxRegionsPerNode, MAX_LEVELS_UNLIMITED);
	}

	@Override
	public void insert(final Region region, final int maxRegionsPerNode, final int maxLevels){
		if(maxLevels < MAX_LEVELS_UNLIMITED)
			throw new IllegalArgumentException("Invalid number of max levels: (" + maxLevels + ")");

		final Stack<InsertItem> stack = new Stack<>();
		stack.push(new InsertItem(this, BitCode.ofEmpty(), region));
		while(!stack.isEmpty()){
			final InsertItem item = stack.pop();
			final RegionQuadTree itemNode = item.node;
			final BitCode itemCode = item.code;
			final Region itemRegion = item.region;

			if(itemNode.hasChildren()){
				final int childIndex = getChildIndex(itemNode.envelope, itemRegion);
				//if it doesn't belong to the current node, let one of the children find where to put it
				if(childIndex != INDEX_SELF){
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
			if(itemNode.regions == null)
				itemNode.regions = new ArrayList<>(maxRegionsPerNode);
			itemNode.regions.add(itemRegion);

			//if number of regions is greater than the maximum, split the node
			if(itemNode.regions.size() > maxRegionsPerNode
					&& (maxLevels < 0 || itemCode.getLevel() < maxLevels)){
				if(!itemNode.hasChildren())
					itemNode.split();

				//redistribute sub-regions to the right child where it belongs
				int i = 0;
				while(i < itemNode.regions.size()){
					final Region nodeRegion = itemNode.regions.get(i);
					final int childIndex = getChildIndex(itemNode.envelope, nodeRegion);
					if(childIndex != INDEX_SELF){
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

	private static class InsertItem{
		final RegionQuadTree node;
		final BitCode code;
		final Region region;

		private InsertItem(final RegionQuadTree node, final BitCode code, final Region region){
			this.node = node;
			this.code = code;
			this.region = region;
		}
	}

	private void split(){
		final double x = envelope.getX();
		final double y = envelope.getY();
		final double width = envelope.getWidth() / 2.;
		final double height = envelope.getHeight() / 2.;

		//FIXME ge xé na manièra de kavar sti "Region.of"?
		children[INDEX_NORTH_WEST_CHILD] = create(Region.of(x, y, width, height));
		children[INDEX_NORTH_EAST_CHILD] = create(Region.of(x + width, y, width, height));
		children[INDEX_SOUTH_WEST_CHILD] = create(Region.of(x, y + height, width, height));
		children[INDEX_SOUTH_EAST_CHILD] = create(Region.of(x + width, y + height, width, height));
	}

	private static Region calculateRegion(final Region envelope, final int child){
		double x = envelope.getX();
		double y = envelope.getY();
		double width = envelope.getWidth();
		double height = envelope.getHeight();
		final BitCode code = envelope.getCode();
		final int depth = (code != null? code.getLevel(): 0);
		for(int i = 0; i < depth; i ++){
			final int index = code.valueAt(i << 1, 2);
			width /= 2.;
			height /= 2.;
			x += ((index & 0x01) != 0x00? width: 0);
			y += ((index & 0x10) != 0x00? height: 0);
		}

		width /= 2.;
		height /= 2.;
		x += ((child & 0x01) != 0x00? width: 0);
		y += ((child & 0x10) != 0x00? height: 0);
		return Region.of(x, y, width, height);
	}

	private static Region calculateChildRegion(final Region envelope, final int side){
		double x = envelope.getX();
		double y = envelope.getY();
		double width = envelope.getWidth();
		double height = envelope.getHeight();
		width /= 2.;
		height /= 2.;
		x += ((side & 0x01) != 0x00? width: 0);
		y += ((side & 0x10) != 0x00? height: 0);
		return Region.of(x, y, width, height);
	}

	private static int getChildIndex(final Region envelope, final Region region){
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
		return (children[0] != null);
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
	public boolean delete(final Region region, final int maxRegionsPerNode){
		return delete(region, maxRegionsPerNode, MAX_LEVELS_UNLIMITED);
	}

	@Override
	public boolean delete(final Region region, final int maxRegionsPerNode, final int maxLevels){
		RegionQuadTree currentNode = this;
		while(currentNode != null){
			final int index = getChildIndex(currentNode.envelope, region);
			if(index == INDEX_SELF || currentNode.children[index] == null){
				final List<Region> nodeRegions = currentNode.regions;
				for(int i = 0; i < (nodeRegions != null? nodeRegions.size(): 0); i ++){
					final Region nodeRegion = nodeRegions.get(i);
					if(nodeRegion.equals(region)){
						nodeRegions.remove(i)
							.setCode(null);

						//re-balance the tree
						if(nodeRegions.isEmpty() && currentNode.hasChildren()){
							final List<Region> descendants = getAllDescendants(currentNode);
							clear(currentNode);
							for(final Region descendant : descendants)
								currentNode.insert(descendant, maxRegionsPerNode, maxLevels);
						}

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

	private static List<Region> getAllDescendants(final RegionQuadTree node){
		final List<Region> descendants = new ArrayList<>();
		final Stack<RegionQuadTree> stack = new Stack<>();
		stack.push(node);
		while(!stack.isEmpty()){
			final RegionQuadTree current = stack.pop();

			descendants.addAll(current.regions);
			if(current.hasChildren())
				for(final RegionQuadTree child : current.children)
					stack.push(child);
		}
		return descendants;
	}

	private static void clear(final RegionQuadTree node){
		final Stack<RegionQuadTree> stack = new Stack<>();
		stack.push(node);
		while(!stack.isEmpty()){
			final RegionQuadTree current = stack.pop();

			current.regions.clear();

			if(current.hasChildren())
				for(int i = 0; i < 4; i ++){
					stack.push(current.children[i]);
					current.children[i] = null;
				}
		}
	}


	@Override
	public boolean intersects(final Region region){
		final Stack<RegionQuadTree> stack = new Stack<>();
		stack.push(this);
		while(!stack.isEmpty()){
			final RegionQuadTree node = stack.pop();

			final int index = getChildIndex(node.envelope, region);
			if(index == INDEX_SELF || !node.hasChildren()){
				if(node.hasChildren())
					for(final RegionQuadTree child : node.children)
						if(region.intersects(child.envelope))
							stack.push(child);
			}
			else if(node.children[index] != null)
				//the search area is in one of the children totally, but it is still not possible to exclude the objects on this node, because
				//that search area could include one
				stack.push(node.children[index]);

			if(node.regions != null)
				for(final Region nodeRegion : node.regions)
					if(nodeRegion.intersects(region))
						return true;
		}

		return false;
	}

	@Override
	public boolean contains(final Region region){
		final Stack<RegionQuadTree> stack = new Stack<>();
		stack.push(this);
		while(!stack.isEmpty()){
			final RegionQuadTree node = stack.pop();

			final int index = getChildIndex(node.envelope, region);
			if(index == INDEX_SELF || !node.hasChildren()){
				if(node.hasChildren())
					for(final RegionQuadTree child : node.children)
						if(child.envelope.intersects(region))
							stack.push(child);
			}
			else if(node.children[index] != null)
				//the search area is in one of the children totally, but it is still not possible to exclude the objects on this node, because
				//that search area could include one
				stack.push(node.children[index]);

			if(node.regions != null)
				for(final Region nodeRegion : node.regions)
					if(nodeRegion.intersects(region))
						return true;
		}

		return false;
	}

	@Override
	public Collection<Region> query(final Region region){
		final List<Region> returnList = new ArrayList<>();

		final Stack<RegionQuadTree> stack = new Stack<>();
		stack.push(this);
		while(!stack.isEmpty()){
			final RegionQuadTree node = stack.pop();

			final int index = getChildIndex(node.envelope, region);
			if(index == INDEX_SELF || !node.hasChildren()){
				if(node.hasChildren())
					for(final RegionQuadTree child : node.children)
						if(child.envelope.contains(region))
							stack.push(child);
			}
			else if(node.children[index] != null)
				//the search area is in one of the children totally, but it is still not possible to exclude the objects on this node, because
				//that search area could include one
				stack.push(node.children[index]);

			if(node.regions != null)
				for(final Region nodeRegion : node.regions)
					if(nodeRegion.contains(region))
						returnList.add(nodeRegion);
		}

		return returnList;
	}

}
