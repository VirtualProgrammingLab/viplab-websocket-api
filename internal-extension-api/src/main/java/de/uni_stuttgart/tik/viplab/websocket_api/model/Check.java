package de.uni_stuttgart.tik.viplab.websocket_api.model;

/**
 * Define checks performed on input data that must pass, to consider the input
 * valid. The Check defines multiple possible validation methods. Only the
 * explicitly given validation methods will be used to check the input.
 * 
 * @author Leon
 */
public class Check {
	/**
	 * The input must match this regex pattern.
	 */
	public String pattern = null;

	/**
	 * The input must be a valid value for this type.
	 */
	public String type = null;
}
