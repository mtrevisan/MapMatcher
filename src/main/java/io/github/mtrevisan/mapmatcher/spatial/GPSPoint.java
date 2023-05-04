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
package io.github.mtrevisan.mapmatcher.spatial;

import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;

import java.time.ZonedDateTime;


public class GPSPoint extends Point{

	private static final GeometryFactory FACTORY = new GeometryFactory(new GeoidalCalculator());


	private final ZonedDateTime timestamp;


	public static GPSPoint of(final double longitude, final double latitude, final ZonedDateTime timestamp){
		return new GPSPoint(longitude, latitude, timestamp);
	}

	private GPSPoint(final double longitude, final double latitude, final ZonedDateTime timestamp){
		super(FACTORY, longitude, latitude);

		this.timestamp = timestamp;
	}

	public ZonedDateTime getTimestamp(){
		return timestamp;
	}

}
