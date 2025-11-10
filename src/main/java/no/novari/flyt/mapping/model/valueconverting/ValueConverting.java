package no.novari.flyt.mapping.model.valueconverting;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Getter
@Builder
@Jacksonized
public class ValueConverting {
    private final Long id;
    private final Long fromApplicationId;
    private final String fromTypeId;
    private final String toApplicationId;
    private final String toTypeId;
    private final Map<String, String> convertingMap;
}
