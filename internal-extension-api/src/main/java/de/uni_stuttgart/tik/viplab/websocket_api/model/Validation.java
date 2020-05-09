package de.uni_stuttgart.tik.viplab.websocket_api.model;

import java.util.List;

/**
 * Define checks performed on input data that must pass, to consider the input
 * valid. The Validation defines multiple possible validation methods. Only the
 * explicitly given validation methods will be used to validate the input.
 * 
 * @author Leon
 */
public class Validation {
	/**
	 * The input must match this regex pattern.
	 */
	public String pattern = null;

	/**
	 * The input must be a valid value for this type. (currently not part of the
	 * spec)
	 */
	public String type = null;

	/**
	 * A whitelist of allowed values.
	 */
	public List<String> oneof = null;

	// range must be clarified in the spec
}
