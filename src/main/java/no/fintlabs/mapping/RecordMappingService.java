package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fintlabs.mapping.fields.RecordMappingField;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Service
public class RecordMappingService {

    public JournalpostResource map(Map<String, String> caseValuesByFieldKey) {
        JournalpostResource journalpostResource = new JournalpostResource();

        journalpostResource.setTittel(caseValuesByFieldKey.get(RecordMappingField.TITTEL.getFieldKey()));
        journalpostResource.setOffentligTittel(caseValuesByFieldKey.get(RecordMappingField.OFFENTLIG_TITTEL.getFieldKey()));

        Optional.ofNullable(caseValuesByFieldKey.get(RecordMappingField.ADMINISTRATIV_ENHET.getFieldKey())).map(Link::new).ifPresent(journalpostResource::addAdministrativEnhet);

        SkjermingResource skjermingResource = new SkjermingResource();
        Optional.ofNullable(caseValuesByFieldKey.get(RecordMappingField.TILGANGSRESTRIKSJON.getFieldKey())).map(Link::new).ifPresent(skjermingResource::addTilgangsrestriksjon);
        Optional.ofNullable(caseValuesByFieldKey.get(RecordMappingField.SKJERMINGSHJEMMEL.getFieldKey())).map(Link::new).ifPresent(skjermingResource::addSkjermingshjemmel);
        journalpostResource.setSkjerming(skjermingResource);

        journalpostResource.setDokumentbeskrivelse(new ArrayList<>());

        return journalpostResource;
    }

}
