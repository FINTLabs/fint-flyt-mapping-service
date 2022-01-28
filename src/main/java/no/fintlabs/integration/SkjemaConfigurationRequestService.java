package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.util.FintKafkaRequestReplyUtil;
import no.fintlabs.kafka.util.RequestReplyOperationArgs;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class SkjemaConfigurationRequestService {

    private final ReplyingKafkaTemplate<String, String, String> skjemaConfigurationReplyingKafkaTemplate;
    private final TopicDescription skjemaConfigurationTopic;

    public SkjemaConfigurationRequestService(
            @Qualifier("skjemaConfigurationReplyingKafkaTemplate") ReplyingKafkaTemplate<String, String, String> skjemaConfigurationReplyingKafkaTemplate,
            @Qualifier("skjemaConfigurationTopic") TopicDescription skjemaConfigurationTopic
    ) {
        this.skjemaConfigurationReplyingKafkaTemplate = skjemaConfigurationReplyingKafkaTemplate;
        this.skjemaConfigurationTopic = skjemaConfigurationTopic;
    }

    public Optional<IntegrationConfiguration> get(String skjemaId) {
        return FintKafkaRequestReplyUtil.get(new RequestReplyOperationArgs<>(
                this.skjemaConfigurationTopic.name(),
                skjemaId,
                skjemaConfigurationReplyingKafkaTemplate,
                IntegrationConfiguration.class
        ));
    }

}
