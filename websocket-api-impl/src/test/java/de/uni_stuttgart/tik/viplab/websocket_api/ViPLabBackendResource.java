package de.uni_stuttgart.tik.viplab.websocket_api;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.providers.connectors.InMemoryConnector;

/**
 * Use the in-memory connector to avoid having to use a broker.
 */
public class ViPLabBackendResource implements QuarkusTestResourceLifecycleManager {

	@Override
	public Map<String, String> start() {
		Map<String, String> env = new HashMap<>();
		env.putAll(InMemoryConnector.switchOutgoingChannelsToInMemory("computations"));
		return env;
	}

	@Override
	public void stop() {
		InMemoryConnector.clear();
	}
}
