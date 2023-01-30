package no.fintlabs.kafka.configuration;

import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.RequestProducer;
import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.model.configuration.ElementMapping;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigurationElementMappingRequestProducerService {

    private final RequestTopicNameParameters requestTopicNameParameters;
    private final RequestProducer<Long, ElementMapping> configurationRequestProducer;

    public ConfigurationElementMappingRequestProducerService(
            @Value("${fint.kafka.application-id}") String applicationId,
            RequestProducerFactory requestProducerFactory,
            ReplyTopicService replyTopicService
    ) {
        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters.builder()
                .applicationId(applicationId)
                .resource("configuration-elements")
                .build();

        replyTopicService.ensureTopic(replyTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        this.requestTopicNameParameters = RequestTopicNameParameters.builder()
                .resource("mapping")
                .parameterName("configuration-id")
                .build();

        this.configurationRequestProducer = requestProducerFactory.createProducer(
                replyTopicNameParameters,
                Long.class,
                ElementMapping.class
        );
    }

    public Optional<ElementMapping> get(Long configurationId) {
        return configurationRequestProducer.requestAndReceive(
                RequestProducerRecord.<Long>builder()
                        .topicNameParameters(requestTopicNameParameters)
                        .value(configurationId)
                        .build()
        ).map(ConsumerRecord::value);
    }
}
