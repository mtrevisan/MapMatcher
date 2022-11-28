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
package io.github.mtrevisan.mapmatcher.helpers.test;

/* To use these functions:
	1. Start with a KalmanFilter created by alloc_filter_velocity2d.
	2. At fixed intervals, call update_velocity2d with the lat/long.
	3. At any time, to get an estimate for the current position,
	bearing, or speed, use the functions:
	get_lat_long
	get_bearing
	get_mph
	*/

import io.github.mtrevisan.mapmatcher.helpers.GPSCoordinate;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;


public class GeoTrackFilter{

	// FIXME Radius should be calculated depending on latitude instead
	// http://en.wikipedia.org/wiki/Earth_radius#Radius_at_a_given_geodetic_latitude
	private static final double EARTH_RADIUS_IN_METERS = 6371009;

	private KalmanFilter filter;


	/*
	 * Create a GPS filter that only tracks two dimensions of position and velocity.
	 * The inherent assumption is that changes in velocity are randomly distributed around 0. Noise is a parameter you can use to alter
	 * the expected noise. 1 is the original, and the higher it is, the more a path will be "smoothed".
	 */
	GeoTrackFilter(double noise){
		//The state model has four dimensions: x, y, x', y'.
		//Each time step we can only observe position, not velocity, so the observation vector has only two dimensions.
		filter = new KalmanFilter(4, 2);

		//Assuming the axes are rectilinear does not work well at the poles, but it has the bonus that we don't need to convert between
		//lat/long and more rectangular coordinates. The slight inaccuracy of our physics model is not too important.
		filter.stateTransition.setIdentity();
		set_seconds_per_timestep(1.);

		/* We observe (x, y) in each time step */
		filter.observationModel.set_matrix(1., 0., 0., 0., 0., 1., 0., 0.);

		/* Noise in the world. */
		double pos = 0.000001;
		filter.processNoiseCovariance.set_matrix(pos, 0., 0., 0., 0., pos, 0., 0., 0., 0., 1., 0., 0., 0., 0., 1.);

		/* Noise in our observation */
		filter.observationNoiseCovariance.set_matrix(pos * noise, 0., 0., pos * noise);

		/* The start position is totally unknown, so give a high variance */
		filter.stateEstimate.set_matrix(0., 0., 0., 0.);
		filter.estimateCovariance.setIdentity();
		double trillion = 1000. * 1000. * 1000. * 1000.;
		filter.estimateCovariance.scale_matrix(trillion);
	}

	/* Set the seconds per timestep in the velocity2d model. */
	/*
	 * The position units are in thousandths of latitude and longitude. The
	 * velocity units are in thousandths of position units per second.
	 *
	 * So if there is one second per timestep, a velocity of 1 will change the
	 * lat or long by 1 after a million timesteps.
	 *
	 * Thus a typical position is hundreds of thousands of units. A typical
	 * velocity is maybe ten.
	 */
	void set_seconds_per_timestep(double seconds_per_timestep){
		/*
		 * unit_scaler accounts for the relation between position and velocity
		 * units
		 */
		double unit_scaler = 0.001;
		filter.stateTransition.data[0][2] = unit_scaler * seconds_per_timestep;
		filter.stateTransition.data[1][3] = unit_scaler * seconds_per_timestep;
	}

	/* Update the velocity2d model with new gps data. */
	void update_velocity2d(double lat, double lon, double seconds_since_last_timestep){
		set_seconds_per_timestep(seconds_since_last_timestep);
		filter.observation.set_matrix(lat * 1000., lon * 1000.);
		filter.update();
	}

	/* Extract a lat long from a velocity2d Kalman filter. */
	double[] get_lat_long(){
		double[] latlon = new double[2];
		latlon[0] = filter.stateEstimate.data[0][0] / 1000.;
		latlon[1] = filter.stateEstimate.data[1][0] / 1000.;
		return latlon;
	}

	/*
	 * Extract velocity with lat-long-per-second units from a velocity2d Kalman
	 * filter.
	 */
	double[] get_velocity(){
		double[] delta_latlon = new double[2];
		delta_latlon[0] = filter.stateEstimate.data[2][0] / (1000. * 1000.);
		delta_latlon[1] = filter.stateEstimate.data[3][0] / (1000. * 1000.);
		return delta_latlon;
	}

