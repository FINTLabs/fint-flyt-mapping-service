package no.fintlabs.integration.error;

import no.fintlabs.integration.error.mappers.MissingInstanceFieldsErrorMapper;
import no.fintlabs.integration.error.mappers.MissingMappingFieldsErrorMapper;
import no.fintlabs.kafka.event.error.Error;
import no.fintlabs.kafka.event.error.*;
import no.fintlabs.validation.exceptions.MissingInstanceFieldsValidationException;
import no.fintlabs.validation.exceptions.MissingMappingFieldsValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ErrorEventProducerService {

    private final ErrorEventProducer errorEventProducer;
    private final ErrorEventTopicNameParameters instanceToCaseMappingErrorTopicNameParameters;
    private final MissingInstanceFieldsErrorMapper missingInstanceFieldsErrorMapper;
    private final MissingMappingFieldsErrorMapper missingMappingFieldsErrorMapper;

    public ErrorEventProducerService(
            @Value("${fint.org-id}") String orgId,
            ErrorEventProducer errorEventProducer,
            MissingInstanceFieldsErrorMapper missingInstanceFieldsErrorMapper,
            MissingMappingFieldsErrorMapper missingMappingFieldsErrorMapper,
            ErrorEventTopicService errorEventTopicService
    ) {
        this.errorEventProducer = errorEventProducer;
        this.instanceToCaseMappingErrorTopicNameParameters = ErrorEventTopicNameParameters.builder()
                .orgId(orgId)
                .domainContext("skjema")
                .errorEventName("instance-to-case-mapping")
                .build();
        this.missingInstanceFieldsErrorMapper = missingInstanceFieldsErrorMapper;
        this.missingMappingFieldsErrorMapper = missingMappingFieldsErrorMapper;

        errorEventTopicService.ensureTopic(instanceToCaseMappingErrorTopicNameParameters, 0);
    }

    public void sendMissingInstanceFieldsErrorEvent(MissingInstanceFieldsValidationException e) {
        errorEventProducer.send(
                ErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceToCaseMappingErrorTopicNameParameters)
                        .errorCollection(missingInstanceFieldsErrorMapper.map(e))
                        .build()
        );
    }

    public void sendMissingMappingFieldsErrorEvent(MissingMappingFieldsValidationException e) {
        errorEventProducer.send(
                ErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceToCaseMappingErrorTopicNameParameters)
                        .errorCollection(missingMappingFieldsErrorMapper.map(e))
                        .build()
        );
    }

    public void sendNoConfigurationForIntegrationErrorEvent() {
        sendErrorEventWithASingleErrorCode(ErrorCode.NO_CONFIGURATION_FOR_INTEGRATION);
    }

    public void sendGeneralSystemErrorEvent() {
        sendErrorEventWithASingleErrorCode(ErrorCode.GENERAL_SYSTEM_ERROR);
    }

    private void sendErrorEventWithASingleErrorCode(ErrorCode errorCode) {
        errorEventProducer.send(
                ErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceToCaseMappingErrorTopicNameParameters)
                        .errorCollection(new ErrorCollection(Error.builder().errorCode(errorCode.getCode()).build()))
                        .build()
        );
    }

}
