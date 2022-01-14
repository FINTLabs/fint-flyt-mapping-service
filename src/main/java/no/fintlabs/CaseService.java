package no.fintlabs;

import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import no.fintlabs.model.instance.Instance;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CaseService {

    private final FieldMappingService fieldMappingService;

    public CaseService(FieldMappingService fieldMappingService) {
        this.fieldMappingService = fieldMappingService;
    }

    public SakResource createSak(IntegrationConfiguration integrationConfiguration, Instance instance) {

        SakResource sakResource = new SakResource();

        Map<String, String> caseValuesByFieldKey = fieldMappingService.mapCaseFields(
                integrationConfiguration.getCaseConfiguration().getFields(),
                instance.getFields()
        );

        sakResource.setTittel(caseValuesByFieldKey.get("title"));
        sakResource.setOffentligTittel(caseValuesByFieldKey.get("offentligTittel"));

        return sakResource;
    }

}
