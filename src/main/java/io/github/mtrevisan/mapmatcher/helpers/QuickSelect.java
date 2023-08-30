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
package io.github.mtrevisan.mapmatcher.helpers;

import java.util.Comparator;
import java.util.List;


public class QuickSelect{

	/**
	 * Determines the k<sup>th</sup> order statistic for the given list.
	 *
	 * @param list	The list.
	 * @param index	The <em>k</em> value to use.
	 * @param cmp	The comparator to use.
	 * @return	The k<sup>th</sup> order statistic for the list.
	 */
	public static <T> T select(final List<T> list, final int index, final Comparator<? super T> cmp){
		return select(list, 0, list.size() - 1, index, cmp);
	}

	/**
	 * Determines the k<sup>th</sup> order statistic for the given list.
	 *
	 * @param list	The list.
	 * @param leftIndex	The left index of the current sublist.
	 * @param rightIndex	The right index of the current sublist.
	 * @param index	The <em>k</em> value to use.
	 * @param cmp	The comparator to use.
	 * @return	The k<sup>th</sup> order statistic for the list.
	 */
	public static <T> T select(final List<T> list, int leftIndex, int rightIndex, final int index, final Comparator<? super T> cmp){
		while(true){
			if(leftIndex == rightIndex)
				return list.get(leftIndex);

			int pivot = pivotIndex(leftIndex, rightIndex);
			pivot = partition(list, leftIndex, rightIndex, pivot, cmp);
			if(index == pivot)
				return list.get(index);

			if(index < pivot)
				rightIndex = pivot - 1;
			else
				leftIndex = pivot + 1;
		}
	}

	/**
	 * Randomly partitions a set about a pivot such that the values to the left
	 * of the pivot are less than or equal to the pivot and the values to the
	 * right of the pivot are greater than the pivot.
	 *
	 * @param list	The list.
	 * @param leftIndex	The left index of the current sublist.
	 * @param rightIndex	The right index of the current sublist.
	 * @param pivotIndex	The pivot index.
	 * @return	The index of the pivot.
	 */
	private static <T> int partition(final List<T> list, final int leftIndex, final int rightIndex, final int pivotIndex,
			final Comparator<? super T> cmp){
		final T pivotValue = list.get(pivotIndex);
		//move pivot to end
		swap(list, pivotIndex, rightIndex);

		int storeIndex = leftIndex;
		for(int i = leftIndex; i < rightIndex; i ++)
			if(cmp.compare(list.get(i), pivotValue) < 0){
				swap(list, storeIndex, i);
				storeIndex ++;
			}

		//move pivot to its final place
		swap(list, rightIndex, storeIndex);

		return storeIndex;
	}

	/**
	 * Swaps two elements in a list.
	 *
	 * @param list	The list.
	 * @param i	The index of the first element to swap.
	 * @param j	The index of the second element to swap.
	 */
	private static <T> void swap(final List<T> list, final int i, final int j){
		final T value = list.get(i);
		list.set(i, list.get(j));
		list.set(j, value);
	}

	private static int pivotIndex(final int left, final int right){
		return left + ((right - left) >> 1);
	}

}
