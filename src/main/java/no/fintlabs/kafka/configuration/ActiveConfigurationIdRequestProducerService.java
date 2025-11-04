package no.fintlabs.kafka.configuration;

import no.fintlabs.kafka.consuming.ListenerConfiguration;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;
import no.fintlabs.kafka.requestreply.RequestTemplate;
import no.fintlabs.kafka.requestreply.RequestTemplateFactory;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.kafka.requestreply.topic.configuration.ReplyTopicConfiguration;
import no.fintlabs.kafka.requestreply.topic.name.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.name.RequestTopicNameParameters;
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ActiveConfigurationIdRequestProducerService {

    private final RequestTopicNameParameters requestTopicNameParameters;
    private final RequestTemplate<Long, Long> requestTemplate;

    public ActiveConfigurationIdRequestProducerService(
            @Value("${fint.kafka.application-id}") String applicationId,
            RequestTemplateFactory requestTemplateFactory,
            ReplyTopicService replyTopicService,
            KafkaRequestReplyProperties kafkaRequestReplyProperties
    ) {
        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters.builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .builder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .applicationId(applicationId)
                .resourceName("active-configuration-id")
                .build();

        replyTopicService.createOrModifyTopic(replyTopicNameParameters, ReplyTopicConfiguration
                .builder()
                .retentionTime(kafkaRequestReplyProperties.getReplyRetentionTime())
                .build()
        );

        this.requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .builder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .resourceName("active-configuration-id")
                .parameterName("integration-id")
                .build();

        this.requestTemplate = requestTemplateFactory.createTemplate(
                replyTopicNameParameters,
                Long.class,
                Long.class,
                kafkaRequestReplyProperties.getReplyTimeout(),
                ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build()
        );
    }

    public Optional<Long> get(Long integrationId) {
        return Optional.ofNullable(
                requestTemplate.requestAndReceive(
                        RequestProducerRecord.<Long>builder()
                                .topicNameParameters(requestTopicNameParameters)
                                .value(integrationId)
                                .build()
                ).value()
        );
    }

}
