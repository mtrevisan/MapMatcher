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

import io.github.mtrevisan.mapmatcher.distances.DistanceCalculator;
import io.github.mtrevisan.mapmatcher.distances.GeodeticCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class PolylineTest{

	@Test
	void should_simplify_lineString(){
		Coordinate node12_31_41 = Coordinate.of(12.238140517207398, 45.65897415921759);
		Coordinate node22 = Coordinate.of(12.242949896905884, 45.69828882177029);
		Coordinate node23 = Coordinate.of(12.200627355552967, 45.732876303059044);
		DistanceCalculator distanceCalculator = new GeodeticCalculator();

		Polyline polyline = Polyline.ofSimplified(distanceCalculator, 2_000., node12_31_41, node22, node23);

		Assertions.assertEquals(2, polyline.size());
	}

	@Test
	void should_not_simplify_lineString(){
		Coordinate node12_31_41 = Coordinate.of(12.238140517207398, 45.65897415921759);
		Coordinate node22 = Coordinate.of(12.242949896905884, 45.69828882177029);
		Coordinate node23 = Coordinate.of(12.200627355552967, 45.732876303059044);
		DistanceCalculator distanceCalculator = new GeodeticCalculator();

		Polyline polyline = Polyline.ofSimplified(distanceCalculator, 10., node12_31_41, node22, node23);

		Assertions.assertEquals(3, polyline.size());
	}

}
