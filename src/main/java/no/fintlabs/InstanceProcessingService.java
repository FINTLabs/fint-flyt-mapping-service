package no.fintlabs;

import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.integration.CaseEventProducerService;
import no.fintlabs.integration.SkjemaConfigurationRequestService;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import no.fintlabs.model.instance.Instance;
import org.springframework.stereotype.Service;

@Service
public class InstanceProcessingService {

    private final SkjemaConfigurationRequestService skjemaConfigurationRequestService;
    private final CaseService caseService;
    private final CaseEventProducerService caseEventProducerService;

    public InstanceProcessingService(
            SkjemaConfigurationRequestService skjemaConfigurationRequestService,
            CaseService caseService,
            CaseEventProducerService caseEventProducerService) {
        this.skjemaConfigurationRequestService = skjemaConfigurationRequestService;
        this.caseService = caseService;
        this.caseEventProducerService = caseEventProducerService;
    }

    public void process(InstanceFlowConsumerRecord<Instance> flytConsumerRecord) {
        InstanceFlowHeaders consumerRecordInstanceFlowHeaders = flytConsumerRecord.getInstanceFlowHeaders();

        String sourceApplicationIntegrationId = consumerRecordInstanceFlowHeaders.getSourceApplicationIntegrationId();
        IntegrationConfiguration integrationConfiguration = this.skjemaConfigurationRequestService.get(sourceApplicationIntegrationId)
                .orElseThrow(() -> new NoSuchIntegrationConfigurationException(sourceApplicationIntegrationId));

        SakResource newOrUpdatedCase = this.caseService.createOrUpdateCase(integrationConfiguration, flytConsumerRecord.getConsumerRecord().value());

        InstanceFlowHeaders instanceFlowHeaders = consumerRecordInstanceFlowHeaders.toBuilder()
                .configurationId(String.valueOf(integrationConfiguration.getId()))
                .build();

        caseEventProducerService.sendNewOrUpdatedCase(instanceFlowHeaders, newOrUpdatedCase);
    }

}
