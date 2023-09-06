/**
 * Copyright (c) 2024 Mauro Trevisan
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

import io.github.mtrevisan.mapmatcher.helpers.SpatialNode;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.Objects;


public class Region implements Comparable<Region>{

	/** The minimum x-coordinate. */
	private double minX;
	/** The minimum y-coordinate. */
	private double minY;
	/** The maximum x-coordinate. */
	private double maxX;
	/** The maximum y-coordinate. */
	private double maxY;

	/** Store linear region quadtree location code. */
	private BitCode code;
	private SpatialNode node;
	private boolean boundary;


	/**
	 * Creates a region defined by maximum and minimum values.
	 *
	 * @param region The region to copy from.
	 */
	public static Region of(final Region region){
		return new Region(region.minX, region.minY, region.maxX, region.maxY);
	}

	/**
	 * Creates a region defined by maximum and minimum values.
	 *
	 * @param minX The minimum x-value.
	 * @param minY The minimum y-value.
	 * @param maxX The maximum x-value.
	 * @param maxY The maximum y-value.
	 */
	public static Region of(final double minX, final double minY, final double maxX, final double maxY){
		return new Region(minX, minY, maxX, maxY);
	}

	/**
	 * Creates a region defined by two points.
	 *
	 * @param p1	The first point.
	 * @param p2	The second point.
	 */
	public static Region of(final Point p1, final Point p2){
		return of(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
			Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()));
	}

	/**
	 * Creates a region defined by a single point.
	 *
	 * @param p	The point.
	 */
	public static Region of(final Point p){
		return of(p.getX(), p.getY(), p.getX(), p.getY());
	}

	public static Region ofEmpty(){
		return new Region();
	}


	protected Region(){
		setToNull();
	}

	protected Region(final double minX, final double minY, final double maxX, final double maxY){
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	/**
	 * Returns the region's minimum x-value.
	 * <p>
	 * <code>Double.NaN</code> indicates that this is a null region.
	 * </p>
	 *
	 * @return	The minimum x-coordinate.
	 */
	public double getMinX(){
		return minX;
	}

	/**
	 * Returns the region's minimum y-value.
	 * <p>
	 * <code>Double.NaN</code> indicates that this is a null region.
	 * </p>
	 *
	 * @return	The minimum y-coordinate.
	 */
	public double getMinY(){
		return minY;
	}

	/**
	 * Returns the region's maximum x-value.
	 * <p>
	 * <code>Double.NaN</code> indicates that this is a null region.
	 * </p>
	 *
	 * @return	The maximum x-coordinate.
	 */
	public double getMaxX(){
		return maxX;
	}

	/**
	 * Returns the region's maximum y-value.
	 * <p>
	 * <code>Double.NaN</code> indicates that this is a null region.
	 * </p>
	 *
	 * @return	The maximum y-coordinate.
	 */
	public double getMaxY(){
		return maxY;
	}

	public double getMidX(){
		return (maxX + minX) / 2.;
	}

	public double getMidY(){
		return (maxY + minY) / 2.;
	}

	public double getExtentX(){
		return (maxX - minX);
	}

	public double getExtentY(){
		return (maxY - minY);
	}

	public double euclideanArea(){
		return (maxX - minX) * (maxY - minY);
	}

	/**
	 * Returns whether this region is a "null" region.
	 *
	 * @return	Whether this region is uninitialized or is the region of the empty geometry.
	 */
	public boolean isNull(){
		return (maxX < minX || maxY < minY);
	}

	/**
	 * Makes this region a "null" region, that is, the region of the empty geometry.
	 */
	public void setToNull(){
		minX = Double.NaN;
		minY = Double.NaN;
		maxX = Double.NaN;
		maxY = Double.NaN;
	}

	public BitCode getCode(){
		return code;
	}

	public void setCode(final BitCode code){
		this.code = code;
	}

	public int getLevel(){
		return (code.length() >> 1);
	}

	public SpatialNode getNode(){
		return node;
	}

	public void setNode(final SpatialNode node){
		this.node = node;
	}

	public boolean isBoundary(){
		return boundary;
	}

	public void setBoundary(){
		boundary = true;
	}


	/**
	 * Enlarges this region so that it contains the given {@link Point}.
	 * <p>
	 * Has no effect if the point is already on or within the region.
	 * </p>
	 *
	 * @param points	The point(s) to include.
	 */
	public void expandToInclude(final Point... points){
		for(final Point point : points)
			expandToInclude(point.getX(), point.getY());
	}

	/**
	 * Enlarges this region so that it contains the given point.
	 * <p>
	 * Has no effect if the point is already on or within the region.
	 * </p>
	 *
	 * @param x	The value to lower the minimum x to or to raise the maximum x to.
	 * @param y	The value to lower the minimum y to or to raise the maximum y to.
	 */
	public void expandToInclude(final double x, final double y){
		if(isNull()){
			minX = x;
			minY = y;
			maxX = x;
			maxY = y;
		}
		else{
			minX = Math.min(minX, x);
			minY = Math.min(minY, y);
			maxX = Math.max(maxX, x);
			maxY = Math.max(maxY, y);
		}
	}

	/**
	 * Enlarges this region so that it contains the <code>other</code> region.
	 * <p>
	 * Has no effect if <code>other</code> is wholly on or within the region.
	 * </p>
	 *
	 * @param other	The region to expand to include.
	 */
	public void expandToInclude(final Region other){
		if(other.isNull())
			return;

		if(isNull()){
			minX = other.getMinX();
			minY = other.getMinY();
			maxX = other.getMaxX();
			maxY = other.getMaxY();
		}
		else{
			minX = Math.min(minX, other.minX);
			minY = Math.min(minY, other.minY);
			maxX = Math.max(maxX, other.maxX);
			maxY = Math.max(maxY, other.maxY);
		}
	}

	/**
	 * Expands this region by a given distance in all directions.
	 * <p>
	 * Both positive and negative distances are supported.
	 * </p>
	 *
	 * @param distance	The distance to expand the region.
	 */
	public void expandBy(final double distance){
		expandBy(distance, distance);
	}

	/**
	 * Expands this region by a given distance in all directions.
	 * <p>
	 * Both positive and negative distances are supported.
	 * </p>
	 *
	 * @param deltaX	The distance to expand the region along the X axis.
	 * @param deltaY	The distance to expand the region along the Y axis.
	 */
	public void expandBy(final double deltaX, final double deltaY){
		if(isNull())
			return;

		minX -= deltaX;
		minY -= deltaY;
		maxX += deltaX;
		maxY += deltaY;

		//check for region disappearing
		if(maxX < minX || maxY < minY)
			setToNull();
	}

	/**
	 * Computes the intersection of two {@link Region}s.
	 *
	 * @param region	The region to intersect with.
	 * @return	A new region representing the intersection of the regions (this will be the null region if either argument is null,
	 * or they do not intersect
	 */
	public Region intersection(final Region region){
		if(isNull() || region.isNull() || !intersects(region))
			return ofEmpty();

		final double intersectionMinX = Math.max(minX, region.minX);
		final double intersectionMinY = Math.max(minY, region.minY);
		final double intersectionMaxX = Math.min(maxX, region.maxX);
		final double intersectionMaxY = Math.min(maxY, region.maxY);
		return of(intersectionMinX, intersectionMinY, intersectionMaxX, intersectionMaxY);
	}

	/**
	 * Tests if the region defined by <code>other</code> intersects the region of this region.
	 *
	 * @param region	The region which this region is being checked for intersection.
	 * @return	Whether the regions intersect.
	 */
	public boolean intersects(final Region region){
		return !(region == null || isNull() || region.isNull()
			|| maxX < region.minX || region.maxX < minX
			|| maxY < region.minY || region.maxY < minY);
	}

	/**
	 * Tests if the region defined by <code>other</code> is fully contained into the region of this region.
	 *
	 * @param region	The region which this region is being checked for containment.
	 * @return	Whether the regions is fully contained.
	 */
	public boolean contains(final Region region){
		return (region != null && !isNull() && !region.isNull()
		  && minX >= region.minX && region.maxX <= maxX
		  && minY >= region.minY && region.maxY <= maxY);
	}

	/**
	 * Tests if the point <code>p</code> intersects (lies inside) the region of this region.
	 *
	 * @param p	The point to be tested.
	 * @return	Whether the point intersects this region.
	 */
	public boolean contains(final Point p){
		return !(p == null || isNull()
			|| p.getX() > maxX || p.getX() < minX
			|| p.getY() > maxY || p.getY() < minY);
	}


	/**
	 * Compares two regions using lexicographic ordering.
	 * <p>
	 * The ordering comparison is based on the usual numerical comparison between the sequence of ordinates.<br/>
	 * Null regions are less than all non-null regions.
	 * </p>
	 *
	 * @param region	An region object.
	 */
	@Override
	public int compareTo(final Region region){
		if(isNull())
			return (region.isNull()? 0: -1);
		if(region.isNull())
			return 1;

		//compare based on numerical ordering of ordinates
		int cmp = Double.compare(minX, region.minX);
		if(cmp == 0)
			cmp = Double.compare(minY, region.minY);
		if(cmp == 0)
			cmp = Double.compare(maxX, region.maxX);
		if(cmp == 0)
			cmp = Double.compare(maxY, region.maxY);
		return cmp;
	}

	@Override
	public boolean equals(final Object obj){
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;

		final Region other = (Region)obj;
		return (Objects.equals(minX, other.minX) && Objects.equals(minY, other.minY)
				  && Objects.equals(maxX, other.maxX) && Objects.equals(maxY, other.maxY));
	}

	@Override
	public int hashCode(){
		return Objects.hash(minX, minY, maxX, maxY);
	}

	@Override
	public String toString(){
		return (boundary? "Boundary": "Region")
				 + (code != null? "(" + code + ")": "")
				 + "[" + minX + " : " + maxX + ", " + minY + " : " + maxY + "]";
	}

}
