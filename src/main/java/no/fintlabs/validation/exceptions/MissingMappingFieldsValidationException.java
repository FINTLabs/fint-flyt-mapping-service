package no.fintlabs.validation.exceptions;

import lombok.Getter;
import no.fintlabs.validation.MappedFieldsValidationService;

public class MissingMappingFieldsValidationException extends ValidationException {

    @Getter
    private final MappedFieldsValidationService.Error validationError;

    public MissingMappingFieldsValidationException(MappedFieldsValidationService.Error validationError) {
        super("Validation error(s): " + validationError);
        this.validationError = validationError;
    }
}
