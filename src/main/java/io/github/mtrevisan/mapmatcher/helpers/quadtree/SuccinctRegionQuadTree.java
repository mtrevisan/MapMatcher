package io.github.mtrevisan.mapmatcher.helpers.quadtree;

import io.github.mtrevisan.mapmatcher.helpers.RegionTree;
import io.github.mtrevisan.mapmatcher.helpers.kdtree.Region;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import org.agrona.collections.Int2ObjectHashMap;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Stack;


/**
 * A Quadtree is a spatial index structure for efficient range querying
 * of items bounded by 2D rectangles.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Quadtree">Quadtree</a>
 * @see <a href="https://www.graphhopper.com/blog/2012/05/29/tricks-to-speed-up-neighbor-searches-of-quadtrees-geo-spatial-java/">Tricks to Speed up Neighbor Searches of Quadtrees</a>
 * @see <a href="https://github.com/varunpant/Quadtree/blob/master/src/main/java/com/github/varunpant/quadtree/QuadTree.java">varunpant QuadTree</a>
 * @see <a href="https://hal.science/hal-01876579/file/paper.pdf">Packed-Memory Quadtree: a cache-oblivious data structure for visual exploration of streaming spatiotemporal big data</a>
 * @see <a href="https://github.com/ryanmpelletier/java-simple-quadtree">java-simple-quadtree</a>
 * @see <a href="https://ruc.udc.es/dspace/bitstream/handle/2183/13769/BernardoRoca_Guillermode_TD_2014.pdf">New data structures and algorithms for the efficient management of large spatial datasets</a>
 *
 * @see <a href="https://www.baeldung.com/cs/b-trees-vs-btrees">The Difference Between B-trees and B+trees</a>
 *
 * quad k-d
 * https://core.ac.uk/download/pdf/41827175.pdf
 */
public class SuccinctRegionQuadTree implements RegionTree{

	private static final int INDEX_SELF = -1;
	private static final int INDEX_NORTH_WEST_CHILD = 0;
	private static final int INDEX_NORTH_EAST_CHILD = 1;
	private static final int INDEX_SOUTH_WEST_CHILD = 2;
	private static final int INDEX_SOUTH_EAST_CHILD = 3;

	private final Region envelope;
	private final SuccinctRegionQuadTree[] children;
	private Region region;

	//SELF is 111, NW is 000, NE is 001, SW is 010, SE is 011, end of code if 100
	private final BitSet structure = new BitSet();
	private Int2ObjectHashMap<Point> data;


	public static SuccinctRegionQuadTree create(final Region envelope){
		return new SuccinctRegionQuadTree(envelope);
	}


	private SuccinctRegionQuadTree(final Region envelope){
		this.envelope = envelope;
		this.children = new SuccinctRegionQuadTree[4];
	}


	@Override
	public void insert(final Region region){
		final Stack<NodeIDRegionItem> stack = new Stack<>();
		stack.push(new NodeIDRegionItem(this, "", region));
		while(!stack.isEmpty()){
			final NodeIDRegionItem item = stack.pop();
			final SuccinctRegionQuadTree itemNode = item.node;
			final String itemID = item.id;
			final Region itemRegion = item.region;

			if(itemNode.children[0] != null){
				final int childIndex = itemNode.getChildIndex(itemRegion);
				if(childIndex != INDEX_SELF){
					stack.push(new NodeIDRegionItem(itemNode.children[childIndex], itemID + childIndex, itemRegion));
					continue;
				}
			}

			if(itemNode.region != null){
				itemNode.split();

				//redistribute sub-regions to the right child
				int childIndex = itemNode.getChildIndex(itemNode.region);
				if(childIndex != INDEX_SELF){
					stack.push(new NodeIDRegionItem(itemNode.children[childIndex], itemID + childIndex, itemNode.region));
					itemNode.region = null;
				}
				childIndex = itemNode.getChildIndex(itemRegion);
				if(childIndex != INDEX_SELF)
					stack.push(new NodeIDRegionItem(itemNode.children[childIndex], itemID + childIndex, itemRegion));
				else{
					if(itemNode.region == null)
						itemNode.region = itemRegion;

					itemRegion.setCode(itemID);
				}
			}
			else{
				itemNode.region = itemRegion;

				itemRegion.setCode(itemID);
			}
		}
	}

	private static class NodeIDRegionItem{
		final SuccinctRegionQuadTree node;
		final String id;
		final Region region;

		private NodeIDRegionItem(final SuccinctRegionQuadTree node, final String id, final Region region){
			this.node = node;
			this.id = id;
			this.region = region;
		}
	}

	private void split(){
		final double childWidth = envelope.getWidth() / 2.;
		final double childHeight = envelope.getHeight() / 2.;

		final double x = envelope.getX();
		final double y = envelope.getY();
		children[INDEX_NORTH_WEST_CHILD] = create(Region.of(x, y, childWidth, childHeight));
		children[INDEX_NORTH_EAST_CHILD] = create(Region.of(x + childWidth, y, childWidth, childHeight));
		children[INDEX_SOUTH_WEST_CHILD] = create(Region.of(x, y + childHeight, childWidth, childHeight));
		children[INDEX_SOUTH_EAST_CHILD] = create(Region.of(x + childWidth, y + childHeight, childWidth, childHeight));
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


	@Override
	public boolean delete(final Region region){
		SuccinctRegionQuadTree currentNode = this;
		while(currentNode != null){
			final int index = currentNode.getChildIndex(region);
			if(index == INDEX_SELF || currentNode.children[index] == null){
				if(currentNode.region != null && currentNode.region.equals(region)){
					currentNode.region = null;
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
		final Stack<SuccinctRegionQuadTree> stack = new Stack<>();
		stack.push(this);
		while(!stack.isEmpty()){
			final SuccinctRegionQuadTree node = stack.pop();

			final int index = node.getChildIndex(region);
			if(index == INDEX_SELF){
				if(node.children[0] != null)
					for(final SuccinctRegionQuadTree child : node.children)
						if(region.intersects(child.envelope))
							stack.push(child);
			}
			else if(node.children[index] != null)
				stack.push(node.children[index]);

			if(node.region != null && node.region.intersects(region))
				return true;
		}

		return false;
	}

	@Override
	public Collection<Region> query(final Region region){
		final List<Region> returnList = new ArrayList<>();

		final Stack<SuccinctRegionQuadTree> stack = new Stack<>();
		stack.push(this);
		while(!stack.isEmpty()){
			final SuccinctRegionQuadTree node = stack.pop();

			final int index = node.getChildIndex(region);
			if(index == INDEX_SELF){
				if(node.children[0] != null)
					for(final SuccinctRegionQuadTree child : node.children)
						if(region.intersects(child.envelope))
							stack.push(child);
			}
			else if(node.children[index] != null)
				stack.push(node.children[index]);

			if(node.region.intersects(region))
				returnList.add(node.region);
		}

		return returnList;
	}


	@Override
	public boolean isEmpty(){
		return (region == null);
	}

}
