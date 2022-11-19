package io.github.mtrevisan.mapmatcher.pathfinding;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.weight.EdgeWeightCalculator;


public class EuclideanDistanceTestEdgeWeightCalculator implements EdgeWeightCalculator{

	@Override
	public double calculateWeight(Edge edge){
		Coordinates fromCoords = edge.getFrom().getCoordinates();
		Coordinates toCoords = edge.getTo().getCoordinates();
		return Math.sqrt(Math.pow(fromCoords.getLatitude() - toCoords.getLatitude(), 2) + Math.pow(fromCoords.getLongitude() - toCoords.getLongitude(), 2));
	}

	@Override
	public double estimateWeight(Vertex start, Vertex end){
		return calculateWeight(new Edge(start, end, 0));
	}

}
