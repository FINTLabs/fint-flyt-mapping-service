package no.novari.flyt.mapping;

import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.novari.flyt.mapping.exception.InstanceFieldNotFoundException;
import no.novari.flyt.mapping.exception.ValueConvertingKeyNotFoundException;
import no.novari.flyt.mapping.exception.ValueConvertingNotFoundException;
import no.novari.flyt.mapping.kafka.InstanceMappedEventProducerService;
import no.novari.flyt.mapping.kafka.configuration.ActiveConfigurationIdRequestProducerService;
import no.novari.flyt.mapping.kafka.configuration.ConfigurationMappingRequestProducerService;
import no.novari.flyt.mapping.kafka.error.InstanceMappingErrorEventProducerService;
import no.novari.flyt.mapping.model.configuration.ObjectMapping;
import no.novari.flyt.mapping.model.instance.InstanceObject;
import no.novari.flyt.mapping.service.InstanceMappingService;
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
        } catch (ValueConvertingNotFoundException exception) {
            instanceMappingErrorEventProducerService.publishMissingValueConvertingErrorEvent(
                    flytConsumerRecord.getInstanceFlowHeaders(),
                    exception.getValueConvertingId()
            );
            return;
        } catch (ValueConvertingKeyNotFoundException exception) {
            instanceMappingErrorEventProducerService.publishMissingValueConvertingKeyErrorEvent(
                    flytConsumerRecord.getInstanceFlowHeaders(),
                    exception.getValueConvertingId(),
                    exception.getValueConvertingKey()
            );
            return;
        } catch (InstanceFieldNotFoundException exception) {
            instanceMappingErrorEventProducerService.publishInstanceFieldNotFoundErrorEvent(
                    flytConsumerRecord.getInstanceFlowHeaders(),
                    exception.getInstanceFieldKey()
            );
            return;
        }

        InstanceFlowHeaders instanceFlowHeaders = flytConsumerRecord.getInstanceFlowHeaders().toBuilder()
                .configurationId(configurationId)
                .build();

        instanceMappedEventProducerService.publish(instanceFlowHeaders, mappedInstance);
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
