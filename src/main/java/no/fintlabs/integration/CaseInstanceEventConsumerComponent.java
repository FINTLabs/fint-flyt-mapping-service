package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.topic.DomainContext;
import no.fintlabs.kafka.topic.TopicNameService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CaseInstanceEventConsumerComponent {

    private final SkjemaConfigurationRequestService skjemaConfigurationRequestService;
    private final CaseEventProducerService caseEventProducerService;

    public CaseInstanceEventConsumerComponent(
            SkjemaConfigurationRequestService skjemaConfigurationRequestService,
            CaseEventProducerService caseEventProducerService
    ) {
        this.skjemaConfigurationRequestService = skjemaConfigurationRequestService;
        this.caseEventProducerService = caseEventProducerService;
    }

    @Bean
    @Qualifier("caseInstanceEventConsumerTopic")
    public String caseInstanceEventConsumerTopic(TopicNameService topicNameService) {
        return topicNameService.generateEventTopicName(DomainContext.SKJEMA, "new-case-instance");
    }

    @KafkaListener(topics = "#{caseInstanceEventConsumerTopic}")
    public void consume(ConsumerRecord<String, String> consumerRecord) {
        log.info("Received: " + consumerRecord.value());
        // TODO: 05/01/2022 :
        //  1. Get skjemaId from instance
        //  2. Request configuration by skjemaId from SkjemaConfigurationRequestService
        //  3. Map instance with configuration
        //  4. Produce result to kafka through CaseEventProducerService
    }

}
