package no.fintlabs.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.InstanceProcessingService;
import no.fintlabs.kafka.topic.DomainContext;
import no.fintlabs.kafka.topic.TopicService;
import no.fintlabs.model.instance.Instance;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CaseInstanceEventConsumerComponent {

    private final ObjectMapper objectMapper;
    private final InstanceProcessingService instanceProcessingService;

    public CaseInstanceEventConsumerComponent(
            ObjectMapper objectMapper,
            InstanceProcessingService instanceProcessingService
    ) {
        this.objectMapper = objectMapper;
        this.instanceProcessingService = instanceProcessingService;
    }

    @Bean
    @Qualifier("caseInstanceEventConsumerTopic")
    public TopicDescription caseInstanceEventConsumerTopic(TopicService topicService) {
        return topicService.getOrCreateEventTopic(DomainContext.SKJEMA, "new-case-instance");
    }

    @KafkaListener(topics = "#{caseInstanceEventConsumerTopic.name()}")
    public void consume(ConsumerRecord<String, String> consumerRecord) {
        // TODO: 05/01/2022 :
        //  1. Get skjemaId from instance
        //  2. Request configuration by skjemaId from SkjemaConfigurationRequestService
        //  3. Map instance with configuration
        //  4. Produce result to kafka through CaseEventProducerService
        log.info("Received: " + consumerRecord.value());
        Instance instance;
        try {
            instance = objectMapper.readValue(consumerRecord.value(), Instance.class);
        } catch (JsonProcessingException e) {
            log.error("Could not deserialize instance", e);
            // TODO: 28/01/2022 How to handle exception before we know the id of the instance. Cant notify of issue related to instance -- include reference in header?
            return;
        }
        this.instanceProcessingService.process(instance);
    }

}
