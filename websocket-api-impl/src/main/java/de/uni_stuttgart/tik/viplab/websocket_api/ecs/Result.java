package de.uni_stuttgart.tik.viplab.websocket_api.ecs;

import java.util.List;

public class Result {
	public String ID;
	public String comment;
	/**
	 * The Status is "final" or "intermediate"
	 */
	public String status;
	public int index;
	public Computation computation;
	public Solution Solution;
	public List<Element> elements;

	public static class Computation {
		public String startTime;
		/**
		 * Format: 1111ms
		 */
		public String duration;
		public String finishTime;
		public String CC_versionLong;
		public String CC_Version;
		public String chain_version;
		public TechnicalInfo technicalInfo;
	}

	public static class TechnicalInfo {
		public String host;
		public String PID;
		public String ID;
	}

	public static class UserInfo {
		public String summary;
		public List<InfoElement> elements;
	}

	public static class InfoElement {
		/**
		 * "error" | "warning" | "info"
		 */
		public String severity;
		public String type;
		public String message;
		public Source source;
		public Output output;
	}

	public static class Source {
		public String elementID;
		/**
		 * Index is one-based
		 */
		public int line;
		/**
		 * Index is one-based
		 */
		public int col;
	}

	public static class Output {
		public String elementID;
		public String extract;
		public int begin;
		public int end;
	}

	public static class Element {
		public String identifier;
		public String name;
		public String MIMEtype;
		public String emphasis;
		public String value;
	}

	public static class Wrapper {
		public Result Result;
		
		public Wrapper() {
			
		}

		public Wrapper(Result result) {
			Result = result;
		}
	}
}
