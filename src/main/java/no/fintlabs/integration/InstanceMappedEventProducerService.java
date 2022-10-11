package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducer;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerFactory;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import no.fintlabs.model.mappedinstance.MappedInstance;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InstanceMappedEventProducerService {

    private final InstanceFlowEventProducer<MappedInstance> instanceFlowEventProducer;
    private final EventTopicNameParameters eventTopicNameParameters;

    public InstanceMappedEventProducerService(
            InstanceFlowEventProducerFactory instanceFlowEventProducerFactory,
            EventTopicService eventTopicService) {
        this.instanceFlowEventProducer = instanceFlowEventProducerFactory.createProducer(MappedInstance.class);
        this.eventTopicNameParameters = EventTopicNameParameters.builder()
                .eventName("instance-mapped")
                .build();
        eventTopicService.ensureTopic(eventTopicNameParameters, 0);
    }

    public void publish(InstanceFlowHeaders instanceFlowHeaders, MappedInstance mappedInstance) {
        instanceFlowEventProducer.send(
                InstanceFlowEventProducerRecord.<MappedInstance>builder()
                        .topicNameParameters(eventTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(mappedInstance)
                        .build()
        );
    }

}
