package no.novari.flyt.mapping.kafka.error

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplate
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory
import no.novari.flyt.kafka.model.Error
import no.novari.flyt.kafka.model.ErrorCollection
import no.novari.flyt.kafka.model.InstanceErrorEvent
import no.novari.flyt.kafka.model.InstanceErrorOrigin
import no.novari.flyt.mapping.kafka.configuration.KafkaEventProperties
import no.novari.kafka.topic.ErrorEventTopicService
import no.novari.kafka.topic.configuration.EventCleanupFrequency
import no.novari.kafka.topic.configuration.EventTopicConfiguration
import no.novari.kafka.topic.name.ErrorEventTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.stereotype.Service

@Service
class InstanceErrorEventProducerService(
    instanceFlowTemplateFactory: InstanceFlowTemplateFactory,
    errorEventTopicService: ErrorEventTopicService,
    kafkaEventProperties: KafkaEventProperties,
) {
    private val errorEventTopicNameParameters: ErrorEventTopicNameParameters =
        ErrorEventTopicNameParameters
            .builder()
            .errorEventName("instance-error")
            .topicNamePrefixParameters(
                TopicNamePrefixParameters
                    .stepBuilder()
                    .orgIdApplicationDefault()
                    .domainContextApplicationDefault()
                    .build(),
            ).build()
    private val instanceFlowTemplate: InstanceFlowTemplate<InstanceErrorEvent> =
        instanceFlowTemplateFactory.createTemplate(InstanceErrorEvent::class.java)

    init {

        errorEventTopicService.createOrModifyTopic(
            errorEventTopicNameParameters,
            EventTopicConfiguration
                .stepBuilder()
                .partitions(PARTITIONS)
                .retentionTime(kafkaEventProperties.instanceProcessingEventsRetentionTime)
                .cleanupFrequency(EventCleanupFrequency.NORMAL)
                .build(),
        )
    }

    fun publishConfigurationNotFoundErrorEvent(instanceFlowHeaders: InstanceFlowHeaders) {
        publishErrorEventWithASingleErrorCode(instanceFlowHeaders, ErrorCode.CONFIGURATION_NOT_FOUND)
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
        publishErrorEventWithASingleErrorCode(instanceFlowHeaders, ErrorCode.GENERAL_SYSTEM_ERROR)
    }

    private fun publishErrorEvent(
        instanceFlowHeaders: InstanceFlowHeaders,
        errorCode: ErrorCode,
        args: Map<String, String>,
    ) {
        instanceFlowTemplate.send(
            InstanceFlowProducerRecord
                .builder<InstanceErrorEvent>()
                .instanceFlowHeaders(instanceFlowHeaders)
                .topicNameParameters(errorEventTopicNameParameters)
                .value(
                    InstanceErrorEvent(
                        InstanceErrorOrigin.MAPPING,
                        ErrorCollection(
                            Error
                                .builder()
                                .errorCode(errorCode.getCode())
                                .args(args)
                                .build(),
                        ),
                    ),
                ).build(),
        )
    }

    private fun publishErrorEventWithASingleErrorCode(
        instanceFlowHeaders: InstanceFlowHeaders,
        errorCode: ErrorCode,
    ) {
        instanceFlowTemplate.send(
            InstanceFlowProducerRecord
                .builder<InstanceErrorEvent>()
                .instanceFlowHeaders(instanceFlowHeaders)
                .topicNameParameters(errorEventTopicNameParameters)
                .value(
                    InstanceErrorEvent(
                        InstanceErrorOrigin.MAPPING,
                        ErrorCollection(
                            Error
                                .builder()
                                .errorCode(errorCode.getCode())
                                .build(),
                        ),
                    ),
                ).build(),
        )
    }

    companion object {
        private const val PARTITIONS = 1
    }
}
