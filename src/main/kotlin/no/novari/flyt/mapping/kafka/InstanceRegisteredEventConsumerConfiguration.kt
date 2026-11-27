package no.novari.flyt.mapping.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import no.novari.flyt.kafka.instanceflow.headers.INSTANCE_FLOW_HEADERS_KEY
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.headers.instanceFlowHeadersOrNull
import no.novari.flyt.mapping.InstanceProcessingService
import no.novari.flyt.mapping.kafka.configuration.KafkaConsumerProperties
import no.novari.flyt.mapping.kafka.error.InstanceErrorEventProducerService
import no.novari.flyt.mapping.model.instance.InstanceObject
import no.novari.kafka.starter.topic.EventTopicName
import no.novari.kafka.starter.topic.KafkaTopics
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Configuration
class InstanceRegisteredEventConsumerConfiguration {
    @Bean
    fun instanceRegisteredTopic(topics: KafkaTopics): EventTopicName = topics.event("instance-registered")

    @Bean
    fun instanceRequestedForRetryTopic(topics: KafkaTopics): EventTopicName =
        topics.event("instance-requested-for-retry")

    /** Retries with exponential back-off, then publishes a general system error and skips the record. */
    @Bean
    fun kafkaErrorHandler(
        consumerProperties: KafkaConsumerProperties,
        errorEventProducerService: InstanceErrorEventProducerService,
    ): CommonErrorHandler {
        val backoff = consumerProperties.instanceProcessingBackoff
        val backOff =
            ExponentialBackOffWithMaxRetries(backoff.maxRetries).apply {
                initialInterval = backoff.initialInterval.toMillis()
                multiplier = backoff.multiplier
                maxInterval = backoff.maxInterval.toMillis()
            }
        return DefaultErrorHandler(
            { record, exception ->
                // A failing recoverer skips the record instead of retrying it again, as in v7.
                runCatching {
                    record.headers().instanceFlowHeadersOrNull()?.let(
                        errorEventProducerService::publishGeneralSystemErrorEvent,
                    )
                        ?: log.warn(exception) { "Missing instance flow headers on record, skipping" }
                }.onFailure { log.error(it) { "Could not publish the error event, skipping record" } }
            },
            backOff,
        )
    }
}

@Component
class InstanceEventListener(
    private val processingService: InstanceProcessingService,
) {
    @KafkaListener(topics = ["#{@instanceRegisteredTopic.name}", "#{@instanceRequestedForRetryTopic.name}"])
    fun on(
        instance: InstanceObject,
        @Header(INSTANCE_FLOW_HEADERS_KEY) headers: InstanceFlowHeaders,
    ) = processingService.process(instance, headers)
}
