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
package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import io.github.mtrevisan.mapmatcher.helpers.SpatialNode;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.Objects;


public class Region implements Comparable<Region>{

	/** The x-coordinate. */
	private double x;
	/** The y-coordinate. */
	private double y;
	/** The width along the x-coordinate. */
	private double width;
	/** The height along the y-coordinate. */
	private double height;

	private SpatialNode node;
	private boolean boundary;


	/**
	 * Creates an <code>Envelope</code> for a region defined by maximum and minimum values.
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
	 * Creates an <code>Envelope</code> for a region defined by two points.
	 *
	 * @param p1	The first point.
	 * @param p2	The second point.
	 */
	public static Region of(final Point p1, final Point p2){
		return of(p1.getX(), p1.getY(), p2.getX() - p1.getX(), p2.getY() - p1.getY());
	}

	/**
	 * Creates an envelope for a region defined by a single point.
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
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Returns the <code>Envelope</code>s x-value.
	 *
	 * @return	The x-coordinate.
	 */
	public double getX(){
		return x;
	}

	/**
	 * Returns the <code>Envelope</code>s y-value.
	 *
	 * @return	The y-coordinate.
	 */
	public double getY(){
		return y;
	}

	/**
	 * Returns the <code>Envelope</code>s width along the x-value.
	 * <p>
	 * <code>-1</code> indicates that this is a null <code>Envelope</code>.
	 * </p>
	 *
	 * @return	The width along the x-coordinate.
	 */
	public double getWidth(){
		return width;
	}

	/**
	 * Returns the <code>Envelope</code>s height along the y-value.
	 * <p>
	 * <code>-1</code> indicates that this is a null <code>Envelope</code>.
	 * </p>
	 *
	 * @return	The height along the y-coordinate.
	 */
	public double getHeight(){
		return height;
	}

	/**
	 * Returns whether this <code>Envelope</code> is a "null" envelope.
	 *
	 * @return	Whether this <code>Envelope</code> is uninitialized or is the envelope of the empty geometry.
	 */
	public boolean isNull(){
		return (width < 0 || height < 0);
	}

	/**
	 * Makes this <code>Envelope</code> a "null" envelope, that is, the envelope of the empty geometry.
	 */
	public void setToNull(){
		x = 0.;
		y = 0.;
		width = -1;
		height = -1.;
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
	 * Enlarges this <code>Envelope</code> so that it contains the given {@link Point}.
	 * <p>
	 * Has no effect if the point is already on or within the envelope.
	 * </p>
	 *
	 * @param pp	The point(s) to include.
	 */
	public void expandToInclude(final Point... pp){
		for(final Point p : pp)
			expandToInclude(p.getX(), p.getY());
	}

	/**
	 * Enlarges this <code>Envelope</code> so that it contains the given point.
	 * <p>
	 * Has no effect if the point is already on or within the envelope.
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
			if(x < this.x)
				this.x = x;
			if(y < this.y)
				this.y = y;
			if(x > width)
				width = x;
			if(y > height)
				height = y;
		}
	}

	/**
	 * Enlarges this <code>Envelope</code> so that it contains the <code>other</code> Envelope.
	 * <p>
	 * Has no effect if <code>other</code> is wholly on or within the envelope.
	 * </p>
	 *
	 * @param other	The <code>Envelope</code> to expand to include.
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
			if(other.x < x)
				x = other.x;
			if(other.y < y)
				y = other.y;
			if(other.width > width)
				width = other.width;
			if(other.height > height)
				height = other.height;
		}
	}

	/**
	 * Expands this envelope by a given distance in all directions.
	 * <p>
	 * Both positive and negative distances are supported.
	 * </p>
	 *
	 * @param distance	The distance to expand the envelope.
	 */
	public void expandBy(final double distance){
		expandBy(distance, distance);
	}

	/**
	 * Expands this envelope by a given distance in all directions.
	 * <p>
	 * Both positive and negative distances are supported.
	 * </p>
	 *
	 * @param deltaX	The distance to expand the envelope along the X axis.
	 * @param deltaY	The distance to expand the envelope along the Y axis.
	 */
	public void expandBy(final double deltaX, final double deltaY){
		if(isNull())
			return;

		x -= deltaX;
		y -= deltaY;
		width += deltaX;
		height += deltaY;

		//check for envelope disappearing
		if(x > width || y > height)
			setToNull();
	}

	/**
	 * Computes the intersection of two {@link Region}s.
	 *
	 * @param envelope	The envelope to intersect with.
	 * @return	A new Envelope representing the intersection of the envelopes (this will be the null envelope if either argument is null,
	 * or they do not intersect
	 */
	public Region intersection(final Region envelope){
		if(isNull() || envelope.isNull() || !intersects(envelope))
			return ofEmpty();

		final double intMinX = Math.max(x, envelope.x);
		final double intMinY = Math.max(y, envelope.y);
		final double intMaxX = Math.min(width, envelope.width);
		final double intMaxY = Math.min(height, envelope.height);
		return of(intMinX, intMinY, intMaxX, intMaxY);
	}

	/**
	 * Tests if the region defined by <code>other</code> intersects the region of this <code>Envelope</code>.
	 *
	 * @param envelope	The <code>Envelope</code> which this <code>Envelope</code> is being checked for intersecting
	 * @return	Whether the <code>Envelope</code>s intersect.
	 */
	public boolean intersects(final Region envelope){
		return !(isNull() || envelope.isNull()
			|| envelope.x > width || envelope.width < x || envelope.y > height || envelope.height < y);
	}

	/**
	 * Tests if the point <code>p</code> intersects (lies inside) the region of this <code>Envelope</code>.
	 *
	 * @param p	The point to be tested.
	 * @return	Whether the point intersects this envelope.
	 */
	public boolean intersects(final Point p){
		return !(isNull() || p.getX() > width || p.getX() < x || p.getY() > height || p.getY() < y);
	}


	/**
	 * Compares two envelopes using lexicographic ordering.
	 * <p>
	 * The ordering comparison is based on the usual numerical comparison between the sequence of ordinates.<br/>
	 * Null envelopes are less than all non-null envelopes.
	 * </p>
	 *
	 * @param envelope	An envelope object.
	 */
	@Override
	public int compareTo(final Region envelope){
		if(isNull())
			return (envelope.isNull()? 0: -1);
		if(envelope.isNull())
			return 1;

		//compare based on numerical ordering of ordinates
		int cmp = Double.compare(x, envelope.x);
		if(cmp == 0)
			cmp = Double.compare(y, envelope.y);
		if(cmp == 0)
			cmp = Double.compare(width, envelope.width);
		if(cmp == 0)
			cmp = Double.compare(height, envelope.height);
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
		return "Env[" + x + " : " + (x + width) + ", " + y + " : " + (y + height) + "]";
	}

}
