package no.fintlabs.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.topic.DomainContext;
import no.fintlabs.kafka.topic.TopicNameService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CaseEventProducerService {

    private final KafkaTemplate<String, String> template;
    private final ObjectMapper objectMapper;

    public CaseEventProducerService(KafkaTemplate<String, String> template, TopicNameService topicNameService, ObjectMapper objectMapper) {
        template.setDefaultTopic(topicNameService.generateEventTopicName(DomainContext.SKJEMA, "new-case"));
        this.template = template;
        this.objectMapper = objectMapper;
    }

    // TODO: 05/01/2022 Specify type
    public void produce(Object newCase) {
        try {
            String payload = this.objectMapper.writeValueAsString(newCase);
            template.sendDefault(payload);
        } catch (JsonProcessingException e) {
            log.error("Could not write case as string", e);
        }
    }
}
