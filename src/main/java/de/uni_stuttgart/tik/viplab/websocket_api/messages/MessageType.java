package de.uni_stuttgart.tik.viplab.websocket_api.messages;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageType {
	String value();
}
