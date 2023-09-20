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

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;


public class GPSPositionFilter{

	private final KalmanFilter filter;


	/*
	 * Create a GPS filter that only tracks one dimension of position.
	 * Noise that the higher `observationNoise` is, the more a path will be "smoothed".
	 */
	public GPSPositionFilter(final double processNoise, final double observationNoise){
		//The state model has two dimensions: x, y
		filter = new KalmanFilter(2, 2);

		//Assuming the axes are rectilinear does not work well at the poles, but it has the bonus that we don't need to convert between
		//lat/long and more rectangular coordinates. The slight inaccuracy of our physics model is not too important.
		final RealMatrix stateTransition = MatrixUtils.createRealIdentityMatrix(filter.getStateDimensions());
		filter.setStateTransition(stateTransition);

		//observe (x, y) in each time step
		filter.setObservationModel(MatrixUtils.createRealMatrix(new double[][]{
			new double[]{1., 0.},
			new double[]{0., 1.}
		}));

		//noise in the world
		filter.setProcessNoiseCovariance(MatrixUtils.createRealMatrix(new double[][]{
			new double[]{processNoise, 0.},
			new double[]{0., processNoise}
		}));

		//noise in the observations
		filter.setObservationNoiseCovariance(MatrixUtils.createRealMatrix(new double[][]{
			new double[]{observationNoise, 0.},
			new double[]{0., observationNoise},
		}));

		//initial state
		filter.setInitialStateEstimate(MatrixUtils.createRealMatrix(filter.getStateDimensions(), 1));

		//the start position is totally unknown, so give a high variance
		final double trillion = 1_000. * 1_000. * 1_000. * 1_000.;
		final RealMatrix initialEstimateCovariance = MatrixUtils.createRealIdentityMatrix(filter.getStateDimensions())
			.scalarMultiply(trillion);
		filter.setInitialEstimateCovariance(initialEstimateCovariance);
	}

	public void updatePosition(final double latitude, final double longitude){
		filter.setObservation(MatrixUtils.createRealMatrix(new double[][]{
			new double[]{latitude},
			new double[]{longitude}
		}));
		filter.update();
	}

	/** Extract filtered position. */
	public double[] getPosition(){
		final double[] latLon = new double[2];
		latLon[0] = filter.getStateEstimate(0, 0);
		latLon[1] = filter.getStateEstimate(1, 0);
		return latLon;
	}

}
