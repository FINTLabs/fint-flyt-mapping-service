package no.fintlabs;

import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import no.fintlabs.model.instance.Instance;
import org.springframework.stereotype.Service;

@Service
public class CaseService {

    public SakResource createSak(IntegrationConfiguration integrationConfiguration, Instance instance) {


        SakResource sakResource = new SakResource();

        integrationConfiguration.getCaseConfiguration().getFields().forEach(field -> {
            field.
        });
        instance.getFields().get()
        sakResource.setTittel(integrationConfiguration.getCaseConfiguration().getFields());
        return sakResource;
    }
}
