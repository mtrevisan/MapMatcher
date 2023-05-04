/**
 * Copyright (c) 2022 Mauro Trevisan
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class MathHelperTest{

	@Test
	void regular_large_numbers(){
		Assertions.assertTrue(nearlyEqual(1_000_000., 1_000_001.));
		Assertions.assertTrue(nearlyEqual(1_000_001., 1_000_000.));
		Assertions.assertFalse(nearlyEqual(10_000., 10_001.));
		Assertions.assertFalse(nearlyEqual(10_001., 10_000.));
	}

	@Test
	void negative_large_numbers(){
		Assertions.assertTrue(nearlyEqual(-1_000_000., -1_000_001.));
		Assertions.assertTrue(nearlyEqual(-1_000_001., -1_000_000.));
		Assertions.assertFalse(nearlyEqual(-10_000., -10_001.));
		Assertions.assertFalse(nearlyEqual(-10_001., -10_000.));
	}

	@Test
	void numbers_around_one(){
		Assertions.assertTrue(nearlyEqual(1.000_000_1, 1.000_000_2));
		Assertions.assertTrue(nearlyEqual(1.000_000_2, 1.000_000_1));
		Assertions.assertFalse(nearlyEqual(1.000_2, 1.000_1));
		Assertions.assertFalse(nearlyEqual(1.000_1, 1.000_2));
	}

	@Test
	void numbers_around_minus_one(){
		Assertions.assertTrue(nearlyEqual(-1.000_001, -1.000_002));
		Assertions.assertTrue(nearlyEqual(-1.000_002, -1.000_001));
		Assertions.assertFalse(nearlyEqual(-1.000_1, -1.000_2));
		Assertions.assertFalse(nearlyEqual(-1.000_2, -1.000_1));
	}

	@Test
	void numbers_between_one_and_zero(){
		Assertions.assertTrue(nearlyEqual(0.000_000_001_000_001, 0.000_000_001_000_002));
		Assertions.assertTrue(nearlyEqual(0.000_000_001_000_002, 0.000_000_001_000_001));
		Assertions.assertFalse(nearlyEqual(0.000_000_000_001_002, 0.000_000_000_001_001));
		Assertions.assertFalse(nearlyEqual(0.000_000_000_001_001, 0.000_000_000_001_002));
	}

	@Test
	void numbers_between_minus_one_and_zero(){
		Assertions.assertTrue(nearlyEqual(-0.000_000_001_000_001, -0.000_000_001_000_002));
		Assertions.assertTrue(nearlyEqual(-0.000_000_001_000_002, -0.000_000_001_000_001));
		Assertions.assertFalse(nearlyEqual(-0.000_000_000_001_002, -0.000_000_000_001_001));
		Assertions.assertFalse(nearlyEqual(-0.000_000_000_001_001, -0.000_000_000_001_002));
	}

	@Test
	void small_differences_away_from_zero(){
		Assertions.assertTrue(nearlyEqual(0.3, 0.300_000_03));
		Assertions.assertTrue(nearlyEqual(-0.3, -0.300_000_03));
	}

	@Test
	void comparisons_involving_zero(){
		Assertions.assertTrue(nearlyEqual(0., 0.));
		Assertions.assertTrue(nearlyEqual(0., -0.));
		Assertions.assertTrue(nearlyEqual(-0., -0.));
		Assertions.assertFalse(nearlyEqual(0.000_000_01, 0.));
		Assertions.assertFalse(nearlyEqual(0., 0.000_000_01));
		Assertions.assertFalse(nearlyEqual(-0.000_000_01, 0.));
		Assertions.assertFalse(nearlyEqual(0., -0.000_000_01));

		Assertions.assertTrue(MathHelper.nearlyEqual(0., 1.e-310, 0.01));
		Assertions.assertTrue(MathHelper.nearlyEqual(1.e-310, 0., 0.01));
		Assertions.assertFalse(MathHelper.nearlyEqual(1.e-310, 0., 0.000_001));
		Assertions.assertFalse(MathHelper.nearlyEqual(0., 1.e-310, 0.000_001));

		Assertions.assertTrue(MathHelper.nearlyEqual(0., -1.e-310, 0.1));
		Assertions.assertTrue(MathHelper.nearlyEqual(-1.e-310, 0., 0.1));
		Assertions.assertFalse(MathHelper.nearlyEqual(-1.e-310, 0., 0.000_000_01));
		Assertions.assertFalse(MathHelper.nearlyEqual(0., -1.e-310, 0.000_000_01));
	}

	@Test
	void comparisons_involving_extrame_value__overflow_potential(){
		Assertions.assertTrue(nearlyEqual(Double.MAX_VALUE, Double.MAX_VALUE));
		Assertions.assertFalse(nearlyEqual(Double.MAX_VALUE, -Double.MAX_VALUE));
		Assertions.assertFalse(nearlyEqual(-Double.MAX_VALUE, Double.MAX_VALUE));
		Assertions.assertFalse(nearlyEqual(Double.MAX_VALUE, Double.MAX_VALUE / 2.));
		Assertions.assertFalse(nearlyEqual(Double.MAX_VALUE, -Double.MAX_VALUE / 2.));
		Assertions.assertFalse(nearlyEqual(-Double.MAX_VALUE, Double.MAX_VALUE / 2.));
	}

	@Test
	void comparisons_involving_infinities(){
		Assertions.assertTrue(nearlyEqual(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
		Assertions.assertTrue(nearlyEqual(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
		Assertions.assertFalse(nearlyEqual(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
		Assertions.assertFalse(nearlyEqual(Double.POSITIVE_INFINITY, Double.MAX_VALUE));
		Assertions.assertFalse(nearlyEqual(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE));
	}

	@Test
	void comparisons_involving_nan_values(){
		Assertions.assertFalse(nearlyEqual(Double.NaN, Double.NaN));
		Assertions.assertFalse(nearlyEqual(Double.NaN, 0.));
		Assertions.assertFalse(nearlyEqual(-0., Double.NaN));
		Assertions.assertFalse(nearlyEqual(Double.NaN, -0.));
		Assertions.assertFalse(nearlyEqual(0., Double.NaN));
		Assertions.assertFalse(nearlyEqual(Double.NaN, Double.POSITIVE_INFINITY));
		Assertions.assertFalse(nearlyEqual(Double.POSITIVE_INFINITY, Double.NaN));
		Assertions.assertFalse(nearlyEqual(Double.NaN, Double.NEGATIVE_INFINITY));
		Assertions.assertFalse(nearlyEqual(Double.NEGATIVE_INFINITY, Double.NaN));
		Assertions.assertFalse(nearlyEqual(Double.NaN, Double.MAX_VALUE));
		Assertions.assertFalse(nearlyEqual(Double.MAX_VALUE, Double.NaN));
		Assertions.assertFalse(nearlyEqual(Double.NaN, -Double.MAX_VALUE));
		Assertions.assertFalse(nearlyEqual(-Double.MAX_VALUE, Double.NaN));
		Assertions.assertFalse(nearlyEqual(Double.NaN, Double.MIN_VALUE));
		Assertions.assertFalse(nearlyEqual(Double.MIN_VALUE, Double.NaN));
		Assertions.assertFalse(nearlyEqual(Double.NaN, -Double.MIN_VALUE));
		Assertions.assertFalse(nearlyEqual(-Double.MIN_VALUE, Double.NaN));
	}

	@Test
	void comparisons_of_numbers_on_opposite_sides_of_zero(){
		Assertions.assertFalse(nearlyEqual(1.000_000_001, -1.));
		Assertions.assertFalse(nearlyEqual(-1., 1.000_000_001));
		Assertions.assertFalse(nearlyEqual(-1.000_000_001, 1.));
		Assertions.assertFalse(nearlyEqual(1., -1.000_000_001));
		Assertions.assertTrue(nearlyEqual(10. * Double.MIN_VALUE, 10. * -Double.MIN_VALUE));
		Assertions.assertTrue(nearlyEqual(10_000. * Double.MIN_VALUE, 10_000. * -Double.MIN_VALUE));
	}

	@Test
	void comparisons_of_numbers_very_close_to_zero(){
		Assertions.assertTrue(nearlyEqual(Double.MIN_VALUE, Double.MIN_VALUE));
		Assertions.assertTrue(nearlyEqual(Double.MIN_VALUE, -Double.MIN_VALUE));
		Assertions.assertTrue(nearlyEqual(-Double.MIN_VALUE, Double.MIN_VALUE));
		Assertions.assertTrue(nearlyEqual(Double.MIN_VALUE, 0.));
		Assertions.assertTrue(nearlyEqual(0., Double.MIN_VALUE));
		Assertions.assertTrue(nearlyEqual(-Double.MIN_VALUE, 0.));
		Assertions.assertTrue(nearlyEqual(0., -Double.MIN_VALUE));

		Assertions.assertFalse(nearlyEqual(0.000_000_001, -Double.MIN_VALUE));
		Assertions.assertFalse(nearlyEqual(0.000_000_001, Double.MIN_VALUE));
		Assertions.assertFalse(nearlyEqual(Double.MIN_VALUE, 0.000_000_001));
		Assertions.assertFalse(nearlyEqual(-Double.MIN_VALUE, 0.000_000_001));
	}


	private static boolean nearlyEqual(double a, double b){
		return MathHelper.nearlyEqual(a, b, 0.000_01);
	}

}
