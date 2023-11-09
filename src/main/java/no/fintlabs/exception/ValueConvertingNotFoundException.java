package no.fintlabs.exception;

import lombok.Getter;

@Getter
public class ValueConvertingNotFoundException extends RuntimeException {

    private final Long valueConvertingId;

    public ValueConvertingNotFoundException(Long valueConvertingId) {
        super("Could not find value converter with id=" + valueConvertingId);
        this.valueConvertingId = valueConvertingId;
    }
}
