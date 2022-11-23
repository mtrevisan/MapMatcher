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
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class GrahamScanConvexHullCalculator implements ConvexHullCalculator{

	private static final Comparator<Node> MIN_Y_COMPARATOR =
		Comparator.comparingDouble((Node v) -> v.getCoordinate().getY())
			.thenComparingDouble(v -> v.getCoordinate().getX());


	@Override
	public List<Node> calculateConvexHull(final Collection<Node> vertices){
		if(vertices.size() <= 2)
			return new ArrayList<>(vertices);

		final var source = Collections.min(vertices, MIN_Y_COMPARATOR);

		final var remaining = vertices.stream()
			.filter(v -> !v.equals(source))
			.sorted(Comparator.comparingDouble((target) -> angleFromSource(source, target)))
			.toList();

		final List<Node> result = new ArrayList<>();
		result.add(source);
		result.add(remaining.get(0));
		result.add(remaining.get(1));

		for(int i = 2; i < remaining.size(); i ++){
			final var curr = remaining.get(i);
			while(result.size() >= 2 && isClockwiseTurn(result.get(result.size() - 2), result.get(result.size() - 1), curr))
				result.remove(result.size() - 1);
			result.add(curr);
		}

		return result;
	}

	private boolean isClockwiseTurn(final Node p, final Node q, final Node r){
		final var pp = p.getCoordinate();
		final var qq = q.getCoordinate();
		final var rr = r.getCoordinate();
		return ((qq.getY() - rr.getY()) * (pp.getX() - rr.getX()) <= (qq.getX() - rr.getX()) * (pp.getY() - rr.getY()));
	}

	private double angleFromSource(final Node source, final Node target){
		final Coordinate s = source.getCoordinate();
		final Coordinate t = target.getCoordinate();
		final var latDiff = s.getY() - t.getY();
		final var lngDiff = s.getX() - t.getX();
		return Math.atan2(latDiff, lngDiff);
	}

}
