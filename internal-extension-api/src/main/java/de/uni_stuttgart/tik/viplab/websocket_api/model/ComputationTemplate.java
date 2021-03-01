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

	/**
	 * Parameters defined here can be given by the ComputationTask as arguments.
	 * The parameters are can be used to supply values at runtime to the
	 * configuration of this ComputationTemplate.
	 */
	public Map<String, Parameter> parameters = Collections.emptyMap();

	public static class File {
		public String identifier;
		public String path;
		public Map<String, Object> metadata;
		public List<Part> parts = Collections.emptyList();

		public static class Part {
			public String identifier;
			/**
			 * Base64 encoded content
			 */
			public String content;
			public String access;
			/**
			 * Only used when access is template. The parameters defined can be
			 * used in the template and are validated before inserted into the
			 * template.
			 */
			public Map<String, Parameter> parameters = null;

			public Map<String, Object> metadata;

			public static final String ACCESS_INVISIBLE = "invisible";
			public static final String ACCESS_VISIBLE = "visible";
			public static final String ACCESS_MODIFIABLE = "modifiable";
			public static final String ACCESS_TEMPLATE = "template";
		}
	}
}
