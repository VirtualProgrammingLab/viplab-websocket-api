package de.uni_stuttgart.tik.viplab.websocket_api.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate.File;

public class ComputationTask {
	/**
	 * The identifier of this Task
	 */
	public String identifier;
	/**
	 * Identifier of the ComputationTemplate used for this Task
	 */
	public String template;

	public Map<String, String> arguments;

	public Map<String, Object> metadata;
	/**
	 * New or changed files
	 */
	public List<File> files = Collections.emptyList();
}
