package de.uni_stuttgart.tik.viplab.websocket_api;

import de.uni_stuttgart.tik.viplab.websocket_api.NotificationService.Session;

public class WebsocketSessionWrapper implements Session {

	private final jakarta.websocket.Session session;

	public WebsocketSessionWrapper(jakarta.websocket.Session session) {
		this.session = session;
	}

	@Override
	public void send(Object message) {
		ComputationWebSocket.send(message, session);
	}

	@Override
	public boolean isOpen() {
		return this.session.isOpen();
	}

}
