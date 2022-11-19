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
package io.github.mtrevisan.mapmatcher.weight;

import io.github.mtrevisan.mapmatcher.graph.Coordinates;


class HaversineDistanceCalculator{

	//[km]
	private static final double EARTH_RADIUS = 6371.;


	public double calculateDistance(Coordinates startCoordinates, Coordinates endCoordinatess){
		double startLatitude = startCoordinates.getLatitude();
		double startLongitude = startCoordinates.getLongitude();
		double endLatitude = endCoordinatess.getLatitude();
		double endLongitude = endCoordinatess.getLongitude();

		double deltaLatitude = Math.toRadians(endLatitude - startLatitude);
		double deltaLongitude = Math.toRadians(endLongitude - startLongitude);

		startLatitude = Math.toRadians(startLatitude);
		endLatitude = Math.toRadians(endLatitude);

		double a = haversine(deltaLatitude) + StrictMath.cos(startLatitude) * StrictMath.cos(endLatitude) * haversine(deltaLongitude);
		double c = 2. * StrictMath.atan2(Math.sqrt(a), Math.sqrt(1. - a));

		return EARTH_RADIUS * c;
	}

	private double haversine(double val){
		return Math.pow(StrictMath.sin(val / 2.), 2.);
	}

}
