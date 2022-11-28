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

import io.github.mtrevisan.mapmatcher.helpers.GPSCoordinate;
import org.apache.commons.math3.linear.MatrixUtils;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;


public class GPSPositionFilter{

	private final KalmanFilter filter;


	/*
	 * Create a GPS filter that only tracks two dimensions of position and velocity.
	 * The inherent assumption is that changes in velocity are randomly distributed around 0. Noise is a parameter you can use to alter
	 * the expected noise. 1 is the original, and the higher it is, the more a path will be "smoothed".
	 */
	public GPSPositionFilter(final double noise){
		//The state model has four dimensions: x, y, dx/dt, dy/dt.
		//Each time step we can only observe position, not velocity, so the observation vector has only two dimensions.
		filter = new KalmanFilter(2, 2);

		//Assuming the axes are rectilinear does not work well at the poles, but it has the bonus that we don't need to convert between
		//lat/long and more rectangular coordinates. The slight inaccuracy of our physics model is not too important.
		filter.setStateTransition(MatrixUtils.createRealIdentityMatrix(filter.getStateDimension()));

		//observe (x, y) in each time step
		filter.setObservationModel(MatrixUtils.createRealMatrix(new double[][]{
			new double[]{1., 0.},
			new double[]{0., 1.}
		}));

		//noise in the world
		double pos = 0.000_001;
		filter.setProcessNoiseCovariance(MatrixUtils.createRealMatrix(new double[][]{
			new double[]{pos, 0.},
			new double[]{0., pos}
		}));

		//noise in the observations
		filter.setObservationNoiseCovariance(MatrixUtils.createRealMatrix(new double[][]{
			new double[]{pos * noise, 0.},
			new double[]{0., pos * noise},
		}));

		//the start position is totally unknown, so give a high variance
		filter.setInitialStateEstimate(MatrixUtils.createRealMatrix(filter.getStateDimension(), 1));
		final double trillion = 1_000. * 1_000. * 1_000. * 1_000.;
		filter.setInitialEstimateCovariance(MatrixUtils.createRealIdentityMatrix(filter.getStateDimension())
			.scalarMultiply(trillion));
	}

	public void updatePosition(final double latitude, final double longitude){
		filter.setObservation(MatrixUtils.createRealMatrix(new double[][]{
			new double[]{latitude},
			new double[]{longitude}
		}));
		filter.update();
	}

	public double[] getPosition(){
		final double[] latLon = new double[2];
		latLon[0] = filter.getStateEstimate(0, 0);
		latLon[1] = filter.getStateEstimate(1, 0);
		return latLon;
	}

	public static void main(String[] args){
		ZonedDateTime timestamp = ZonedDateTime.now();
		final GPSCoordinate[] observations = new GPSCoordinate[]{
			new GPSCoordinate(12.172704737567187, 45.59108565830172, timestamp),
			new GPSCoordinate(12.229859503941071, 45.627705048963094, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.241610951232218, 45.6422714215264, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.243213421318018, 45.65646065552491, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.272057882852266, 45.662060679461206, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.273057882852266, 45.663060679461206, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS))),
			new GPSCoordinate(12.274057882852266, 45.662060679461206, (timestamp = timestamp.plus(60, ChronoUnit.SECONDS)))
		};
		GPSCoordinate[] filtered = new GPSCoordinate[observations.length];
		GPSPositionFilter filter = new GPSPositionFilter(100.);
		filtered[0] = observations[0];
		for(int i = 1; i < observations.length; i ++){
			filter.updatePosition(observations[i].getY(), observations[i].getX());
			double[] latLon = filter.getPosition();
			filtered[i] = new GPSCoordinate(latLon[1], latLon[0], observations[i].getTimestamp());
		}
	}

}
