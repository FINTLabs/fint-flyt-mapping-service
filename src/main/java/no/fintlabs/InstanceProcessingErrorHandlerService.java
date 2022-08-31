package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.kafka.InstanceFlowErrorHandler;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.integration.error.ErrorEventProducerService;
import no.fintlabs.validation.exceptions.MissingInstanceFieldsValidationException;
import no.fintlabs.validation.exceptions.MissingMappingFieldsValidationException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
public class InstanceProcessingErrorHandlerService extends InstanceFlowErrorHandler {

    private final ErrorEventProducerService errorEventProducerService;

    public InstanceProcessingErrorHandlerService(
            InstanceFlowHeadersMapper instanceFlowHeadersMapper,
            ErrorEventProducerService errorEventProducerService
    ) {
        super(instanceFlowHeadersMapper);
        this.errorEventProducerService = errorEventProducerService;
    }

    @Override
    public void handleInstanceFlowRecord(Throwable cause, InstanceFlowHeaders instanceFlowHeaders, ConsumerRecord<?, ?> consumerRecord) {
        if (cause instanceof MissingInstanceFieldsValidationException) {
            errorEventProducerService.sendMissingInstanceFieldsErrorEvent(
                    instanceFlowHeaders,
                    (MissingInstanceFieldsValidationException) cause
            );
        } else if (cause instanceof MissingMappingFieldsValidationException) {
            errorEventProducerService.sendMissingMappingFieldsErrorEvent(
                    instanceFlowHeaders,
                    (MissingMappingFieldsValidationException) cause
            );
        } else if (cause instanceof NoSuchIntegrationConfigurationException) {
            errorEventProducerService.sendNoConfigurationForIntegrationErrorEvent(instanceFlowHeaders);
        } else if (cause instanceof RuntimeException) {
            errorEventProducerService.sendGeneralSystemErrorEvent(instanceFlowHeaders);
        }
    }

}
