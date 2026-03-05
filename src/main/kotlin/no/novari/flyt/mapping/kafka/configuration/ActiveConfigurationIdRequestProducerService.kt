package no.novari.flyt.mapping.kafka.configuration

import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.requestreply.RequestProducerRecord
import no.novari.kafka.requestreply.RequestTemplate
import no.novari.kafka.requestreply.RequestTemplateFactory
import no.novari.kafka.requestreply.topic.ReplyTopicService
import no.novari.kafka.requestreply.topic.configuration.ReplyTopicConfiguration
import no.novari.kafka.requestreply.topic.name.ReplyTopicNameParameters
import no.novari.kafka.requestreply.topic.name.RequestTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ActiveConfigurationIdRequestProducerService(
    @Value("\${novari.kafka.application-id}") applicationId: String,
    requestTemplateFactory: RequestTemplateFactory,
    replyTopicService: ReplyTopicService,
    kafkaRequestReplyProperties: KafkaRequestReplyProperties,
) {
    private val requestTopicNameParameters: RequestTopicNameParameters
    private val requestTemplate: RequestTemplate<Long, Long>

    init {
        val replyTopicNameParameters =
            ReplyTopicNameParameters
                .builder()
                .topicNamePrefixParameters(
                    TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build(),
                ).applicationId(applicationId)
                .resourceName("active-configuration-id")
                .build()

        replyTopicService.createOrModifyTopic(
            replyTopicNameParameters,
            ReplyTopicConfiguration
                .builder()
                .retentionTime(kafkaRequestReplyProperties.replyRetentionTime)
                .build(),
        )

        requestTopicNameParameters =
            RequestTopicNameParameters
                .builder()
                .topicNamePrefixParameters(
                    TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build(),
                ).resourceName("active-configuration-id")
                .parameterName("integration-id")
                .build()

        requestTemplate =
            requestTemplateFactory.createTemplate(
                replyTopicNameParameters,
                Long::class.java,
                Long::class.java,
                kafkaRequestReplyProperties.replyTimeout,
                ListenerConfiguration
                    .stepBuilder()
                    .groupIdApplicationDefault()
                    .maxPollRecordsKafkaDefault()
                    .maxPollIntervalKafkaDefault()
                    .continueFromPreviousOffsetOnAssignment()
                    .build(),
            )
    }

    fun get(integrationId: Long): Long? {
        return requestTemplate
            .requestAndReceive(
                RequestProducerRecord
                    .builder<Long>()
                    .topicNameParameters(requestTopicNameParameters)
                    .value(integrationId)
                    .build(),
            ).value()
    }
}
