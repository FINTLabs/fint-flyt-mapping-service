package no.fintlabs.exception;

import lombok.Getter;

@Getter
public class ValueConvertingKeyNotFoundException extends RuntimeException {

    private final Long valueConvertingId;
    private final String valueConvertingKey;

    public ValueConvertingKeyNotFoundException(Long valueConvertingId, String valueConvertingKey) {
        super("Value converting map does not contain key=" + valueConvertingKey + " in value converter with id=" + valueConvertingId);
        this.valueConvertingId = valueConvertingId;
        this.valueConvertingKey = valueConvertingKey;
    }
}
