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
package io.github.mtrevisan.mapmatcher.helpers.rtree;

import io.github.mtrevisan.mapmatcher.helpers.quadtree.Region;


class HilbertEncoder{

	private final int level;
	private final double minX;
	private final double minY;
	private final double strideX;
	private final double strideY;


	HilbertEncoder(final int level, final Region extent){
		this.level = level;
		final int hilbertSide = (1 << level) - 1;

		minX = extent.getMinX();
		minY = extent.getMinY();
		strideX = extent.getExtentX() / hilbertSide;
		strideY = extent.getExtentY() / hilbertSide;
	}

	int encode(final Region region){
		final int x = (int)((region.getMidX() - minX) / strideX);
		final int y = (int)((region.getMidY() - minY) / strideY);
		return HilbertCode.encode(level, x, y);
	}

}
