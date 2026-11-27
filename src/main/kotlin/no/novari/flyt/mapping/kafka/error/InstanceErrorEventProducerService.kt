package no.novari.flyt.mapping.kafka.error

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.producing.send
import no.novari.flyt.kafka.model.Error
import no.novari.flyt.kafka.model.ErrorCollection
import no.novari.flyt.kafka.model.InstanceErrorEvent
import no.novari.flyt.kafka.model.InstanceErrorOrigin
import no.novari.flyt.mapping.kafka.configuration.KafkaEventProperties
import no.novari.kafka.starter.topic.EventCleanupFrequency
import no.novari.kafka.starter.topic.EventTopicConfig
import no.novari.kafka.starter.topic.EventTopicName
import no.novari.kafka.starter.topic.KafkaTopics
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Configuration
class InstanceErrorTopicConfiguration {
    @Bean
    fun instanceErrorTopic(topics: KafkaTopics): EventTopicName = topics.errorEvent("instance-error")

    @Bean
    fun instanceErrorTopicCreation(
        topics: KafkaTopics,
        instanceErrorTopic: EventTopicName,
        properties: KafkaEventProperties,
    ): NewTopic =
        topics.newTopic(
            instanceErrorTopic,
            EventTopicConfig(
                retention = properties.instanceProcessingEventsRetentionTime,
                cleanupFrequency = EventCleanupFrequency.NORMAL,
                partitions = 1,
            ),
        )
}

@Service
class InstanceErrorEventProducerService(
    private val kafkaTemplate: KafkaTemplate<String, InstanceErrorEvent>,
    // By name: with one EventTopicName bean in a context, Spring would inject it whatever its name.
    @param:Qualifier("instanceErrorTopic")
    private val instanceErrorTopic: EventTopicName,
) {
    fun publishConfigurationNotFoundErrorEvent(instanceFlowHeaders: InstanceFlowHeaders) {
        publishErrorEvent(instanceFlowHeaders, ErrorCode.CONFIGURATION_NOT_FOUND)
    }

    fun publishInstanceFieldNotFoundErrorEvent(
        instanceFlowHeaders: InstanceFlowHeaders,
        instanceFieldKey: String,
    ) {
        publishErrorEvent(
            instanceFlowHeaders,
            ErrorCode.INSTANCE_FIELD_NOT_FOUND,
            mapOf("instanceFieldKey" to instanceFieldKey),
        )
    }

    fun publishMissingValueConvertingErrorEvent(
        instanceFlowHeaders: InstanceFlowHeaders,
        valueConvertingId: Long,
    ) {
        publishErrorEvent(
            instanceFlowHeaders,
            ErrorCode.VALUE_CONVERTING_NOT_FOUND,
            mapOf("valueConvertingId" to valueConvertingId.toString()),
        )
    }

    fun publishMissingValueConvertingKeyErrorEvent(
        instanceFlowHeaders: InstanceFlowHeaders,
        valueConvertingId: Long,
        valueConvertingKey: String,
    ) {
        publishErrorEvent(
            instanceFlowHeaders,
            ErrorCode.VALUE_CONVERTING_KEY_NOT_FOUND,
            mapOf(
                "valueConvertingId" to valueConvertingId.toString(),
                "valueConvertingKey" to valueConvertingKey,
            ),
        )
    }

    fun publishGeneralSystemErrorEvent(instanceFlowHeaders: InstanceFlowHeaders) {
        publishErrorEvent(instanceFlowHeaders, ErrorCode.GENERAL_SYSTEM_ERROR)
    }

    /** Errors without args are written without `args`, as in v7. */
    private fun publishErrorEvent(
        instanceFlowHeaders: InstanceFlowHeaders,
        errorCode: ErrorCode,
        args: Map<String, String>? = null,
    ) {
        kafkaTemplate.send(
            instanceErrorTopic,
            instanceFlowHeaders,
            InstanceErrorEvent(
                InstanceErrorOrigin.MAPPING,
                ErrorCollection(Error(errorCode = errorCode.getCode(), args = args)),
            ),
        )
    }
}
