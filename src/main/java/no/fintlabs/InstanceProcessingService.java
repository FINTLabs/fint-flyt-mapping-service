package no.fintlabs;

import no.fintlabs.exception.InstanceFieldNotFoundException;
import no.fintlabs.exception.ValueConvertingKeyNotFoundException;
import no.fintlabs.exception.ValueConvertingNotFoundException;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.InstanceMappedEventProducerService;
import no.fintlabs.kafka.configuration.ActiveConfigurationIdRequestProducerService;
import no.fintlabs.kafka.configuration.ConfigurationMappingRequestProducerService;
import no.fintlabs.kafka.error.InstanceMappingErrorEventProducerService;
import no.fintlabs.mapping.InstanceMappingService;
import no.fintlabs.model.configuration.ObjectMapping;
import no.fintlabs.model.instance.InstanceObject;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
            Optional<Long> configurationIdOptional = this.activeConfigurationIdRequestProducerService.get(
                    flytConsumerRecord.getInstanceFlowHeaders().getIntegrationId()
            );
            if (configurationIdOptional.isEmpty()) {
                instanceMappingErrorEventProducerService.publishConfigurationNotFoundErrorEvent(
                        flytConsumerRecord.getInstanceFlowHeaders()
                );
                return;
            }
            Long configurationId = configurationIdOptional.get();

            Optional<ObjectMapping> objectMappingOptional = this.configurationMappingRequestProducerService.get(configurationId);
            if (objectMappingOptional.isEmpty()) {
                instanceMappingErrorEventProducerService.publishConfigurationNotFoundErrorEvent(
                        flytConsumerRecord.getInstanceFlowHeaders()
                );
                return;
            }
            ObjectMapping objectMapping = objectMappingOptional.get();

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

}
