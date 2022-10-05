package no.fintlabs;

import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.integration.CaseCreatedEventProducerService;
import no.fintlabs.integration.SkjemaConfigurationRequestService;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import no.fintlabs.model.instance.Instance;
import org.springframework.stereotype.Service;

@Service
public class InstanceProcessingService {

    private final SkjemaConfigurationRequestService skjemaConfigurationRequestService;
    private final CaseService caseService;
    private final CaseCreatedEventProducerService caseCreatedEventProducerService;

    public InstanceProcessingService(
            SkjemaConfigurationRequestService skjemaConfigurationRequestService,
            CaseService caseService,
            CaseCreatedEventProducerService caseCreatedEventProducerService) {
        this.skjemaConfigurationRequestService = skjemaConfigurationRequestService;
        this.caseService = caseService;
        this.caseCreatedEventProducerService = caseCreatedEventProducerService;
    }

    public void process(InstanceFlowConsumerRecord<Instance> flytConsumerRecord) {
        InstanceFlowHeaders consumerRecordInstanceFlowHeaders = flytConsumerRecord.getInstanceFlowHeaders();

        String sourceApplicationIntegrationId = consumerRecordInstanceFlowHeaders.getSourceApplicationIntegrationId();
        IntegrationConfiguration integrationConfiguration = this.skjemaConfigurationRequestService.get(sourceApplicationIntegrationId)
                .orElseThrow(() -> new NoSuchIntegrationConfigurationException(sourceApplicationIntegrationId));

        SakResource newOrUpdatedCase = this.caseService.createOrUpdateCase(integrationConfiguration, flytConsumerRecord.getConsumerRecord().value());

        InstanceFlowHeaders instanceFlowHeaders = consumerRecordInstanceFlowHeaders.toBuilder()
                .configurationId(integrationConfiguration.getId())
                .build();

        caseCreatedEventProducerService.publish(instanceFlowHeaders, newOrUpdatedCase);
    }

}
