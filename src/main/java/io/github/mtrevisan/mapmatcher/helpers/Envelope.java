/**
 * Copyright (c) 2021 Mauro Trevisan
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
package io.github.mtrevisan.mapmatcher.helpers;

import java.util.Objects;


public class Envelope implements Comparable<Envelope>{

	/** The minimum x-coordinate. */
	private double minX;
	/** The maximum x-coordinate. */
	private double maxX;
	/** The minimum y-coordinate. */
	private double minY;
	/** The maximum y-coordinate. */
	private double maxY;


	/**
	 * Creates an <code>Envelope</code> for a region defined by maximum and minimum values.
	 *
	 * @param x1	The first x-value.
	 * @param x2	The second x-value.
	 * @param y1	The first y-value.
	 * @param y2	The second y-value.
	 */
	public static Envelope of(final double x1, final double x2, final double y1, final double y2){
		return new Envelope(x1, x2, y1, y2);
	}

	/**
	 * Creates an <code>Envelope</code> for a region defined by two Coordinates.
	 *
	 * @param p1	The first Coordinate.
	 * @param p2	The second Coordinate.
	 */
	public static Envelope of(final Coordinate p1, final Coordinate p2){
		return of(p1.getX(), p2.getX(), p1.getY(), p2.getY());
	}

	/**
	 * Creates an <code>Envelope</code> for a region defined by a single Coordinate.
	 *
	 * @param p	The Coordinate.
	 */
	public static Envelope of(final Coordinate p){
		return of(p.getX(), p.getX(), p.getY(), p.getY());
	}

	public static Envelope ofEmpty(){
		return new Envelope();
	}


	private Envelope(){
		setToNull();
	}

	private Envelope(final double x1, final double x2, final double y1, final double y2){
		if(x1 < x2){
			minX = x1;
			maxX = x2;
		}
		else{
			minX = x2;
			maxX = x1;
		}

		if(y1 < y2){
			minY = y1;
			maxY = y2;
		}
		else{
			minY = y2;
			maxY = y1;
		}
	}

	/**
	 * Returns the <code>Envelope</code>s minimum x-value.
	 * <p>
	 * <code>min x &gt; max x</code> indicates that this is a null <code>Envelope</code>.
	 * </p>
	 *
	 * @return	The minimum x-coordinate.
	 */
	public double getMinX(){
		return minX;
	}

	/**
	 * Returns the <code>Envelope</code>s maximum x-value.
	 * <p>
	 * <code>min x &gt; max x</code> indicates that this is a null <code>Envelope</code>.
	 * </p>
	 *
	 * @return	The maximum x-coordinate.
	 */
	public double getMaxX(){
		return maxX;
	}

	/**
	 * Returns the <code>Envelope</code>s minimum y-value.
	 * <p>
	 * <code>min y &gt; max y</code> indicates that this is a null <code>Envelope</code>.
	 * </p>
	 *
	 * @return	The minimum y-coordinate.
	 */
	public double getMinY(){
		return minY;
	}

	/**
	 * Returns the <code>Envelope</code>s maximum y-value.
	 * <p>
	 * <code>min y &gt; max y</code> indicates that this is a null <code>Envelope</code>.
	 * </p>
	 *
	 * @return	The maximum y-coordinate.
	 */
	public double getMaxY(){
		return maxY;
	}

	/**
	 * Returns whether this <code>Envelope</code> is a "null" envelope.
	 *
	 * @return	Whether this <code>Envelope</code> is uninitialized or is the envelope of the empty geometry.
	 */
	public boolean isNull(){
		return (maxX < minX);
	}

	/**
	 * Makes this <code>Envelope</code> a "null" envelope, that is, the envelope of the empty geometry.
	 */
	public void setToNull(){
		minX = 0.;
		maxX = -1.;
		minY = 0.;
		maxY = -1.;
	}


	/**
	 * Returns the difference between the maximum and minimum <code>x</code> values.
	 *
	 * @return	<code>max x - min x</code>, or <code>0</code> if this is a null <code>Envelope</code>.
	 */
	public double getWidth(){
		return (isNull()? 0: maxX - minX);
	}

	/**
	 * Returns the difference between the maximum and minimum <code>y</code> values.
	 *
	 * @return	<code>max y - min y</code>, or <code>0</code> if this is a null <code>Envelope</code>.
	 */
	public double getHeight(){
		return (isNull()? 0: maxY - minY);
	}

	/**
	 * Gets the area of this envelope.
	 *
	 * @return	The area of the envelope (0 if the envelope is null).
	 */
	public double getArea(){
		return getWidth() * getHeight();
	}


	/**
	 * Enlarges this <code>Envelope</code> so that it contains the given {@link Coordinate}.
	 * <p>
	 * Has no effect if the point is already on or within the envelope.
	 * </p>
	 *
	 * @param p	The Coordinate to expand to include.
	 */
	public void expandToInclude(final Coordinate p){
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
			minX = x;
			maxX = x;
			minY = y;
			maxY = y;
		}
		else{
			if(x < minX)
				minX = x;
			if(x > maxX)
				maxX = x;
			if(y < minY)
				minY = y;
			if(y > maxY)
				maxY = y;
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
	public void expandToInclude(final Envelope other){
		if(other.isNull())
			return;

		if(isNull()){
			minX = other.getMinX();
			maxX = other.getMaxX();
			minY = other.getMinY();
			maxY = other.getMaxY();
		}
		else{
			if(other.minX < minX)
				minX = other.minX;
			if(other.maxX > maxX)
				maxX = other.maxX;
			if(other.minY < minY)
				minY = other.minY;
			if(other.maxY > maxY)
				maxY = other.maxY;
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

		minX -= deltaX;
		maxX += deltaX;
		minY -= deltaY;
		maxY += deltaY;

		//check for envelope disappearing
		if(minX > maxX || minY > maxY)
			setToNull();
	}

	/**
	 * Computes the intersection of two {@link Envelope}s.
	 *
	 * @param envelope	The envelope to intersect with.
	 * @return	A new Envelope representing the intersection of the envelopes (this will be the null envelope if either argument is null,
	 * or they do not intersect
	 */
	public Envelope intersection(final Envelope envelope){
		if(isNull() || envelope.isNull() || !intersects(envelope))
			return ofEmpty();

		final double intMinX = Math.max(minX, envelope.minX);
		final double intMinY = Math.max(minY, envelope.minY);
		final double intMaxX = Math.min(maxX, envelope.maxX);
		final double intMaxY = Math.min(maxY, envelope.maxY);
		return of(intMinX, intMaxX, intMinY, intMaxY);
	}

	/**
	 * Tests if the region defined by <code>other</code> intersects the region of this <code>Envelope</code>.
	 *
	 * @param envelope	The <code>Envelope</code> which this <code>Envelope</code> is being checked for intersecting
	 * @return	Whether the <code>Envelope</code>s intersect.
	 */
	public boolean intersects(final Envelope envelope){
		return !(isNull() || envelope.isNull()
			|| envelope.minX > maxX || envelope.maxX < minX || envelope.minY > maxY || envelope.maxY < minY);
	}

	/**
	 * Tests if the point <code>p</code> intersects (lies inside) the region of this <code>Envelope</code>.
	 *
	 * @param p	The <code>Coordinate</code> to be tested.
	 * @return	Whether the point intersects this <code>Envelope</code>.
	 */
	public boolean intersects(final Coordinate p){
		return !(isNull() || p.getX() > maxX || p.getX() < minX || p.getY() > maxY || p.getY() < minY);
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
	public int compareTo(final Envelope envelope){
		if(isNull())
			return (envelope.isNull()? 0: -1);
		if(envelope.isNull())
			return 1;

		//compare based on numerical ordering of ordinates
		int cmp = Double.compare(minX, envelope.minX);
		if(cmp == 0)
			cmp = Double.compare(minY, envelope.minY);
		if(cmp == 0)
			cmp = Double.compare(maxX, envelope.maxX);
		if(cmp == 0)
			cmp = Double.compare(maxY, envelope.maxY);
		return cmp;
	}

	@Override
	public boolean equals(final Object obj){
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;

		final Envelope other = (Envelope)obj;
		return (Objects.equals(minX, other.minX) && Objects.equals(minY, other.minY)
			&& Objects.equals(maxX, other.maxX) && Objects.equals(maxY, other.maxY));
	}

	@Override
	public int hashCode(){
		return Objects.hash(minX, minY, maxX, maxY);
	}

	@Override
	public String toString(){
		return "Env[" + minX + " : " + maxX + ", " + minY + " : " + maxY + "]";
	}

}
