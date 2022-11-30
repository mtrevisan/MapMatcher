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

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;


/**
 * @see <a href="http://en.wikipedia.org/wiki/Kalman_filter">Kalman filter</a>
 */
public class KalmanFilter{

	private final int stateDimension;
	private final int observationDimension;

	//state-transition model, F_k
	private RealMatrix stateTransition;
	//observation mode, H_k
	private RealMatrix observationModel;
	//covariance of the process noise, Q_k
	private RealMatrix processNoiseCovariance;
	//covariance of the observation noise, R_k
	private RealMatrix observationNoiseCovariance;

	//the observation is modified by the user before every time step
	//observation of the true state x_k, z_k
	private RealMatrix observation;

	//x-hat_k|k-1
	private RealMatrix predictedState;
	//P-hat_k|k-1
	private RealMatrix predictedEstimateCovariance;
	//x-hat_k|k
	private RealMatrix stateEstimate;
	//a posteriori estimate covariance matrix (a measure of the estimated accuracy of the state estimate), P_k|k
	private RealMatrix estimateCovariance;


	public KalmanFilter(final int stateDimension, final int observationDimension){
		this.stateDimension = stateDimension;
		this.observationDimension = observationDimension;
	}

	public int getStateDimension(){
		return stateDimension;
	}

	public void setStateTransition(final RealMatrix stateTransition){
		if(stateTransition.getRowDimension() != stateDimension || stateTransition.getColumnDimension() != stateDimension)
			throw new IllegalArgumentException("State transition matrix must be of dimension "
				+ stateDimension + "×" + stateDimension);

		this.stateTransition = stateTransition;
	}

	public void setStateTransition(final int row, final int column, final double value){
		stateTransition.setEntry(row, column, value);
	}

	public void setObservationModel(final RealMatrix observationModel){
		if(observationModel.getRowDimension() != observationDimension || observationModel.getColumnDimension() != stateDimension)
			throw new IllegalArgumentException("Observation model matrix must be of dimension "
				+ observationDimension + "×" + stateDimension);

		this.observationModel = observationModel;
	}

	public void setProcessNoiseCovariance(final RealMatrix processNoiseCovariance){
		if(processNoiseCovariance.getRowDimension() != stateDimension || processNoiseCovariance.getColumnDimension() != stateDimension)
			throw new IllegalArgumentException("Process noise covariance matrix must be of dimension "
				+ stateDimension + "×" + stateDimension);

		this.processNoiseCovariance = processNoiseCovariance;
	}

	public void setObservationNoiseCovariance(final RealMatrix observationNoiseCovariance){
		if(observationNoiseCovariance.getRowDimension() != observationDimension
				|| observationNoiseCovariance.getColumnDimension() != observationDimension)
			throw new IllegalArgumentException("Observation noise covariance matrix must be of dimension "
				+ observationDimension + "×" + observationDimension);

		this.observationNoiseCovariance = observationNoiseCovariance;
	}

	public void setInitialStateEstimate(final RealMatrix stateEstimate){
		if(stateEstimate.getRowDimension() != stateDimension
				|| stateEstimate.getColumnDimension() != 1)
			throw new IllegalArgumentException("Initial state estimate matrix must be of dimension " + stateDimension + "×" + 1);

		this.stateEstimate = stateEstimate;
	}

	public void setInitialEstimateCovariance(final RealMatrix estimateCovariance){
		if(estimateCovariance.getRowDimension() != stateDimension
				|| estimateCovariance.getColumnDimension() != stateDimension)
			throw new IllegalArgumentException("Initial estimate covariance matrix must be of dimension "
				+ stateDimension + "×" + stateDimension);

		this.estimateCovariance = estimateCovariance;
	}

	public void setObservation(final RealMatrix observation){
		if(observation.getRowDimension() != observationDimension
			|| observation.getColumnDimension() != 1)
			throw new IllegalArgumentException("Observation matrix must be of dimension " + observationDimension + "×" + 1);

		this.observation = observation;
	}

	public double getStateEstimate(final int row, final int column){
		return stateEstimate.getEntry(row, column);
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
		//predicted (a priori) state estimate, x-hat_k|j-1 = F_k * x_k-1|k-1 + B_k * u_k, where B_k is the control matrix, and u_k is the
		//	control variable
		predictedState = stateTransition.multiply(stateEstimate);

		//predicted (a priori) state estimate covariance, P-hat_k|k-1 = F_k * P_k-1|k-1 * F_k^T + Q_k
		predictedEstimateCovariance = stateTransition.multiply(estimateCovariance)
			.multiply(stateTransition.transpose())
			.add(processNoiseCovariance);
	}

	private void estimate(){
		//innovation, or measurement pre-fit residual, y-tilde_k = z_k - H_k * x-hat_k|k-1
		final RealMatrix innovation = observation.subtract(observationModel.multiply(predictedState));

		final RealMatrix verticalScratch = predictedEstimateCovariance.multiply(observationModel.transpose());
		//innovation, or pre-fit residual, covariance, S_k = H_k * P-hat_k|k-1 * H_k^T + R_k
		final RealMatrix innovationCovariance = observationModel.multiply(verticalScratch)
			.add(observationNoiseCovariance);

		//calculate the optimal Kalman gain, K_k = P-hat_k|k-1 * H_k^T * S_k^-1
		final RealMatrix optimalGain = verticalScratch.multiply(MatrixUtils.inverse(innovationCovariance));

		//updated (a posteriori) state estimate, x_k|k = x-hat_k|k-1 + K_k * y-tilde_k
		stateEstimate = predictedState.add(optimalGain.multiply(innovation));

		//updated (a posteriori) estimate covariance, P_k|k = (I - K_k * H_k) * P-hat_k|k-1
		estimateCovariance = MatrixUtils.createRealIdentityMatrix(stateDimension)
			.subtract(optimalGain.multiply(observationModel))
			.multiply(predictedEstimateCovariance);
	}

}
