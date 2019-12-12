package de.uni_stuttgart.tik.viplab.websocket_api.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ComputationTemplate {
	public String identifier;
	/**
	 * The base environment used for the Computation, the environment defines
	 * language, runtime, libraries and tools
	 */
	public String environment;
	public Map<String, Object> metadata;
	public List<File> files = Collections.emptyList();
	/**
	 * The configuration of the environment. All environment specific
	 * configuration goes in here.
	 */
	public Map<String, Object> configuration = Collections.emptyMap();

	public static class File {
		/**
		 * only valid in the context of this ComputationTemplate
		 */
		public String identifier;
		public String path;
		public Map<String, Object> metadata;
		public List<Part> parts;

		public static class Part {
			public String identifier;
			/**
			 * Base64 encoded content
			 */
			public String content;
			public String access;
		}

		public enum Access {
			INVISIBLE, VISIBLE, MODIFIABLE
		}
	}
}
