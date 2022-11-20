package io.github.mtrevisan.mapmatcher.weight;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class DurationEdgeWeightCalculatorTest{

	@Test
	void should_return_edge_duration_in_minutes(){
		Coordinates[] coordinates = new Coordinates[]{
			Coordinates.of(14.552797, 121.058805),
			Coordinates.of(14.593999, 120.994260),
			Coordinates.of(77.870317, 96.591876),
			Coordinates.of(21.719527, -4.815018),
			Coordinates.of(-17.727830, 23.704799),
			Coordinates.of(58.585396, -130.279576)
		};
		double[] expectedDurations = new double[]{9_979.5, 5_939_630.2, 8_999_043.8};
		double[] maxSpeeds = new double[]{50., 80., 100.};
		for(int i = 0; i < expectedDurations.length; i ++){
			Coordinates fromCoordinates = coordinates[i << 1];
			Coordinates toCoordinates = coordinates[(i << 1) + 1];
			double expectedDuration = expectedDurations[i];
			double maxSpeed = maxSpeeds[i];
			DurationEdgeWeightCalculator edgeWeightCalculator = new DurationEdgeWeightCalculator();
			double actualDistance = edgeWeightCalculator.calculateWeight(new Edge(
				new Vertex("1", fromCoordinates),
				new Vertex("2", toCoordinates), maxSpeed));

			Assertions.assertEquals(expectedDuration, actualDistance, 0.05);
		}
	}

	@Test
	void should_return_duration_in_minutes_between_vertices_with_max_possible_speed(){
		Coordinates[] coordinates = new Coordinates[]{
			Coordinates.of(14.552797, 121.058805),
			Coordinates.of(14.593999, 120.994260),
			Coordinates.of(77.870317, 96.591876),
			Coordinates.of(21.719527, -4.815018),
			Coordinates.of(-17.727830, 23.704799),
			Coordinates.of(58.585396, -130.279576)
		};
		double[] expectedDistances = new double[]{3_564.1, 3_394_074.4, 6_427_888.5};

		for(int i = 0; i < expectedDistances.length; i ++){
			Coordinates fromCoordinates = coordinates[i << 1];
			Coordinates toCoordinates = coordinates[(i << 1) + 1];
			double expectedDistance = expectedDistances[i];
			DurationEdgeWeightCalculator edgeWeightCalculator = new DurationEdgeWeightCalculator();

			double actualDistance = edgeWeightCalculator.calculateWeight(
				new Vertex("1", fromCoordinates),
				new Vertex("2", toCoordinates));

			Assertions.assertEquals(expectedDistance, actualDistance, 0.05);
		}
	}

}
