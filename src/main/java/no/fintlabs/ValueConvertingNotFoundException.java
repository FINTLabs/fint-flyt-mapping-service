package no.fintlabs;

import lombok.Getter;

public class ValueConvertingNotFoundException extends RuntimeException {

    @Getter
    private final Long valueConvertingId;

    public ValueConvertingNotFoundException(Long valueConvertingId) {
        super("Could not find value converter with id=" + valueConvertingId);
        this.valueConvertingId = valueConvertingId;
    }
}
