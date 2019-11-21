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
	public Config config;

	public static class Element {
		public String identifier;
		public String group;
		public boolean visible = false;
		public boolean modifiable = false;
		public String namne;
		public String MIMEtype;
		public String syntaxHighlighting;
		public String emphasis;
		public String value;
	}

	static class Routing {
		public String solutionQueue;
	}

	static class Config {
		// TODO
	}
}
