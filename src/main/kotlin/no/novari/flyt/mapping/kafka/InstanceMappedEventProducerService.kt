package no.novari.flyt.mapping.kafka

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.producing.send
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
class InstanceMappedTopicConfiguration {
    @Bean
    fun instanceMappedTopic(topics: KafkaTopics): EventTopicName = topics.event("instance-mapped")

    @Bean
    fun instanceMappedTopicCreation(
        topics: KafkaTopics,
        instanceMappedTopic: EventTopicName,
        properties: KafkaEventProperties,
    ): NewTopic =
        topics.newTopic(
            instanceMappedTopic,
            EventTopicConfig(
                retention = properties.instanceProcessingEventsRetentionTime,
                cleanupFrequency = EventCleanupFrequency.NORMAL,
                partitions = 1,
            ),
        )
}

@Service
class InstanceMappedEventProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    // By name: with one EventTopicName bean in a context, Spring would inject it whatever its name.
    @param:Qualifier("instanceMappedTopic")
    private val instanceMappedTopic: EventTopicName,
) {
    fun publish(
        instanceFlowHeaders: InstanceFlowHeaders,
        mappedInstance: Any,
    ) {
        kafkaTemplate.send(instanceMappedTopic, instanceFlowHeaders, mappedInstance)
    }
}
