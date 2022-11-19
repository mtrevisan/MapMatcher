/**
 * Copyright (c) 2022 Mauro Trevisan
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mtrevisan.mapmatcher.weight;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;
import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.graph.Vertex;


public class DurationEdgeWeightCalculator implements EdgeWeightCalculator{

	private static final int MAX_ALLOWED_SPEED = 140;

	private static final HaversineDistanceCalculator DISTANCE_CALCULATOR = new HaversineDistanceCalculator();


	@Override
	public double calculateWeight(final Edge edge){
		final var coordinatesFrom = edge.getFrom().getCoordinates();
		final var coordinatesTo = edge.getTo().getCoordinates();
		return DISTANCE_CALCULATOR.calculateDistance(coordinatesFrom, coordinatesTo) / edge.getMaxSpeed() * 60.;
	}

	@Override
	public double estimateWeight(final Vertex start, final Vertex end){
		final var coordinatesStart = start.getCoordinates();
		final var coordinatesEnd = end.getCoordinates();
		return DISTANCE_CALCULATOR.calculateDistance(coordinatesStart, coordinatesEnd) / MAX_ALLOWED_SPEED * 60.;
	}

}
