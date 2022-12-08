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
package io.github.mtrevisan.mapmatcher.spatial.intersection;

import io.github.mtrevisan.mapmatcher.spatial.Geometry;
import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;
import io.github.mtrevisan.mapmatcher.spatial.topologies.EuclideanCalculator;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import io.github.mtrevisan.mapmatcher.spatial.topologies.TopologyCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class BentleyOttmannTest{

	@Test
	void find_intersections1(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		List<Polyline> polylines = Arrays.asList(
			factory.createPolyline(factory.createPoint(0.8, 6.1), factory.createPoint(11.72, 9.32)),
			factory.createPolyline(factory.createPoint(6.84, 3.56), factory.createPoint(15.06, 8.38)),
			factory.createPolyline(factory.createPoint(3.5, 8.17), factory.createPoint(10.44, 4.08)),
			factory.createPolyline(factory.createPoint(13.32, 4.22), factory.createPoint(2.42, 12.67))
		);

		TopologyCalculator calculator = new EuclideanCalculator();
		BentleyOttmann bentleyOttmann = new BentleyOttmann(calculator);
		bentleyOttmann.addPolylines(polylines);
		bentleyOttmann.findIntersections();

		Assertions.assertEquals(4, bentleyOttmann.intersections().size());
	}

	@Test
	void find_intersections2(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		List<Polyline> polylines = Arrays.asList(
			factory.createPolyline(factory.createPoint(0.8, 6.1), factory.createPoint(11.72, 9.32)),
			factory.createPolyline(factory.createPoint(6.84, 3.56), factory.createPoint(15.06, 8.38)),
			factory.createPolyline(factory.createPoint(3.5, 8.17), factory.createPoint(10.44, 4.08)),
			factory.createPolyline(factory.createPoint(13.32, 4.22), factory.createPoint(2.42, 12.67)),
			factory.createPolyline(factory.createPoint(5.39, 5.68), factory.createPoint(8.39, 11.23)),
			factory.createPolyline(factory.createPoint(9.5, 3.91), factory.createPoint(11, 10.06))
		);

		TopologyCalculator calculator = new EuclideanCalculator();
		BentleyOttmann bentleyOttmann = new BentleyOttmann(calculator);
		bentleyOttmann.addPolylines(polylines);
		bentleyOttmann.findIntersections();

		Assertions.assertEquals(11, bentleyOttmann.intersections().size());
	}

	@Test
	void find_intersections3(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		List<Polyline> polylines = Arrays.asList(
			factory.createPolyline(factory.createPoint(1.76, 6.86), factory.createPoint(3.84, 4.76)),
			factory.createPolyline(factory.createPoint(3.96, 5.36), factory.createPoint(1.22, 2.4)),
			factory.createPolyline(factory.createPoint(0.8, 3.1), factory.createPoint(8, 1.7)),
			factory.createPolyline(factory.createPoint(7.4, 1.5), factory.createPoint(9.4, 4.8)),
			factory.createPolyline(factory.createPoint(2.2, 6.04), factory.createPoint(4.9, 8.46)),
			factory.createPolyline(factory.createPoint(3.72, 8.5), factory.createPoint(9.64, 4.02)),
			factory.createPolyline(factory.createPoint(6.2, 6.98), factory.createPoint(5.9, 1.82))
		);

		TopologyCalculator calculator = new EuclideanCalculator();
		BentleyOttmann bentleyOttmann = new BentleyOttmann(calculator);
		bentleyOttmann.addPolylines(polylines);
		bentleyOttmann.findIntersections();

		Assertions.assertEquals(8, bentleyOttmann.intersections().size());
	}

	@Test
	void find_intersections4(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		List<Polyline> polylines = Arrays.asList(
			factory.createPolyline(factory.createPoint(0.8, 6.1), factory.createPoint(11.72, 9.32)),
			factory.createPolyline(factory.createPoint(6.84, 3.56), factory.createPoint(15.06, 8.38)),
			factory.createPolyline(factory.createPoint(3.5, 8.17), factory.createPoint(10.44, 4.08)),
			factory.createPolyline(factory.createPoint(13.32, 4.22), factory.createPoint(2.42, 12.67))
		);

		TopologyCalculator calculator = new EuclideanCalculator();
		BentleyOttmann bentleyOttmann = new BentleyOttmann(calculator);
		bentleyOttmann.addPolylines(polylines);

		Map<Geometry, List<Point>> intersectionsOnPolyline = new HashMap<>();
		bentleyOttmann.findIntersections((polyline1, polyline2, intersection) -> {
			intersectionsOnPolyline.computeIfAbsent(polyline1, k -> new ArrayList<>(1))
				.add(intersection);
			intersectionsOnPolyline.computeIfAbsent(polyline2, k -> new ArrayList<>(1))
				.add(intersection);
		});

		Assertions.assertEquals(4, intersectionsOnPolyline.size());
	}

	@Test
	void find_intersections5(){
		GeometryFactory factory = new GeometryFactory(new GeoidalCalculator());
		List<Polyline> polylines = Arrays.asList(
			factory.createPolyline(factory.createPoint(2.0681655529586, 6.7721029389621), factory.createPoint(3.8750905945826, 4.7795349136096)),
			factory.createPolyline(factory.createPoint(3.7637048043455, 5.0765636875751), factory.createPoint(2.5013325149917, 2.9107288774094)),
			factory.createPolyline(factory.createPoint(2.4146991225851, 3.1706290546293), factory.createPoint(4.4196433468528, 1.6731089858861)),
			factory.createPolyline(factory.createPoint(4.1844955674634, 1.7597423782927), factory.createPoint(7.1547833071193, 2.8612240817484)),
			factory.createPolyline(factory.createPoint(6.870130732069, 2.9354812752398), factory.createPoint(8.887451155252, 1.3637040130053)),
			factory.createPolyline(factory.createPoint(8.5904223812864, 1.4008326097509), factory.createPoint(12.7364490178894, 5.1384446821513)),
			factory.createPolyline(factory.createPoint(12.7735776146351, 4.8785445049314), factory.createPoint(11.1399193578244, 8.7399185664841)),
			factory.createPolyline(factory.createPoint(11.4369481317899, 8.5666517816708), factory.createPoint(7.0681499147127, 9.6557572862113)),
			factory.createPolyline(factory.createPoint(7.3032976941021, 9.7300144797027), factory.createPoint(5.2736010720039, 8.3067516044509)),
			factory.createPolyline(factory.createPoint(5.5582536470543, 8.3686325990271), factory.createPoint(3.1696472564143, 9.0369473404497)),
			factory.createPolyline(factory.createPoint(3.5161808260408, 9.160709329602), factory.createPoint(2.092917950789, 6.5493313584879)),
			factory.createPolyline(factory.createPoint(5.0434037721806, 9.2448674822256), factory.createPoint(2.0347498158874, 5.3649291223001)),
			factory.createPolyline(factory.createPoint(3.2599935084955, 2.1656817027123), factory.createPoint(6.47285474689, 9.7758064156891))
		);

		TopologyCalculator calculator = new EuclideanCalculator();
		BentleyOttmann bentleyOttmann = new BentleyOttmann(calculator);
		bentleyOttmann.addPolylines(polylines);
		bentleyOttmann.findIntersections();

		Assertions.assertEquals(15, bentleyOttmann.intersections().size());
	}

}
