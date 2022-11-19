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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class GrahamScanConvexHullCalculator implements ConvexHullCalculator{

	private static final Comparator<Vertex> minLatComparator = Comparator.comparingDouble((Vertex v) -> v.getCoordinates().getLatitude()).thenComparingDouble(v -> v.getCoordinates().getLongitude());

	@Override
	public List<Vertex> calculateConvexHull(Collection<Vertex> vertices){
		if(vertices.size() <= 2){
			return new ArrayList<>(vertices);
		}

		final var source = Collections.min(vertices, minLatComparator);

		var remaining = vertices.stream().filter(v -> !v.equals(source)).sorted(Comparator.comparingDouble((target) -> angleFromSource(source, target))).collect(Collectors.toList());

		List<Vertex> result = new ArrayList<>();
		result.add(source);
		result.add(remaining.get(0));
		result.add(remaining.get(1));

		for(int i = 2; i < remaining.size(); i++){
			var curr = remaining.get(i);
			while(result.size() >= 2 && isClockwiseTurn(result.get(result.size() - 2), result.get(result.size() - 1), curr)){
				result.remove(result.size() - 1);
			}
			result.add(curr);
		}

		return result;
	}

	private boolean isClockwiseTurn(Vertex p, Vertex q, Vertex r){
		var pp = p.getCoordinates();
		var qq = q.getCoordinates();
		var rr = r.getCoordinates();

		return (qq.getLatitude() - rr.getLatitude()) * (pp.getLongitude() - rr.getLongitude()) <= (qq.getLongitude() - rr.getLongitude()) * (pp.getLatitude() - rr.getLatitude());
	}

	private double angleFromSource(Vertex source, Vertex target){
		double latDiff = source.getCoordinates().getLatitude() - target.getCoordinates().getLatitude();
		double lngDiff = source.getCoordinates().getLongitude() - target.getCoordinates().getLongitude();
		return Math.atan2(latDiff, lngDiff);
	}

}
