package de.uni_stuttgart.tik.viplab.websocket_api.amqp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;
import jakarta.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;

import de.uni_stuttgart.tik.viplab.websocket_api.NotificationService;
import de.uni_stuttgart.tik.viplab.websocket_api.ViPLabBackendConnector;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.ComputationResultMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.ErrorMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.model.Computation;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationResult;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;
import de.uni_stuttgart.tik.viplab.websocket_api.transformation.ComputationMerger;

@ApplicationScoped
public class AMQPConnector implements ViPLabBackendConnector {

  enum DumpType {
    None, Invalid, All
  }

  @Inject
  @Channel("preparations")
  Emitter<String> preparations;

  @Inject
  @Channel("computations")
  Emitter<String> computations;

  @Inject
  NotificationService notificationService;

  @Inject
  ComputationMerger merger;

  @Inject
  Validator validator;

  @Inject
  Logger logger;

  @Inject
  @ConfigProperty(name = "viplab.amqp.dumpmessages", defaultValue = "None")
  DumpType dumpMessages;
  @Inject
  @ConfigProperty(name = "viplab.amqp.dumpdirectory")
  Optional<String> dumpDirectoryConfig;

  private Jsonb jsonb;

  private Path dumpDirectory;

  @PostConstruct
  public void init() {
    JsonbConfig config = new JsonbConfig().withDeserializers(new ArtifactDeserializer());
    jsonb = JsonbBuilder.create(config);
    if (DumpType.Invalid == dumpMessages || DumpType.All == dumpMessages) {
      if (dumpDirectoryConfig.isPresent()) {
        dumpDirectory = Paths.get(dumpDirectoryConfig.get());
        if (!Files.isDirectory(dumpDirectory)) {
          logger.error("Dumpdirectory {} is not a directory, dumping is being disabled", dumpDirectory.toAbsolutePath());
          dumpMessages = DumpType.None;
        }
      } else {
        logger.warn("viplab.amqp.dumpdirectory is not defined. Not enabling dumping");
        dumpMessages = DumpType.None;
      }
    }
  }

  @Override
  public CompletionStage<String> createComputation(ComputationTemplate template, ComputationTask task) {
    Computation computation = merger.merge(template,
            task);
    String computationJson = jsonb.toJson(computation);
    return computations.send(computationJson)
            .thenApply(v -> computation.identifier);
  }

  @Override
  public CompletionStage<String> prepareComputation(ComputationTemplate template) {
    String templateJson = jsonb.toJson(template);
    return preparations.send(templateJson)
            .thenApply(v -> template.identifier);
  }

  @Incoming("results")
  public CompletionStage<Void> processResults(Message<String> message) {
    try {
      ComputationResult result = jsonb.fromJson(message.getPayload(),
              ComputationResult.class);
      if (validator.validate(result)
              .isEmpty()) {
        ComputationResultMessage resultMessage = new ComputationResultMessage(result);
        notificationService.notify("computation:" + result.computation,
                session -> session.send(resultMessage));
        if (DumpType.All == dumpMessages) {
          UUID uuid = UUID.randomUUID();
          logger.debug("Send result message(result id:{} for computation {}. Content-id: {})",
                  result.identifier,
                  result.computation,
                  uuid);
          dumpMessage(uuid.toString(),
                  message);
        } else {
          logger.debug("Send result message(result id:{} for computation {})",
                  result.identifier,
                  result.computation);
        }

      } else {
        String computationId;
        if (StringUtils.isNotBlank(result.computation)) {
          ErrorMessage em = new ErrorMessage();
          em.message = message.getPayload();
          // now: report error back using the notification service
          notificationService.notify("computation:" + result.computation,
                  session -> session.send(em));
          computationId = result.computation;
        } else {
          computationId = "__unset__";
        }
        if (DumpType.Invalid == dumpMessages || DumpType.All == dumpMessages) {
          UUID uuid = UUID.randomUUID();
          logger.error("Error validating result object, but found computationid {}. Content-id: {}",
                  computationId,
                  uuid);
          dumpMessage(uuid.toString(),
                  message);
        } else {
          logger.error("Error validating result object. Computation-Id: {}",
                  computationId);
        }

      }
    } catch (JsonbException ex) {
      UUID uuid = UUID.randomUUID();
      logger.error("Invalid json received: {}",
              uuid);
      if (DumpType.Invalid == dumpMessages || DumpType.All == dumpMessages) {
        dumpMessage(uuid.toString(),
                message);
      }
    }
    return message.ack();
  }

  private void dumpMessage(String uuid, Message<String> message) {
    Path outfile = dumpDirectory.resolve(uuid + ".message");
    BufferedWriter writer = null;
    try {
      writer = Files.newBufferedWriter(outfile,
              StandardCharsets.UTF_8);
      writer.write(message.getPayload());
    } catch (IOException e) {
      logger.warn("Error writing message to dumpfile");
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
