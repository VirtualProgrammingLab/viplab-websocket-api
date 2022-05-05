package de.uni_stuttgart.tik.viplab.websocket_api.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ComputationTask {
	/**
	 * The identifier of this Task
	 */
	public String identifier;
	/**
	 * Identifier of the ComputationTemplate used for this Task
	 */
	public String template;

	public Map<String, Object> arguments;

	public Map<String, Object> metadata;
	/**
	 * New or changed files
	 */
	public List<File> files = Collections.emptyList();
	
	public static class File {
		/**
		 * Reference to the ComputationTemplate File
		 */
		public String identifier;
		public List<Part> parts = Collections.emptyList();

		public static class Part {
			public String identifier;
			/**
			 * Base64 encoded content
			 */
			public String content;
		}
	}
}
