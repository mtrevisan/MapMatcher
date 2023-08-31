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
package io.github.mtrevisan.mapmatcher.helpers.hilbertrtree;

import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;


class HilbertEncoder{

	private final int level;
	private final double minX;
	private final double minY;
	private final double strideX;
	private final double strideY;


	HilbertEncoder(final int level, final Region extent){
		this.level = level;
		final int hSide = (int)Math.pow(2, level) - 1;

		minX = extent.getX();
		final double extentX = extent.getWidth();
		strideX = extentX / hSide;

		minY = extent.getX();
		final double extentY = extent.getHeight();
		strideY = extentY / hSide;
	}

	int encode(final Region env){
		final double middleX = env.getWidth() / 2 + env.getX();
		final int x = (int)((middleX - minX) / strideX);

		final double middleY = env.getHeight() / 2 + env.getY();
		final int y = (int)((middleY - minY) / strideY);

		return HilbertCode.encode(level, x, y);
	}

}
