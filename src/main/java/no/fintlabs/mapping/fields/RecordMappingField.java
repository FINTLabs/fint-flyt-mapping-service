package no.fintlabs.mapping.fields;

public enum RecordMappingField implements MappingField {
    TITTEL("tittel", true),
    OFFENTLIG_TITTEL("offentligTittel", true),
    JOURNALPOSTTYPE("journalposttype", true),
    ADMINISTRATIV_ENHET("administrativenhet", true),
    TILGANGSRESTRIKSJON("tilgangsrestriksjon", true),
    SKJERMINGSHJEMMEL("skjermingshjemmel", true);

    private final String fieldKey;
    private final boolean required;

    RecordMappingField(String fieldKey, boolean required) {
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
