package no.fintlabs.mapping.fields;

public enum CaseMappingField implements MappingField {
    TITTEL("tittel", true),
    OFFENTLIG_TITTEL("offentligTittel", true),
    ADMINISTRATIV_ENHET("administrativenhet", true),
    ARKIVDEL("arkivdel", true),
    JOURNALENHET("journalenhet", true),
    TILGANGSRESTRIKSJON("tilgangsrestriksjon", true),
    SKJERMINGSHJEMMEL("skjermingshjemmel", true),
    SAKSANSVARLIG("saksansvarlig", true),
    PRIMARORDNINGSPRINSIPP("primarordningsprinsipp", true),
    PRIMARKLASSE("primarklasse", true),
    SEKUNDARORDNINGSPRINSIPP("sekundarordningsprinsipp", true),
    SEKUNDARKLASSE("sekundarklasse", true),
    TERTIARORDNINGSPRINSIPP("tertiarordningsprinsipp", true),
    TERTIARKLASSE("tertiarklasse", true);

    private final String fieldKey;
    private final boolean required;

    CaseMappingField(String fieldKey, boolean required) {
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
