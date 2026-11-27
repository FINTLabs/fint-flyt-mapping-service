package no.novari.flyt.mapping.kafka

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.producing.send
import no.novari.flyt.mapping.InstanceProcessingService
import no.novari.flyt.mapping.kafka.error.InstanceErrorEventProducerService
import no.novari.flyt.mapping.model.instance.InstanceObject
import no.novari.kafka.starter.topic.EventTopicName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.after
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.util.ReflectionTestUtils
import java.util.UUID

@SpringBootTest(
    properties = [
        "spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "fint.application-id=test-mapping-service",
        "fint.org-id=test-org",
        "novari.kafka.topic.org-id=test-org",
        "novari.kafka.topic.domain-context=test-domain",
        "novari.kafka.topic.default-replicas=1",
        "fint.flyt.authorization.sso.client-id=test-client",
        "fint.flyt.authorization.sso.client-secret=test-secret",
        "novari.flyt.mapping-service.intgr-conf-client.base-url=http://localhost",
        "novari.flyt.mapping-service.kafka.consumer.instance-processing-backoff.initial-interval=200ms",
        "novari.flyt.mapping-service.kafka.consumer.instance-processing-backoff.multiplier=1.5",
        "novari.flyt.mapping-service.kafka.consumer.instance-processing-backoff.max-interval=2s",
        "novari.flyt.mapping-service.kafka.consumer.instance-processing-backoff.max-retries=2",
        "logging.level.org.apache.kafka=ERROR",
        "logging.level.org.springframework.kafka=ERROR",
    ],
)
@EmbeddedKafka(
    partitions = 1,
    topics = [
        "test-org.test-domain.event.instance-registered",
        "test-org.test-domain.event.instance-requested-for-retry",
    ],
)
class InstanceRegisteredEventConsumerConfigurationIntegrationTest {
    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    @Autowired
    private lateinit var instanceRegisteredTopic: EventTopicName

    @Autowired
    private lateinit var instanceRequestedForRetryTopic: EventTopicName

    @Autowired
    private lateinit var listeners: KafkaListenerEndpointRegistry

    @Autowired
    private lateinit var errorHandler: CommonErrorHandler

    @MockitoBean
    private lateinit var instanceProcessingService: InstanceProcessingService

    @MockitoBean
    private lateinit var instanceErrorEventProducerService: InstanceErrorEventProducerService

    @BeforeEach
    fun waitForAssignment() {
        listeners.listenerContainers.forEach { ContainerTestUtils.waitForAssignment(it, 2) }
    }

    @Test
    fun `both topics are consumed in the application group`() {
        assertThat(listeners.listenerContainers.map { it.groupId }).containsExactly("test-mapping-service")
        assertThat(listeners.listenerContainers.flatMap { it.containerProperties.topics!!.toList() })
            .containsExactlyInAnyOrder(instanceRegisteredTopic.name, instanceRequestedForRetryTopic.name)
    }

    @Test
    fun `an instance requested for retry is processed with its headers`() {
        val headers = headers()
        val instance = InstanceObject(valuePerKey = mapOf("key" to "value"))

        kafkaTemplate.send(instanceRequestedForRetryTopic, headers, instance)

        verify(instanceProcessingService, timeout(5000)).process(eq(instance), eq(headers))
    }

    @Test
    fun `a failing instance is retried with exponential back-off, then a general system error is published`() {
        doThrow(RuntimeException("boom")).`when`(instanceProcessingService).process(any(), any())
        val headers = headers()

        kafkaTemplate.send(instanceRegisteredTopic, headers, InstanceObject(valuePerKey = mapOf("key" to "value")))

        verify(instanceErrorEventProducerService, timeout(5000)).publishGeneralSystemErrorEvent(eq(headers))
        val failureTracker =
            ReflectionTestUtils.invokeMethod<Any>(
                errorHandler as DefaultErrorHandler,
                "getFailureTracker",
            )
        val backOff =
            ReflectionTestUtils.getField(
                requireNotNull(failureTracker),
                "backOff",
            ) as ExponentialBackOffWithMaxRetries
        assertThat(backOff.initialInterval).isEqualTo(200L)
        assertThat(backOff.multiplier).isEqualTo(1.5)
        assertThat(backOff.maxInterval).isEqualTo(2_000L)
        assertThat(backOff.maxRetries).isEqualTo(2)
    }

    @Test
    fun `a record without instance flow headers is skipped without an error event`() {
        kafkaTemplate.send(instanceRegisteredTopic.name, InstanceObject(valuePerKey = mapOf("key" to "value")))

        verify(instanceErrorEventProducerService, after(2000).never()).publishGeneralSystemErrorEvent(any())
        verify(instanceProcessingService, after(0).never()).process(any(), any())
    }

    private fun headers() =
        InstanceFlowHeaders(sourceApplicationId = 1L, correlationId = UUID.randomUUID(), integrationId = 123L)
}
