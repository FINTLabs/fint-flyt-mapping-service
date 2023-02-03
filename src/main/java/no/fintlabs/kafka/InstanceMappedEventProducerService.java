package no.fintlabs.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducer;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerFactory;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InstanceMappedEventProducerService {

    private final InstanceFlowEventProducer<Object> instanceFlowEventProducer;
    private final EventTopicNameParameters eventTopicNameParameters;

    public InstanceMappedEventProducerService(
            InstanceFlowEventProducerFactory instanceFlowEventProducerFactory,
            EventTopicService eventTopicService) {
        this.instanceFlowEventProducer = instanceFlowEventProducerFactory.createProducer(Object.class);
        this.eventTopicNameParameters = EventTopicNameParameters.builder()
                .eventName("instance-mapped")
                .build();
        eventTopicService.ensureTopic(eventTopicNameParameters, 0);
    }

    public void publish(InstanceFlowHeaders instanceFlowHeaders, Object mappedInstance) {
        instanceFlowEventProducer.send(
                InstanceFlowEventProducerRecord.builder()
                        .topicNameParameters(eventTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(mappedInstance)
                        .build()
        );
    }

}
