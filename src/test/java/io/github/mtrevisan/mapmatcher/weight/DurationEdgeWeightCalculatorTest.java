package io.github.mtrevisan.mapmatcher.weight;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Node;
import io.github.mtrevisan.mapmatcher.helpers.WGS84GeometryHelper;
import io.github.mtrevisan.mapmatcher.pathfinding.calculators.DurationCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;


class DurationEdgeWeightCalculatorTest{

	@Test
	void should_return_edge_duration_in_minutes(){
		Coordinate[] coordinates = new Coordinate[]{
			new Coordinate(14.552797, 121.058805),
			new Coordinate(14.593999, 120.994260),
			new Coordinate(77.870317, 96.591876),
			new Coordinate(21.719527, -4.815018),
			new Coordinate(-17.727830, 23.704799),
			new Coordinate(58.585396, -130.279576)
		};
		double[] expectedDurations = new double[]{0.091_9, 86.936_2, 103.114_3};
		double[] maxSpeeds = new double[]{50., 80., 100.};
		for(int i = 0; i < expectedDurations.length; i ++){
			Coordinate fromCoordinates = coordinates[i << 1];
			Coordinate toCoordinates = coordinates[(i << 1) + 1];
			double expectedDuration = expectedDurations[i];
			double maxSpeed = maxSpeeds[i];
			DurationCalculator edgeWeightCalculator = new DurationCalculator();
			final Edge edge = Edge.createDirectEdge(new Node("0", fromCoordinates), new Node("1", toCoordinates),
				WGS84GeometryHelper.createLineString(new Coordinate[]{fromCoordinates, toCoordinates}));
			edge.setWeight(maxSpeed);
			double actualDistance = edgeWeightCalculator.calculateWeight(edge);

			Assertions.assertEquals(expectedDuration, actualDistance, 0.000_05);
		}
	}

	@Test
	void should_return_duration_in_minutes_between_vertices_with_max_possible_speed(){
		Coordinate[] coordinates = new Coordinate[]{
			new Coordinate(14.552797, 121.058805),
			new Coordinate(14.593999, 120.994260),
			new Coordinate(77.870317, 96.591876),
			new Coordinate(21.719527, -4.815018),
			new Coordinate(-17.727830, 23.704799),
			new Coordinate(58.585396, -130.279576)
		};
		double[] expectedDistances = new double[]{0.032_8, 49.677_8, 73.653_1};

		for(int i = 0; i < expectedDistances.length; i ++){
			Coordinate fromCoordinates = coordinates[i << 1];
			Coordinate toCoordinates = coordinates[(i << 1) + 1];
			double expectedDistance = expectedDistances[i];
			DurationCalculator edgeWeightCalculator = new DurationCalculator();

			double actualDistance = edgeWeightCalculator.calculateWeight(
				new Node("0", fromCoordinates),
				new Node("1", toCoordinates));

			Assertions.assertEquals(expectedDistance, actualDistance, 0.000_05);
		}
	}

}
