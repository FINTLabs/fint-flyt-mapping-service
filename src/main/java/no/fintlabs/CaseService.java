package no.fintlabs;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
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

        SakResource sakResource = getSak(integrationConfiguration, instance);
        createNewJournalpostAndDokumentbeskrivelse(integrationConfiguration, instance, sakResource);

        /*
            todo: Don't know where this should be applied
            applicantConfiguration:
                "KorrespondansepartNavn",
                "Adresse.adresselinje",
                "Adresse.postnummer",
                "Adresse.poststed",
                "Kontaktinformasjon.mobiltelefonnummer",
                "Kontaktinformasjon.epostadresse",
                "tilgangsrestriksjon",
                "skjermingshjemmel",
         */

        return sakResource;
    }

    private SakResource getSak(IntegrationConfiguration integrationConfiguration, Instance instance) {
        switch (integrationConfiguration.getCaseConfiguration().getCaseCreationStrategy()) {
            case NEW:
                return createNewSak(integrationConfiguration, instance);
            case EXISTING:
                // TODO: 14/01/2022 Handle existing
                return null;
            case COLLECTION:
                // TODO: 14/01/2022 Handle Collection
                return null;
            default:
                throw new RuntimeException();
        }
    }

    private SakResource createNewSak(IntegrationConfiguration integrationConfiguration, Instance instance) {
        SakResource sakResource = new SakResource();
        Map<String, String> caseValuesByFieldKey = fieldMappingService.mapCaseFields(integrationConfiguration.getCaseConfiguration().getFields(), instance.getFields());

        sakResource.setTittel(caseValuesByFieldKey.get("title"));
        sakResource.setOffentligTittel(caseValuesByFieldKey.get("offentligTittel"));
        // TODO: 14/01/2022 What is this? 
        // "caseType",
        // TODO: 14/01/2022 Will the value contain the link (applies to all links below):
        sakResource.addAdministrativEnhet(new Link(caseValuesByFieldKey.get("administrativenhet")));
        sakResource.addArkivdel(new Link(caseValuesByFieldKey.get("arkivdel")));
        sakResource.addJournalenhet(new Link(caseValuesByFieldKey.get("journalenhet")));
        // TODO: 14/01/2022 What is this?
        // tilgangsrestriksjon
        // skjermingshjemmel
        sakResource.addSaksansvarlig(new Link(caseValuesByFieldKey.get("saksansvarlig")));
        // primarordningsprinsipp
        //sekundarordningsprinsipp
        // primarklasse
        // sekundarklasse

        return sakResource;
    }

    private JournalpostResource createNewJournalpostAndDokumentbeskrivelse(IntegrationConfiguration integrationConfiguration, Instance instance, SakResource sakResource) {
        // TODO: 14/01/2022 Implement

        /*
            recordConfiguration:
                "tittel",
                "offentigTittel",
                "DokumentBeskrivelse.dokumentType",
                "administrativenhet",
                "journalstatus",
                "tilgangsrestriksjon",
                "skjermingshjemmel",

            documentConfiguration:
                "tittel",}
                "dokumentStatus",
                "tilgangsrestriksjon",
                "skjermingshjemmel",
                "DokumentBeskrivelse.dokumentObjekt.variantFormat"}
                "DokumentBeskrivelse.dokumentObjekt.filformat",
        */

        return new JournalpostResource();
    }
}
