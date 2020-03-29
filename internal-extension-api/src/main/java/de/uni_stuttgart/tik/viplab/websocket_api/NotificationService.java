package de.uni_stuttgart.tik.viplab.websocket_api;

import java.util.function.Consumer;

public interface NotificationService {

	void subscribe(String topic, Session session);

	/**
	 * Subscribe the session the the given topic. This method is idempotent.
	 * 
	 * @param topic
	 *            the topic of the subscription, topics can be structured
	 *            hierarchical by separating the levels using a {@code :}.
	 * @param session
	 *            the WebSocket Session.
	 */
	void unsubscribe(String topic, Session session);

	void notify(String topic, Consumer<Session> action);

	public static interface Session {
		public void send(Object message);
		
		public boolean isOpen();
	}

}
