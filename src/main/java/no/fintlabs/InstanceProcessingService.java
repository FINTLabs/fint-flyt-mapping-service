package no.fintlabs;

import no.fintlabs.exception.ConfigurationNotFoundException;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.InstanceMappedEventProducerService;
import no.fintlabs.kafka.configuration.ActiveConfigurationIdRequestProducerService;
import no.fintlabs.kafka.configuration.ConfigurationMappingRequestProducerService;
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

    public InstanceProcessingService(
            ConfigurationMappingRequestProducerService configurationMappingRequestProducerService,
            InstanceMappedEventProducerService instanceMappedEventProducerService,
            ActiveConfigurationIdRequestProducerService activeConfigurationIdRequestProducerService,
            InstanceMappingService instanceMappingService
    ) {
        this.configurationMappingRequestProducerService = configurationMappingRequestProducerService;
        this.instanceMappedEventProducerService = instanceMappedEventProducerService;
        this.activeConfigurationIdRequestProducerService = activeConfigurationIdRequestProducerService;
        this.instanceMappingService = instanceMappingService;
    }

    public void process(InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord) {
        InstanceFlowHeaders consumerRecordInstanceFlowHeaders = flytConsumerRecord.getInstanceFlowHeaders();

        InstanceObject instance = flytConsumerRecord.getConsumerRecord().value();

        Long integrationId = consumerRecordInstanceFlowHeaders.getIntegrationId();

        Long configurationId = this.activeConfigurationIdRequestProducerService.get(integrationId)
                .orElseThrow(() -> ConfigurationNotFoundException.fromIntegrationId(integrationId));

        ObjectMapping objectMapping = this.configurationMappingRequestProducerService.get(configurationId)
                .orElseThrow(() -> ConfigurationNotFoundException.fromConfigurationId(configurationId));

        Object mappedInstance = this.instanceMappingService.toMappedInstanceObject(objectMapping, instance);

        InstanceFlowHeaders instanceFlowHeaders = consumerRecordInstanceFlowHeaders.toBuilder()
                .configurationId(configurationId)
                .build();

        instanceMappedEventProducerService.publish(instanceFlowHeaders, mappedInstance);
    }

}
