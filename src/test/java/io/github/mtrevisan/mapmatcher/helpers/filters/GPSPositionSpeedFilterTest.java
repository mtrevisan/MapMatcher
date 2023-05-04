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
package io.github.mtrevisan.mapmatcher.helpers.filters;

import io.github.mtrevisan.mapmatcher.TestPathHelper;
import io.github.mtrevisan.mapmatcher.spatial.GPSPoint;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;


class GPSPositionSpeedFilterTest{

	@Test
	void filter(){
		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSPoint[] observations = new GPSPoint[]{
			GPSPoint.of(12.172704737567187, 45.59108565830172, timestamp),
			GPSPoint.of(12.229859503941071, 45.627705048963094, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.241610951232218, 45.6422714215264, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.243213421318018, 45.65646065552491, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.272057882852266, 45.662060679461206, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.273057882852266, 45.662160679461206, (timestamp = TestPathHelper.advanceTime(timestamp, 60))),
			GPSPoint.of(12.274057882852266, 45.662060679461206, (timestamp = TestPathHelper.advanceTime(timestamp, 60)))
		};
		final GPSPositionSpeedFilter filter = new GPSPositionSpeedFilter(3., 5.);
		final GPSPoint[] filtered = new GPSPoint[observations.length];
		filtered[0] = observations[0];
		for(int i = 1; i < observations.length; i ++){
			filter.updatePosition(observations[i].getY(), observations[i].getX(),
				ChronoUnit.SECONDS.between(observations[i - 1].getTimestamp(), observations[i].getTimestamp()));
			final double[] position = filter.getPosition();
			filtered[i] = GPSPoint.of(position[1], position[0], observations[i].getTimestamp());
		}

		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		final Polyline filteredPolyline = factory.createPolyline(filtered);
		final String expected = "LINESTRING (12.172704737567187 45.59108565830172, 12.229859503941055 45.62770504896303," +
			" 12.241610951293309 45.642271421754465, 12.243227378335003 45.6564611741726, 12.272020508158173 45.662072494559595," +
			" 12.273095919119376 45.66216831699165, 12.274058174532831 45.66206098650002)";
		Assertions.assertEquals(expected, filteredPolyline.toString());
	}

}
