package no.fintlabs;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.InstanceMappedEventProducerService;
import no.fintlabs.kafka.configuration.ActiveConfigurationIdRequestProducerService;
import no.fintlabs.kafka.configuration.ConfigurationElementsRequestProducerService;
import no.fintlabs.mapping.InstanceMappingService;
import no.fintlabs.model.configuration.ConfigurationElement;
import no.fintlabs.model.instance.Instance;
import no.fintlabs.model.mappedinstance.MappedInstance;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class InstanceProcessingService {

    private final ConfigurationElementsRequestProducerService configurationElementsRequestProducerService;
    private final InstanceMappedEventProducerService instanceMappedEventProducerService;
    private final ActiveConfigurationIdRequestProducerService activeConfigurationIdRequestProducerService;
    private final InstanceMappingService instanceMappingService;

    public InstanceProcessingService(
            ConfigurationElementsRequestProducerService configurationElementsRequestProducerService,
            InstanceMappedEventProducerService instanceMappedEventProducerService,
            ActiveConfigurationIdRequestProducerService activeConfigurationIdRequestProducerService,
            InstanceMappingService instanceMappingService
    ) {
        this.configurationElementsRequestProducerService = configurationElementsRequestProducerService;
        this.instanceMappedEventProducerService = instanceMappedEventProducerService;
        this.activeConfigurationIdRequestProducerService = activeConfigurationIdRequestProducerService;
        this.instanceMappingService = instanceMappingService;
    }

    public void process(InstanceFlowConsumerRecord<Instance> flytConsumerRecord) {
        InstanceFlowHeaders consumerRecordInstanceFlowHeaders = flytConsumerRecord.getInstanceFlowHeaders();

        Instance instance = flytConsumerRecord.getConsumerRecord().value();

        Long integrationId = consumerRecordInstanceFlowHeaders.getIntegrationId();

        Long configurationId = this.activeConfigurationIdRequestProducerService.get(integrationId)
                .orElseThrow(() -> ConfigurationNotFoundException.fromIntegrationId(integrationId));

        Collection<ConfigurationElement> configurationElements = this.configurationElementsRequestProducerService.get(configurationId)
                .orElseThrow(() -> ConfigurationNotFoundException.fromConfigurationId(configurationId));

        MappedInstance mappedInstance = this.instanceMappingService.map(instance, configurationElements);

        InstanceFlowHeaders instanceFlowHeaders = consumerRecordInstanceFlowHeaders.toBuilder()
                .configurationId(configurationId)
                .build();

        instanceMappedEventProducerService.publish(instanceFlowHeaders, mappedInstance);

    }

}
