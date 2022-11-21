package io.github.mtrevisan.mapmatcher.pathfinding;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Vertex;
import io.github.mtrevisan.mapmatcher.weight.EdgeWeightCalculator;


public class EuclideanDistanceTestEdgeWeightCalculator implements EdgeWeightCalculator{

	@Override
	public double calculateWeight(final Edge edge){
		return calculateWeight(edge.getFrom(), edge.getTo());
	}

	@Override
	public double calculateWeight(final Vertex from, final Vertex to){
		final var fromCoordinates = from.getGeometry().getCoordinate();
		final var toCoordinates = to.getGeometry().getCoordinate();
		final var deltaY = fromCoordinates.getY() - toCoordinates.getY();
		final var deltaX = fromCoordinates.getX() - toCoordinates.getX();
		return Math.sqrt(deltaY * deltaY + deltaX * deltaX);
	}

}
