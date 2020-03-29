package de.uni_stuttgart.tik.viplab.websocket_api.ecs;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class Exercise {
	public String postTime;
	public int TTL;
	public String identifier;
	public String department;
	public String comment;
	public String name;
	public String description;
	public List<Element> elements;
	public String environment;
	public Routing routing;
	public Map<String, URI> elementMap;
	public Map<String, Map<String, Object>> elementProperties;
	public Map<String, Object> config;

	public static class Element {
		public String identifier;
		public String group;
		public boolean visible = false;
		public boolean modifiable = false;
		public String name;
		public String MIMEtype;
		public String syntaxHighlighting;
		public String emphasis;
		public String value;

		@Override
		public String toString() {
			return "Element [" + (identifier != null ? "identifier=" + identifier + ", " : "")
					+ (group != null ? "group=" + group + ", " : "") + "visible=" + visible + ", modifiable="
					+ modifiable + ", " + (name != null ? "name=" + name + ", " : "")
					+ (MIMEtype != null ? "MIMEtype=" + MIMEtype + ", " : "")
					+ (syntaxHighlighting != null ? "syntaxHighlighting=" + syntaxHighlighting + ", " : "")
					+ (emphasis != null ? "emphasis=" + emphasis + ", " : "") + (value != null ? "value=" + value : "")
					+ "]";
		}

	}

	public static class Routing {
		public String solutionQueue;

		@Override
		public String toString() {
			return "Routing [" + (solutionQueue != null ? "solutionQueue=" + solutionQueue : "") + "]";
		}

	}

	public static class Wrapper {
		public Exercise Exercise;

		public Wrapper(Exercise exercise) {
			Exercise = exercise;
		}

	}

	@Override
	public String toString() {
		return "Exercise [" + (postTime != null ? "postTime=" + postTime + ", " : "") + "TTL=" + TTL + ", "
				+ (identifier != null ? "identifier=" + identifier + ", " : "")
				+ (department != null ? "department=" + department + ", " : "")
				+ (comment != null ? "comment=" + comment + ", " : "") + (name != null ? "name=" + name + ", " : "")
				+ (description != null ? "description=" + description + ", " : "")
				+ (elements != null ? "elements=" + elements + ", " : "")
				+ (environment != null ? "environment=" + environment + ", " : "")
				+ (routing != null ? "routing=" + routing + ", " : "")
				+ (elementMap != null ? "elementMap=" + elementMap + ", " : "")
				+ (elementProperties != null ? "elementProperties=" + elementProperties + ", " : "")
				+ (config != null ? "config=" + config : "") + "]";
	}
}
