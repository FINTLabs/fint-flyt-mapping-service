package no.novari.flyt.mapping.kafka;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplate;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory;
import no.novari.flyt.mapping.kafka.configuration.KafkaEventProperties;
import no.novari.kafka.topic.EventTopicService;
import no.novari.kafka.topic.configuration.EventCleanupFrequency;
import no.novari.kafka.topic.configuration.EventTopicConfiguration;
import no.novari.kafka.topic.name.EventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InstanceMappedEventProducerService {

    private final EventTopicNameParameters eventTopicNameParameters;

    private static final int PARTITIONS = 1;
    private final InstanceFlowTemplate<Object> instanceFlowTemplate;

    public InstanceMappedEventProducerService(
            InstanceFlowTemplateFactory instanceFlowTemplateFactory,
            EventTopicService eventTopicService,
            KafkaEventProperties kafkaEventProperties
    ) {
        this.instanceFlowTemplate = instanceFlowTemplateFactory.createTemplate(Object.class);
        this.eventTopicNameParameters = EventTopicNameParameters.builder()
                .eventName("instance-mapped")
                .topicNamePrefixParameters(
                        TopicNamePrefixParameters
                                .builder()
                                .orgIdApplicationDefault()
                                .domainContextApplicationDefault()
                                .build()
                )
                .build();
        eventTopicService.createOrModifyTopic(eventTopicNameParameters, EventTopicConfiguration
                .builder()
                .partitions(PARTITIONS)
                .retentionTime(kafkaEventProperties.getInstanceProcessingEventsRetentionTime())
                .cleanupFrequency(EventCleanupFrequency.NORMAL)
                .build()
        );
    }

    public void publish(InstanceFlowHeaders instanceFlowHeaders, Object mappedInstance) {
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .builder()
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .topicNameParameters(eventTopicNameParameters)
                        .value(mappedInstance)
                        .build()
        );
    }

}
