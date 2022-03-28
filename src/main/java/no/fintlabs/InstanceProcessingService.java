package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.integration.CaseEventProducerService;
import no.fintlabs.integration.SkjemaConfigurationRequestService;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import no.fintlabs.model.instance.Instance;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InstanceProcessingService {

    private final SkjemaConfigurationRequestService skjemaConfigurationRequestService;
    private final CaseService caseService;
    private final CaseEventProducerService caseEventProducerService;

    public InstanceProcessingService(
            SkjemaConfigurationRequestService skjemaConfigurationRequestService,
            CaseService caseService,
            CaseEventProducerService caseEventProducerService
    ) {
        this.skjemaConfigurationRequestService = skjemaConfigurationRequestService;
        this.caseService = caseService;
        this.caseEventProducerService = caseEventProducerService;
    }

    public void process(Instance instance) {
        String integrationId = "TODO"; // TODO: 28/01/2022 Get from skjema headers
        IntegrationConfiguration integrationConfiguration = this.skjemaConfigurationRequestService.get(integrationId)
                .orElseThrow(() -> new NoSuchIntegrationConfigurationException(integrationId));
        SakResource newOrUpdatedCase = this.caseService.createOrUpdateCase(integrationConfiguration, instance);
        caseEventProducerService.sendNewOrUpdatedCase(newOrUpdatedCase);
    }

}
