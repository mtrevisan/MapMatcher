package io.github.mtrevisan.mapmatcher.helpers.quadtree;

import io.github.mtrevisan.mapmatcher.helpers.RegionTree;
import io.github.mtrevisan.mapmatcher.helpers.bplustree.BPlusTree;

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
public class RegionQuadTree implements RegionTree{

	private static final int INDEX_SELF = -1;
	private static final int INDEX_NORTH_WEST_CHILD = 0;
	private static final int INDEX_NORTH_EAST_CHILD = 1;
	private static final int INDEX_SOUTH_WEST_CHILD = 2;
	private static final int INDEX_SOUTH_EAST_CHILD = 3;

	private final Region envelope;
	private final RegionQuadTree[] children;
	private final List<Region> regions;

	private final int maxRegionsPerNode;


	public static RegionQuadTree create(final Region envelope, final int maxRegionsPerNode){
		return new RegionQuadTree(envelope, maxRegionsPerNode);
	}


	private RegionQuadTree(final Region envelope, final int maxRegionsPerNode){
		if(maxRegionsPerNode < 1)
			throw new IllegalArgumentException("Maximum number of regions for this node must be greater than zero");

		this.envelope = envelope;
		this.children = new RegionQuadTree[4];
		this.regions = new ArrayList<>(maxRegionsPerNode);

		this.maxRegionsPerNode = maxRegionsPerNode;
	}


//	@Override
//	public void insert(final Region region){
//		insert(region, "");
//	}
//
//	private void insert(final Region region, final String code){
//		if(children[0] != null){
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
		stack.push(new InsertItem(this, "", region));
		while(!stack.isEmpty()){
			final InsertItem item = stack.pop();
			final RegionQuadTree itemNode = item.node;
			final String itemCode = item.code;
			final Region itemRegion = item.region;

			if(itemNode.children[0] != null){
				final int childIndex = itemNode.getChildIndex(itemRegion);
				if(childIndex != INDEX_SELF){
					stack.push(new InsertItem(itemNode.children[childIndex], itemCode + childIndex, itemRegion));
					continue;
				}
			}

			//store linear region quadtree location code:
			//	NW is 0, NE is 1, SW is 2, SE is 3
			//	the number of bytes halved is the number of level the region is
			itemRegion.setCode(itemCode);
			itemNode.regions.add(itemRegion);

			if(itemNode.regions.size() > itemNode.maxRegionsPerNode){
				itemNode.split();

				//redistribute sub-regions to the right child
				int i = 0;
				while(i < itemNode.regions.size()){
					final Region nodeRegion = itemNode.regions.get(i);
					final int childIndex = itemNode.getChildIndex(nodeRegion);
					if(childIndex != INDEX_SELF){
						stack.push(new InsertItem(itemNode.children[childIndex], itemCode + childIndex, nodeRegion));
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
		final String code;
		final Region region;

		private InsertItem(final RegionQuadTree node, final String code, final Region region){
			this.node = node;
			this.code = code;
			this.region = region;
		}
	}

	private void split(){
		final double childWidth = envelope.getWidth() / 2.;
		final double childHeight = envelope.getHeight() / 2.;

		final double x = envelope.getX();
		final double y = envelope.getY();
		children[INDEX_NORTH_WEST_CHILD] = create(Region.of(x, y, childWidth, childHeight), maxRegionsPerNode);
		children[INDEX_NORTH_EAST_CHILD] = create(Region.of(x + childWidth, y, childWidth, childHeight), maxRegionsPerNode);
		children[INDEX_SOUTH_WEST_CHILD] = create(Region.of(x, y + childHeight, childWidth, childHeight), maxRegionsPerNode);
		children[INDEX_SOUTH_EAST_CHILD] = create(Region.of(x + childWidth, y + childHeight, childWidth, childHeight), maxRegionsPerNode);
	}

	protected int getChildIndex(final Region region){
		int index = INDEX_SELF;
		final double verticalDividingLine = this.envelope.getX() + this.envelope.getWidth() / 2.;
		final double horizontalDividingLine = this.envelope.getY() + this.envelope.getHeight() / 2.;

		final boolean fitsCompletelyInNorthHalf = (region.getY() < horizontalDividingLine
			&& region.getHeight() + region.getY() < horizontalDividingLine);
		final boolean fitsCompletelyInSouthHalf = (region.getY() > horizontalDividingLine);
		final boolean fitsCompletelyInWestHalf = (region.getX() < verticalDividingLine
			&& region.getX() + region.getWidth() < verticalDividingLine);
		final boolean fitsCompletelyInEastHalf = (region.getX() > verticalDividingLine);

		if(fitsCompletelyInEastHalf){
			if(fitsCompletelyInNorthHalf)
				index = INDEX_NORTH_EAST_CHILD;
			else if(fitsCompletelyInSouthHalf)
				index = INDEX_SOUTH_EAST_CHILD;
		}
		else if(fitsCompletelyInWestHalf){
			if(fitsCompletelyInNorthHalf)
				index = INDEX_NORTH_WEST_CHILD;
			else if(fitsCompletelyInSouthHalf)
				index = INDEX_SOUTH_WEST_CHILD;
		}
		return index;
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
		RegionQuadTree currentNode = this;
		while(currentNode != null){
			final int index = currentNode.getChildIndex(region);
			if(index == INDEX_SELF || currentNode.children[index] == null){
				final List<Region> nodeRegions = currentNode.regions;
				for(int i = 0; i < nodeRegions.size(); i ++)
					if(nodeRegions.get(i).equals(region)){
						nodeRegions.remove(i)
							.setCode(null);
						return true;
					}
				break;
			}
			else
				currentNode = currentNode.children[index];
		}

		return false;
	}


	@Override
	public boolean contains(final Region region){
		final Stack<RegionQuadTree> stack = new Stack<>();
		stack.push(this);
		while(!stack.isEmpty()){
			final RegionQuadTree node = stack.pop();

			final int index = node.getChildIndex(region);
			if(index == INDEX_SELF){
				if(node.children[0] != null)
					for(final RegionQuadTree child : node.children)
						if(region.intersects(child.envelope))
							stack.push(child);
			}
			else if(node.children[index] != null)
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

		final Stack<RegionQuadTree> stack = new Stack<>();
		stack.push(this);
		while(!stack.isEmpty()){
			final RegionQuadTree node = stack.pop();

			final int index = node.getChildIndex(region);
			if(index == INDEX_SELF){
				if(node.children[0] != null)
					for(final RegionQuadTree child : node.children)
						if(region.intersects(child.envelope))
							stack.push(child);
			}
			else if(node.children[index] != null)
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
