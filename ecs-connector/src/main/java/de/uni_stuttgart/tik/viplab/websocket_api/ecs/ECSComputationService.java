package de.uni_stuttgart.tik.viplab.websocket_api.ecs;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletionStage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.JsonbBuilder;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import de.uni_stuttgart.tik.ecs.ecc.ECSDatabaseService;
import de.uni_stuttgart.tik.viplab.websocket_api.NotificationService;
import de.uni_stuttgart.tik.viplab.websocket_api.ViPLabBackendConnector;
import de.uni_stuttgart.tik.viplab.websocket_api.ecs.Result.Wrapper;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;

@ApplicationScoped
public class ECSComputationService implements ViPLabBackendConnector {

	@Inject
	@Channel("solutions")
	Emitter<Object> solutions;

	@Inject
	NotificationService notificationService;

	@Inject
	ECSMessagesConverter converter;

	@Inject
	Config config;

	private ECSDatabaseService<Exercise.Wrapper> ecsDatabaseService;

	@PostConstruct
	public void setup() {
		URI url = URI.create(config.getValue("viplab.ecs.exercises.url", String.class));
		String receiverMemberships = config.getValue("viplab.ecs.exercises.receiverMemberships", String.class);
		String username = config.getValue("viplab.ecs.exercises.username", String.class);
		String password = config.getValue("viplab.ecs.exercises.password", String.class);

		ecsDatabaseService = new ECSDatabaseService<>(url, username, password, receiverMemberships);
	}

	@Override
	public CompletionStage<String> createComputation(ComputationTemplate template, ComputationTask task) {
		Exercise exercise = this.converter.convertComputationTemplateToExercise(template);
		URI exerciseURL = createExercise(exercise);

		Solution solution = this.converter.convertComputationTaskToSolution(task, exerciseURL);
		return sendSolutions(solution).thenApply(v -> solution.ID);
	}

	@Incoming("results")
	public CompletionStage<Void> processResults(Message<InputStream> message) {
		try {
			Wrapper fromJson = JsonbBuilder.create().fromJson(message.getPayload(), Result.Wrapper.class);
			Result result = fromJson.Result;

			notificationService.notify("computation:" + result.Solution.ID, session -> {
				session.send(result);
			});
			return message.ack();
		} catch (Exception e) {
			e.printStackTrace();
			return message.ack();
		}
	}

	private URI createExercise(Exercise exercise) {
		System.out.println(Thread.currentThread().getContextClassLoader());
		return ecsDatabaseService.store(new Exercise.Wrapper(exercise));
	}

	private CompletionStage<Void> sendSolutions(Solution msg) {
		return solutions.send(new Solution.Wrapper(msg));
	}

}
