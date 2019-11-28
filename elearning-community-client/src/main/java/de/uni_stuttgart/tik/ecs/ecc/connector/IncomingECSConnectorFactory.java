package de.uni_stuttgart.tik.ecs.ecc.connector;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import de.uni_stuttgart.tik.ecs.ecc.ECSMessageClient;
import de.uni_stuttgart.tik.ecs.ecc.auth.BasicAuthenticationFilter;

/**
 * ECS Connector as defined by Microprofile Reactive Messaging Specification
 * 
 * @author Leon Kiefer
 */
@Liveness
@ApplicationScoped
@Connector("ecs")
public class IncomingECSConnectorFactory implements IncomingConnectorFactory, HealthCheck {

	@Resource
	private ManagedScheduledExecutorService executor;

	private final List<ECSInput<Object>> inputs = Collections.synchronizedList(new ArrayList<>());

	@Override
	public PublisherBuilder<? extends Message<?>> getPublisherBuilder(Config config) {
		URI url = URI.create(config.getValue(ConnectorConfig.SERVER_URL, String.class));
		String username = config.getValue(ConnectorConfig.USERNAME, String.class);
		String password = config.getValue(ConnectorConfig.PASSWORD, String.class);
		long pollingRate = config.getOptionalValue(ConnectorConfig.POLLING_DELAY, Long.class).orElse(1000l);

		ECSMessageClient ecsClient = RestClientBuilder.newBuilder().baseUri(url)
				.register(new BasicAuthenticationFilter(username, password)).build(ECSMessageClient.class);

		ECSInput<Object> ecsInput = new ECSInput<>(ecsClient, executor, Object.class, pollingRate);
		inputs.add(ecsInput);
		return ecsInput.getPublisher();
	}

	@PreDestroy
	private void shutdown() {
		synchronized (inputs) {
			for (ECSInput<Object> input : inputs) {
				try {
					input.shutdown();
				} catch (Exception e) {
					// Ensures we attempt to shutdown all inputs
					// and also that we get an FFDC for any errors
				}
			}
		}
	}

	@Override
	public HealthCheckResponse call() {
		boolean running = true;
		for (ECSInput<Object> input : inputs) {
			running &= input.isRunning();
		}
		return HealthCheckResponse.named("ECS Polling connections").state(running).build();
	}

}
