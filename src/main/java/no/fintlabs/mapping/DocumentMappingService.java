package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.DokumentobjektResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fintlabs.mapping.fields.DocumentMappingField;
import no.fintlabs.mapping.fields.RecordMappingField;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DocumentMappingService {

    public DokumentbeskrivelseResource map(Map<String, String> caseValuesByFieldKey) {
        DokumentbeskrivelseResource dokumentbeskrivelseResource = new DokumentbeskrivelseResource();

        dokumentbeskrivelseResource.setTittel(caseValuesByFieldKey.get(RecordMappingField.TITTEL.getFieldKey()));

        Optional.ofNullable(caseValuesByFieldKey.get(DocumentMappingField.DOKUMENT_STATUS.getFieldKey())).map(Link::new).ifPresent(dokumentbeskrivelseResource::addDokumentstatus);

        SkjermingResource skjermingResource = new SkjermingResource();
        Optional.ofNullable(caseValuesByFieldKey.get(DocumentMappingField.TILGANGSRESTRIKSJON.getFieldKey())).map(Link::new).ifPresent(skjermingResource::addTilgangsrestriksjon);
        Optional.ofNullable(caseValuesByFieldKey.get(DocumentMappingField.SKJERMINGSHJEMMEL.getFieldKey())).map(Link::new).ifPresent(skjermingResource::addSkjermingshjemmel);
        dokumentbeskrivelseResource.setSkjerming(skjermingResource);

        DokumentobjektResource dokumentobjektResource = new DokumentobjektResource();
        Optional.ofNullable(caseValuesByFieldKey.get(DocumentMappingField.DOKUMENTOBJEKT_VARIANTFORMAT.getFieldKey())).map(Link::new).ifPresent(dokumentobjektResource::addVariantFormat);
        dokumentbeskrivelseResource.setDokumentobjekt(List.of(dokumentobjektResource));

        return dokumentbeskrivelseResource;
    }

}
