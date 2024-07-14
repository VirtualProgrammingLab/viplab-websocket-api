package de.uni_stuttgart.tik.viplab.websocket_api.amqp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import de.uni_stuttgart.tik.viplab.websocket_api.NotificationService;
import de.uni_stuttgart.tik.viplab.websocket_api.NotificationService.Session;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.ComputationResultMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.ErrorMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.transformation.ComputationMerger;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.inject.ConfigExtension;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.providers.MediatorFactory;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.smallrye.reactive.messaging.providers.connectors.WorkerPoolRegistry;
import io.smallrye.reactive.messaging.providers.extension.ChannelProducer;
import io.smallrye.reactive.messaging.providers.extension.EmitterFactoryImpl;
import io.smallrye.reactive.messaging.providers.extension.HealthCenter;
import io.smallrye.reactive.messaging.providers.extension.MediatorManager;
import io.smallrye.reactive.messaging.providers.extension.ReactiveMessagingExtension;
import io.smallrye.reactive.messaging.providers.impl.ConfiguredChannelFactory;
import io.smallrye.reactive.messaging.providers.impl.ConnectorFactories;
import io.smallrye.reactive.messaging.providers.impl.InternalChannelRegistry;
import io.smallrye.reactive.messaging.providers.wiring.Wiring;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@EnableWeld
class AMQPConnectorWithDumpTypeNoneTest {

  private NotificationService notificationService;

  private Logger logger;

  @TempDir
  static Path sharedTempDir;

  @Inject
  @Any
  InMemoryConnector connector;

  @BeforeEach
  public void install() {
    Map<String, String> conf = new HashMap<>();
    conf.put("mp.messaging.incoming.results.connector",
            InMemoryConnector.CONNECTOR);
    conf.put("mp.messaging.incoming.results.data",
            "not read");
    conf.put("mp.messaging.outgoing.computations.connector",
            InMemoryConnector.CONNECTOR);
    conf.put("mp.messaging.outgoing.computations.data",
            "not read");
    conf.put("mp.messaging.outgoing.preparations.connector",
            InMemoryConnector.CONNECTOR);
    conf.put("mp.messaging.outgoing.preparations.data",
            "not read");
    conf.put("viplab.amqp.dumpmessages",
            AMQPConnector.DumpType.None.toString());
    conf.put("viplab.amqp.dumpdirectory",
            sharedTempDir.toString());
    installConfig(conf);
  }

  public void installConfig(Map<String,String> configMap) {
    SmallRyeConfig config = new SmallRyeConfigBuilder()
        .withSources(KeyValuesConfigSource.config(configMap))
        .addDefaultInterceptors()
        .build();
    ConfigProviderResolver.instance()
        .releaseConfig(ConfigProvider.getConfig(AMQPConnectorWithDumpTypeAllTest.class.getClassLoader()));
    ConfigProviderResolver.instance().registerConfig(config, AMQPConnectorWithDumpTypeAllTest.class.getClassLoader());
  }

  @WeldSetup
  public WeldInitiator weld = WeldInitiator.from(AMQPConnector.class,
          MediatorFactory.class,
          MediatorManager.class,
          WorkerPoolRegistry.class,
          ExecutionHolder.class,
          InternalChannelRegistry.class,
          ChannelProducer.class,
          ConnectorFactories.class,
          ConfiguredChannelFactory.class,
          // MetricDecorator.class,
          // MicrometerDecorator.class,
          HealthCenter.class,
          Wiring.class,
          // In memory connector
          InMemoryConnector.class,
          EmitterFactoryImpl.class,
          // SmallRye config
          // io.smallrye.config.inject.ConfigProducer.class,
          ReactiveMessagingExtension.class,
          ConfigExtension.class)
          .addBeans(createLogger())
          .addBeans(MockBean.of(Mockito.mock(ComputationMerger.class),
                  ComputationMerger.class))
          .addBeans(createNotificationService())
          .addBeans(MockBean.of(produceValidator(),
                  Validator.class))
          .build();

  public MockBean<?> createNotificationService() {
    notificationService = Mockito.mock(NotificationService.class);
    return MockBean.builder()
            .addType(NotificationService.class)
            .addQualifier(Any.Literal.INSTANCE)
            .creating(notificationService)
            .build();
  }

  public MockBean<?> createLogger() {
    logger = Mockito.mock(Logger.class);
    return MockBean.builder()
            .addType(Logger.class)
            .addQualifier(Any.Literal.INSTANCE)
            .creating(logger)
            .build();
  }

