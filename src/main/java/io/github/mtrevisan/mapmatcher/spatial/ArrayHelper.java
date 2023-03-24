package io.github.mtrevisan.mapmatcher.spatial;


public class ArrayHelper{

	private ArrayHelper(){}


	/**
	 * Reverses the order of the given array.
	 *
	 * @param array	The array to reverse in place.
	 */
	public static <T> void reverse(final T[] array){
		int i = 0;
		int j = array.length - 1;
		T tmp;
		while(j > i){
			tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;

			j --;
			i ++;
		}
	}

}
