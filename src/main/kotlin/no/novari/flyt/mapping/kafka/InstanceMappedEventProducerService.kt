package no.novari.flyt.mapping.kafka

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplate
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory
import no.novari.flyt.mapping.kafka.configuration.KafkaEventProperties
import no.novari.kafka.topic.EventTopicService
import no.novari.kafka.topic.configuration.EventCleanupFrequency
import no.novari.kafka.topic.configuration.EventTopicConfiguration
import no.novari.kafka.topic.name.EventTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.stereotype.Service

@Service
class InstanceMappedEventProducerService(
    instanceFlowTemplateFactory: InstanceFlowTemplateFactory,
    eventTopicService: EventTopicService,
    kafkaEventProperties: KafkaEventProperties,
) {
    private val eventTopicNameParameters: EventTopicNameParameters =
        EventTopicNameParameters
            .builder()
            .eventName("instance-mapped")
            .topicNamePrefixParameters(
                TopicNamePrefixParameters
                    .stepBuilder()
                    .orgIdApplicationDefault()
                    .domainContextApplicationDefault()
                    .build(),
            ).build()
    private val instanceFlowTemplate: InstanceFlowTemplate<Any> =
        instanceFlowTemplateFactory.createTemplate(Any::class.java)

    init {

        eventTopicService.createOrModifyTopic(
            eventTopicNameParameters,
            EventTopicConfiguration
                .stepBuilder()
                .partitions(PARTITIONS)
                .retentionTime(kafkaEventProperties.instanceProcessingEventsRetentionTime)
                .cleanupFrequency(EventCleanupFrequency.NORMAL)
                .build(),
        )
    }

    fun publish(
        instanceFlowHeaders: InstanceFlowHeaders,
        mappedInstance: Any,
    ) {
        instanceFlowTemplate.send(
            InstanceFlowProducerRecord
                .builder<Any>()
                .instanceFlowHeaders(instanceFlowHeaders)
                .topicNameParameters(eventTopicNameParameters)
                .value(mappedInstance)
                .build(),
        )
    }

    companion object {
        private const val PARTITIONS = 1
    }
}
