package de.uni_stuttgart.tik.viplab.websocket_api;

/**
 * An WebSocket Server exception that is send to the WebSocket Client if thrown
 * while processing a message of the client. The Exception represent an
 * exception that is caused by the client request.
 */
public class ComputationWebsocketException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4562063538748775210L;

	public ComputationWebsocketException(String message) {
		super(message);
	}

	public ComputationWebsocketException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
