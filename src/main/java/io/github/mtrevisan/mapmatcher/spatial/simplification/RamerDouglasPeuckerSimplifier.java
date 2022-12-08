/**
 * Copyright (c) 2021 Mauro Trevisan
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
package io.github.mtrevisan.mapmatcher.spatial.simplification;

import io.github.mtrevisan.mapmatcher.spatial.GeodeticHelper;
import io.github.mtrevisan.mapmatcher.spatial.Point;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Stack;


/**
 * The Ramer–Douglas–Peucker algorithm is an algorithm for reducing the number of points in a curve that is approximated by a series
 * of points.
 * <p>
 * Best/Average case: Θ(n ⋅ log2(n))
 * Worst case: Θ(n²)
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm">Ramer–Douglas–Peucker algorithm</a>
 * @see <a href="https://www.cs.swarthmore.edu/~adanner/cs97/s08/pdf/speedingPeuckerDouglas.pdf">Speeding up the Douglas–Peucker line–simplification algorithm</a>
 * @see <a href="https://www.dca.fee.unicamp.br/projects/prosim/publications/congress/wu-roci-2003-rfm.pdf">A non–self–intersection Douglas–Peucker algorithm</a>
 */
public class RamerDouglasPeuckerSimplifier{

	private double distanceTolerance;


	/**
	 * Sets the distance tolerance for the simplification.
	 * <p>
	 * All vertices in the simplified linestring will be within this distance of the original linestring.
	 * </p>
	 *
	 * @param distanceTolerance	The approximation tolerance to use [m].
	 */
	public void setDistanceTolerance(final double distanceTolerance){
		if(distanceTolerance <= 0.)
			throw new IllegalArgumentException("Distance tolerance must be greater than zero");

		this.distanceTolerance = distanceTolerance;
	}


	public Point[] simplify(final Point... points){
		final BitSet preservePoints = new BitSet(points.length);

		return simplify(preservePoints, points);
	}

	public Point[] simplify(final BitSet preservePoints, final Point... points){
		if(preservePoints.size() < points.length)
			throw new IllegalArgumentException("Preserve points length differs from points length");

		if(points.length < 3)
			return points;

		int startIndex = 0;
		int endIndex = points.length - 1;

		preservePoints.set(startIndex, true);
		preservePoints.set(endIndex, true);

		final Stack<KeyValuePair> stack = new Stack<>();
		stack.push(new KeyValuePair(startIndex, endIndex));

		while(!stack.isEmpty()){
			final KeyValuePair element = stack.pop();
			startIndex = element.startIndex;
			endIndex = element.endIndex;

			//find the point with the maximum distance from line between the start and end
			double maxDistance = distanceTolerance;
			int maxIndex = startIndex;
			for(int k = maxIndex + 1; k < endIndex; k ++)
				if(!preservePoints.get(k)){
					final Point nearestPoint = GeodeticHelper.onTrackClosestPoint(points[startIndex], points[endIndex], points[k]);
					final double distance = nearestPoint.distance(points[k]);
					if(distance > maxDistance){
						maxIndex = k;
						maxDistance = distance;
					}
				}

			//if max distance is greater than tolerance then split and simplify, otherwise return the segment
			if(maxDistance > distanceTolerance){
				stack.push(new KeyValuePair(startIndex, maxIndex));
				stack.push(new KeyValuePair(maxIndex, endIndex));
			}
			else{
				preservePoints.set(startIndex, true);
				preservePoints.set(endIndex, true);
			}
		}

		final List<Point> simplifiedPoints = new ArrayList<>(points.length);
		for(int i = 0; i < points.length; i ++)
			if(preservePoints.get(i))
				simplifiedPoints.add(points[i]);
		return simplifiedPoints.toArray(Point[]::new);
	}


	private static final class KeyValuePair{
		private final int startIndex;
		private final int endIndex;

		private KeyValuePair(final int startIndex, final int endIndex){
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
	}

}
