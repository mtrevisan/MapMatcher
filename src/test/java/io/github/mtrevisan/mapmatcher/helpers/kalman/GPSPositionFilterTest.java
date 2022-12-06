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
package io.github.mtrevisan.mapmatcher.helpers.kalman;

import io.github.mtrevisan.mapmatcher.spatial.GPSPoint;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.distances.GeodeticCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;


class GPSPositionFilterTest{

	@Test
	void filter(){
		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSPoint[] observations = new GPSPoint[]{GPSPoint.of(12.172704737567187, 45.59108565830172, timestamp), GPSPoint.of(12.229859503941071, 45.627705048963094, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))), GPSPoint.of(12.241610951232218, 45.6422714215264, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))), GPSPoint.of(12.243213421318018, 45.65646065552491, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))), GPSPoint.of(12.272057882852266, 45.662060679461206, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))), GPSPoint.of(12.273057882852266, 45.662160679461206, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))), GPSPoint.of(12.274057882852266, 45.662060679461206, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS)))};
		final GPSPositionFilter filter = new GPSPositionFilter(3., 2.5);
		final GPSPoint[] filtered = new GPSPoint[observations.length];
		filtered[0] = observations[0];
		for(int i = 1; i < observations.length; i ++){
			filter.updatePosition(observations[i].getY(), observations[i].getX());
			final double[] position = filter.getPosition();
			filtered[i] = GPSPoint.of(position[1], position[0], observations[i].getTimestamp());
		}

		final GeometryFactory factory = new GeometryFactory(new GeodeticCalculator());
		final Polyline filteredPolyline = factory.createPolyline(filtered);
		final String expected = "LINESTRING (12.172704737567187 45.59108565830172, 12.229859503910493 45.62770504884901," +
			" 12.237938776924098 45.637719619689236, 12.241386712880304 45.649970282765004, 12.261309951439834 45.65782390745157," +
			" 12.26893518749547 45.66063877835369, 12.27225985949525 45.66156160402833)";
		Assertions.assertEquals(expected, filteredPolyline.toString());
	}

}
