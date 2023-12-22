package io.github.mtrevisan.mapmatcher.spatial.flexiblepolyline;

import io.github.mtrevisan.mapmatcher.spatial.GeometryFactory;
import io.github.mtrevisan.mapmatcher.spatial.Point3D;

import java.util.ArrayList;
import java.util.List;


/**
 * The polyline encoding is a lossy-compressed representation of a list of coordinate pairs or coordinate triples.
 * <p>
 * It achieves that by:
 * <ol>
 * 	<li>Reducing the decimal digits of each value.
 * 	<li>Encoding only the offset from the previous point.
 * 	<li>Using variable length for each coordinate delta.
 * 	<li>Using 64 URL-safe characters to display the result.
 * </ol>
 * The advantage of this encoding are the following:
 * <ul>
 * 	<li>Output string is composed by only URL-safe characters.
 * 	<li>Floating point precision is configurable.
 * 	<li>It allows to encode a 3rd dimension with a given precision, which may be a level, altitude, elevation or some other custom value.
 * </ul>
 * </p>
 */
public class FlexiblePolylineEncoderDecoder{

	/**
	 * Header version
	 * A change in the version may affect the logic to encode and decode the rest of the header and data
	 */
	public static final byte FORMAT_VERSION = 1;


	/**
	 * Encode the list of coordinate triples.<BR><BR>
	 * The third dimension value will be eligible for encoding only when ThirdDimension is other than ABSENT.
	 * This is lossy compression based on precision accuracy.
	 *
	 * @param coordinates       {@link List} of coordinate triples that to be encoded.
	 * @param precision         Floating point precision of the coordinate to be encoded.
	 * @param thirdDimension    {@link ThirdDimension} which may be a level, altitude, elevation or some other custom value
	 * @param thirdDimPrecision Floating point precision for thirdDimension value
	 * @return URL-safe encoded {@link String} for the given coordinates.
	 */
	public static String encode(final List<Point3D> coordinates, final int precision, final ThirdDimension thirdDimension,
			final int thirdDimPrecision){
		if(coordinates == null || coordinates.isEmpty())
			throw new IllegalArgumentException("Invalid coordinates!");
		if(thirdDimension == null)
			throw new IllegalArgumentException("Invalid thirdDimension");

		final Encoder enc = new Encoder(precision, thirdDimension, thirdDimPrecision);
		for(final Point3D coordinate : coordinates)
			enc.add(coordinate);
		return enc.getEncoded();
	}

	/**
	 * Decode the encoded input {@link String} to {@link List} of coordinate triples.<BR><BR>
	 *
	 * @param encoded URL-safe encoded {@link String}
	 * @return {@link List} of coordinate triples that are decoded from input
	 * @see #getThirdDimension(String, GeometryFactory) getThirdDimension
	 */
	public static List<Point3D> decode(final String encoded, final GeometryFactory factory){
		if(encoded == null || encoded.trim().isEmpty())
			throw new IllegalArgumentException("Invalid argument!");

		final List<Point3D> result = new ArrayList<>();
		final Decoder dec = new Decoder(encoded, factory);

		Point3D coord;
		while((coord = dec.decodeOne()) != null)
			result.add(coord);
		return result;
	}

	/**
	 * ThirdDimension type from the encoded input {@link String}
	 *
	 * @param encoded URL-safe encoded coordinate triples {@link String}
	 * @return type of {@link ThirdDimension}
	 */
	public static ThirdDimension getThirdDimension(final String encoded, final GeometryFactory factory){
		return (new Decoder(encoded, factory))
			.getThirdDimension();
	}

	public byte getVersion(){
		return FORMAT_VERSION;
	}

}
