package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.RequestProducer;
import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
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
            @Value("${fint.kafka.application-id}") String applicationId,
            RequestProducerFactory requestProducerFactory,
            ReplyTopicService replyTopicService
    ) {
        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters.builder()
                .applicationId(applicationId)
                .resource("flyt.configuration")
                .build();

        replyTopicService.ensureTopic(replyTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        this.requestTopicNameParameters = RequestTopicNameParameters.builder()
                .resource("flyt.configuration")
                .parameterName("skjemaid")
                .build();

        this.requestProducer = requestProducerFactory.createProducer(
                replyTopicNameParameters,
                String.class,
                IntegrationConfiguration.class
        );
    }

    public Optional<IntegrationConfiguration> get(String sourceApplication, String sourceApplicationIntegrationId) {
        return requestProducer.requestAndReceive(
                RequestProducerRecord.<String>builder()
                        .topicNameParameters(requestTopicNameParameters)
                        .value(sourceApplication + sourceApplicationIntegrationId) // TODO: 10/04/2022 DTO?
                        .build()
        ).map(ConsumerRecord::value);
    }

}
