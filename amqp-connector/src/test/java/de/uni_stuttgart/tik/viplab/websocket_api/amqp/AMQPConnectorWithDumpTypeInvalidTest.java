package de.uni_stuttgart.tik.viplab.websocket_api.amqp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.ConnectorLiteral;
import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;

import de.uni_stuttgart.tik.viplab.websocket_api.NotificationService;
import de.uni_stuttgart.tik.viplab.websocket_api.NotificationService.Session;
import de.uni_stuttgart.tik.viplab.websocket_api.amqp.AMQPConnector.DumpType;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.ComputationResultMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.messages.ErrorMessage;
import de.uni_stuttgart.tik.viplab.websocket_api.transformation.ComputationMerger;
import io.smallrye.config.SmallRyeConfigProviderResolver;
import io.smallrye.config.inject.ConfigExtension;
import io.smallrye.config.inject.ConfigProducer;
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
import io.smallrye.reactive.messaging.providers.metrics.MetricDecorator;
import io.smallrye.reactive.messaging.providers.metrics.MicrometerDecorator;
import io.smallrye.reactive.messaging.providers.wiring.Wiring;
import io.smallrye.reactive.messaging.test.common.config.MapBasedConfig;

@EnableWeld
class AMQPConnectorWithDumpTypeInvalidTest {

  private NotificationService notificationService;

  private Logger logger;

  @TempDir
  static Path sharedTempDir;

  @Inject
  @Any
  InMemoryConnector connector;

  @BeforeEach
  public void install() {
    Map<String, Object> conf = new HashMap<>();
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
            AMQPConnector.DumpType.Invalid.toString());
    conf.put("viplab.amqp.dumpdirectory",
            sharedTempDir.toString());
    installConfig(new MapBasedConfig(conf));
  }

  public static void releaseConfig() {
    SmallRyeConfigProviderResolver.instance()
            .releaseConfig(ConfigProvider.getConfig(AMQPConnectorWithDumpTypeInvalidTest.class.getClassLoader()));
    clearConfigFile();
  }

  private static void clearConfigFile() {
    File out = new File("target/test-classes/META-INF/microprofile-config.properties");
    if (out.isFile()) {
      out.delete();
    }
  }

  public static void installConfig(MapBasedConfig config) {
    releaseConfig();
    if (config != null) {
      config.write();
    } else {
      clearConfigFile();
    }
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
          // .addBeans(MockBean.of(DumpType.Invalid,
          // DumpType.class))
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
    Message<String> message = Message.of("{\"computation\":\"asdfasfd\",\"identifier\":\"a-random-uuid\"}");
    System.out.println(this.connector);
    InMemoryConnector bean = weld.getBeanManager()
            .createInstance()
            .select(InMemoryConnector.class,
                    ConnectorLiteral.of(InMemoryConnector.CONNECTOR))
            .get();
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
    List<Path> files = null;
    try (Stream<Path> stream = Files.list(sharedTempDir)) {
      files = stream.filter(file -> !Files.isDirectory(file))
              .collect(Collectors.toList());
    }
    assertEquals(1,
            files.size());
    assertEquals(messagePayload,
            Files.readString(files.get(0)));
    assertEquals(true,
            Files.deleteIfExists(files.get(0)));
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
    List<Path> files = null;
    try (Stream<Path> stream = Files.list(sharedTempDir)) {
      files = stream.filter(file -> !Files.isDirectory(file))
              .collect(Collectors.toList());
    }
    assertEquals(1,
            files.size());
    assertEquals(messagePayload,
            Files.readString(files.get(0)));
    assertEquals(true,
            Files.deleteIfExists(files.get(0)));
  }

  @Test
  void testInvalidJSON(AMQPConnector connector) throws NoSuchMethodException, SecurityException, IOException {
    String messagePayload = "{\"akeyasdfasfd\"}";
    Message<String> message = Message.of(messagePayload);
    connector.processResults(message);

    verify(logger,
            times(1)).error(eq("Invalid json received: {}"),
                    any(String.class));
    List<Path> files = null;
    try (Stream<Path> stream = Files.list(sharedTempDir)) {
      files = stream.filter(file -> !Files.isDirectory(file))
              .collect(Collectors.toList());
    }
    assertEquals(1,
            files.size());
    assertEquals(messagePayload,
            Files.readString(files.get(0)));
    assertEquals(true,
            Files.deleteIfExists(files.get(0)));
  }

}
