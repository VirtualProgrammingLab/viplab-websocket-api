package de.uni_stuttgart.tik.viplab.websocket_api.model;

import java.util.Collections;
import java.util.List;

public class ComputationResult {

	public String identifier;

	public String version;

	/**
	 * The identifier of corresponding Computation
	 */

	public String computation;

	public String status;

	public String timestamp;

	public Output output;

	public List<Artifact> artifacts = Collections.emptyList();


	public static class Output {
		public String stdout;
		public String stderr;
	}

	public static class Artifact {

		enum TYPE {
			notifications,
			file,
			s3file
		}

		public String identifier;
		public TYPE type;
	}

	public static class Notifications extends Artifact{

		public Notifications() {
			type = TYPE.notifications;
		}

		public String summary;

		public List<Notification> notifications = Collections.emptyList();

		public static class Notification {
			public String severity;
			public String type;
			public String message;
		}
	}

	public static class File extends Artifact{

		public File() {
			type = TYPE.file;
		}

		public String path;
		public String MIMEtype;
		public String content;
	}
}
