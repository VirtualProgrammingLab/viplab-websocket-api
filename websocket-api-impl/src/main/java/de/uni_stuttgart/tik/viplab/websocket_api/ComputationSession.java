package de.uni_stuttgart.tik.viplab.websocket_api;

import javax.websocket.Session;

import com.auth0.jwt.interfaces.DecodedJWT;

public class ComputationSession {
	public static final String SESSION_JWT = "session.jwt";
	public static void mustBeAuthenticated(Session session) {
		if (!session.getUserProperties().containsKey(SESSION_JWT)) {
			throw new IllegalStateException("Not authenticated");
		}
	}
	
	public static DecodedJWT getJWT(Session session) {
		return (DecodedJWT) session.getUserProperties().get(SESSION_JWT);
	}
	
	

}
