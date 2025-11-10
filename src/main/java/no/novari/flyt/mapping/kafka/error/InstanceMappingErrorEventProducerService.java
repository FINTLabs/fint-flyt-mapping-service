package no.novari.flyt.mapping.kafka.error;

import no.fintlabs.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.fintlabs.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord;
import no.fintlabs.flyt.kafka.instanceflow.producing.InstanceFlowTemplate;
import no.fintlabs.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory;
import no.novari.flyt.mapping.kafka.configuration.KafkaEventProperties;
import no.fintlabs.kafka.model.Error;
import no.fintlabs.kafka.model.ErrorCollection;
import no.fintlabs.kafka.topic.ErrorEventTopicService;
import no.fintlabs.kafka.topic.configuration.EventCleanupFrequency;
import no.fintlabs.kafka.topic.configuration.EventTopicConfiguration;
import no.fintlabs.kafka.topic.name.ErrorEventTopicNameParameters;
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InstanceMappingErrorEventProducerService {

    private final ErrorEventTopicNameParameters errorEventTopicNameParameters;
    private final InstanceFlowTemplate<ErrorCollection> instanceFlowTemplate;

    private static final int PARTITIONS = 1;

    public InstanceMappingErrorEventProducerService(
            InstanceFlowTemplateFactory instanceFlowTemplateFactory,
            ErrorEventTopicService errorEventTopicService,
            KafkaEventProperties kafkaEventProperties
    ) {
        this.instanceFlowTemplate = instanceFlowTemplateFactory.createTemplate(
                ErrorCollection.class
        );
        this.errorEventTopicNameParameters = ErrorEventTopicNameParameters.builder()
                .errorEventName("instance-mapping-error")
                .topicNamePrefixParameters(
                        TopicNamePrefixParameters
                                .builder()
                                .orgIdApplicationDefault()
                                .domainContextApplicationDefault()
                                .build()
                )
                .build();

        errorEventTopicService.createOrModifyTopic(errorEventTopicNameParameters, EventTopicConfiguration
                .builder()
                .partitions(PARTITIONS)
                .retentionTime(kafkaEventProperties.getInstanceProcessingEventsRetentionTime())
                .cleanupFrequency(EventCleanupFrequency.NORMAL)
                .build()
        );
    }

    public void publishConfigurationNotFoundErrorEvent(InstanceFlowHeaders instanceFlowHeaders) {
        publishErrorEventWithASingleErrorCode(instanceFlowHeaders, ErrorCode.CONFIGURATION_NOT_FOUND);
    }

    public void publishInstanceFieldNotFoundErrorEvent(InstanceFlowHeaders instanceFlowHeaders, String instanceFieldKey) {
        publishErrorEvent(
                instanceFlowHeaders,
                ErrorCode.INSTANCE_FIELD_NOT_FOUND,
                Map.of(
                        "instanceFieldKey", instanceFieldKey
                ));
    }

    public void publishMissingValueConvertingErrorEvent(InstanceFlowHeaders instanceFlowHeaders, Long valueConvertingId) {
        publishErrorEvent(
                instanceFlowHeaders,
                ErrorCode.VALUE_CONVERTING_NOT_FOUND,
                Map.of(
                        "valueConvertingId", String.valueOf(valueConvertingId)
                ));
    }

    public void publishMissingValueConvertingKeyErrorEvent(InstanceFlowHeaders instanceFlowHeaders, Long valueConvertingId, String valueConvertingKey) {
        publishErrorEvent(
                instanceFlowHeaders,
                ErrorCode.VALUE_CONVERTING_KEY_NOT_FOUND,
                Map.of(
                        "valueConvertingId", String.valueOf(valueConvertingId),
                        "valueConvertingKey", valueConvertingKey
                ));
    }

    public void publishGeneralSystemErrorEvent(InstanceFlowHeaders instanceFlowHeaders) {
        publishErrorEventWithASingleErrorCode(instanceFlowHeaders, ErrorCode.GENERAL_SYSTEM_ERROR);
    }


    private void publishErrorEvent(InstanceFlowHeaders instanceFlowHeaders, ErrorCode errorCode, Map<String, String> args) {
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<ErrorCollection>builder()
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .topicNameParameters(errorEventTopicNameParameters)
                        .value(new ErrorCollection(Error
                                .builder()
                                .errorCode(errorCode.getCode())
                                .args(args)
                                .build()))
                        .build()
        );
    }


    private void publishErrorEventWithASingleErrorCode(InstanceFlowHeaders instanceFlowHeaders, ErrorCode errorCode) {
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<ErrorCollection>builder()
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .topicNameParameters(errorEventTopicNameParameters)
                        .value(new ErrorCollection(Error
                                .builder()
                                .errorCode(errorCode.getCode())
                                .build()))
                        .build()
        );
    }

}
