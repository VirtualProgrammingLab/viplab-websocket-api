package de.uni_stuttgart.tik.viplab.websocket_api;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.annotation.Timed;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;

/**
 * This NotificationService handles WebSocket sessions and make it possible to
 * subscribe to events on a per session basis. This implementation is
 * Thread-safe.
 */
@ApplicationScoped
public class NotificationServiceImpl implements NotificationService {
	private ConcurrentHashMap<String, Set<Session>> subscriptions = new ConcurrentHashMap<>();

	@Inject
	MeterRegistry registry;

	@PostConstruct
	public void init() {
		Gauge.builder("topics-count", this, NotificationServiceImpl::getTopicCount)
				.description("Number of subscriptions")
				.register(registry);
	}

	public int getTopicCount() {
		return subscriptions.size();
	}

	@Scheduled(every = "60s")
	@Timed(value = "subscriptions-cleanup")
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
