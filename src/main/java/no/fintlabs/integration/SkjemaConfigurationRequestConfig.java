package no.fintlabs.integration;

import no.fintlabs.kafka.FintKafkaReplyTemplateFactory;
import no.fintlabs.kafka.topic.DomainContext;
import no.fintlabs.kafka.topic.TopicService;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.time.Duration;

@Configuration
public class SkjemaConfigurationRequestConfig {

    private static final String skjemaConfigurationResourceReference = "skjema.configuration";

    @Bean
    @Qualifier("skjemaConfigurationTopic")
    public TopicDescription skjemaConfigurationTopic(TopicService topicService) {
        return topicService.getOrCreateRequestTopic(
                DomainContext.SKJEMA,
                skjemaConfigurationResourceReference,
                false,
                "skjemaid"
        );
    }

    @Bean
    @Qualifier("skjemaConfigurationReplyTopic")
    public TopicDescription skjemaConfigurationReplyTopic(TopicService topicService) {
        return topicService.getOrCreateReplyTopic(DomainContext.SKJEMA, skjemaConfigurationResourceReference);
    }

    @Bean
    @Qualifier("skjemaConfigurationReplyingKafkaTemplate")
    public ReplyingKafkaTemplate<String, String, String> skjemaConfigurationReplyingKafkaTemplate(
            @Qualifier("skjemaConfigurationReplyTopic") TopicDescription skjemaConfigurationReplyTopic,
            ProducerFactory<String, String> producerFactory,
            ConsumerFactory<String, String> consumerFactory
    ) {
        ReplyingKafkaTemplate<String, String, String> skjemaConfigurationReplyingKafkaTemplate = FintKafkaReplyTemplateFactory.create(
                producerFactory,
                consumerFactory,
                skjemaConfigurationReplyTopic.name()
        );
        skjemaConfigurationReplyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofSeconds(30));
        skjemaConfigurationReplyingKafkaTemplate.setSharedReplyTopic(true);
        return skjemaConfigurationReplyingKafkaTemplate;
    }

}
