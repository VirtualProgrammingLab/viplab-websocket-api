package de.uni_stuttgart.tik.viplab.websocket_api.ecs.connector;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import de.uni_stuttgart.tik.viplab.websocket_api.ecs.ECSMessageClient;
import de.uni_stuttgart.tik.viplab.websocket_api.ecs.auth.BasicAuthenticationFilter;

/**
 * ECS Connector as defined by Microprofile Reactive Messaging Specification
 * 
 * @author Leon Kiefer
 */
@ApplicationScoped
@Connector("ecs")
public class IncomingECSConnectorFactory implements IncomingConnectorFactory {

	@Resource
	private ManagedScheduledExecutorService executor;
	
	private final List<ECSInput<Object>> inputs = Collections.synchronizedList(new ArrayList<>());

	@Override
	public PublisherBuilder<? extends Message<?>> getPublisherBuilder(Config config) {
		URI url = URI.create(config.getValue(ECSConnector.SERVER_URL, String.class));
		String username = config.getValue(ECSConnector.USERNAME, String.class);
		String password = config.getValue(ECSConnector.PASSWORD, String.class);

		ECSMessageClient ecsClient = RestClientBuilder.newBuilder().baseUri(url)
				.register(new BasicAuthenticationFilter(username, password)).build(ECSMessageClient.class);

		ECSInput<Object> ecsInput = new ECSInput<>(ecsClient, executor, Object.class);
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

}
