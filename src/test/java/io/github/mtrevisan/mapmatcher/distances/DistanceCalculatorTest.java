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
package io.github.mtrevisan.mapmatcher.distances;

import io.github.mtrevisan.mapmatcher.spatial.Coordinate;
import io.github.mtrevisan.mapmatcher.spatial.distances.DistanceCalculator;
import io.github.mtrevisan.mapmatcher.spatial.distances.GeodeticCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class DistanceCalculatorTest{

	@Test
	void should_calculate_approximate_distance_in_km_between__startCoordinates_and__endCoordinates_close_to_expectedDistance(){
		final DistanceCalculator calculator = new GeodeticCalculator();
		final DistanceCalculator alternateCalculator = new GeodeticCalculator();
		final Coordinate[] coordinates = new Coordinate[]{Coordinate.of(121.058805, 14.552797), Coordinate.of(120.994260, 14.593999), Coordinate.of(96.591876, 77.870317), Coordinate.of(-4.815018, 21.719527), Coordinate.of(23.704799, -17.727830), Coordinate.of(-130.279576, 58.585396)};
		final double[] expectedDistances = new double[]{
			8_316.3,
			7_919_506.9,
			14_998_406.4
		};

		for(int i = 0; i < expectedDistances.length; i ++){
			Coordinate startCoordinates = coordinates[i << 1];
			Coordinate endCoordinates = coordinates[(i << 1) + 1];
			double actualDistance = calculator.distance(startCoordinates, endCoordinates);

			Assertions.assertEquals(expectedDistances[i], actualDistance, 0.05);
		}
	}

}
