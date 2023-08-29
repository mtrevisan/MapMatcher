package io.github.mtrevisan.mapmatcher.helpers.quadtree;

import io.github.mtrevisan.mapmatcher.helpers.RegionTree;
import io.github.mtrevisan.mapmatcher.helpers.kdtree.Region;

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
	private final List<Region> envelopes;

	private final int maxEnvelopes;


	public static RegionQuadTree create(final Region envelope){
		return new RegionQuadTree(envelope, 100);
	}

	public static RegionQuadTree create(final Region envelope, final int maxEnvelopes){
		return new RegionQuadTree(envelope, maxEnvelopes);
	}


	private RegionQuadTree(final Region envelope, final int maxEnvelopes){
		if(maxEnvelopes < 1)
			throw new IllegalArgumentException("Maximum number of envelopes for this node must be greater than zero");

		this.envelope = envelope;
		this.children = new RegionQuadTree[4];
		this.envelopes = new ArrayList<>();

		this.maxEnvelopes = maxEnvelopes;
	}



	@Override
	public void insert(final Region envelope){
		if(children[0] != null){
			final int indexToPlaceObject = getChildIndex(envelope);
			if(indexToPlaceObject != RegionQuadTree.INDEX_SELF){
				children[indexToPlaceObject].insert(envelope);
				return;
			}
		}
		envelopes.add(envelope);

		if(envelopes.size() > maxEnvelopes){
			split();

			int i = 0;
			while(i < envelopes.size()){
				final int indexToPlaceObject = getChildIndex(envelopes.get(i));
				if(indexToPlaceObject != RegionQuadTree.INDEX_SELF)
					children[indexToPlaceObject].insert(envelopes.remove(i));
				else
					i ++;
			}
		}
	}

	private void split(){
		final double childWidth = envelope.getWidth() / 2.;
		final double childHeight = envelope.getHeight() / 2.;

		final double x = envelope.getX();
		final double y = envelope.getY();
		children[RegionQuadTree.INDEX_NORTH_WEST_CHILD] = RegionQuadTree.create(Region.of(x, y, childWidth, childHeight),
			maxEnvelopes);
		children[RegionQuadTree.INDEX_NORTH_EAST_CHILD] = RegionQuadTree.create(Region.of(x + childWidth, y, childWidth, childHeight),
			maxEnvelopes);
		children[RegionQuadTree.INDEX_SOUTH_WEST_CHILD] = RegionQuadTree.create(Region.of(x, y + childHeight, childWidth, childHeight),
			maxEnvelopes);
		children[RegionQuadTree.INDEX_SOUTH_EAST_CHILD] = RegionQuadTree.create(Region.of(x + childWidth, y + childHeight, childWidth,
			childHeight), maxEnvelopes);
	}

	protected int getChildIndex(final Region envelope){
		int index = INDEX_SELF;
		final double verticalDividingLine = envelope.getX() + envelope.getWidth() / 2.;
		final double horizontalDividingLine = envelope.getY() + envelope.getHeight() / 2.;

		final boolean fitsCompletelyInNorthHalf = (envelope.getY() < horizontalDividingLine
			&& envelope.getHeight() + envelope.getY() < horizontalDividingLine);
		final boolean fitsCompletelyInSouthHalf = (envelope.getY() > horizontalDividingLine);
		final boolean fitsCompletelyInWestHalf = (envelope.getX() < verticalDividingLine
			&& envelope.getX() + envelope.getWidth() < verticalDividingLine);
		final boolean fitsCompletelyInEastHalf = (envelope.getX() > verticalDividingLine);

		if(fitsCompletelyInEastHalf){
			if(fitsCompletelyInNorthHalf)
				index = RegionQuadTree.INDEX_NORTH_EAST_CHILD;
			else if(fitsCompletelyInSouthHalf)
				index = RegionQuadTree.INDEX_SOUTH_EAST_CHILD;
		}
		else if(fitsCompletelyInWestHalf){
			if(fitsCompletelyInNorthHalf)
				index = RegionQuadTree.INDEX_NORTH_WEST_CHILD;
			else if(fitsCompletelyInSouthHalf)
				index = RegionQuadTree.INDEX_SOUTH_WEST_CHILD;
		}
		return index;
	}


	@Override
	public boolean delete(final Region envelope){
		int index = getChildIndex(envelope);
		if(index == INDEX_SELF || children[index] == null){
			for(int i = 0; i < envelopes.size(); i ++){
				if(envelopes.get(i).equals(envelope)){
					envelopes.remove(i);
					return true;
				}
			}
		}
		else{
			children[index].delete(envelope);
			return true;
		}

		return false;
	}


	@Override
	public boolean contains(final Region envelope){
		final Stack<RegionQuadTree> stack = new Stack<>();
		stack.push(this);
		while(!stack.isEmpty()){
			final RegionQuadTree current = stack.pop();

			final int index = current.getChildIndex(envelope);
			if(index == RegionQuadTree.INDEX_SELF){
				if(current.children[0] != null)
					for(final RegionQuadTree child : current.children)
						if(envelope.intersects(child.envelope))
							stack.push(child);
			}
			else if(current.children[index] != null)
				stack.push(current.children[index]);

			for(final Region region : current.envelopes)
				if(region.intersects(envelope))
					return true;
		}

		return false;
	}

	@Override
	public Collection<Region> query(final Region envelope){
		final List<Region> returnList = new ArrayList<>();

		final Stack<RegionQuadTree> stack = new Stack<>();
		stack.push(this);
		while(!stack.isEmpty()){
			final RegionQuadTree current = stack.pop();

			final int index = current.getChildIndex(envelope);
			if(index == RegionQuadTree.INDEX_SELF){
				if(current.children[0] != null)
					for(final RegionQuadTree child : current.children)
						if(envelope.intersects(child.envelope))
							stack.push(child);
			}
			else if(current.children[index] != null)
				stack.push(current.children[index]);

			for(final Region region : current.envelopes)
				if(region.intersects(envelope))
					returnList.add(region);
		}

		return returnList;
	}


	@Override
	public boolean isEmpty(){
		return envelopes.isEmpty();
	}

}
