package no.fintlabs;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import no.fintlabs.model.instance.Instance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static no.fintlabs.SakField.*;

@Service
public class CaseService {

    private final InstanceFieldsValidator instanceFieldsValidator;
    private final MappedFieldsValidator mappedFieldsValidator;

    private final FieldMappingService fieldMappingService;
    private final KlassifikasjonsMappingService klassifikasjonsMappingService;

    public CaseService(
            InstanceFieldsValidator instanceFieldsValidator,
            MappedFieldsValidator mappedFieldsValidator,
            FieldMappingService fieldMappingService,
            KlassifikasjonsMappingService klassifikasjonsMappingService
    ) {
        this.instanceFieldsValidator = instanceFieldsValidator;
        this.mappedFieldsValidator = mappedFieldsValidator;
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
        this.instanceFieldsValidator.validate(integrationConfiguration, instance);
        switch (integrationConfiguration.getCaseConfiguration().getCaseCreationStrategy()) {
            case NEW:
                Map<String, String> caseValuesByFieldKey = fieldMappingService.mapCaseFields(integrationConfiguration.getCaseConfiguration().getFields(), instance.getFields());
                this.mappedFieldsValidator.validate(caseValuesByFieldKey, SakField.values());
                return createNewSak(caseValuesByFieldKey);
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

    private SakResource createNewSak(Map<String, String> caseValuesByFieldKey) {
        SakResource sakResource = new SakResource();
        sakResource.setTittel(caseValuesByFieldKey.get(TITTEL.getFieldKey()));
        sakResource.setOffentligTittel(caseValuesByFieldKey.get(OFFENTLIG_TITTEL.getFieldKey()));

        Optional.ofNullable(caseValuesByFieldKey.get(ADMINISTRATIV_ENHET.getFieldKey())).map(Link::new).ifPresent(sakResource::addAdministrativEnhet);
        Optional.ofNullable(caseValuesByFieldKey.get(ARKIVDEL.getFieldKey())).map(Link::new).ifPresent(sakResource::addArkivdel);
        Optional.ofNullable(caseValuesByFieldKey.get(JOURNALENHET.getFieldKey())).map(Link::new).ifPresent(sakResource::addJournalenhet);

        SkjermingResource skjermingResource = new SkjermingResource();
        Optional.ofNullable(caseValuesByFieldKey.get(TILGANGSRESTRIKSJON.getFieldKey())).map(Link::new).ifPresent(skjermingResource::addTilgangsrestriksjon);
        Optional.ofNullable(caseValuesByFieldKey.get(SKJERMINGSHJEMMEL.getFieldKey())).map(Link::new).ifPresent(skjermingResource::addSkjermingshjemmel);
        sakResource.setSkjerming(skjermingResource);

        Optional.ofNullable(caseValuesByFieldKey.get(SAKSANSVARLIG.getFieldKey())).map(Link::new).ifPresent(sakResource::addSaksansvarlig);

        sakResource.setKlasse(klassifikasjonsMappingService.getKlasseResources(caseValuesByFieldKey, List.of(
                new KlassifikasjonsMappingService.FieldKeys(PRIMARORDNINGSPRINSIPP.getFieldKey(), PRIMARKLASSE.getFieldKey()),
                new KlassifikasjonsMappingService.FieldKeys(SEKUNDARORDNINGSPRINSIPP.getFieldKey(), SEKUNDARKLASSE.getFieldKey()),
                new KlassifikasjonsMappingService.FieldKeys(TERTIARORDNINGSPRINSIPP.getFieldKey(), TERTIARKLASSE.getFieldKey())
        )));

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
