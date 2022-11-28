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
package io.github.mtrevisan.mapmatcher.helpers;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;


public class KalmanFilter{

	private static final float MIN_ACCURACY = 3.f;


	private GPSCoordinate coordinate;

	//initial estimate of error
	private float variance = -1.f;


	public void initializeState(final GPSCoordinate coordinate){
		initializeState(coordinate, MIN_ACCURACY);
	}

	public void initializeState(final GPSCoordinate coordinate, final float accuracy){
		this.coordinate = coordinate;
		this.variance = accuracy * accuracy;
	}

	/**
	 * Kalman filter processing for latitude and longitude.
	 *
	 * @param coordinate	New coordinate.
	 * @param speed	New measurement of speed [m/s].
	 * @param accuracy	New measurement of accuracy.
	 */
	public void processWithAccuracy(final GPSCoordinate coordinate, final float speed, final float accuracy){
		process(coordinate, speed, Math.max(accuracy, MIN_ACCURACY));
	}

	/**
	 * Kalman filter processing for latitude and longitude.
	 *
	 * @param coordinate	New coordinate.
	 * @param speed	New measurement of speed [m/s].
	 */
	public void process(final GPSCoordinate coordinate, final float speed, final float accuracy){
		if(variance < 0)
			initializeState(coordinate, accuracy);
		else{
			//else apply Kalman filter
			final long duration = ChronoUnit.SECONDS.between(coordinate.getTimestamp(), this.coordinate.getTimestamp());
			ZonedDateTime newTimestamp = this.coordinate.getTimestamp();
			if(duration > 0l){
				//time has moved on, so the uncertainty in the current position increases
				variance += duration * speed * speed;
				newTimestamp = coordinate.getTimestamp();
			}

			//Kalman gain matrix k = covariance * inv(covariance + measurementVariance)
			//(because 'k' is dimensionless, it doesn't matter that variance has different units from latitude and longitude)
			final float k = variance / (variance + accuracy * accuracy);
			//apply 'k'
			final double newLatitude = this.coordinate.getY() + k * (coordinate.getY() - this.coordinate.getY());
			final double newLongitude = this.coordinate.getX() + k * (coordinate.getX() - this.coordinate.getX());

			//new covariance matrix is (I - k) * covariance
			variance *= 1.f - k;

			//export new point
			this.coordinate = new GPSCoordinate(newLongitude, newLatitude, newTimestamp);
		}
	}

	public GPSCoordinate getCoordinate(){
		return coordinate;
	}

}