  public Validator produceValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    return factory.getValidator();
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCompleteResultJSON(AMQPConnector connector) throws NoSuchMethodException, SecurityException, IOException {
    String messagePayload = "{\"computation\":\"asdfasfd\",\"identifier\":\"a-random-uuid\"}";
    Message<String> message = Message.of(messagePayload);
    connector.processResults(message);
    ArgumentCaptor<Consumer<Session>> sessionConsumerCaptor = ArgumentCaptor.forClass(Consumer.class);
    verify(notificationService,
            times(1)).notify(eq("computation:asdfasfd"),
                    sessionConsumerCaptor.capture());
    Consumer<Session> sessionConsumer = sessionConsumerCaptor.getValue();
    Session session = Mockito.mock(Session.class);
    sessionConsumer.accept(session);
    Method sendMethod = mockingDetails(session).getMockHandler()
            .getMockSettings()
            .getTypeToMock()
            .getMethod("send",
                    Object.class);

    mockingDetails(session).getInvocations()
            .stream()
            .filter(invocation -> invocation.getMethod()
                    .equals(sendMethod))
            .forEach(parameters -> {
              Object[] args = parameters.getArguments();
              // TODO add proper testing code for the value of the message
            });
    verify(session).send(Mockito.isA(ComputationResultMessage.class));
    long fileCount = -1;
    try (Stream<Path> stream = Files.list(sharedTempDir)) {
      fileCount = stream.filter(file -> !Files.isDirectory(file))
              .count();
    }
    assertEquals(0,
            fileCount);
    verify(logger,
            never()).error(any());
    verify(logger,
            never()).warn(any());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testIncompleteResultJSON(AMQPConnector connector) throws NoSuchMethodException, SecurityException, IOException {
    String messagePayload = "{\"computation\":\"asdfasfd\"}";
    Message<String> message = Message.of(messagePayload);
    connector.processResults(message);
    ArgumentCaptor<Consumer<Session>> sessionConsumerCaptor = ArgumentCaptor.forClass(Consumer.class);
    verify(notificationService,
            times(1)).notify(eq("computation:asdfasfd"),
                    sessionConsumerCaptor.capture());
    Consumer<Session> sessionConsumer = sessionConsumerCaptor.getValue();
    Session session = Mockito.mock(Session.class);
    sessionConsumer.accept(session);
    Method sendMethod = mockingDetails(session).getMockHandler()
            .getMockSettings()
            .getTypeToMock()
            .getMethod("send",
                    Object.class);

    mockingDetails(session).getInvocations()
            .stream()
            .filter(invocation -> invocation.getMethod()
                    .equals(sendMethod))
            .forEach(parameters -> {
              Object[] args = parameters.getArguments();
              // TODO add proper testing code for the value of the message
            });
    verify(session).send(Mockito.isA(ErrorMessage.class));
    long fileCount = -1;
    try (Stream<Path> stream = Files.list(sharedTempDir)) {
      fileCount = stream.filter(file -> !Files.isDirectory(file))
              .count();
    }
    assertEquals(0,
            fileCount);
    verify(logger,
            never()).error(any());
    verify(logger,
            never()).warn(any());
  }

  @Test
  void testValidJSON(AMQPConnector connector) throws NoSuchMethodException, SecurityException, IOException {
    String messagePayload = "{\"akey\":\"asdfasfd\"}";
    Message<String> message = Message.of(messagePayload);
    connector.init();
    connector.processResults(message);

    verify(notificationService,
            never()).notify(null,
                    null);// notify(eq("computation:asdfasfd"),
    long fileCount = -1;
    try (Stream<Path> stream = Files.list(sharedTempDir)) {
      fileCount = stream.filter(file -> !Files.isDirectory(file))
              .count();
    }
    assertEquals(0,
            fileCount);
  }

  @Test
  void testInvalidJSON(AMQPConnector connector) throws NoSuchMethodException, SecurityException, IOException {
    String messagePayload = "{\"akeyasdfasfd\"}";
    Message<String> message = Message.of(messagePayload);
    connector.processResults(message);

    verify(logger,
            times(1)).error(eq("Invalid json received: {}"),
                    any(UUID.class));
    long fileCount = -1;
    try (Stream<Path> stream = Files.list(sharedTempDir)) {
      fileCount = stream.filter(file -> !Files.isDirectory(file))
              .count();
    }
    assertEquals(0,
            fileCount);
  }

}
