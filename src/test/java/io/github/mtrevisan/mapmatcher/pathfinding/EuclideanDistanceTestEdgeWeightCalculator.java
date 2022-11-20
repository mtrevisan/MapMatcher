package io.github.mtrevisan.mapmatcher.pathfinding;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.weight.EdgeWeightCalculator;


public class EuclideanDistanceTestEdgeWeightCalculator implements EdgeWeightCalculator{

	@Override
	public double calculateWeight(Edge edge){
		Coordinates fromCoordinates = edge.getFrom().getGeometry();
		Coordinates toCoordinates = edge.getTo().getGeometry();
		final var deltaLatitude = fromCoordinates.getLatitude() - toCoordinates.getLatitude();
		final var deltaLongitude = fromCoordinates.getLongitude() - toCoordinates.getLongitude();
		return Math.sqrt(deltaLatitude * deltaLatitude + deltaLongitude * deltaLongitude);
	}

	@Override
	public double calculateWeight(Vertex start, Vertex end){
		return calculateWeight(new Edge(start, end, 0.));
	}

}
