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
package io.github.mtrevisan.mapmatcher.helpers.hprtree;

import io.github.mtrevisan.mapmatcher.helpers.spatial.Coordinate;


/**
 * Encodes points as the index along finite planar Hilbert curves.
 * <p>
 * The planar Hilbert Curve is a continuous space-filling curve.<br/>
 * In the limit the Hilbert curve has infinitely many vertices and fills the space of the unit square.<br/>
 * A sequence of finite approximations to the infinite Hilbert curve is defined by the level number.<br/>
 * The finite Hilbert curve at level n H<sub>n</sub> contains 2<sup>n + 1</sup> points.<br/>
 * Each finite Hilbert curve defines an ordering of the points in the 2-dimensional range square containing the curve.<br/>
 * Curves fill the range square of side 2<sup>level</sup>.<br/>
 * Curve points have ordinates in the range [0, 2<sup>level</sup> - 1].<br/>
 * The index of a point along a Hilbert curve is called the Hilbert code.<br/>
 * The code for a given point is specific to the level chosen.<br/>
 * </p>
 * <p>
 * This implementation represents codes using 32-bit integers.<br/>
 * This allows levels 0 to 16 to be handled.<br/>
 * The class supports encoding points in the range of a given level curve and decoding the point for a given code value.
 * </p>
 * <p>
 * The Hilbert order has the property that it tends to preserve locality.<br/>
 * This means that codes which are near in value will have spatially proximate points. The converse is not always true - the delta between
 * codes for nearby points is not always small. But the average delta is small enough that the Hilbert order is an effective way of
 * linearizing space to support range queries.
 * </p>
 *
 * @author Martin Davis
 */
class HilbertCode{

	/**
	 * The maximum curve level that can be represented.
	 */
	static final int MAX_LEVEL = 16;


	/**
	 * The number of points in the curve for the given level.
	 * <p>
	 * The number of points is 2<sup>2 * level</sup>.
	 * </p>
	 *
	 * @param level	The level of the curve.
	 * @return	The number of points.
	 */
	static int size(final int level){
		checkLevel(level);
		return (int)Math.pow(2, 2 * level);
	}

	/**
	 * The maximum ordinate value for points in the curve for the given level.
	 * <p>
	 * The maximum ordinate is 2<sup>level</sup> - 1.
	 * </p>
	 *
	 * @param level	The level of the curve.
	 * @return	The maximum ordinate value.
	 */
	static int maxOrdinate(final int level){
		checkLevel(level);
		return (int)Math.pow(2, level) - 1;
	}

	/**
	 * The level of the finite Hilbert curve which contains at least the given number of points.
	 *
	 * @param numberOfPoints	The number of points required.
	 * @return	The level of the curve.
	 */
	static int level(final int numberOfPoints){
		final int pow2 = (int)((Math.log(numberOfPoints) / Math.log(2.)));
		final int level = pow2 / 2;
		final int size = size(level);
		return (size < numberOfPoints? level + 1: level);
	}

	private static void checkLevel(final int level){
		if(level > MAX_LEVEL)
			throw new IllegalArgumentException("Level must be in range 0 to " + MAX_LEVEL);
	}

