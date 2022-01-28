package no.fintlabs.mapping;

public class NoSuchFieldException extends RuntimeException {

    public NoSuchFieldException(String fieldKey) {
        super("Instance fields does not contain field with key=" + fieldKey);
    }

}
