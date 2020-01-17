package de.uni_stuttgart.tik.viplab.websocket_api.ecs;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.stream.Stream;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

class ECSMessagesConverterTest {

	private ECSMessagesConverter sut;

	private Jsonb jsonb;

	@BeforeEach
	private void setup() {
		sut = new ECSMessagesConverter();
		sut.setClock(Clock.fixed(Instant.parse("2009-09-16T12:46:48.52Z"), ZoneId.of("UTC")));
		jsonb = JsonbBuilder.create();
	}

	@ParameterizedTest
	@MethodSource("exampleTaskJsonProvider")
	void testTaskTransformation(String taskJson, String solutionJson) {
		ComputationTask task = jsonb.fromJson(taskJson, ComputationTask.class);
		URI exerciseURL = URI.create("https://ecs.example.com/exercise/12345");
		Solution solution = sut.convertComputationTaskToSolution(task, exerciseURL);
		String actualSolutionJson = jsonb.toJson(new Solution.Wrapper(solution));
		assertThat(actualSolutionJson, SameJSONAs.sameJSONAs(solutionJson));
	}

	@ParameterizedTest
	@MethodSource("exampleTemplateJsonProvider")
	void testTemplateTransformation(String templateJson, String exerciseJson) {
		ComputationTemplate template = jsonb.fromJson(templateJson, ComputationTemplate.class);
		Exercise exercise = sut.convertComputationTemplateToExercise(template);
		String actualExerciseJson = jsonb.toJson(new Exercise.Wrapper(exercise));
		assertThat(actualExerciseJson, SameJSONAs.sameJSONAs(exerciseJson));
	}

	private static String loadFile(String fileName) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				ECSMessagesConverterTest.class.getResourceAsStream(fileName), StandardCharsets.UTF_8))) {
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

	private static Stream<Arguments> exampleTemplateJsonProvider() {
		return Stream.of("C.check.ex.tp", "Java.ff_10.ex", "C.complex.ex").map(fileName -> {
			return Arguments.of(loadFile(fileName + ".computation-template.json"), loadFile(fileName + ".json"));
		});
	}

	private static Stream<Arguments> exampleTaskJsonProvider() {
		return Stream.of("C.huge.solution", "matlab.ff_1a.solution", "generic.noModifications.solution")
				.map(fileName -> {
					return Arguments.of(loadFile(fileName + ".computation-task.json"), loadFile(fileName + ".json"));
				});
	}
}
