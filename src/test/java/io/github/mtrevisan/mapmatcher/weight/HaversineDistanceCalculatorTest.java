package io.github.mtrevisan.mapmatcher.weight;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class HaversineDistanceCalculatorTest{

	@Test
	void should_calculate_approximate_distance_in_km_between__startCoordinates_and__endCoordinates_close_to_expectedDistance(){
		HaversineDistanceCalculator distanceCalculator = new HaversineDistanceCalculator();

		Coordinates[] coordinates = new Coordinates[]{
			new Coordinates(14.552797, 121.058805), new Coordinates(14.593999, 120.994260),
			new Coordinates(77.870317, 96.591876), new Coordinates(21.719527, -4.815018),
			new Coordinates(- 17.727830, 23.704799), new Coordinates(58.585396, - 130.279576)
		};
		double[] expectedDistances = new double[]{
			8.321,
			7910.8281,
			15001.64302
		};

		for(int i = 0; i < coordinates.length; i ++){
			Coordinates startCoordinates = coordinates[i ++];
			Coordinates endCoordinates = coordinates[i];
			double actualDistance = distanceCalculator.calculateDistance(startCoordinates, endCoordinates);

			Assertions.assertEquals(expectedDistances[i / 2], actualDistance, 0.00005);
		}
	}

}
