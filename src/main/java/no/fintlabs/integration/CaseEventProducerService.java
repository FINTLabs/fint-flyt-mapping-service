package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.kafka.event.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CaseEventProducerService {

    private final EventProducer<SakResource> newOrUpdatedCaseProducer;
    private final EventTopicNameParameters newOrUpdatedCaseTopicNameParameters;

    public CaseEventProducerService(
            @Value("${fint.org-id}") String orgId,
            FintKafkaEventProducerFactory fintKafkaEventProducerFactory,
            EventTopicService eventTopicService) {
        this.newOrUpdatedCaseProducer = fintKafkaEventProducerFactory.createProducer(SakResource.class);
        this.newOrUpdatedCaseTopicNameParameters = EventTopicNameParameters.builder()
                .orgId(orgId)
                .domainContext("skjema")
                .eventName("new-or-updated-case")
                .build();
        eventTopicService.ensureTopic(newOrUpdatedCaseTopicNameParameters, 0);
    }

    public void sendNewOrUpdatedCase(SakResource newOrUpdatedCase) {
        newOrUpdatedCaseProducer.send(
                EventProducerRecord.<SakResource>builder()
                        .topicNameParameters(newOrUpdatedCaseTopicNameParameters)
                        .value(newOrUpdatedCase)
                        .build()
        );
    }

}
