package no.fintlabs.kafka.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.RequestProducer;
import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.model.configuration.ConfigurationElement;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class ConfigurationElementsRequestProducerService {

    private final RequestTopicNameParameters requestTopicNameParameters;
    private final RequestProducer<Long, ConfigurationElementsDto> configurationRequestProducer;

    public ConfigurationElementsRequestProducerService(
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
                .resource("configuration-elements")
                .parameterName("configuration-id")
                .build();

        this.configurationRequestProducer = requestProducerFactory.createProducer(
                replyTopicNameParameters,
                Long.class,
                ConfigurationElementsDto.class
        );
    }

    public Optional<Collection<ConfigurationElement>> get(Long configurationId) {
        return configurationRequestProducer.requestAndReceive(
                        RequestProducerRecord.<Long>builder()
                                .topicNameParameters(requestTopicNameParameters)
                                .value(configurationId)
                                .build()
                ).map(ConsumerRecord::value)
                .map(ConfigurationElementsDto::getElements);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ConfigurationElementsDto {
        private Collection<ConfigurationElement> elements;
    }
}
