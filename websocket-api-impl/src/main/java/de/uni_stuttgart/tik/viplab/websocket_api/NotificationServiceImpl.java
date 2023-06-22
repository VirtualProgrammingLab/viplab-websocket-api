package de.uni_stuttgart.tik.viplab.websocket_api;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Timed;

import io.quarkus.scheduler.Scheduled;

/**
 * This NotificationService handles WebSocket sessions and make it possible to
 * subscribe to events on a per session basis. This implementation is
 * Thread-safe.
 */
@ApplicationScoped
public class NotificationServiceImpl implements NotificationService {
	private ConcurrentHashMap<String, Set<Session>> subscriptions = new ConcurrentHashMap<>();

	@Gauge(name= "topics-count", unit = MetricUnits.NONE)
	public int getTopicCount() {
		return subscriptions.size();
	}

	@Scheduled(every = "60s")
	@Timed(name = "subscriptions-cleanup")
	protected void cleanUpSubscriptions() {
		this.subscriptions.forEach((topic, sessions) -> sessions.stream().filter(session -> !session.isOpen())
				.forEach(session -> this.unsubscribe(topic, session)));
	}

	@Override
	public void subscribe(String topic, Session session) {
		subscriptions.compute(topic, (t, sessions) -> {
			if (sessions == null) {
				sessions = new CopyOnWriteArraySet<Session>();
			}
			sessions.add(session);
			return sessions;
		});
	}

	@Override
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

	@Override
	public void notify(String topic, Consumer<Session> action) {
		subscriptions.getOrDefault(topic, Collections.emptySet()).forEach(action);
	}
}
