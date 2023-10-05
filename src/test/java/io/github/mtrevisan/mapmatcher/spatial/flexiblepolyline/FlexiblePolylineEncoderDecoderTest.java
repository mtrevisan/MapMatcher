package io.github.mtrevisan.mapmatcher.spatial.flexiblepolyline;

import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point3D;
import io.github.mtrevisan.mapmatcher.spatial.topologies.GeoidalCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


class FlexiblePolylineEncoderDecoderTest{

	private static final GeometryFactory FACTORY = new GeometryFactory(new GeoidalCalculator());


	@Test
	void fail_on_empty_coordinates_list(){
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> FlexiblePolylineEncoderDecoder.encode(new ArrayList<>(), 5, ThirdDimension.ABSENT, 0));
	}

	@Test
	void fail_on_invalid_third_dimension(){
		List<Point3D> pairs = new ArrayList<>();
		pairs.add(Point3D.of(FACTORY, 8.6982122, 50.1022829));
		ThirdDimension invalid = null;

		Assertions.assertThrows(IllegalArgumentException.class,
			() -> FlexiblePolylineEncoderDecoder.encode(pairs, 5, invalid, 0));
	}

	@Test
	void convert_value(){
		Converter conv = new Converter(5);
		StringBuilder result = new StringBuilder();
		conv.encodeValue(-179.98321, result);

		Assertions.assertEquals("h_wqiB", result.toString());
	}

	@Test
	void simple_point2d_encoding(){
		List<Point3D> pairs = new ArrayList<>();
		pairs.add(Point3D.of(FACTORY, 8.6982122, 50.1022829));
		pairs.add(Point3D.of(FACTORY, 8.6956695, 50.1020076));
		pairs.add(Point3D.of(FACTORY, 8.6914960, 50.1006313));
		pairs.add(Point3D.of(FACTORY, 8.6875156, 50.0987800));

		String computed = FlexiblePolylineEncoderDecoder.encode(pairs, 5, ThirdDimension.ABSENT, 0);

		Assertions.assertEquals("BFoz5xJ67i1B1B7PzIhaxL7Y", computed);
	}

	@Test
	void complex_point2d_encoding(){
		List<Point3D> pairs = new ArrayList<>();
		pairs.add(Point3D.of(FACTORY, 13.3866272, 52.5199356));
		pairs.add(Point3D.of(FACTORY, 13.2816896, 52.5100899));
		pairs.add(Point3D.of(FACTORY, 13.1935196, 52.4351807));
		pairs.add(Point3D.of(FACTORY, 13.1964502, 52.4107285));
		pairs.add(Point3D.of(FACTORY, 13.1557798, 52.38871));
		pairs.add(Point3D.of(FACTORY, 13.1491003, 52.3727798));
		pairs.add(Point3D.of(FACTORY, 13.1154604, 52.3737488));
		pairs.add(Point3D.of(FACTORY, 13.0872202, 52.3875198));
		pairs.add(Point3D.of(FACTORY, 13.0706196, 52.4029388));
		pairs.add(Point3D.of(FACTORY, 13.0755529, 52.4105797));

		String computed = FlexiblePolylineEncoderDecoder.encode(pairs, 5, ThirdDimension.ABSENT, 0);

		Assertions.assertEquals("BF05xgKuy2xCx9B7vUl0OhnR54EqSzpEl-HxjD3pBiGnyGi2CvwFsgD3nD4vB6e", computed);
	}

	@Test
	void point3d_encode(){
		List<Point3D> tuples = new ArrayList<>();
		tuples.add(Point3D.of(FACTORY, 8.6982122, 50.1022829, 10.));
		tuples.add(Point3D.of(FACTORY, 8.6956695, 50.1020076, 20.));
		tuples.add(Point3D.of(FACTORY, 8.6914960, 50.1006313, 30.));
		tuples.add(Point3D.of(FACTORY, 8.6875156, 50.0987800, 40.));

		String computed = FlexiblePolylineEncoderDecoder.encode(tuples, 5, ThirdDimension.ALTITUDE, 0);

		Assertions.assertEquals("BlBoz5xJ67i1BU1B7PUzIhaUxL7YU", computed);
	}

	@Test
	void fail_on_null_coordinates(){
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			FlexiblePolylineEncoderDecoder.decode(null, FACTORY);
		});
	}

	@Test
	void fail_on_empty_coordinates(){
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			FlexiblePolylineEncoderDecoder.decode("", FACTORY);
		});
	}

	@Test
	void test_third_dimension(){
		Assertions.assertSame(FlexiblePolylineEncoderDecoder.getThirdDimension("BFoz5xJ67i1BU", FACTORY), ThirdDimension.ABSENT);
		Assertions.assertSame(FlexiblePolylineEncoderDecoder.getThirdDimension("BVoz5xJ67i1BU", FACTORY), ThirdDimension.LEVEL);
		Assertions.assertSame(FlexiblePolylineEncoderDecoder.getThirdDimension("BlBoz5xJ67i1BU", FACTORY), ThirdDimension.ALTITUDE);
		Assertions.assertSame(FlexiblePolylineEncoderDecoder.getThirdDimension("B1Boz5xJ67i1BU", FACTORY), ThirdDimension.ELEVATION);
	}

	@Test
	void test_decode_convert_value(){
		String encoded = "h_wqiB";
		Converter conv = new Converter(5);
		final double computed = conv.decodeValue(new StringCharacterIterator(encoded));

		Assertions.assertEquals(-179.98321, computed);
	}

	@Test
	void simple_point2d_decoding(){
		List<Point3D> computed = FlexiblePolylineEncoderDecoder.decode("BFoz5xJ67i1B1B7PzIhaxL7Y", FACTORY);

		List<Point3D> expected = new ArrayList<>();
		expected.add(Point3D.of(FACTORY, 8.69821, 50.10228));
		expected.add(Point3D.of(FACTORY, 8.69567, 50.10201));
		expected.add(Point3D.of(FACTORY, 8.69150, 50.10063));
		expected.add(Point3D.of(FACTORY, 8.68752, 50.09878));
		Assertions.assertEquals(expected.size(), computed.size());
		for(int i = 0; i < computed.size(); ++ i)
			Assertions.assertEquals(expected.get(i), computed.get(i));
	}

	@Test
	void complex_point2d_decoding(){
		List<Point3D> computed = FlexiblePolylineEncoderDecoder.decode("BF05xgKuy2xCx9B7vUl0OhnR54EqSzpEl-HxjD3pBiGnyGi2CvwFsgD3nD4vB6e", FACTORY);

		List<Point3D> pairs = new ArrayList<>();
		pairs.add(Point3D.of(FACTORY, 13.38663, 52.51994));
		pairs.add(Point3D.of(FACTORY, 13.28169, 52.51009));
		pairs.add(Point3D.of(FACTORY, 13.19352, 52.43518));
		pairs.add(Point3D.of(FACTORY, 13.19645, 52.41073));
		pairs.add(Point3D.of(FACTORY, 13.15578, 52.38871));
		pairs.add(Point3D.of(FACTORY, 13.14910, 52.37278));
		pairs.add(Point3D.of(FACTORY, 13.11546, 52.37375));
		pairs.add(Point3D.of(FACTORY, 13.08722, 52.38752));
		pairs.add(Point3D.of(FACTORY, 13.07062, 52.40294));
		pairs.add(Point3D.of(FACTORY, 13.07555, 52.41058));
		Assertions.assertEquals(pairs.size(), computed.size());
		for(int i = 0; i < computed.size(); ++ i)
			Assertions.assertEquals(pairs.get(i), computed.get(i));
	}

	@Test
	void point3d_decode(){
		List<Point3D> computed = FlexiblePolylineEncoderDecoder.decode("BlBoz5xJ67i1BU1B7PUzIhaUxL7YU", FACTORY);

		List<Point3D> tuples = new ArrayList<>();
		tuples.add(Point3D.of(FACTORY, 8.69821, 50.10228, 10.));
		tuples.add(Point3D.of(FACTORY, 8.69567, 50.10201, 20.));
		tuples.add(Point3D.of(FACTORY, 8.69150, 50.10063, 30.));
		tuples.add(Point3D.of(FACTORY, 8.68752, 50.09878, 40.));
		Assertions.assertEquals(tuples.size(), computed.size());
		for(int i = 0; i < computed.size(); ++ i)
			Assertions.assertEquals(tuples.get(i), computed.get(i));
	}

	@Test
	@SuppressWarnings("DataFlowIssue")
	void encoding_smoke_test(){
		ClassLoader cl = FlexiblePolylineEncoderDecoderTest.class.getClassLoader();
		int lineNo = 0;
		try(
				BufferedReader input = new BufferedReader(new InputStreamReader(cl.getResourceAsStream("flexiblepolyline/original.txt")));
				BufferedReader encoded = new BufferedReader(new InputStreamReader(cl.getResourceAsStream("flexiblepolyline/round_half_up/encoded.txt")));
				){
			String inputFileLine;
			String encodedFileLine;
			//read line by line and validate the test
			while((inputFileLine = input.readLine()) != null && (encodedFileLine = encoded.readLine()) != null){
				lineNo ++;
				int precision;
				int thirdDimPrecision = 0;
				boolean hasThirdDimension = false;
				ThirdDimension thirdDimension = ThirdDimension.ABSENT;
				inputFileLine = inputFileLine.trim();
				encodedFileLine = encodedFileLine.trim();

				//file parsing
				String[] inputs = inputFileLine
					.substring(1, inputFileLine.length() - 1)
					.split(";");
				String[] meta = inputs[0].trim()
					.substring(1, inputs[0].trim().length() - 1)
					.split(",");
				precision = Integer.parseInt(meta[0]);

				if(meta.length > 1){
					thirdDimPrecision = Integer.parseInt(meta[1].trim());
					thirdDimension = ThirdDimension.fromNum(Integer.parseInt(meta[2].trim()));
					hasThirdDimension = true;
				}
				List<Point3D> points = extractPoint3D(inputs[1], hasThirdDimension);
				String encodedComputed = FlexiblePolylineEncoderDecoder.encode(points, precision, thirdDimension, thirdDimPrecision);
				String encodedExpected = encodedFileLine;
				Assertions.assertEquals(encodedExpected, encodedComputed);
			}
		}
		catch(Exception e){
			e.printStackTrace();

			System.err.format("LineNo: " + lineNo + " validation got exception: %s%n", e);
		}
	}

	@Test
	@SuppressWarnings("DataFlowIssue")
	void decoding_smoke_test(){
		ClassLoader cl = FlexiblePolylineEncoderDecoderTest.class.getClassLoader();
		int lineNo = 0;
		try(
				BufferedReader encoded = new BufferedReader(new InputStreamReader(cl.getResourceAsStream("flexiblepolyline/round_half_up/encoded.txt")));
				BufferedReader decoded = new BufferedReader(new InputStreamReader(cl.getResourceAsStream("flexiblepolyline/round_half_up/decoded.txt")));
				){
			String encodedFileLine;
			String decodedFileLine;
			//read line by line and validate the test
			while((encodedFileLine = encoded.readLine()) != null && (decodedFileLine = decoded.readLine()) != null){
				lineNo ++;
				boolean hasThirdDimension = false;
				ThirdDimension expectedDimension = ThirdDimension.ABSENT;
				encodedFileLine = encodedFileLine.trim();
				decodedFileLine = decodedFileLine.trim();

				//file parsing
				String[] output = decodedFileLine.substring(1, decodedFileLine.length() - 1).split(";");
				String[] meta = output[0].trim().substring(1, output[0].trim().length() - 1).split(",");
				if(meta.length > 1){
					expectedDimension = ThirdDimension.fromNum(Integer.valueOf(meta[2].trim()));
					hasThirdDimension = true;
				}
				String decodedInputLine = decodedFileLine.substring(1, decodedFileLine.length() - 1).split(";")[1];
				List<Point3D> expectedLatLngZs = extractPoint3D(decodedInputLine, hasThirdDimension);

				//validate third dimension
				ThirdDimension computedDimension = FlexiblePolylineEncoderDecoder.getThirdDimension(encodedFileLine, FACTORY);
				Assertions.assertEquals(expectedDimension, computedDimension);

				//validate point
				List<Point3D> computedLatLngZs = FlexiblePolylineEncoderDecoder.decode(encodedFileLine, FACTORY);
				Assertions.assertEquals(expectedLatLngZs.size(), computedLatLngZs.size());
				for(int i = 0; i < computedLatLngZs.size(); i ++)
					Assertions.assertEquals(expectedLatLngZs.get(i), computedLatLngZs.get(i));
			}
		}
		catch(Exception e){
			e.printStackTrace();

			System.err.format("LineNo: " + lineNo + " validation got exception: %s%n", e);
		}
	}

	private static List<Point3D> extractPoint3D(String line, boolean hasThirdDimension){
		List<Point3D> points = new ArrayList<>();
		String[] coordinates = line.trim().substring(1, line.trim().length() - 1).split(",");
		for(int itr = 0; itr < coordinates.length && !isNullOrEmpty(coordinates[itr]); ){
			double y = Double.parseDouble(coordinates[itr++].trim().replace("(", ""));
			double x = Double.parseDouble(coordinates[itr++].trim().replace(")", ""));
			double z = (hasThirdDimension? Double.parseDouble(coordinates[itr++].trim().replace(")", "")): 0.);
			points.add(Point3D.of(FACTORY, x, y, z));
		}
		return points;
	}

	public static boolean isNullOrEmpty(final String str){
		return (str == null || str.trim().isEmpty());
	}

	@Test
	void testVeryLongLine(){
		//default line length
		int lineLength = 1000;

		final int precision = 10;
		Random random = new Random(System.currentTimeMillis());
		List<Point3D> coordinates = new ArrayList<>();
		for(int i = 0; i <= lineLength; i++){
			Point3D nextPoint = Point3D.of(FACTORY, random.nextDouble(), random.nextDouble(), random.nextDouble());
			coordinates.add(nextPoint);
		}

		String encoded = FlexiblePolylineEncoderDecoder.encode(coordinates, precision, ThirdDimension.ALTITUDE, precision);

		long startTime = System.nanoTime();
		FlexiblePolylineEncoderDecoder.decode(encoded, FACTORY);
		System.out.println("duration: " + ((System.nanoTime() - startTime) / 1000) + " µs");
	}

}
