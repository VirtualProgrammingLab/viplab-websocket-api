package de.uni_stuttgart.tik.viplab.websocket_api.model;

import java.util.Collections;
import java.util.List;

public class ComputationResult {
	/**
	 * The identifier of corresponding Computation
	 */
	
	public String computation;

	public String status;
	
	public String timestamp;
	
	public Notification notifications;

	public Output output;

	public List<File> files = Collections.emptyList();
	
	public static class Notification {
		public String summary;
		
		public List<Element> elements = Collections.emptyList();
		
		public static class Element {
			public String severity;
			public String type;
			public String message; 
			
		}
	}
	
	public static class Output {
		public String stdout;
		public String stderr;
	}
	
	public static class File {
		public String identifier;
		public String path;
		public String MIMEtype;
		public String content;
	}
}
