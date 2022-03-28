package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.*;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class SkjemaConfigurationRequestService {

    private final RequestTopicNameParameters requestTopicNameParameters;
    private final RequestProducer<String, IntegrationConfiguration> requestProducer;

    public SkjemaConfigurationRequestService(
            @Value("${fint.org-id}") String orgId,
            @Value("${fint.kafka.application-id}") String applicationId,
            FintKafkaRequestProducerFactory fintKafkaRequestProducerFactory,
            ReplyTopicService replyTopicService
    ) {
        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters.builder()
                .orgId(orgId)
                .domainContext("skjema")
                .applicationId(applicationId)
                .resource("skjema.configuration")
                .build();

        replyTopicService.ensureTopic(replyTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        this.requestTopicNameParameters = RequestTopicNameParameters.builder()
                .orgId(orgId)
                .domainContext("skjema")
                .resource("skjema.configuration")
                .parameterName("skjemaid")
                .build();

        this.requestProducer = fintKafkaRequestProducerFactory.createProducer(
                replyTopicNameParameters,
                String.class,
                IntegrationConfiguration.class
        );
    }

    public Optional<IntegrationConfiguration> get(String skjemaId) {
        return requestProducer.requestAndReceive(
                RequestProducerRecord.<String>builder()
                        .topicNameParameters(requestTopicNameParameters)
                        .value(skjemaId)
                        .build()
        ).map(ConsumerRecord::value);
    }

}
