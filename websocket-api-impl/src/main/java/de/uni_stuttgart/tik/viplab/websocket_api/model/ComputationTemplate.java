package de.uni_stuttgart.tik.viplab.websocket_api.model;

import java.util.List;
import java.util.Map;

public class ComputationTemplate {
	public String identifier;
	/**
	 * The base environment used for the Computation, the environment defines language, runtime, libraries and tools
	 */
	public String environment;
	public Map<String, Object> metadata;
	public List<File> files;
	public Map<String, Object> configuration;

	static class File {
		/**
		 * only valid in the context of this ComputationTemplate
		 */
		public String identifier;
		public String path;
		public String access;
		public Map<String, Object> metadata;
		public List<Part> parts;

		static class Part {
			public String identifier;
			/**
			 * Base64 encoded content
			 */
			public String content;
		}

		enum Access {
			INVISIBLE, VISIBLE, MODIFIABLE
		}
	}
}
