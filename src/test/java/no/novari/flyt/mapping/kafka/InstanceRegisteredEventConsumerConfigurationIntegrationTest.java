package no.novari.flyt.mapping.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.novari.Application;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeadersMapper;
import no.novari.flyt.mapping.InstanceProcessingService;
import no.novari.flyt.mapping.kafka.error.InstanceMappingErrorEventProducerService;
import no.novari.flyt.mapping.model.instance.InstanceObject;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService;
import no.novari.kafka.topic.name.EventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import no.novari.kafka.topic.name.TopicNameService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        classes = Application.class,
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "fint.application-id=test-mapping-service",
                "fint.org-id=test-org",
                "novari.kafka.topic.org-id=test-org",
                "novari.kafka.topic.domain-context=test-domain",
                "novari.kafka.default-replicas=1",
                "novari.flyt.mapping-service.kafka.consumer.instance-processing-backoff.initial-interval=200ms",
                "novari.flyt.mapping-service.kafka.consumer.instance-processing-backoff.multiplier=1.5",
                "novari.flyt.mapping-service.kafka.consumer.instance-processing-backoff.max-interval=2s",
                "novari.flyt.mapping-service.kafka.consumer.instance-processing-backoff.max-retries=2",
                "logging.level.kafka=ERROR",
                "logging.level.org.apache.kafka=ERROR",
                "logging.level.org.apache.zookeeper=ERROR",
                "logging.level.org.springframework.kafka=ERROR"
        }
)
@EmbeddedKafka(
        partitions = 1,
        topics = {"test-org.test-domain.event.instance-registered"}
)
@Import(InstanceRegisteredEventConsumerConfigurationIntegrationTest.TestKafkaProducerConfiguration.class)
class InstanceRegisteredEventConsumerConfigurationIntegrationTest {

    private static final String EVENT_NAME = "instance-registered";

    @Autowired
    @Qualifier("testKafkaTemplate")
    private KafkaTemplate<String, InstanceObject> kafkaTemplate;

    @Autowired
    private InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    @Autowired
    private TopicNameService topicNameService;

    @Autowired
    @Qualifier("instanceRegisteredEventConsumer")
    private ConcurrentMessageListenerContainer<String, InstanceObject> instanceRegisteredEventConsumer;

    @MockitoBean
    private InstanceProcessingService instanceProcessingService;

    @MockitoBean
    private InstanceMappingErrorEventProducerService instanceMappingErrorEventProducerService;

    @MockitoBean
    private SourceApplicationAuthorizationRequestService sourceApplicationAuthorizationRequestService;

    @BeforeEach
    void setUp() {
        ContainerTestUtils.waitForAssignment(instanceRegisteredEventConsumer, 1);
    }

    @Test
    void shouldApplyExponentialBackoffAndRecover() {
        doThrow(new RuntimeException("boom"))
                .when(instanceProcessingService)
                .process(any());

        InstanceFlowHeaders instanceFlowHeaders = InstanceFlowHeaders
                .builder()
                .sourceApplicationId(1L)
                .correlationId(UUID.randomUUID())
                .integrationId(123L)
                .build();

        sendInstanceRegisteredEvent(instanceFlowHeaders);

        verify(instanceMappingErrorEventProducerService, timeout(2000))
                .publishGeneralSystemErrorEvent(eq(instanceFlowHeaders));

        assertThat(instanceRegisteredEventConsumer.getCommonErrorHandler())
                .isInstanceOf(DefaultErrorHandler.class);

        DefaultErrorHandler errorHandler =
                (DefaultErrorHandler) instanceRegisteredEventConsumer.getCommonErrorHandler();
        assertThat(errorHandler).isNotNull();
        Object failureTracker = ReflectionTestUtils.invokeMethod(
                Objects.requireNonNull(errorHandler),
                "getFailureTracker"
        );
        assertThat(failureTracker).isNotNull();
        Object backOff = ReflectionTestUtils.getField(
                Objects.requireNonNull(failureTracker),
                "backOff"
        );
        assertThat(backOff).isNotNull();

        assertThat(backOff).isInstanceOf(ExponentialBackOffWithMaxRetries.class);

        ExponentialBackOffWithMaxRetries exponentialBackOff =
                (ExponentialBackOffWithMaxRetries) backOff;
        assertThat(exponentialBackOff.getInitialInterval()).isEqualTo(200L);
        assertThat(exponentialBackOff.getMultiplier()).isEqualTo(1.5);
        assertThat(exponentialBackOff.getMaxInterval()).isEqualTo(2_000L);
        assertThat(exponentialBackOff.getMaxRetries()).isEqualTo(2);
    }

    private void sendInstanceRegisteredEvent(InstanceFlowHeaders instanceFlowHeaders) {
        InstanceObject instanceObject = InstanceObject
                .builder()
                .valuePerKey(Map.of("key", "value"))
                .build();

        String topicName = topicNameService.validateAndMapToTopicName(
                EventTopicNameParameters
                        .builder()
                        .topicNamePrefixParameters(TopicNamePrefixParameters
                                .stepBuilder()
                                .orgIdApplicationDefault()
                                .domainContextApplicationDefault()
                                .build())
                        .eventName(EVENT_NAME)
                        .build()
        );

        ProducerRecord<String, InstanceObject> record = new ProducerRecord<>(
                topicName,
                "key",
                instanceObject
        );
        record.headers().add(instanceFlowHeadersMapper.toHeader(instanceFlowHeaders));
        kafkaTemplate.send(record);
    }

    @TestConfiguration
    static class TestKafkaProducerConfiguration {

        @Bean(name = "testProducerFactory")
        ProducerFactory<String, InstanceObject> producerFactory(
                EmbeddedKafkaBroker embeddedKafkaBroker,
                ObjectMapper objectMapper
        ) {
            Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafkaBroker);
            props.put("value.serializer", JsonSerializer.class);
            return new DefaultKafkaProducerFactory<>(
                    props,
                    new StringSerializer(),
                    new JsonSerializer<>(objectMapper)
            );
        }

        @Bean(name = "testKafkaTemplate")
        KafkaTemplate<String, InstanceObject> kafkaTemplate(
                @Qualifier("testProducerFactory") ProducerFactory<String, InstanceObject> producerFactory
        ) {
            return new KafkaTemplate<>(producerFactory);
        }
    }
}
