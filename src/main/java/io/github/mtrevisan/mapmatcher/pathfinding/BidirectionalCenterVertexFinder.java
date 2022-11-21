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
package io.github.mtrevisan.mapmatcher.pathfinding;

import io.github.mtrevisan.mapmatcher.graph.Vertex;

import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;


class BidirectionalCenterVertexFinder{

	Vertex findCenterVertex(final Vertex candidate, final double candidateBidirectionalScore, final Queue<Vertex> pqForward,
			final Queue<Vertex> pqBackward){
		final Map<Vertex, Double> scoresForward = buildMinVertexScoreMap(pqForward);
		final Map<Vertex, Double> scoresBackward = buildMinVertexScoreMap(pqBackward);

		var minVertex = candidate;
		var minScore = candidateBidirectionalScore;

		for(final Map.Entry<Vertex, Double> forwardEntry : scoresForward.entrySet()){
			if(!scoresBackward.containsKey(forwardEntry.getKey()))
				continue;

			final var currScore = scoresBackward.get(forwardEntry.getKey()) + forwardEntry.getValue();
			if(minScore > currScore){
				minScore = currScore;
				minVertex = forwardEntry.getKey();
			}
		}

		return minVertex;
	}

	private Map<Vertex, Double> buildMinVertexScoreMap(final Queue<Vertex> scoredVertices){
		return scoredVertices.stream()
			.collect(Collectors.toMap(v -> v, Vertex::getWeight, Math::min));
	}

}
