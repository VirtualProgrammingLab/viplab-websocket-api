package de.uni_stuttgart.tik.viplab.websocket_api;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Timed;

/**
 * This NotificationService handles WebSocket sessions and make it possible to
 * subscribe to events on a per session basis. This implementation is
 * Thread-safe.
 */
@ApplicationScoped
public class NotificationService {
	private ConcurrentHashMap<String, Set<Session>> subscriptions = new ConcurrentHashMap<>();

	@Resource
	private ScheduledExecutorService executorService;

	@Gauge(name= "topics-count", unit = MetricUnits.NONE)
	public int getTopicCount() {
		return subscriptions.size();
	}

	@PostConstruct
	private void startCleanupTask() {
		executorService.scheduleAtFixedRate(this::cleanUpSubscriptions, 0, 1, TimeUnit.MINUTES);
	}

	@Timed(name = "subscriptions-cleanup")
	protected void cleanUpSubscriptions() {
		this.subscriptions.forEach((topic, sessions) -> sessions.stream().filter(session -> !session.isOpen())
				.forEach(session -> this.unsubscribe(topic, session)));
	}

	public void subscribe(String topic, Session session) {
		subscriptions.compute(topic, (t, sessions) -> {
			if (sessions == null) {
				sessions = new CopyOnWriteArraySet<Session>();
			}
			sessions.add(session);
			return sessions;
		});
	}

	/**
	 * Subscribe the session the the given topic. This method is idempotent.
	 * 
	 * @param topic
	 *            the topic of the subscription, topics can be structured
	 *            hierarchical by separating the levels using a {@code :}.
	 * @param session
	 *            the WebSocket Session.
	 */
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
