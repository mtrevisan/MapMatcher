package io.github.mtrevisan.mapmatcher.helpers.kdtree;

import java.io.Serial;


public class MaximumTreeDepthReachedException extends Exception{

	@Serial
	private static final long serialVersionUID = -3241540435024864300L;


	public static MaximumTreeDepthReachedException create(){
		return new MaximumTreeDepthReachedException();
	}

	private MaximumTreeDepthReachedException(){
		super("Maximum tree depth reached (addressable limit reached), unable to expand further");
	}

}
