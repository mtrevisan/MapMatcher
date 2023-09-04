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

	private static final double NULL_DIMENSION = -1.;


	/** The x-coordinate. */
	private double x;
	/** The y-coordinate. */
	private double y;
	/** The width along the x-coordinate. */
	private double width;
	/** The height along the y-coordinate. */
	private double height;

	/** Store linear region quadtree location code. */
	private BitCode code;
	private SpatialNode node;
	private boolean boundary;


	/**
	 * Creates a region defined by maximum and minimum values.
	 *
	 * @param x The x-value.
	 * @param y The y-value.
	 * @param width The width along the x-value.
	 * @param height The height along the y-value.
	 */
	public static Region of(final double x, final double y, final double width, final double height){
		return new Region(x, y, width, height);
	}

	/**
	 * Creates a region defined by two points.
	 *
	 * @param p1	The first point.
	 * @param p2	The second point.
	 */
	public static Region of(final Point p1, final Point p2){
		return of(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
			Math.abs(p2.getX() - p1.getX()), Math.abs(p2.getY() - p1.getY()));
	}

	/**
	 * Creates a region defined by a single point.
	 *
	 * @param p	The point.
	 */
	public static Region of(final Point p){
		return of(p.getX(), p.getY(), 0., 0.);
	}

	public static Region ofEmpty(){
		return new Region();
	}


	protected Region(){
		setToNull();
	}

	protected Region(final double x, final double y, final double width, final double height){
		if(width < 0.)
			throw new IllegalArgumentException("Width must be non-negative.");
		if(height < 0.)
			throw new IllegalArgumentException("Height must be non-negative.");

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Returns the region's x-value.
	 *
	 * @return	The x-coordinate.
	 */
	public double getX(){
		return x;
	}

	/**
	 * Returns the region's y-value.
	 *
	 * @return	The y-coordinate.
	 */
	public double getY(){
		return y;
	}

	/**
	 * Returns the region's width along the x-value.
	 * <p>
	 * <code>-1</code> indicates that this is a null region.
	 * </p>
	 *
	 * @return	The width along the x-coordinate.
	 */
	public double getWidth(){
		return width;
	}

	/**
	 * Returns the region's height along the y-value.
	 * <p>
	 * <code>-1</code> indicates that this is a null region.
	 * </p>
	 *
	 * @return	The height along the y-coordinate.
	 */
	public double getHeight(){
		return height;
	}

	/**
	 * Returns whether this region is a "null" region.
	 *
	 * @return	Whether this region is uninitialized or is the region of the empty geometry.
	 */
	public boolean isNull(){
		return (width < 0. || height < 0.);
	}

	/**
	 * Makes this region a "null" region, that is, the region of the empty geometry.
	 */
	public void setToNull(){
		width = NULL_DIMENSION;
		height = NULL_DIMENSION;
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
			this.x = x;
			this.y = y;
			width = x;
			height = y;
		}
		else{
			final double newLeft = Math.min(this.x, x);
			final double newTop = Math.min(this.y, y);
			final double newRight = Math.max(this.x + width, x);
			final double newBottom = Math.max(this.y + height, y);
			this.x = newLeft;
			this.y = newTop;
			width = newRight - newLeft;
			height = newBottom - newTop;
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
			x = other.getX();
			y = other.getY();
			width = other.getWidth();
			height = other.getHeight();
		}
		else{
			final double newLeft = Math.min(x, other.x);
			final double newTop = Math.min(y, other.y);
			final double newRight = Math.max(x + width, other.x + other.width);
			final double newBottom = Math.max(y + height, other.y + other.height);
			x = newLeft;
			y = newTop;
			width = newRight - newLeft;
			height = newBottom - newTop;
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

		x -= deltaX;
		y -= deltaY;
		width += deltaX;
		height += deltaY;

		//check for region disappearing
		if(width < 0. || height < 0.)
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

		final double intersectionMinX = Math.max(x, region.x);
		final double intersectionMinY = Math.max(y, region.y);
		final double intersectionMaxX = Math.min(x + width, region.x + region.width);
		final double intersectionMaxY = Math.min(y + height, region.y + region.height);
		return of(intersectionMinX, intersectionMinY,
			intersectionMaxX - intersectionMinX, intersectionMaxY - intersectionMinY);
	}

	/**
	 * Tests if the region defined by <code>other</code> intersects the region of this region.
	 *
	 * @param region	The region which this region is being checked for intersection.
	 * @return	Whether the regions intersect.
	 */
	public boolean intersects(final Region region){
		return !(region == null || isNull() || region.isNull()
			|| x + width < region.x || region.x + region.width < x
			|| y + height < region.y || region.y + region.height < y);
	}

	/**
	 * Tests if the region defined by <code>other</code> is fully contained into the region of this region.
	 *
	 * @param region	The region which this region is being checked for containment.
	 * @return	Whether the regions is fully contained.
	 */
	public boolean contains(final Region region){
		return (region != null && !isNull() && !region.isNull()
			&& x >= region.x && region.x + region.width <= x + width
			&& y >= region.y && region.y + region.height <= y + height);
	}

	/**
	 * Tests if the point <code>p</code> intersects (lies inside) the region of this region.
	 *
	 * @param p	The point to be tested.
	 * @return	Whether the point intersects this region.
	 */
	public boolean contains(final Point p){
		return !(p == null || isNull()
			|| p.getX() > x + width || p.getX() < x
			|| p.getY() > y + height || p.getY() < y);
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
		int cmp = Double.compare(x, region.x);
		if(cmp == 0)
			cmp = Double.compare(y, region.y);
		if(cmp == 0)
			cmp = Double.compare(width, region.width);
		if(cmp == 0)
			cmp = Double.compare(height, region.height);
		return cmp;
	}

	@Override
	public boolean equals(final Object obj){
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;

		final Region other = (Region)obj;
		return (Objects.equals(x, other.x) && Objects.equals(y, other.y)
			&& Objects.equals(width, other.width) && Objects.equals(height, other.height));
	}

	@Override
	public int hashCode(){
		return Objects.hash(x, y, width, height);
	}

	@Override
	public String toString(){
		return (boundary? "Boundary": "Region")
			+ (code != null? "(" + code + ")": "")
			+ "[" + x + " : " + (x + width) + ", " + y + " : " + (y + height) + "]";
	}

}
