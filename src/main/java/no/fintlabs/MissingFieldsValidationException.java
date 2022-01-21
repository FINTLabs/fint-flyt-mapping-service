package no.fintlabs;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class MissingFieldsValidationException extends ValidationException {

    @Getter
    private final List<String> missingFields;

    public MissingFieldsValidationException(String fieldSource, List<String> missingFields) {
        super(String.format("%s does not contain the following required fields: %s", fieldSource, missingFields));
        this.missingFields = Collections.unmodifiableList(missingFields);
    }
}