	/**
	 * Encodes a point (x,y) in the range of the Hilbert curve at a given level as the index of the point along the curve.
	 * <p>
	 * The index will lie in the range [0, 2<sup>level + 1</sup>].
	 * </p>
	 *
	 * @param level	The level of the Hilbert curve.
	 * @param x	The x ordinate of the point.
	 * @param y	The y ordinate of the point.
	 * @return	The index of the point along the Hilbert curve.
	 */
	static int encode(final int level, int x, int y){
		//Fast Hilbert curve algorithm by http://threadlocalmutex.com/ ported from C++ https://github.com/rawrunprotected/hilbert_curves
		//(public domain)
		final int levelClamp = levelClamp(level);

		x = x << (16 - levelClamp);
		y = y << (16 - levelClamp);

		long a = x ^ y;
		long b = 0xFFFF ^ a;
		long c = 0xFFFF ^ (x | y);
		long d = x & (y ^ 0xFFFF);

		long aa = a | (b >> 1);
		long bb = (a >> 1) ^ a;
		long cc = ((c >> 1) ^ (b & (d >> 1))) ^ c;
		long dd = ((a & (c >> 1)) ^ (d >> 1)) ^ d;

		a = aa;
		b = bb;
		c = cc;
		d = dd;
		aa = ((a & (a >> 2)) ^ (b & (b >> 2)));
		bb = ((a & (b >> 2)) ^ (b & ((a ^ b) >> 2)));
		cc ^= ((a & (c >> 2)) ^ (b & (d >> 2)));
		dd ^= ((b & (c >> 2)) ^ ((a ^ b) & (d >> 2)));

		a = aa;
		b = bb;
		c = cc;
		d = dd;
		aa = ((a & (a >> 4)) ^ (b & (b >> 4)));
		bb = ((a & (b >> 4)) ^ (b & ((a ^ b) >> 4)));
		cc ^= ((a & (c >> 4)) ^ (b & (d >> 4)));
		dd ^= ((b & (c >> 4)) ^ ((a ^ b) & (d >> 4)));

		a = aa;
		b = bb;
		c = cc;
		d = dd;
		cc ^= ((a & (c >> 8)) ^ (b & (d >> 8)));
		dd ^= ((b & (c >> 8)) ^ ((a ^ b) & (d >> 8)));

		a = cc ^ (cc >> 1);
		b = dd ^ (dd >> 1);

		long i0 = x ^ y;
		long i1 = b | (0xFFFF ^ (i0 | a));

		i0 = (i0 | (i0 << 8)) & 0x00FF_00FF;
		i0 = (i0 | (i0 << 4)) & 0x0F0F_0F0F;
		i0 = (i0 | (i0 << 2)) & 0x3333_3333;
		i0 = (i0 | (i0 << 1)) & 0x5555_5555;

		i1 = (i1 | (i1 << 8)) & 0x00FF_00FF;
		i1 = (i1 | (i1 << 4)) & 0x0F0F_0F0F;
		i1 = (i1 | (i1 << 2)) & 0x3333_3333;
		i1 = (i1 | (i1 << 1)) & 0x5555_5555;

		final long index = ((i1 << 1) | i0) >> (32 - 2 * levelClamp);
		return (int)index;
	}

	/**
	 * Clamps a level to the range valid for the index algorithm used.
	 *
	 * @param level	The level of a Hilbert curve.
	 * @return	A valid level.
	 */
	private static int levelClamp(final int level){
		//clamp order to [1, 16]
		return Math.min(Math.max(level, 1), MAX_LEVEL);
	}

	/**
	 * Computes the point on a Hilbert curve of given level for a given code index.
	 * <p>
	 * The point ordinates will lie in the range [0, 2<sup>level</sup></i> - 1].
	 * </p>
	 *
	 * @param level	The Hilbert curve level.
	 * @param index	The index of the point on the curve.
	 * @return	The point on the Hilbert curve.
	 */
	static Coordinate decode(final int level, int index){
		checkLevel(level);
		final int levelClamp = levelClamp(level);

		index = index << (32 - 2 * levelClamp);

		final long i0 = deinterleave(index);
		final long i1 = deinterleave(index >> 1);

		final long t0 = (i0 | i1) ^ 0xFFFF;
		final long t1 = i0 & i1;

		final long prefixT0 = prefixScan(t0);
		final long prefixT1 = prefixScan(t1);

		final long a = (((i0 ^ 0xFFFF) & prefixT1) | (i0 & prefixT0));

		final long x = (a ^ i1) >> (16 - levelClamp);
		final long y = (a ^ i0 ^ i1) >> (16 - levelClamp);

		return Coordinate.of(x, y);
	}

	private static long prefixScan(long x){
		x = (x >> 8) ^ x;
		x = (x >> 4) ^ x;
		x = (x >> 2) ^ x;
		x = (x >> 1) ^ x;
		return x;
	}

	private static long deinterleave(int x){
		x = x & 0x55555555;
		x = (x | (x >> 1)) & 0x33333333;
		x = (x | (x >> 2)) & 0x0F0F0F0F;
		x = (x | (x >> 4)) & 0x00FF00FF;
		x = (x | (x >> 8)) & 0x0000FFFF;
		return x;
	}

}
