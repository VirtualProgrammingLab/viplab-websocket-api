package de.uni_stuttgart.tik.viplab.websocket_api.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate.File;

public class Computation {
	/**
	 * The identifier of this Computation
	 */

	public String environment;
	public String identifier;

	public Map<String, Object> configuration;

	/**
	 * The files
	 */
	public List<File> files = Collections.emptyList();

}
