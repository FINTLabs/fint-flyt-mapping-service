package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.integration.CaseEventProducerService;
import no.fintlabs.integration.SkjemaConfigurationRequestService;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import no.fintlabs.model.instance.Instance;
import no.fintlabs.validation.exceptions.ValidationException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

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
        try {
            String skjemaId = "TODO"; // TODO: 28/01/2022 Get from instance
            IntegrationConfiguration integrationConfiguration = this.skjemaConfigurationRequestService.get(skjemaId)
                    .orElseThrow(() -> new NoSuchElementException("No skjema with id=" + skjemaId));

            SakResource newOrUpdatedCase;
            try {
                newOrUpdatedCase = this.caseService.createOrUpdateCase(integrationConfiguration, instance);
            } catch (ValidationException e) {
                log.error("Validation error", e);
                // TODO: 28/01/2022 Publish validation error event?
                return;
            }
            caseEventProducerService.newOrUpdatedTopic(newOrUpdatedCase);
        } catch (Exception e) {
            log.error("Could not process instance", e);
            // TODO: 28/01/2022 Publish case processing failed event
        }
    }

}
