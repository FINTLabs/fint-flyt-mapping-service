package no.fintlabs;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.integration.InstanceMappedEventProducerService;
import no.fintlabs.integration.configuration.ActiveConfigurationIdRequestProducerService;
import no.fintlabs.integration.configuration.ConfigurationRequestService;
import no.fintlabs.mapping.InstanceMappingService;
import no.fintlabs.model.configuration.Configuration;
import no.fintlabs.model.instance.Instance;
import no.fintlabs.model.mappedinstance.MappedInstance;
import org.springframework.stereotype.Service;

@Service
public class InstanceProcessingService {

    private final ConfigurationRequestService configurationRequestService;
    private final InstanceMappedEventProducerService instanceMappedEventProducerService;
    private final ActiveConfigurationIdRequestProducerService activeConfigurationIdRequestProducerService;
    private final InstanceMappingService instanceMappingService;

    public InstanceProcessingService(
            ConfigurationRequestService configurationRequestService,
            InstanceMappedEventProducerService instanceMappedEventProducerService,
            ActiveConfigurationIdRequestProducerService activeConfigurationIdRequestProducerService,
            InstanceMappingService instanceMappingService
    ) {
        this.configurationRequestService = configurationRequestService;
        this.instanceMappedEventProducerService = instanceMappedEventProducerService;
        this.activeConfigurationIdRequestProducerService = activeConfigurationIdRequestProducerService;
        this.instanceMappingService = instanceMappingService;
    }

    public void process(InstanceFlowConsumerRecord<Instance> flytConsumerRecord) {
        InstanceFlowHeaders consumerRecordInstanceFlowHeaders = flytConsumerRecord.getInstanceFlowHeaders();

        Instance instance = flytConsumerRecord.getConsumerRecord().value();

        Long integrationId = consumerRecordInstanceFlowHeaders.getIntegrationId();

        Long configurationId = this.activeConfigurationIdRequestProducerService.get(integrationId)
                .orElseThrow(() -> ConfigurationNotFoundException.fromConfigurationId(integrationId));

        Configuration configuration = this.configurationRequestService.get(configurationId)
                .orElseThrow(() -> ConfigurationNotFoundException.fromConfigurationId(configurationId));

        MappedInstance mappedInstance = this.instanceMappingService.map(instance, configuration);

        InstanceFlowHeaders instanceFlowHeaders = consumerRecordInstanceFlowHeaders.toBuilder()
                .configurationId(configurationId)
                .build();

        instanceMappedEventProducerService.publish(instanceFlowHeaders, mappedInstance);

    }

}
