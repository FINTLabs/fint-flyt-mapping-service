package no.fintlabs.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.kafka.topic.DomainContext;
import no.fintlabs.kafka.topic.TopicService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CaseEventProducerService {

    private final KafkaTemplate<String, String> template;
    private final ObjectMapper objectMapper;

    private final String newOrUpdatedCaseTopicName;
    private final String couldNotProcessInstanceTopicName;

    public CaseEventProducerService(KafkaTemplate<String, String> template, TopicService topicService, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
        this.newOrUpdatedCaseTopicName = topicService.getOrCreateEventTopic(DomainContext.SKJEMA, "new-or-updated-case").name();
        this.couldNotProcessInstanceTopicName = topicService.getOrCreateEventTopic(DomainContext.SKJEMA, "could-not-process-topic").name();
    }

    public void newOrUpdatedTopic(SakResource newOrUpdatedCase) throws JsonProcessingException {
        String payload = this.objectMapper.writeValueAsString(newOrUpdatedCase);
        this.template.send(this.newOrUpdatedCaseTopicName, payload);
    }

    // TODO: 28/01/2022 Reference
    public void couldNotProcessInstance(Object reference) throws JsonProcessingException {
        String payload = this.objectMapper.writeValueAsString(reference);
        this.template.send(this.couldNotProcessInstanceTopicName, payload);
    }
}
