package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.uni_stuttgart.tik.viplab.websocket_api.model.Computation;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import io.quarkus.test.junit.QuarkusTest;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

@QuarkusTest
class ComputationMergerImplTest {

	@Inject
	ComputationMerger sut;

	private Jsonb jsonb = JsonbBuilder.create();

	@ParameterizedTest
	@MethodSource("exampleJsonProvider")
	void testMerge(String taskJson, String templateJson, String computationJson) {
		ComputationTemplate template = jsonb.fromJson(templateJson, ComputationTemplate.class);
		ComputationTask task = jsonb.fromJson(taskJson, ComputationTask.class);

		Computation actualComputation = sut.merge(template, task);

		assertNotNull(actualComputation.identifier);
		actualComputation.identifier = "5032ad45-5d16-45b5-8bf7-44fc60b12edf";
		String actualSolutionJson = jsonb.toJson(actualComputation);
		assertThat(actualSolutionJson, SameJSONAs.sameJSONAs(computationJson));
	}

	private static String loadFile(String fileName) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				ComputationMergerImplTest.class.getResourceAsStream(fileName), StandardCharsets.UTF_8))) {
			StringBuilder builder = new StringBuilder();
			int c = 0;
			while ((c = reader.read()) != -1) {
				builder.append((char) c);
			}
			return builder.toString();
		} catch (IOException e) {
			throw new IllegalArgumentException(fileName, e);
		}
	}

	private static Stream<Arguments> exampleJsonProvider() {
		return Stream.of("minimal", "simple").map(fileName -> {
			return Arguments.of(loadFile(fileName + ".computation-task.json"),
					loadFile(fileName + ".computation-template.json"), loadFile(fileName + ".computation.json"));
		});
	}
}
