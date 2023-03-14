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
package io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.helpers.PathHelper;
import io.github.mtrevisan.mapmatcher.mapmatching.calculators.initial.InitialProbabilityCalculator;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.nio.file.Path;


public class TopologicalTransitionPlugin implements TransitionProbabilityPlugin{

	private static final double PROBABILITY_UNCONNECTED_EDGES = 0.;
	private static final double PROBABILITY_CONNECTED_EDGES = Math.exp(-1.);
	private static final double PROBABILITY_SAME_EDGE = Math.exp(-0.5);


	/**
	 * Calculate transition probability
	 * <p>
	 * If two segments are (r_ij is the so-called topological relationship, and a_ij = e^-r_ij):
	 * <dl>
	 *    <dt>unconnected</dt>
	 *    	<dd><code>r_ij = ∞</code>, thus <code>a_ij = 0</code></dd>
	 *    <dt>connected</dt>
	 *    	<dd><code>r_ij = 1</code>, thus <code>a_ij = e⁻¹ = 0.36787944117</code></dd>
	 *    <dt>the same (i = j)</dt>
	 *    	<dd><code>r_ij = 0</code>, thus <code>a_ij = e⁻⁰⋅⁵ = 0.60653065971</code></dd>
	 * </dl>
	 * </p>
	 */
	@Override
	public double factor(final Edge fromSegment, final Edge toSegment, final Point previousObservation, final Point currentObservation,
			final Polyline path){
		double a = PROBABILITY_UNCONNECTED_EDGES;
		if(PathHelper.isSegmentsTheSame(fromSegment, toSegment))
			a = PROBABILITY_SAME_EDGE;
		else if(path.size() <= 3)
			a = PROBABILITY_CONNECTED_EDGES;
		return InitialProbabilityCalculator.logPr(a);
	}

}