	/*
	 * Extract a bearing from a velocity2d Kalman filter. 0 = north, 90 = east,
	 * 180 = south, 270 = west
	 */
	/*
	 * See http://www.movable-type.co.uk/scripts/latlong.html for formulas
	 */
	double get_bearing(){
		double x, y;
		double[] latlon = get_lat_long();
		double[] delta_latlon = get_velocity();

		/* Convert to radians */
		latlon[0] = Math.toRadians(latlon[0]);
		latlon[1] = Math.toRadians(latlon[1]);
		delta_latlon[0] = Math.toRadians(delta_latlon[0]);
		delta_latlon[1] = Math.toRadians(delta_latlon[1]);

		/* Do math */
		double lat1 = latlon[0] - delta_latlon[0];
		y = Math.sin(delta_latlon[1]) * Math.cos(latlon[0]);
		x = Math.cos(lat1) * Math.sin(latlon[0]) - Math.sin(lat1) * Math.cos(latlon[0]) * Math.cos(delta_latlon[1]);
		double bearing = Math.atan2(y, x);

		/* Convert to degrees */
		bearing = Math.toDegrees(bearing);
		return bearing;
	}

	/* Extract speed in meters per second from a velocity2d Kalman filter. */
	double get_speed(double altitude){
		double[] latlon = get_lat_long();
		double[] delta_latlon = get_velocity();
		/*
		 * First, let's calculate a unit-independent measurement - the radii of
		 * the earth traveled in each second. (Presumably this will be a very
		 * small number.)
		 */

		/* Convert to radians */
		latlon[0] = Math.toRadians(latlon[0]);
		latlon[1] = Math.toRadians(latlon[1]);
		delta_latlon[0] = Math.toRadians(delta_latlon[0]);
		delta_latlon[1] = Math.toRadians(delta_latlon[1]);

		/* Haversine formula */
		double lat1 = latlon[0] - delta_latlon[0];
		double sin_half_dlat = Math.sin(delta_latlon[0] / 2.);
		double sin_half_dlon = Math.sin(delta_latlon[1] / 2.);
		double a = sin_half_dlat * sin_half_dlat + Math.cos(lat1) * Math.cos(latlon[0]) * sin_half_dlon * sin_half_dlon;
		double radians_per_second = 2 * Math.atan2(1000. * Math.sqrt(a), 1000. * Math.sqrt(1. - a));

		/* Convert units */
		double meters_per_second = radians_per_second * (EARTH_RADIUS_IN_METERS + altitude);
		return meters_per_second;
	}

	public static void main(String[] args){
		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSCoordinate[] observations = new GPSCoordinate[]{new GPSCoordinate(12.172704737567187, 45.59108565830172, timestamp), new GPSCoordinate(12.229859503941071, 45.627705048963094, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))), new GPSCoordinate(12.241610951232218, 45.6422714215264, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))), new GPSCoordinate(12.243213421318018, 45.65646065552491, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))), new GPSCoordinate(12.272057882852266, 45.662060679461206, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))), new GPSCoordinate(12.372057882852266, 45.662060679461206, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))), new GPSCoordinate(12.304641441251732, 45.66168736195718, (timestamp = timestamp.plus(2, ChronoUnit.SECONDS))), new GPSCoordinate(12.331349276005653, 45.66168736195718, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS)))};
		GPSCoordinate[] filtered = new GPSCoordinate[observations.length];
		GeoTrackFilter filter = new GeoTrackFilter(1.);
		filtered[0] = observations[0];
		for(int i = 1; i < observations.length; i++){
			filter.update_velocity2d(observations[i].getY(), observations[i].getX(), ChronoUnit.SECONDS.between(observations[i - 1].getTimestamp(), observations[i].getTimestamp()));
			double[] latLon = filter.get_lat_long();
			filtered[i] = new GPSCoordinate(latLon[1], latLon[0], observations[i].getTimestamp());
		}
	}

}
