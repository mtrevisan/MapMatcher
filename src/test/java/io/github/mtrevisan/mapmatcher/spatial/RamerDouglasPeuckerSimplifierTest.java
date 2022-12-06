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
package io.github.mtrevisan.mapmatcher.spatial;

import io.github.mtrevisan.mapmatcher.spatial.distances.EuclideanCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class RamerDouglasPeuckerSimplifierTest{

	@Test
	void simple(){
		GeometryFactory factory = new GeometryFactory(new EuclideanCalculator());
		Point[] points = new Point[]{
			factory.createPoint(1., 1.),
			factory.createPoint(1.3, 2.),
			factory.createPoint(2., 1.2),
			factory.createPoint(3., 1.)
		};

		RamerDouglasPeuckerSimplifier simplifier = new RamerDouglasPeuckerSimplifier();
		simplifier.setDistanceTolerance(0.5);

		Point[] reducedPoints = simplifier.simplify(points);

		Point[] expected = new Point[]{
			factory.createPoint(1., 1.),
			factory.createPoint(1.3, 2.),
			factory.createPoint(3., 1.)
		};
		Assertions.assertArrayEquals(expected, reducedPoints);
	}

}
