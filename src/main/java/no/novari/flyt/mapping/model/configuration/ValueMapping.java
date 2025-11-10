package no.novari.flyt.mapping.model.configuration;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class ValueMapping {

    public enum Type {
        STRING, URL, BOOLEAN, DYNAMIC_STRING, FILE, VALUE_CONVERTING
    }

    private final Type type;

    private final String mappingString;

}
