package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducer;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerFactory;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CaseCreatedEventProducerService {

    private final InstanceFlowEventProducer<SakResource> newOrUpdatedCaseProducer;
    private final EventTopicNameParameters newOrUpdatedCaseTopicNameParameters;

    public CaseCreatedEventProducerService(
            InstanceFlowEventProducerFactory instanceFlowEventProducerFactory,
            EventTopicService eventTopicService) {
        this.newOrUpdatedCaseProducer = instanceFlowEventProducerFactory.createProducer(SakResource.class);
        this.newOrUpdatedCaseTopicNameParameters = EventTopicNameParameters.builder()
                .eventName("case-created")
                .build();
        eventTopicService.ensureTopic(newOrUpdatedCaseTopicNameParameters, 0);
    }

    public void publish(InstanceFlowHeaders instanceFlowHeaders, SakResource newOrUpdatedCase) {
        newOrUpdatedCaseProducer.send(
                InstanceFlowEventProducerRecord.<SakResource>builder()
                        .topicNameParameters(newOrUpdatedCaseTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(newOrUpdatedCase)
                        .build()
        );
    }

}