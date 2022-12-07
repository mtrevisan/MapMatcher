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


public class MathHelper{

	private MathHelper(){}


	/**
	 * @see <a href="https://floating-point-gui.de/errors/comparison/">Comparison</a>
	 *
	 * @param a	The first value.
	 * @param b	The second value.
	 * @param epsilon	The margin.
	 * @return	Whether the values are nearly equal.
	 */
	public static boolean nearlyEqual(final double a, final double b, final double epsilon){
		final double absA = Math.abs(a);
		final double absB = Math.abs(b);
		final double diff = Math.abs(a - b);

		//shortcut, handles infinities
		if(a == b)
			return true;

		//`a` or `b` is zero or both are extremely close to it relative error is less meaningful here
		if(a == 0. || b == 0. || absA + absB < Double.MIN_NORMAL)
			return (diff < epsilon * Double.MIN_NORMAL);

		//use relative error
		return (diff < epsilon * Math.min((absA + absB), Double.MAX_VALUE));
	}

}
