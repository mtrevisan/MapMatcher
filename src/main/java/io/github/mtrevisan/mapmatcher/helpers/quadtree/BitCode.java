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

import java.util.BitSet;


public class BitCode extends BitSet implements Comparable<BitCode>{

	private int bitCount;


	public static BitCode ofEmpty(){
		return new BitCode();
	}


	private BitCode(){}


	public int getLevel(){
		return (bitCount >> 1);
	}

	/**
	 * Appends the given amount of bits, little endian, in the underlying {@link BitSet}.
	 *
	 * @param index	The index from which to start reading.
	 * @param bits	The number of bits to append, zero is LSB.
	 * @return	The value.
	 */
	public int valueAt(final int index, final int bits){
		int value = 0x00;
		for(int i = 0; i < bits; i ++)
			value = (value << 1) | (get(index + i)? 1: 0);
		return value;
	}

	/**
	 * Appends the given amount of bits, little endian, in the underlying {@link BitSet}.
	 *
	 * @param value	The value to append.
	 * @param bits	The number of bits to append, zero is LSB.
	 * @return	This instance.
	 */
	public BitCode append(final int value, final int bits){
		int mask = 1;
		for(int i = 0; i < bits; i ++, bitCount ++, mask <<= 1)
			if((value & mask) != 0)
				set(bitCount);
		return this;
	}

	public BitCode clone(){
		return (BitCode)super.clone();
	}


	@Override
	public String toString(){
		final StringBuilder sb = new StringBuilder(bitCount);
		for(int i = 0; i < bitCount; i ++)
			sb.append(get(i)? '1': '0');
		return sb.toString();
	}

	@Override
	public int compareTo(final BitCode other){
		if(bitCount != other.bitCount)
			return (bitCount - other.bitCount) >> 1;

		final BitSet xor = clone();
		xor.xor(other);
		final int firstDifference = xor.nextSetBit(0);
		if(bitCount == other.bitCount && firstDifference == -1)
			return 0;

		//compare the first different bit
		return (get(firstDifference)? 1: -1);
	}

}
