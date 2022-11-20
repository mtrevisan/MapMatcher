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
package io.github.mtrevisan.mapmatcher.convexhull;

import io.github.mtrevisan.mapmatcher.graph.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;


public class AndrewMonotoneChainConvexHullCalculator implements ConvexHullCalculator{

	private static final Comparator<Vertex> MIN_LAT_COMPARATOR =
		Comparator.comparingDouble((Vertex v) -> v.getGeometry().getCentroid().getCoordinate().getY())
			.thenComparingDouble(v -> v.getGeometry().getCentroid().getX());


	@Override
	public List<Vertex> calculateConvexHull(final Collection<Vertex> vertices){
		if(vertices.size() <= 2)
			return new ArrayList<>(vertices);

		final var sortedByLat = vertices.stream().sorted(MIN_LAT_COMPARATOR)
			.toList();

		final var upperHull = new ArrayList<Vertex>();
		for(final var vertex : sortedByLat)
			filterCounterClockwise(upperHull, vertex);

		final var lowerHull = new ArrayList<Vertex>();
		for(int i = sortedByLat.size() - 1; i >= 0; i--){
			final var vertex = sortedByLat.get(i);
			filterCounterClockwise(lowerHull, vertex);
		}

		upperHull.remove(upperHull.size() - 1);
		lowerHull.remove(lowerHull.size() - 1);

		lowerHull.addAll(upperHull);
		return lowerHull;
	}

	private void filterCounterClockwise(final List<Vertex> lowerHull, final Vertex p){
		while(lowerHull.size() >= 2 && isCounterClockwiseTurn(p, lowerHull.get(lowerHull.size() - 1), lowerHull.get(lowerHull.size() - 2)))
			lowerHull.remove(lowerHull.size() - 1);
		lowerHull.add(p);
	}

	private boolean isCounterClockwiseTurn(final Vertex p, final Vertex q, final Vertex r){
		final var pp = p.getGeometry().getCentroid();
		final var qq = q.getGeometry().getCentroid();
		final var rr = r.getGeometry().getCentroid();
		return ((qq.getY() - rr.getY()) * (pp.getX() - rr.getX()) >= (qq.getX() - rr.getX()) * (pp.getY() - rr.getY()));
	}

}
