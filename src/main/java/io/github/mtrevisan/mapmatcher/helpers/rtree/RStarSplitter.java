package io.github.mtrevisan.mapmatcher.helpers.rtree;

import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


class RStarSplitter implements NodeSplitter{

	private static final int INCREASING_X_LOWER = 0;
	private static final int INCREASING_X_UPPER = 1;
	private static final int INCREASING_Y_LOWER = 2;
	private static final int INCREASING_Y_UPPER = 3;

	@SuppressWarnings("unchecked")
	private static final Comparator<RNode>[] COMPARATORS = new Comparator[4];
	static{
		COMPARATORS[INCREASING_X_LOWER] = Comparator.comparingDouble(node -> node.region.getMinX());
		COMPARATORS[INCREASING_X_UPPER] = Comparator.comparingDouble(node -> node.region.getMaxX());
		COMPARATORS[INCREASING_Y_LOWER] = Comparator.comparingDouble(node -> node.region.getMinY());
		COMPARATORS[INCREASING_Y_UPPER] = Comparator.comparingDouble(node -> node.region.getMaxY());
	}


	private final int minObjects;


	static RStarSplitter create(final RTreeOptions options){
		return new RStarSplitter(options);
	}


	private RStarSplitter(final RTreeOptions options){
		this.minObjects = options.minChildren;
	}


	@Override
	public Object[] pivotNode(final RNode node){
		double minMarginSum = Double.POSITIVE_INFINITY;
		//list is changed each time, so a new list should be created to preserve the original one
		final List<RNode> children = new ArrayList<>(node.children);
		final int childrenCount = children.size();
		final List<RNode> minMarginSumChildren = new ArrayList<>(childrenCount);
		for(int i = 0; i < 4; i ++){
			children.sort(COMPARATORS[i]);

			final double marginSum = marginValueSum(children);
			if(marginSum <= minMarginSum){
				minMarginSum = marginSum;

				minMarginSumChildren.clear();
				minMarginSumChildren.addAll(children);
			}
		}

		//find minimum pair:
		int minIndex = minObjects;
		final Region minRegion1 = minimumBoundingRegion(minMarginSumChildren, 0, minIndex);
		final Region minRegion2 = minimumBoundingRegion(minMarginSumChildren, minIndex, childrenCount);
		double minNonIntersectingArea = minRegion1.nonIntersectingArea(minRegion2);
		double minAreaSum = minRegion1.euclideanArea() + minRegion2.euclideanArea();
		for(int i = minObjects + 1; i < childrenCount - minObjects + 1; i ++){
			final Region region1 = minimumBoundingRegion(minMarginSumChildren, 0, i);
			final Region region2 = minimumBoundingRegion(minMarginSumChildren, i, childrenCount);

			final double nonIntersectingArea = region1.nonIntersectingArea(region2);
			final double areaSum = region1.euclideanArea() + region2.euclideanArea();
			int value = Double.compare(nonIntersectingArea, minNonIntersectingArea);
			if(value == 0)
				value = Double.compare(areaSum, minAreaSum);
			if(value < 0){
				minIndex = i;
				minNonIntersectingArea = nonIntersectingArea;
				minAreaSum = areaSum;
			}
		}

		return new Object[]{
			minMarginSumChildren.subList(0, minIndex),
			minMarginSumChildren.subList(minIndex, minMarginSumChildren.size())
		};
	}

	private double marginValueSum(final List<RNode> list){
		double sum = 0.;
		for(int i = minObjects; i < list.size() - minObjects + 1; i ++)
			sum += minimumBoundingRegion(list, 0, i).euclideanPerimeter()
				+ minimumBoundingRegion(list, i, list.size()).euclideanPerimeter();
		return sum;
	}

	/** Returns the minimum bounding region of a number of items. */
	private Region minimumBoundingRegion(final List<RNode> nodes, final int fromIndexInclusive, final int toIndexExclusive){
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for(int i = fromIndexInclusive; i < toIndexExclusive; i ++){
			final RNode node = nodes.get(i);

			final Region nodeRegion = node.region;
			minX = Math.min(minX, nodeRegion.getMinX());
			minY = Math.min(minY, nodeRegion.getMinY());
			maxX = Math.max(maxX, nodeRegion.getMaxX());
			maxY = Math.max(maxY, nodeRegion.getMaxY());
		}
		return Region.of(minX, minY, maxX, maxY);
	}

}
