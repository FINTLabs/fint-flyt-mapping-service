package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static no.fintlabs.mapping.fields.CaseMappingField.*;

@Service
public class CaseMappingService {

    private final ClassificationMappingService classificationMappingService;

    public CaseMappingService(ClassificationMappingService classificationMappingService) {
        this.classificationMappingService = classificationMappingService;
    }

    public SakResource map(Map<String, String> caseValuesByFieldKey) {
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
        sakResource.setKlasse(classificationMappingService.getKlasseResources(caseValuesByFieldKey, List.of(
                new ClassificationMappingService.FieldKeys(PRIMARORDNINGSPRINSIPP.getFieldKey(), PRIMARKLASSE.getFieldKey()),
                new ClassificationMappingService.FieldKeys(SEKUNDARORDNINGSPRINSIPP.getFieldKey(), SEKUNDARKLASSE.getFieldKey()),
                new ClassificationMappingService.FieldKeys(TERTIARORDNINGSPRINSIPP.getFieldKey(), TERTIARKLASSE.getFieldKey())
        )));

        sakResource.setJournalpost(new ArrayList<>());

        return sakResource;
    }

}
