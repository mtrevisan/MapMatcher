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
package io.github.mtrevisan.mapmatcher.spatial.intersection;

import java.util.Comparator;
import java.util.TreeSet;


public class SweepLineTreeSet extends TreeSet<SweepSegment>{

	SweepLineTreeSet(){
		super(Comparator.comparingDouble(SweepSegment::getYIndex));
	}

	void remove(final SweepSegment segment){
		removeIf(sweepSegment -> sweepSegment.isNearlyEqual(segment));
	}

	void swap(final SweepSegment segment1, final SweepSegment segment2){
		remove(segment1);
		remove(segment2);

		final double swap = segment1.getYIndex();
		segment1.setYIndex(segment2.getYIndex());
		segment2.setYIndex(swap);

		add(segment1);
		add(segment2);
	}

	SweepSegment above(final SweepSegment segment){
		return higher(segment);
	}

	SweepSegment below(final SweepSegment segment){
		return lower(segment);
	}

	void updateYIndexes(final double x){
		for(final SweepSegment segment : this)
			segment.updateYIndex(x);
	}

}
