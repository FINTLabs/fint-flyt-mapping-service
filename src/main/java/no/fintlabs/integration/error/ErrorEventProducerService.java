package no.fintlabs.integration.error;

import no.fintlabs.flyt.kafka.event.error.FlytErrorEventProducer;
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
public class ErrorEventProducerService {

    private final FlytErrorEventProducer errorEventProducer;
    private final ErrorEventTopicNameParameters instanceToCaseMappingErrorTopicNameParameters;
    private final MissingInstanceFieldsErrorMapper missingInstanceFieldsErrorMapper;
    private final MissingMappingFieldsErrorMapper missingMappingFieldsErrorMapper;

    public ErrorEventProducerService(
            FlytErrorEventProducer errorEventProducer,
            MissingInstanceFieldsErrorMapper missingInstanceFieldsErrorMapper,
            MissingMappingFieldsErrorMapper missingMappingFieldsErrorMapper,
            ErrorEventTopicService errorEventTopicService
    ) {
        this.errorEventProducer = errorEventProducer;
        this.instanceToCaseMappingErrorTopicNameParameters = ErrorEventTopicNameParameters.builder()
                .errorEventName("instance-to-case-mapping")
                .build();
        this.missingInstanceFieldsErrorMapper = missingInstanceFieldsErrorMapper;
        this.missingMappingFieldsErrorMapper = missingMappingFieldsErrorMapper;

        errorEventTopicService.ensureTopic(instanceToCaseMappingErrorTopicNameParameters, 0);
    }

    public void sendMissingInstanceFieldsErrorEvent(InstanceFlowHeaders instanceFlowHeaders, MissingInstanceFieldsValidationException e) {
        errorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceToCaseMappingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(missingInstanceFieldsErrorMapper.map(e))
                        .build()
        );
    }

    public void sendMissingMappingFieldsErrorEvent(InstanceFlowHeaders instanceFlowHeaders, MissingMappingFieldsValidationException e) {
        errorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceToCaseMappingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(missingMappingFieldsErrorMapper.map(e))
                        .build()
        );
    }

    public void sendNoConfigurationForIntegrationErrorEvent(InstanceFlowHeaders instanceFlowHeaders) {
        sendErrorEventWithASingleErrorCode(instanceFlowHeaders, ErrorCode.NO_CONFIGURATION_FOR_INTEGRATION);
    }

    public void sendGeneralSystemErrorEvent(InstanceFlowHeaders instanceFlowHeaders) {
        sendErrorEventWithASingleErrorCode(instanceFlowHeaders, ErrorCode.GENERAL_SYSTEM_ERROR);
    }

    private void sendErrorEventWithASingleErrorCode(InstanceFlowHeaders instanceFlowHeaders, ErrorCode errorCode) {
        errorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceToCaseMappingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(new ErrorCollection(Error.builder().errorCode(errorCode.getCode()).build()))
                        .build()
        );
    }

}
