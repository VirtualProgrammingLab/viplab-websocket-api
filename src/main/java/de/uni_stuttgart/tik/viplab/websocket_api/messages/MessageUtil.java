package de.uni_stuttgart.tik.viplab.websocket_api.messages;

public class MessageUtil {
	public static String getTypeOfMessageObject(Object message) {
		if (!message.getClass().isAnnotationPresent(MessageType.class)) {
			throw new IllegalArgumentException("The class has no MessageType: " + message.getClass().getName());
		}
		return message.getClass().getAnnotation(MessageType.class).value();
	}
}
