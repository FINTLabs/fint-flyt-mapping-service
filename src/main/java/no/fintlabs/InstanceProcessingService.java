package no.fintlabs;

import no.fintlabs.exception.InstanceFieldNotFoundException;
import no.fintlabs.exception.ValueConvertingKeyNotFoundException;
import no.fintlabs.exception.ValueConvertingNotFoundException;
import no.fintlabs.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.InstanceMappedEventProducerService;
import no.fintlabs.kafka.configuration.ActiveConfigurationIdRequestProducerService;
import no.fintlabs.kafka.configuration.ConfigurationMappingRequestProducerService;
import no.fintlabs.kafka.error.InstanceMappingErrorEventProducerService;
import no.fintlabs.mapping.InstanceMappingService;
import no.fintlabs.model.configuration.ObjectMapping;
import no.fintlabs.model.instance.InstanceObject;
import org.springframework.stereotype.Service;

@Service
public class InstanceProcessingService {

    private final ConfigurationMappingRequestProducerService configurationMappingRequestProducerService;
    private final InstanceMappedEventProducerService instanceMappedEventProducerService;
    private final ActiveConfigurationIdRequestProducerService activeConfigurationIdRequestProducerService;
    private final InstanceMappingService instanceMappingService;
    private final InstanceMappingErrorEventProducerService instanceMappingErrorEventProducerService;

    public InstanceProcessingService(
            ConfigurationMappingRequestProducerService configurationMappingRequestProducerService,
            InstanceMappedEventProducerService instanceMappedEventProducerService,
            ActiveConfigurationIdRequestProducerService activeConfigurationIdRequestProducerService,
            InstanceMappingService instanceMappingService,
            InstanceMappingErrorEventProducerService instanceMappingErrorEventProducerService
    ) {
        this.configurationMappingRequestProducerService = configurationMappingRequestProducerService;
        this.instanceMappedEventProducerService = instanceMappedEventProducerService;
        this.activeConfigurationIdRequestProducerService = activeConfigurationIdRequestProducerService;
        this.instanceMappingService = instanceMappingService;
        this.instanceMappingErrorEventProducerService = instanceMappingErrorEventProducerService;
    }

    public void process(InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord) {
        try {
            Long configurationId = getConfigurationIdOrPublishError(flytConsumerRecord);
            if (configurationId == null) return;

            ObjectMapping objectMapping = getObjectMappingOrPublishError(flytConsumerRecord, configurationId);
            if (objectMapping == null) return;

            Object mappedInstance;
            try {
                mappedInstance = instanceMappingService.toMappedInstanceObject(
                        objectMapping,
                        flytConsumerRecord.getConsumerRecord().value()
                );
            } catch (ValueConvertingNotFoundException e) {
                instanceMappingErrorEventProducerService.publishMissingValueConvertingErrorEvent(
                        flytConsumerRecord.getInstanceFlowHeaders(),
                        e.getValueConvertingId()
                );
                return;
            } catch (ValueConvertingKeyNotFoundException e) {
                instanceMappingErrorEventProducerService.publishMissingValueConvertingKeyErrorEvent(
                        flytConsumerRecord.getInstanceFlowHeaders(),
                        e.getValueConvertingId(),
                        e.getValueConvertingKey()
                );
                return;
            } catch (InstanceFieldNotFoundException e) {
                instanceMappingErrorEventProducerService.publishInstanceFieldNotFoundErrorEvent(
                        flytConsumerRecord.getInstanceFlowHeaders(),
                        e.getInstanceFieldKey()
                );
                return;
            }

            InstanceFlowHeaders instanceFlowHeaders = flytConsumerRecord.getInstanceFlowHeaders().toBuilder()
                    .configurationId(configurationId)
                    .build();

            instanceMappedEventProducerService.publish(instanceFlowHeaders, mappedInstance);

        } catch (RuntimeException e) {
            instanceMappingErrorEventProducerService.publishGeneralSystemErrorEvent(
                    flytConsumerRecord.getInstanceFlowHeaders()
            );
        }
    }

    private Long getConfigurationIdOrPublishError(InstanceFlowConsumerRecord<InstanceObject> record) {
        var configurationIdOptional = activeConfigurationIdRequestProducerService.get(
                record.getInstanceFlowHeaders().getIntegrationId()
        );
        if (configurationIdOptional.isEmpty()) {
            instanceMappingErrorEventProducerService.publishConfigurationNotFoundErrorEvent(
                    record.getInstanceFlowHeaders()
            );
            return null;
        }
        return configurationIdOptional.get();
    }

    private ObjectMapping getObjectMappingOrPublishError(InstanceFlowConsumerRecord<InstanceObject> record, Long configurationId) {
        var objectMappingOptional = configurationMappingRequestProducerService.get(configurationId);
        if (objectMappingOptional.isEmpty()) {
            instanceMappingErrorEventProducerService.publishConfigurationNotFoundErrorEvent(
                    record.getInstanceFlowHeaders()
            );
            return null;
        }
        return objectMappingOptional.get();
    }

}
