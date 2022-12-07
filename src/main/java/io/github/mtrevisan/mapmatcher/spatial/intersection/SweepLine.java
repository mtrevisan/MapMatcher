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
package io.github.mtrevisan.mapmatcher.spatial.intersection;

import java.util.Comparator;
import java.util.TreeSet;


public class SweepLine extends TreeSet<SweepSegment>{

	SweepLine(){
		super(Comparator.comparingDouble(SweepSegment::position));
	}

	void remove(final SweepSegment s){
		removeIf(sweepSegment -> sweepSegment.nearlyEqual(s));
	}

	void swap(final SweepSegment s1, final SweepSegment s2){
		remove(s1);
		remove(s2);

		final double swap = s1.position();
		s1.setPosition(s2.position());
		s2.setPosition(swap);

		add(s1);
		add(s2);
	}

	SweepSegment above(final SweepSegment s){
		return higher(s);
	}

	SweepSegment below(final SweepSegment s){
		return lower(s);
	}

	void updatePositions(final double x){
		for(final SweepSegment s : this)
			s.updatePosition(x);
	}

}
