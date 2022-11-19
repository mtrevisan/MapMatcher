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
package io.github.mtrevisan.mapmatcher.graph;

import java.util.Objects;


public class Coordinates{

	private final double latitude;
	private final double longitude;


	public Coordinates(final double latitude, final double longitude){
		if(latitude < -90. || latitude > 90.)
			throw new IllegalArgumentException("Latitude must be between -90 and 90 inclusive");
		if(longitude < -180. || longitude > 180.)
			throw new IllegalArgumentException("Longitude must be between -180 and 180 inclusive");

		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude(){
		return latitude;
	}

	public double getLongitude(){
		return longitude;
	}

	@Override
	public boolean equals(final Object o){
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;

		final Coordinates that = (Coordinates)o;
		return (Double.compare(that.latitude, latitude) == 0 && Double.compare(that.longitude, longitude) == 0);
	}

	@Override
	public int hashCode(){
		return Objects.hash(latitude, longitude);
	}

	@Override
	public String toString(){
		return "Coordinates{" + "latitude=" + latitude + ", longitude=" + longitude + '}';
	}

}
