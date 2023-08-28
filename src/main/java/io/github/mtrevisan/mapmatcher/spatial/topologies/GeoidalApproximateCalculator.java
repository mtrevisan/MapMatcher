/**
 * Copyright (c) 2023 Mauro Trevisan
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
package io.github.mtrevisan.mapmatcher.spatial.topologies;

import io.github.mtrevisan.mapmatcher.spatial.GeodeticHelper;
import io.github.mtrevisan.mapmatcher.spatial.Point;


/**
 * Use Haversine approximate equations (considering the Earth a sphere).
 */
public class GeoidalApproximateCalculator extends GeoidalCalculator{

	/**
	 * Construct a new calculator instance with the given precision.
	 *
	 * @param precision	The tolerance distance for considering two points equal.
	 */
	public static GeoidalApproximateCalculator withPrecision(final double precision){
		return new GeoidalApproximateCalculator(precision);
	}


	public GeoidalApproximateCalculator(){
		super();
	}

	private GeoidalApproximateCalculator(final double precision){
		super(precision);
	}

	/**
	 * Calculate orthodromic distance, (azimuth) bearing and final bearing between two points using inverse Vincenty formula.
	 *
	 * @param startPoint	The start point.
	 * @param endPoint	The end point.
	 * @return	The distance.
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
	 */
	@Override
	public double distance(final Point startPoint, final Point endPoint){
		final double lat1 = Math.toRadians(startPoint.getY());
		final double lat2 = Math.toRadians(endPoint.getY());
		final double halfDeltaLat = (lat2 - lat1) / 2.;
		final double halfDeltaLon = Math.toRadians(endPoint.getX() - startPoint.getX()) / 2.;

		final double sinHalfDeltaLat = Math.sin(halfDeltaLat);
		final double sinHalfDeltaLon = Math.sin(halfDeltaLon);
		final double a = sinHalfDeltaLat * sinHalfDeltaLat + sinHalfDeltaLon * sinHalfDeltaLon * Math.cos(lat1) * Math.cos(lat2);
		final double c = 2. * Math.atan2(Math.sqrt(a), Math.sqrt(1. - a));
		return GeodeticHelper.EARTH_RADIUS * c;
	}

}
