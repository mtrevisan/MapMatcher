package io.github.mtrevisan.mapmatcher.mapmatching.calculators;


public class ProbabilityHelper{

	public static double logPr(final double probability){
		return -StrictMath.log(probability);
	}

}
