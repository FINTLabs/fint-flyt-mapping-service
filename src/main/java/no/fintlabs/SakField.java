package no.fintlabs;

import lombok.Data;

public enum SakField {
    TITTEL("tittel"),
    OFFENTLIG_TITTEL("offentligTittel"),
    ADMINISTRATIV_ENHET("administrativenhet"),
    ARKIVDEL("arkivdel"),
    JOURNALENHET("journalenhet"),
    TILGANGSRESTRIKSJON("tilgangsrestriksjon"),
    SKJERMINGSHJEMMEL("skjermingshjemmel"),
    SAKSANSVARLIG("saksansvarlig"),
    PRIMARORDNINGSPRINSIPP("primarordningsprinsipp"),
    PRIMARKLASSE("primarklasse"),
    SEKUNDARORDNINGSPRINSIPP("sekundarordningsprinsipp"),
    SEKUNDARKLASSE("sekundarklasse"),
    TERTIARORDNINGSPRINSIPP("tertiarordningsprinsipp"),
    TERTIARKLASSE("tertiarklasse");

    private final String fieldKey;

    SakField(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getFieldKey() {
        return fieldKey;
    }
}
