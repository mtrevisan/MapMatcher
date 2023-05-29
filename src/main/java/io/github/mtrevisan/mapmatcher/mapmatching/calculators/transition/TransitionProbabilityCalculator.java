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
package io.github.mtrevisan.mapmatcher.mapmatching.calculators.transition;

import io.github.mtrevisan.mapmatcher.graph.Edge;
import io.github.mtrevisan.mapmatcher.spatial.Point;
import io.github.mtrevisan.mapmatcher.spatial.Polyline;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class TransitionProbabilityCalculator{

	private final Set<TransitionProbabilityPlugin> plugins = new HashSet<>(0);


	public final TransitionProbabilityCalculator withPlugin(final TransitionProbabilityPlugin plugin){
		plugins.add(plugin);

		return this;
	}

	public final double transitionProbability(final Edge fromEdge, final Edge toEdge,
			final Point previousObservation, final Point currentObservation, final Polyline path){
		double factor = 0.;
		final Iterator<TransitionProbabilityPlugin> itr = plugins.iterator();
		while(itr.hasNext() && Double.isFinite(factor))
			factor += itr.next()
				.factor(fromEdge, toEdge, previousObservation, currentObservation, path);
		return factor;
	}

}
