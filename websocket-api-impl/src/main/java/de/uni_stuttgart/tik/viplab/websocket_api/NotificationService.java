package de.uni_stuttgart.tik.viplab.websocket_api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;

@ApplicationScoped
public class NotificationService {
	private ConcurrentHashMap<String, Set<Session>> subscriptions = new ConcurrentHashMap<>();
	
	@Gauge(unit = MetricUnits.NONE)
	public int getTopicCount() {
		return subscriptions.size();
	}

	public void subscribe(String topic, Session session) {
		subscriptions.compute(topic, (t, sessions) -> {
			if (sessions == null) {
				sessions = new HashSet<Session>();
			}
			sessions.add(session);
			return sessions;
		});
	}

	public void unsubscribe(String topic, Session session) {
		subscriptions.compute(topic, (t, sessions) -> {
			if (sessions != null) {
				sessions.remove(session);
				if (sessions.isEmpty()) {
					sessions = null;
				}
			}
			return sessions;
		});
	}

	public void notify(String topic, Consumer<Session> action) {
		subscriptions.getOrDefault(topic, Collections.emptySet()).forEach(action);
	}
}
