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
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class GrahamScanConvexHullCalculator implements ConvexHullCalculator{

	private static final Comparator<Vertex> MIN_Y_COMPARATOR =
		Comparator.comparingDouble((Vertex v) -> v.getGeometry().getCentroid().getY())
			.thenComparingDouble(v -> v.getGeometry().getCentroid().getX());


	@Override
	public List<Vertex> calculateConvexHull(final Collection<Vertex> vertices){
		if(vertices.size() <= 2)
			return new ArrayList<>(vertices);

		final var source = Collections.min(vertices, MIN_Y_COMPARATOR);

		final var remaining = vertices.stream()
			.filter(v -> !v.equals(source))
			.sorted(Comparator.comparingDouble((target) -> angleFromSource(source, target)))
			.toList();

		final List<Vertex> result = new ArrayList<>();
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

	private boolean isClockwiseTurn(final Vertex p, final Vertex q, final Vertex r){
		final var pp = p.getGeometry().getCentroid().getCoordinate();
		final var qq = q.getGeometry().getCentroid().getCoordinate();
		final var rr = r.getGeometry().getCentroid().getCoordinate();
		return ((qq.getY() - rr.getY()) * (pp.getX() - rr.getX()) <= (qq.getX() - rr.getX()) * (pp.getY() - rr.getY()));
	}

	private double angleFromSource(final Vertex source, final Vertex target){
		final Coordinate s = source.getGeometry().getCentroid().getCoordinate();
		final Coordinate t = target.getGeometry().getCentroid().getCoordinate();
		final var latDiff = s.getY() - t.getY();
		final var lngDiff = s.getX() - t.getX();
		return Math.atan2(latDiff, lngDiff);
	}

}
