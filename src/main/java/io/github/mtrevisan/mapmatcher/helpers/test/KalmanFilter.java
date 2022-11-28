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

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;


/**
 * @see <a href="http://en.wikipedia.org/wiki/Kalman_filter">Kalman filter</a>
 */
public class KalmanFilter{
	/* k */
	private int timestep;

	/* These parameters define the size of the matrices. */
	private int stateDimension;
	private int observationDimension;

	/* This group of matrices must be specified by the user. */
	/* F_k */
	private RealMatrix stateTransition;
	/* H_k */
	private RealMatrix observationModel;
	/* Q_k */
	private RealMatrix processNoiseCovariance;
	/* R_k */
	private RealMatrix observationNoiseCovariance;

	/* The observation is modified by the user before every time step. */
	/* z_k */
	private RealMatrix observation;

	/* This group of matrices are updated every time step by the filter. */
	/* x-hat_k|k-1 */
	private RealMatrix predictedState;
	/* P_k|k-1 */
	private RealMatrix predictedEstimateCovariance;
	/* y-tilde_k */
	private RealMatrix innovation;
	/* S_k */
	private RealMatrix innovationCovariance;
	/* S_k^-1 */
	private RealMatrix inverseInnovationCovariance;
	/* K_k */
	private RealMatrix optimalGain;
	/* x-hat_k|k */
	private RealMatrix stateEstimate;
	/* P_k|k */
	private RealMatrix estimateCovariance;

	/* This group is used for meaningless intermediate calculations */
	private RealMatrix verticalScratch;
	private RealMatrix smallSquareScratch;
	private RealMatrix bigSquareScratch;


	public KalmanFilter(final int stateDimension, final int observationDimension){
		timestep = 0;
		this.stateDimension = stateDimension;
		this.observationDimension = observationDimension;

		stateTransition = MatrixUtils.createRealMatrix(stateDimension, stateDimension);
		observationModel = MatrixUtils.createRealMatrix(observationDimension, stateDimension);
		processNoiseCovariance = MatrixUtils.createRealMatrix(stateDimension, stateDimension);
		observationNoiseCovariance = MatrixUtils.createRealMatrix(observationDimension, observationDimension);

		observation = MatrixUtils.createRealMatrix(observationDimension, 1);

		predictedState = MatrixUtils.createRealMatrix(stateDimension, 1);
		predictedEstimateCovariance = MatrixUtils.createRealMatrix(stateDimension, stateDimension);
		innovation = MatrixUtils.createRealMatrix(observationDimension, 1);
		innovationCovariance = MatrixUtils.createRealMatrix(observationDimension, observationDimension);
		inverseInnovationCovariance = MatrixUtils.createRealMatrix(observationDimension, observationDimension);
		optimalGain = MatrixUtils.createRealMatrix(stateDimension, observationDimension);
		stateEstimate = MatrixUtils.createRealMatrix(stateDimension, 1);
		estimateCovariance = MatrixUtils.createRealMatrix(stateDimension, stateDimension);

		verticalScratch = MatrixUtils.createRealMatrix(stateDimension, observationDimension);
		smallSquareScratch = MatrixUtils.createRealMatrix(observationDimension, observationDimension);
		bigSquareScratch = MatrixUtils.createRealMatrix(stateDimension, stateDimension);
	}

	/**
	 * Runs one time step of prediction and estimation.
	 * <p>
	 * Before each time step of running this, set <code>f.observation</code> to be the next time step's observation.
	 * </p>
	 * <p>
	 * Before the first step, define the model by setting: <code>f.state_transition</code>, <code>f.observation_model</code>,
	 * <code>f.process_noise_covariance</code>, and <code>f.observation_noise_covariance</code>.
	 * </p>
	 * <p>
	 * It is also advisable to initialize with reasonable guesses for <code>f.state_estimate</code> and <code>f.estimate_covariance</code>.
	 * </p>
	 */
	public void update(){
		predict();
		estimate();
	}

	private void predict(){
		timestep++;

		/* Predict the state */
		predictedState = stateTransition.multiply(stateEstimate);

		/* Predict the state estimate covariance */
		bigSquareScratch = stateTransition.multiply(estimateCovariance);
		predictedEstimateCovariance = bigSquareScratch.multiply(stateTransition.transpose());
		predictedEstimateCovariance = predictedEstimateCovariance.add(processNoiseCovariance);
	}

	private void estimate(){
		//calculate innovation
		innovation = observationModel.multiply(predictedState);
		innovation = observation.subtract(innovation);

		//calculate innovation covariance
		verticalScratch = predictedEstimateCovariance.multiply(observationModel.transpose());
		innovationCovariance = observationModel.multiply(verticalScratch);
		innovationCovariance = innovationCovariance.add(observationNoiseCovariance);

		//invert the innovation covariance
		inverseInnovationCovariance = MatrixUtils.inverse(innovationCovariance);

		//calculate the optimal Kalman gain
		//Note we still have a useful partial product in vertical scratch from the innovation covariance
		optimalGain = verticalScratch.multiply(inverseInnovationCovariance);

		//estimate the state
		stateEstimate = optimalGain.multiply(innovation);
		stateEstimate = stateEstimate.add(predictedState);

		//estimate the state covariance
		bigSquareScratch = optimalGain.multiply(observationModel);
		bigSquareScratch = MatrixUtils.createRealIdentityMatrix(stateDimension).subtract(bigSquareScratch);
		estimateCovariance = bigSquareScratch.multiply(predictedEstimateCovariance);
	}

}
