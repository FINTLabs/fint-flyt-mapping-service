package no.novari.flyt.mapping.kafka.configuration;

import no.novari.flyt.mapping.model.configuration.ObjectMapping;
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.requestreply.RequestProducerRecord;
import no.novari.kafka.requestreply.RequestTemplate;
import no.novari.kafka.requestreply.RequestTemplateFactory;
import no.novari.kafka.requestreply.topic.ReplyTopicService;
import no.novari.kafka.requestreply.topic.configuration.ReplyTopicConfiguration;
import no.novari.kafka.requestreply.topic.name.ReplyTopicNameParameters;
import no.novari.kafka.requestreply.topic.name.RequestTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigurationMappingRequestProducerService {

    private final RequestTopicNameParameters requestTopicNameParameters;
    private final RequestTemplate<Long, ObjectMapping> requestTemplate;

    public ConfigurationMappingRequestProducerService(
            @Value("${novari.kafka.application-id}") String applicationId,
            RequestTemplateFactory requestTemplateFactory,
            ReplyTopicService replyTopicService,
            KafkaRequestReplyProperties kafkaRequestReplyProperties
    ) {
        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters.builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .applicationId(applicationId)
                .resourceName("mapping")
                .build();

        replyTopicService.createOrModifyTopic(replyTopicNameParameters, ReplyTopicConfiguration
                .builder()
                .retentionTime(kafkaRequestReplyProperties.getReplyRetentionTime())
                .build()
        );

        this.requestTopicNameParameters = RequestTopicNameParameters.builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .resourceName("mapping")
                .parameterName("configuration-id")
                .build();

        this.requestTemplate = requestTemplateFactory.createTemplate(
                replyTopicNameParameters,
                Long.class,
                ObjectMapping.class,
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

    public Optional<ObjectMapping> get(Long configurationId) {
        return Optional.ofNullable(
                requestTemplate.requestAndReceive(
                        RequestProducerRecord.<Long>builder()
                                .topicNameParameters(requestTopicNameParameters)
                                .value(configurationId)
                                .build()
                ).value()
        );
    }
}
