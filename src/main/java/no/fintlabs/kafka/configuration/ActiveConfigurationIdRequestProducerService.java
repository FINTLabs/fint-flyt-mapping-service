package no.fintlabs.kafka.configuration;

import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.RequestProducer;
import no.fintlabs.kafka.requestreply.RequestProducerConfiguration;
import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class ActiveConfigurationIdRequestProducerService {

    private final RequestTopicNameParameters requestTopicNameParameters;
    private final RequestProducer<Long, Long> requestProducer;

    public ActiveConfigurationIdRequestProducerService(
            @Value("${fint.kafka.application-id}") String applicationId,
            RequestProducerFactory requestProducerFactory,
            ReplyTopicService replyTopicService
    ) {
        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters.builder()
                .applicationId(applicationId)
                .resource("active-configuration-id")
                .build();

        replyTopicService.ensureTopic(replyTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        this.requestTopicNameParameters = RequestTopicNameParameters.builder()
                .resource("active-configuration-id")
                .parameterName("integration-id")
                .build();

        this.requestProducer = requestProducerFactory.createProducer(
                replyTopicNameParameters,
                Long.class,
                Long.class,
                RequestProducerConfiguration.builder().defaultReplyTimeout(Duration.ofMinutes(2)).build()
        );
    }

    public Optional<Long> get(Long integrationId) {
        return requestProducer.requestAndReceive(
                RequestProducerRecord.<Long>builder()
                        .topicNameParameters(requestTopicNameParameters)
                        .value(integrationId)
                        .build()
        ).map(ConsumerRecord::value);
    }

}
