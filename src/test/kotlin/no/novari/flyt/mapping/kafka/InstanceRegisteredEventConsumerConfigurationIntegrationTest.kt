package no.novari.flyt.mapping.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import no.novari.Application
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeadersMapper
import no.novari.flyt.mapping.InstanceProcessingService
import no.novari.flyt.mapping.kafka.error.InstanceErrorEventProducerService
import no.novari.flyt.mapping.model.instance.InstanceObject
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService
import no.novari.kafka.topic.name.EventTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import no.novari.kafka.topic.name.TopicNameService
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.util.ReflectionTestUtils
import java.util.UUID

@SpringBootTest(
    classes = [Application::class],
    properties = [
        "spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}",
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
        "logging.level.org.springframework.kafka=ERROR",
    ],
)
@EmbeddedKafka(
    partitions = 1,
    topics = ["test-org.test-domain.event.instance-registered"],
)
@Import(InstanceRegisteredEventConsumerConfigurationIntegrationTest.TestKafkaProducerConfiguration::class)
class InstanceRegisteredEventConsumerConfigurationIntegrationTest {
    @Autowired
    @Qualifier("testKafkaTemplate")
    private lateinit var kafkaTemplate: KafkaTemplate<String, InstanceObject>

    @Autowired
    private lateinit var instanceFlowHeadersMapper: InstanceFlowHeadersMapper

    @Autowired
    private lateinit var topicNameService: TopicNameService

    @Autowired
    @Qualifier("instanceRegisteredEventConsumer")
    private lateinit var instanceRegisteredEventConsumer: ConcurrentMessageListenerContainer<String, InstanceObject>

    @MockitoBean
    private lateinit var instanceProcessingService: InstanceProcessingService

    @MockitoBean
    private lateinit var instanceErrorEventProducerService: InstanceErrorEventProducerService

    @MockitoBean
    private lateinit var sourceApplicationAuthorizationRequestService: SourceApplicationAuthorizationRequestService

    @BeforeEach
    fun setUp() {
        ContainerTestUtils.waitForAssignment(instanceRegisteredEventConsumer, 1)
    }

    @Test
    fun shouldApplyExponentialBackoffAndRecover() {
        doThrow(RuntimeException("boom"))
            .`when`(instanceProcessingService)
            .process(any())

        val instanceFlowHeaders =
            InstanceFlowHeaders
                .builder()
                .sourceApplicationId(1L)
                .correlationId(UUID.randomUUID())
                .integrationId(123L)
                .build()

        sendInstanceRegisteredEvent(instanceFlowHeaders)

        verify(instanceErrorEventProducerService, timeout(2000))
            .publishGeneralSystemErrorEvent(eq(instanceFlowHeaders))

        assertThat(instanceRegisteredEventConsumer.commonErrorHandler)
            .isInstanceOf(DefaultErrorHandler::class.java)

        val errorHandler = instanceRegisteredEventConsumer.commonErrorHandler as DefaultErrorHandler
        assertThat(errorHandler).isNotNull()

        val failureTracker =
            ReflectionTestUtils.invokeMethod<Any>(
                errorHandler,
                "getFailureTracker",
            )
        assertThat(failureTracker).isNotNull()

        val backOff =
            ReflectionTestUtils.getField(
                requireNotNull(failureTracker),
                "backOff",
            )
        assertThat(backOff).isNotNull()
        assertThat(backOff).isInstanceOf(ExponentialBackOffWithMaxRetries::class.java)

        val exponentialBackOff = backOff as ExponentialBackOffWithMaxRetries
        assertThat(exponentialBackOff.initialInterval).isEqualTo(200L)
        assertThat(exponentialBackOff.multiplier).isEqualTo(1.5)
        assertThat(exponentialBackOff.maxInterval).isEqualTo(2_000L)
        assertThat(exponentialBackOff.maxRetries).isEqualTo(2)
    }

    private fun sendInstanceRegisteredEvent(instanceFlowHeaders: InstanceFlowHeaders) {
        val instanceObject =
            InstanceObject
                .builder()
                .valuePerKey(mapOf("key" to "value"))
                .build()

        val topicName =
            topicNameService.validateAndMapToTopicName(
                EventTopicNameParameters
                    .builder()
                    .topicNamePrefixParameters(
                        TopicNamePrefixParameters
                            .stepBuilder()
                            .orgIdApplicationDefault()
                            .domainContextApplicationDefault()
                            .build(),
                    ).eventName(EVENT_NAME)
                    .build(),
            )

        val record =
            ProducerRecord(
                topicName,
                "key",
                instanceObject,
            )

        record.headers().add(instanceFlowHeadersMapper.toHeader(instanceFlowHeaders))
        kafkaTemplate.send(record)
    }

    @TestConfiguration
    class TestKafkaProducerConfiguration {
        @Bean(name = ["testProducerFactory"])
        fun producerFactory(
            @Value("\${spring.embedded.kafka.brokers}")
            embeddedKafkaBrokers: String,
            objectMapper: ObjectMapper,
        ): ProducerFactory<String, InstanceObject> {
            val props =
                mutableMapOf<String, Any>(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to embeddedKafkaBrokers,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
                )

            return DefaultKafkaProducerFactory(
                props,
                StringSerializer(),
                JsonSerializer(objectMapper),
            )
        }

        @Bean(name = ["testKafkaTemplate"])
        fun kafkaTemplate(
            @Qualifier("testProducerFactory")
            producerFactory: ProducerFactory<String, InstanceObject>,
        ): KafkaTemplate<String, InstanceObject> {
            return KafkaTemplate(producerFactory)
        }
    }

    companion object {
        private const val EVENT_NAME = "instance-registered"
    }
}
