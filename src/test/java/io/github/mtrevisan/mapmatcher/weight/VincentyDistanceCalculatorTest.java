package io.github.mtrevisan.mapmatcher.weight;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class VincentyDistanceCalculatorTest{

	@Test
	void should_calculate_approximate_distance_in_km_between__startCoordinates_and__endCoordinates_close_to_expectedDistance(){
		Coordinates[] coordinates = new Coordinates[]{
			Coordinates.of(14.552797, 121.058805), Coordinates.of(14.593999, 120.994260), Coordinates.of(77.870317, 96.591876), Coordinates.of(21.719527, -4.815018), Coordinates.of(-17.727830, 23.704799), Coordinates.of(58.585396, -130.279576)};
		double[] expectedDistances = new double[]{
			8_316.3,
			7_919_506.9,
			14_998_406.4
		};

		for(int i = 0; i < expectedDistances.length; i ++){
			Coordinates startCoordinates = coordinates[i << 1];
			Coordinates endCoordinates = coordinates[(i << 1) + 1];
			double actualDistance = EarthEllipsoidalCalculator.distance(startCoordinates, endCoordinates);

			Assertions.assertEquals(expectedDistances[i], actualDistance, 0.05);
		}
	}

}
