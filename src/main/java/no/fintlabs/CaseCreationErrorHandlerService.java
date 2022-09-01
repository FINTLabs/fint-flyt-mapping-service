package no.fintlabs;

import no.fintlabs.flyt.kafka.InstanceFlowErrorHandler;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.integration.error.CaseCreationErrorEventProducerService;
import no.fintlabs.validation.exceptions.MissingInstanceFieldsValidationException;
import no.fintlabs.validation.exceptions.MissingMappingFieldsValidationException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
public class CaseCreationErrorHandlerService extends InstanceFlowErrorHandler {

    private final CaseCreationErrorEventProducerService caseCreationErrorEventProducerService;

    public CaseCreationErrorHandlerService(
            InstanceFlowHeadersMapper instanceFlowHeadersMapper,
            CaseCreationErrorEventProducerService caseCreationErrorEventProducerService
    ) {
        super(instanceFlowHeadersMapper);
        this.caseCreationErrorEventProducerService = caseCreationErrorEventProducerService;
    }

    @Override
    public void handleInstanceFlowRecord(Throwable cause, InstanceFlowHeaders instanceFlowHeaders, ConsumerRecord<?, ?> consumerRecord) {
        if (cause instanceof MissingInstanceFieldsValidationException) {
            caseCreationErrorEventProducerService.publishMissingInstanceFieldsErrorEvent(
                    instanceFlowHeaders,
                    (MissingInstanceFieldsValidationException) cause
            );
        } else if (cause instanceof MissingMappingFieldsValidationException) {
            caseCreationErrorEventProducerService.publishMissingMappingFieldsErrorEvent(
                    instanceFlowHeaders,
                    (MissingMappingFieldsValidationException) cause
            );
        } else if (cause instanceof NoSuchIntegrationConfigurationException) {
            caseCreationErrorEventProducerService.publishNoConfigurationForIntegrationErrorEvent(instanceFlowHeaders);
        } else if (cause instanceof RuntimeException) {
            caseCreationErrorEventProducerService.publishGeneralSystemErrorEvent(instanceFlowHeaders);
        }
    }

}
