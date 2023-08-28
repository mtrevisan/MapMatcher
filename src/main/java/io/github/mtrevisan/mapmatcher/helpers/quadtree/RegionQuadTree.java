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
import io.github.mtrevisan.mapmatcher.helpers.kdtree.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


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
public class RegionQuadTree implements RegionTree{

	private static final int REGION_INDEX_SELF = -1;
	private static final int REGION_INDEX_NORTH_WEST = 0;
	private static final int REGION_INDEX_NORTH_EAST = 1;
	private static final int REGION_INDEX_SOUTH_WEST = 2;
	private static final int REGION_INDEX_SOUTH_EAST = 3;


	private RegionQuadNode root;


	private final Region zone;
	private final List<RegionQuadNode> children;

	//if this is reached, the zone is fully subdivided
	public static final int MAX_ITEM_BY_NODE = 5;
	public static final int MAX_LEVEL = 10;

	int level;

	//the four sub-regions, may be null if not needed
	private RegionQuadTree[] regions;


	public static RegionQuadTree create(final Region definition, final int level){
		return new RegionQuadTree(definition, level);
	}

	private RegionQuadTree(final Region definition, final int level){
		zone = definition;
		children = new ArrayList<>(MAX_ITEM_BY_NODE);
		this.level = level;
	}


	@Override
	public void clear(){
		root = null;
	}

	@Override
	public boolean isEmpty(){
		return (root == null);
	}


	/** Inserts an object into this node or its child nodes. This will split a leaf node if it exceeds the object limit. */
	@Override
	public void insert(final Region region){
		final int regionIndex = findRegion(region);
		if(regionIndex == REGION_INDEX_SELF || level == MAX_LEVEL){
			children.add(new RegionQuadNode(region));
			return;
		}
		else
			regions[regionIndex].insert(region);

		if(children.size() >= MAX_ITEM_BY_NODE && level < MAX_LEVEL){
			//redispatch the elements
			final List<Region> oldNodes = new ArrayList<>(children);
			children.clear();

			for(final Region node : oldNodes)
				insert(node);
		}
	}

	private int findRegion(final Region r){
		//the object does not fit in any of the children, keep it on the parent
		int regionIndex = REGION_INDEX_SELF;

		//https://github.com/ryanmpelletier/java-simple-quadtree/blob/master/src/main/java/com/github/ryanp102694/QuadTree.java#L239
//		final double verticalDividingLine = getZone().getX() + getZone().getWidth() / 2.;
//		final double horizontalDividingLine = getZone().getY() + getZone().getHeight() / 2.;
//		final boolean fitsCompletelyInNorthHalf = (r.getY() < horizontalDividingLine && r.getHeight() + r.getY() < horizontalDividingLine);
//		final boolean fitsCompletelyInSouthHalf = (r.getY() > horizontalDividingLine);
//
//		//fits completely in East half
//		if(r.getX() > verticalDividingLine){
//			if(fitsCompletelyInNorthHalf)
//				regionIndex = REGION_INDEX_NORTH_EAST;
//			else if(fitsCompletelyInSouthHalf)
//				regionIndex = REGION_INDEX_SOUTH_EAST;
//		}
//		//fits completely in West half
//		else if(r.getX() < verticalDividingLine && (r.getX() + r.getWidth() < verticalDividingLine)){
//			if(fitsCompletelyInNorthHalf)
//				regionIndex = REGION_INDEX_NORTH_WEST;
//			else if(fitsCompletelyInSouthHalf)
//				regionIndex = REGION_INDEX_SOUTH_WEST;
//		}

		if(children.size() >= MAX_ITEM_BY_NODE && level < MAX_LEVEL){
			//we don't want to split if we just need to retrieve the region, not inserting an element
			if(regions == null)
				//then create the subregions
				split();

			//can be null if not split
			if(regions != null){
				if(regions[REGION_INDEX_NORTH_WEST].getZone().intersects(r))
					regionIndex = REGION_INDEX_NORTH_WEST;
				else if(regions[REGION_INDEX_NORTH_EAST].getZone().intersects(r))
					regionIndex = REGION_INDEX_NORTH_EAST;
				else if(regions[REGION_INDEX_SOUTH_WEST].getZone().intersects(r))
					regionIndex = REGION_INDEX_SOUTH_WEST;
				else if(regions[REGION_INDEX_SOUTH_EAST].getZone().intersects(r))
					regionIndex = REGION_INDEX_SOUTH_EAST;
			}
		}

		return regionIndex;
	}

	//instantiate the four children
//	private void split(){
//		final int childWidth = this.w / 2;
//		final int childHeight = this.h / 2;
//
//		children[REGION_INDEX_NORTH_EAST] = new QuadTree(this.maxObjects, this.maxLevels,level + 1, this.x + childWidth, this.y, childWidth, childHeight, this);
//		children[REGION_INDEX_NORTH_WEST] = new QuadTree(this.maxObjects, this.maxLevels,this.level + 1, this.x, this.y, childWidth, childHeight, this);
//		children[REGION_INDEX_SOUTH_WEST] = new QuadTree(this.maxObjects, this.maxLevels,this.level + 1, this.x, this.y + childHeight, childWidth, childHeight, this);
//		children[REGION_INDEX_SOUTH_EAST] = new QuadTree(this.maxObjects, this.maxLevels,this.level + 1, this.x + childWidth, this.y + childHeight, childWidth, childHeight, this);
//	}


	protected Region getZone(){
		return zone;
	}

	private void split(){
		final double newWidth = zone.getWidth() / 2.;
		final double newHeight = zone.getHeight() / 2.;
		final int newLevel = level + 1;

		regions = new RegionQuadTree[MAX_ITEM_BY_NODE - 1];
		regions[REGION_INDEX_NORTH_WEST] = create(
			Region.of(zone.getX(), zone.getY(), newWidth, newHeight), newLevel);
		regions[REGION_INDEX_NORTH_EAST] = create(
			Region.of(zone.getX() + newWidth, zone.getY(), newWidth, newHeight), newLevel);
		regions[REGION_INDEX_SOUTH_WEST] = create(
			Region.of(zone.getX(), zone.getY() + newHeight, newWidth, newHeight), newLevel);
		regions[REGION_INDEX_SOUTH_EAST] = create(
			Region.of(zone.getX() + newWidth, zone.getY() + newHeight, newWidth, newHeight), newLevel);
	}



	@Override
	public boolean contains(final Region region){
		envelopes.addAll(this.envelopes);

		final int regionIndex = findRegion(region);
		if(regionIndex == QREGION_INDEX_SELF && this.children[0] != null)
			for(final RegionQuadNode child : this.children){
				if(region.intersects(Region.of(child.getX(), child.getY(), child.getWidth(), child.getHeight())))
					child.query(envelopes, region);
				else if(this.children[regionIndex] != null)
					this.children[regionIndex].query(envelopes, region);
			}
		return !envelopes.isEmpty();
	}

	@Override
	public Collection<Region> regionsInRange(final Region region){
		//TODO
		return null;
	}

}
