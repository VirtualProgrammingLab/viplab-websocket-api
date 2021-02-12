package de.uni_stuttgart.tik.viplab.websocket_api.amqp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.JsonbException;
import javax.validation.Validator;

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
  private DumpType dumpMessages;
  @Inject
  @ConfigProperty(name = "viplab.amqp.dumpdirectory")
  private Optional<String> dumpDirectoryConfig;

  private Jsonb jsonb;

  private File dumpDirectory;

  @PostConstruct
  public void init() {
    JsonbConfig config = new JsonbConfig().withDeserializers(new ArtifactDeserializer());
    jsonb = JsonbBuilder.create(config);
    if (DumpType.Invalid == dumpMessages || DumpType.All == dumpMessages) {
      if (dumpDirectoryConfig.isPresent()) {
        dumpDirectory = new File(dumpDirectoryConfig.get());
        if (!dumpDirectory.isDirectory()) {
          logger.error("Dumpdirectory {} is not a directory, dumping is being disabled");
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
            .thenApply(v -> {
              return computation.identifier;
            });
  }

  @Incoming("results")
  public CompletionStage<Void> processResults(Message<String> message) {
    try {
      UUID uuid = UUID.randomUUID();
      ComputationResult result = jsonb.fromJson(message.getPayload(),
              ComputationResult.class);
      if (validator.validate(result)
              .isEmpty()) {
        ComputationResultMessage resultMessage = new ComputationResultMessage(result);
        notificationService.notify("computation:" + result.computation,
                session -> {
                  session.send(resultMessage);
                });
        logger.debug("Send result message(result id:{} for computation {}: {}",
                result.identifier,
                result.computation,
                uuid.toString());
        if (DumpType.All == dumpMessages) {
          dumpMessage(uuid.toString(),
                  message);
        }
      } else {
        if (StringUtils.isNotBlank(result.computation)) {
          ErrorMessage em = new ErrorMessage();
          em.message = message.getPayload();
          // now: report error back using the notification service
          notificationService.notify("computation:" + result.computation,
                  session -> {
                    session.send(em);
                  });
          logger.error("Error validating result object, but found computationid {}: {}",
                  result.computation,
                  uuid.toString());
        }
        if (DumpType.Invalid == dumpMessages || DumpType.All == dumpMessages) {
          dumpMessage(uuid.toString(),
                  message);
        }
      }
    } catch (JsonbException ex) {
      UUID uuid = UUID.randomUUID();
      logger.error("Invalid json received: {}",
              uuid.toString());
      if (DumpType.Invalid == dumpMessages || DumpType.All == dumpMessages) {
        dumpMessage(uuid.toString(),
                message);
      }
    }
    return message.ack();
  }

  private void dumpMessage(String uuid, Message<String> message) {
    File outfile = new File(dumpDirectory, uuid + ".message");
    FileWriter fw = null;
    try {
      fw = new FileWriter(outfile);
      fw.write(message.getPayload());
    } catch (IOException e) {
      logger.warn("Error writing message to dumpfile");
    } finally {
      if (fw != null) {
        try {
          fw.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
