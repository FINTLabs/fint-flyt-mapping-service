package no.fintlabs;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.InstanceMappedEventProducerService;
import no.fintlabs.kafka.configuration.ActiveConfigurationIdRequestProducerService;
import no.fintlabs.kafka.configuration.ConfigurationElementMappingRequestProducerService;
import no.fintlabs.mapping.InstanceMappingService;
import no.fintlabs.model.configuration.ObjectMapping;
import no.fintlabs.model.instance.InstanceObject;
import org.springframework.stereotype.Service;

@Service
public class InstanceProcessingService {

    private final ConfigurationElementMappingRequestProducerService configurationElementMappingRequestProducerService;
    private final InstanceMappedEventProducerService instanceMappedEventProducerService;
    private final ActiveConfigurationIdRequestProducerService activeConfigurationIdRequestProducerService;
    private final InstanceMappingService instanceMappingService;

    public InstanceProcessingService(
            ConfigurationElementMappingRequestProducerService configurationElementMappingRequestProducerService,
            InstanceMappedEventProducerService instanceMappedEventProducerService,
            ActiveConfigurationIdRequestProducerService activeConfigurationIdRequestProducerService,
            InstanceMappingService instanceMappingService
    ) {
        this.configurationElementMappingRequestProducerService = configurationElementMappingRequestProducerService;
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

        ObjectMapping objectMapping = this.configurationElementMappingRequestProducerService.get(configurationId)
                .orElseThrow(() -> ConfigurationNotFoundException.fromConfigurationId(configurationId));

        Object mappedInstance = this.instanceMappingService.toMappedInstanceElement(objectMapping, instance);

        InstanceFlowHeaders instanceFlowHeaders = consumerRecordInstanceFlowHeaders.toBuilder()
                .configurationId(configurationId)
                .build();

        instanceMappedEventProducerService.publish(instanceFlowHeaders, mappedInstance);
    }

}
