package no.fintlabs;

import no.fintlabs.exception.ConfigurationNotFoundException;
import no.fintlabs.exception.InstanceFieldNotFoundException;
import no.fintlabs.exception.ValueConvertingNotFoundException;
import no.fintlabs.exception.ValueConvertingKeyNotFoundException;
import no.fintlabs.flyt.kafka.InstanceFlowErrorHandler;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.error.InstanceMappingErrorEventProducerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
public class InstanceMappingErrorHandlerService extends InstanceFlowErrorHandler {

    private final InstanceMappingErrorEventProducerService instanceMappingErrorEventProducerService;

    public InstanceMappingErrorHandlerService(
            InstanceFlowHeadersMapper instanceFlowHeadersMapper,
            InstanceMappingErrorEventProducerService instanceMappingErrorEventProducerService
    ) {
        super(instanceFlowHeadersMapper);
        this.instanceMappingErrorEventProducerService = instanceMappingErrorEventProducerService;
    }

    @Override
    public void handleInstanceFlowRecord(Throwable cause, InstanceFlowHeaders instanceFlowHeaders, ConsumerRecord<?, ?> consumerRecord) {
        if (cause instanceof ConfigurationNotFoundException) {
            instanceMappingErrorEventProducerService.publishConfigurationNotFoundErrorEvent(instanceFlowHeaders);
        } else if (cause instanceof ValueConvertingNotFoundException) {
            instanceMappingErrorEventProducerService.publishMissingValueConvertingErrorEvent(
                    instanceFlowHeaders,
                    ((ValueConvertingNotFoundException) cause).getValueConvertingId()
            );
        } else if (cause instanceof ValueConvertingKeyNotFoundException) {
            instanceMappingErrorEventProducerService.publishMissingValueConvertingKeyErrorEvent(
                    instanceFlowHeaders,
                    ((ValueConvertingKeyNotFoundException) cause).getValueConvertingId(),
                    ((ValueConvertingKeyNotFoundException) cause).getValueConvertingKey()
            );
        } else if (cause instanceof InstanceFieldNotFoundException) {
            instanceMappingErrorEventProducerService.publishInstanceFieldNotFoundErrorEvent(
                    instanceFlowHeaders,
                    ((InstanceFieldNotFoundException) cause).getInstanceFieldKey()
            );
        } else if (cause instanceof RuntimeException) {
            instanceMappingErrorEventProducerService.publishGeneralSystemErrorEvent(instanceFlowHeaders);
        }
    }

}
