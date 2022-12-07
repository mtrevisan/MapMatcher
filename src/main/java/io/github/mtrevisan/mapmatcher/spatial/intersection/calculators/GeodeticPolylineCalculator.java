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
package io.github.mtrevisan.mapmatcher.spatial.intersection.calculators;

import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;


public class GeodeticPolylineCalculator implements IntersectionCalculator{

	@Override
	public Point leftmostPoint(final Polyline polyline){
		//TODO
		return null;
	}

	@Override
	public Point rightmostPoint(final Polyline polyline){
		//TODO
		return null;
	}

	@Override
	public double calculateYIndex(final Point pointLeft, final Point pointRight, final double x){
		//TODO
		return 0.;
	}

	@Override
	public int compare(final Point point1, final Point point2){
		//TODO
		return 0;
	}

	@Override
	public Point intersection(final Polyline polyline1, final Polyline polyline2){
		//TODO
		return null;
	}

}
