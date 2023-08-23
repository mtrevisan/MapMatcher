package io.github.mtrevisan.mapmatcher.helpers;

import java.util.Comparator;
import java.util.List;
import java.util.Random;


public class QuickSelect{

	private static final Random random = new Random();


	public static <T> T select(final List<T> list, final int n, final Comparator<? super T> cmp){
		return select(list, 0, list.size() - 1, n, cmp);
	}

	public static <T> T select(final List<T> list, int left, int right, final int n, final Comparator<? super T> cmp){
		for(; ; ){
			if(left == right)
				return list.get(left);

			int pivot = pivotIndex(left, right);
			pivot = partition(list, left, right, pivot, cmp);
			if(n == pivot)
				return list.get(n);

			if(n < pivot)
				right = pivot - 1;
			else
				left = pivot + 1;
		}
	}

	private static <T> int partition(final List<T> list, final int left, final int right, final int pivot, final Comparator<? super T> cmp){
		final T pivotValue = list.get(pivot);
		swap(list, pivot, right);
		int store = left;
		for(int i = left; i < right; i ++)
			if(cmp.compare(list.get(i), pivotValue) < 0){
				swap(list, store, i);
				store ++;
			}

		swap(list, right, store);
		return store;
	}

	private static <T> void swap(final List<T> list, final int i, final int j){
		final T value = list.get(i);
		list.set(i, list.get(j));
		list.set(j, value);
	}

	private static int pivotIndex(final int left, final int right){
		return left + random.nextInt(right - left + 1);
	}

}
