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

import io.github.mtrevisan.mapmatcher.graph.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;


public class AndrewMonotoneChainConvexHullCalculator implements ConvexHullCalculator{

	private static final Comparator<Node> MIN_Y_COMPARATOR =
		Comparator.comparingDouble((Node v) -> v.getCoordinate().getY())
			.thenComparingDouble(v -> v.getCoordinate().getX());


	@Override
	public List<Node> calculateConvexHull(final Collection<Node> vertices){
		if(vertices.size() <= 2)
			return new ArrayList<>(vertices);

		final var sortedByLat = vertices.stream().sorted(MIN_Y_COMPARATOR)
			.toList();

		final var upperHull = new ArrayList<Node>();
		for(final var vertex : sortedByLat)
			filterCounterClockwise(upperHull, vertex);

		final var lowerHull = new ArrayList<Node>();
		for(int i = sortedByLat.size() - 1; i >= 0; i--){
			final var vertex = sortedByLat.get(i);
			filterCounterClockwise(lowerHull, vertex);
		}

		upperHull.remove(upperHull.size() - 1);
		lowerHull.remove(lowerHull.size() - 1);

		lowerHull.addAll(upperHull);
		return lowerHull;
	}

	private void filterCounterClockwise(final List<Node> lowerHull, final Node p){
		while(lowerHull.size() >= 2 && isCounterClockwiseTurn(p, lowerHull.get(lowerHull.size() - 1), lowerHull.get(lowerHull.size() - 2)))
			lowerHull.remove(lowerHull.size() - 1);
		lowerHull.add(p);
	}

	private boolean isCounterClockwiseTurn(final Node p, final Node q, final Node r){
		final var pp = p.getCoordinate();
		final var qq = q.getCoordinate();
		final var rr = r.getCoordinate();
		return ((qq.getY() - rr.getY()) * (pp.getX() - rr.getX()) >= (qq.getX() - rr.getX()) * (pp.getY() - rr.getY()));
	}

}
