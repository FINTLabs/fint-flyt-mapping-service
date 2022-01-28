package no.fintlabs.mapping.fields;

public enum DocumentMappingField implements MappingField {
    TITTEL("tittel", true),
    DOKUMENT_STATUS("dokumentStatus", true),
    TILGANGSRESTRIKSJON("tilgangsrestriksjon", true),
    SKJERMINGSHJEMMEL("skjermingshjemmel", true),
    DOKUMENTOBJEKT_VARIANTFORMAT("DokumentBeskrivelse.dokumentObjekt.variantformat", true);

    private final String fieldKey;
    private final boolean required;

    DocumentMappingField(String fieldKey, boolean required) {
        this.fieldKey = fieldKey;
        this.required = required;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public boolean isRequired() {
        return required;
    }

}
