package no.fintlabs.integration.error;

import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducer;
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.integration.error.mappers.MissingInstanceFieldsErrorMapper;
import no.fintlabs.integration.error.mappers.MissingMappingFieldsErrorMapper;
import no.fintlabs.kafka.event.error.Error;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicService;
import no.fintlabs.validation.exceptions.MissingInstanceFieldsValidationException;
import no.fintlabs.validation.exceptions.MissingMappingFieldsValidationException;
import org.springframework.stereotype.Service;

@Service
public class CaseCreationErrorEventProducerService {

    private final InstanceFlowErrorEventProducer instanceFlowErrorEventProducer;
    private final ErrorEventTopicNameParameters instanceToCaseMappingErrorTopicNameParameters;
    private final MissingInstanceFieldsErrorMapper missingInstanceFieldsErrorMapper;
    private final MissingMappingFieldsErrorMapper missingMappingFieldsErrorMapper;

    public CaseCreationErrorEventProducerService(
            InstanceFlowErrorEventProducer instanceFlowErrorEventProducer,
            MissingInstanceFieldsErrorMapper missingInstanceFieldsErrorMapper,
            MissingMappingFieldsErrorMapper missingMappingFieldsErrorMapper,
            ErrorEventTopicService errorEventTopicService
    ) {
        this.instanceFlowErrorEventProducer = instanceFlowErrorEventProducer;
        this.instanceToCaseMappingErrorTopicNameParameters = ErrorEventTopicNameParameters.builder()
                .errorEventName("case-creation-error")
                .build();
        this.missingInstanceFieldsErrorMapper = missingInstanceFieldsErrorMapper;
        this.missingMappingFieldsErrorMapper = missingMappingFieldsErrorMapper;

        errorEventTopicService.ensureTopic(instanceToCaseMappingErrorTopicNameParameters, 0);
    }

    public void publishMissingInstanceFieldsErrorEvent(InstanceFlowHeaders instanceFlowHeaders, MissingInstanceFieldsValidationException e) {
        instanceFlowErrorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceToCaseMappingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(missingInstanceFieldsErrorMapper.map(e))
                        .build()
        );
    }

    public void publishMissingMappingFieldsErrorEvent(InstanceFlowHeaders instanceFlowHeaders, MissingMappingFieldsValidationException e) {
        instanceFlowErrorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceToCaseMappingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(missingMappingFieldsErrorMapper.map(e))
                        .build()
        );
    }

    public void publishNoConfigurationForIntegrationErrorEvent(InstanceFlowHeaders instanceFlowHeaders) {
        publishErrorEventWithASingleErrorCode(instanceFlowHeaders, ErrorCode.NO_CONFIGURATION_FOR_INTEGRATION);
    }

    public void publishGeneralSystemErrorEvent(InstanceFlowHeaders instanceFlowHeaders) {
        publishErrorEventWithASingleErrorCode(instanceFlowHeaders, ErrorCode.GENERAL_SYSTEM_ERROR);
    }

    private void publishErrorEventWithASingleErrorCode(InstanceFlowHeaders instanceFlowHeaders, ErrorCode errorCode) {
        instanceFlowErrorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceToCaseMappingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(new ErrorCollection(Error.builder().errorCode(errorCode.getCode()).build()))
                        .build()
        );
    }

}
