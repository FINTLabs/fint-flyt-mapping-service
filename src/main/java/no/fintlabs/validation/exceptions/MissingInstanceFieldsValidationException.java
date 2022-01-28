package no.fintlabs.validation.exceptions;

import lombok.Getter;
import no.fintlabs.validation.InstanceFieldsValidationService;

public class MissingInstanceFieldsValidationException extends ValidationException {

    @Getter
    private final InstanceFieldsValidationService.Error validationError;

    public MissingInstanceFieldsValidationException(InstanceFieldsValidationService.Error validationError) {
        super("TODO"); // TODO: 28/01/2022 Message
        this.validationError = validationError;
    }

}
