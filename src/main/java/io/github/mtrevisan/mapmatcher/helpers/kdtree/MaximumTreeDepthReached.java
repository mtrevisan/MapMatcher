package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import java.io.Serial;


public class MaximumTreeDepthReached extends Exception{

	@Serial
	private static final long serialVersionUID = -3241540435024864300L;


	public static MaximumTreeDepthReached create(){
		return new MaximumTreeDepthReached();
	}

	private MaximumTreeDepthReached(){
		super("Maximum tree depth reached (addressable limit reached), unable to expand further");
	}

}
