package io.github.mtrevisan.mapmatcher.convexhull;

import io.github.mtrevisan.mapmatcher.graph.Vertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


class GrahamScanConvexHullCalculatorTest{

	private static final PrecisionModel PRECISION_MODEL = new PrecisionModel(PrecisionModel.FLOATING);
	private static final int SRID_WGS84 = 4326;
	private static final GeometryFactory FACTORY = new GeometryFactory(PRECISION_MODEL, SRID_WGS84);


	@Test
	void should_return_all_vertices_when_2_or_less_vertices_were_given(){
		ArrayList<Vertex> vertices = new ArrayList<>(Arrays.asList(
			new Vertex("1", FACTORY.createPoint(new Coordinate(1., 1.))),
			new Vertex("2", FACTORY.createPoint(new Coordinate(2., 1.)))
		));

		GrahamScanConvexHullCalculator convexHulkCalculator = new GrahamScanConvexHullCalculator();
		List<Vertex> actualResult = convexHulkCalculator.calculateConvexHull(vertices);

		Assertions.assertEquals(vertices, actualResult);
	}

	@Test
	void should_remove_excess_vertices_and_determine_a_valid_convex_hull_when_given_7_points(){
		ArrayList<Vertex> vertices = new ArrayList<>(Arrays.asList(
			new Vertex("1", FACTORY.createPoint(new Coordinate(3., 0.))),
			new Vertex("2", FACTORY.createPoint(new Coordinate(4., 2.))),
			new Vertex("3", FACTORY.createPoint(new Coordinate(1., 1.))),
			new Vertex("4", FACTORY.createPoint(new Coordinate(1., 2.))),
			new Vertex("5", FACTORY.createPoint(new Coordinate(0., 3.))),
			new Vertex("6", FACTORY.createPoint(new Coordinate(0., 0.))),
			new Vertex("7", FACTORY.createPoint(new Coordinate(3., 3.)))
		));

		GrahamScanConvexHullCalculator convexHulkCalculator = new GrahamScanConvexHullCalculator();
		List<Vertex> actualResult = convexHulkCalculator.calculateConvexHull(vertices);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList("6", "2", "7", "5", "1")),
			actualResult.stream().map(Vertex::getId).collect(Collectors.toList()));
	}

	@Test
	void should_remove_excess_vertices_and_determine_a_valid_convex_hull_when_given_an_additional_point(){
		ArrayList<Vertex> vertices = new ArrayList<>(Arrays.asList(
			new Vertex("1", FACTORY.createPoint(new Coordinate(3., 0.))),
			new Vertex("2", FACTORY.createPoint(new Coordinate(1., 1.))),
			new Vertex("3", FACTORY.createPoint(new Coordinate(2., 2.))),
			new Vertex("4", FACTORY.createPoint(new Coordinate(4., 4.))),
			new Vertex("5", FACTORY.createPoint(new Coordinate(0., 0.))),
			new Vertex("6", FACTORY.createPoint(new Coordinate(5., 1.))),
			new Vertex("7", FACTORY.createPoint(new Coordinate(1., 3.))),
			new Vertex("8", FACTORY.createPoint(new Coordinate(3., 3.)))
		));

		GrahamScanConvexHullCalculator convexHulkCalculator = new GrahamScanConvexHullCalculator();
		List<Vertex> actualResult = convexHulkCalculator.calculateConvexHull(vertices);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList("5", "6", "4", "7", "1")),
			actualResult.stream().map(Vertex::getId).collect(Collectors.toList()));
	}

}
