package no.fintlabs;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import no.fintlabs.model.instance.Instance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CaseService {

    private final FieldMappingService fieldMappingService;

    private KlassifikasjonsMappingService klassifikasjonsMappingService;

    public CaseService(FieldMappingService fieldMappingService, KlassifikasjonsMappingService klassifikasjonsMappingService) {
        this.fieldMappingService = fieldMappingService;
        this.klassifikasjonsMappingService = klassifikasjonsMappingService;
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
        // TODO: 14/01/2022 What is this?  // sakstype: Sak
        // "caseType",
        Optional.ofNullable(caseValuesByFieldKey.get("administrativenhet")).map(Link::new).ifPresent(sakResource::addAdministrativEnhet);
        Optional.ofNullable(caseValuesByFieldKey.get("arkivdel")).map(Link::new).ifPresent(sakResource::addArkivdel);
        Optional.ofNullable(caseValuesByFieldKey.get("journalenhet")).map(Link::new).ifPresent(sakResource::addJournalenhet);

        SkjermingResource skjermingResource = new SkjermingResource();
        Optional.ofNullable(caseValuesByFieldKey.get("tilgangsrestriksjon")).map(Link::new).ifPresent(skjermingResource::addTilgangsrestriksjon);
        Optional.ofNullable(caseValuesByFieldKey.get("skjermingshjemmel")).map(Link::new).ifPresent(skjermingResource::addSkjermingshjemmel);
        sakResource.setSkjerming(skjermingResource);

        Optional.ofNullable(caseValuesByFieldKey.get("saksansvarlig")).map(Link::new).ifPresent(sakResource::addSaksansvarlig);
        sakResource.setKlasse(klassifikasjonsMappingService.getKlasseResources(caseValuesByFieldKey)); // todo input keys here

        return sakResource;
    }


    private JournalpostResource createNewJournalpostAndDokumentbeskrivelse(IntegrationConfiguration integrationConfiguration, Instance instance, SakResource sakResource) {
        // TODO: 14/01/2022 Implement

        /*
            recordConfiguration:
                "tittel",
                "offentigTittel",
                "DokumentBeskrivelse.dokumentType", (journalposttype)
                "administrativenhet",
                "journalstatus",
                "tilgangsrestriksjon",
                "skjermingshjemmel",

            documentConfiguration: (Dokumentbeskrivelse)
                "tittel",}
                "dokumentStatus",
                "tilgangsrestriksjon",
                "skjermingshjemmel",
                "DokumentBeskrivelse.dokumentObjekt.variantFormat"} ???
                "DokumentBeskrivelse.dokumentObjekt.filformat",     ???
        */

        return new JournalpostResource();
    }
}
