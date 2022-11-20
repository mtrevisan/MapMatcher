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
			new Coordinates(14.552797, 121.058805), new Coordinates(14.593999, 120.994260),
			new Coordinates(77.870317, 96.591876), new Coordinates(21.719527, -4.815018),
			new Coordinates(-17.727830, 23.704799), new Coordinates(58.585396, -130.279576)
		};
		double[] expectedDurations = new double[]{9.985_188, 5933.121_067, 9000.985_782};
		double[] maxSpeeds = new double[]{50., 80., 100.};
		for(int i = 0; i < expectedDurations.length; i ++){
			Coordinates fromCoordinates = coordinates[i << 1];
			Coordinates toCoordinates = coordinates[(i << 1) + 1];
			double expectedDuration = expectedDurations[i];
			double maxSpeed = maxSpeeds[i];
			DurationEdgeWeightCalculator edgeWeightCalculator = new DurationEdgeWeightCalculator();
			double actualDistance = edgeWeightCalculator.calculateWeight(new Edge(new Vertex(1, fromCoordinates),
				new Vertex(2, toCoordinates), maxSpeed));

			Assertions.assertEquals(expectedDuration, actualDistance, 0.000_000_5);
		}
	}

	@Test
	void should_return_duration_in_minutes_between_vertices_with_max_possible_speed(){
		Coordinates[] coordinates = new Coordinates[]{
			new Coordinates(14.552797, 121.058805), new Coordinates(14.593999, 120.994260),
			new Coordinates(77.870317, 96.591876), new Coordinates(21.719527, -4.815018),
			new Coordinates(-17.727830, 23.704799), new Coordinates(58.585396, -130.279576)
		};
		double[] expectedDistances = new double[]{3.566_138_7, 3390.354_895_4, 6429.275_558_6};

		for(int i = 0; i < expectedDistances.length; i ++){
			Coordinates fromCoordinates = coordinates[i << 1];
			Coordinates toCoordinates = coordinates[(i << 1) + 1];
			double expectedDistance = expectedDistances[i];
			DurationEdgeWeightCalculator edgeWeightCalculator = new DurationEdgeWeightCalculator();

			double actualDistance = edgeWeightCalculator.calculateWeight(new Vertex(1, fromCoordinates),
				new Vertex(2, toCoordinates));

			Assertions.assertEquals(expectedDistance, actualDistance, 0.000_000_05);
		}
	}

}
