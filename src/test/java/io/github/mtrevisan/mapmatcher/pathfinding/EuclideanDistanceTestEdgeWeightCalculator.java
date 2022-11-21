package io.github.mtrevisan.mapmatcher.pathfinding;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.weight.EdgeWeightCalculator;
import org.locationtech.jts.geom.Geometry;


public class EuclideanDistanceTestEdgeWeightCalculator implements EdgeWeightCalculator{

	@Override
	public double calculateWeight(Edge edge){
		Geometry fromCoordinates = edge.getFrom().getGeometry();
		Geometry toCoordinates = edge.getTo().getGeometry();
		final var deltaLatitude = fromCoordinates.getCoordinate().getY() - toCoordinates.getCoordinate().getY();
		final var deltaLongitude = fromCoordinates.getCoordinate().getX() - toCoordinates.getCoordinate().getX();
		return Math.sqrt(deltaLatitude * deltaLatitude + deltaLongitude * deltaLongitude);
	}

	@Override
	public double calculateWeight(Vertex start, Vertex end){
		return calculateWeight(new Edge(start, end, 0.));
	}

}
