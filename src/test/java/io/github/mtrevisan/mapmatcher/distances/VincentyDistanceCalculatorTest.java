package io.github.mtrevisan.mapmatcher.distances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;


class VincentyDistanceCalculatorTest{

	@Test
	void should_calculate_approximate_distance_in_km_between__startCoordinates_and__endCoordinates_close_to_expectedDistance(){
		final DistanceCalculator calculator = new EarthEllipsoidalCalculator();
		final Coordinate[] coordinates = new Coordinate[]{
			new Coordinate(121.058805, 14.552797),
			new Coordinate(120.994260, 14.593999),
			new Coordinate(96.591876, 77.870317),
			new Coordinate(-4.815018, 21.719527),
			new Coordinate(23.704799, -17.727830),
			new Coordinate(-130.279576, 58.585396)
		};
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
