package no.fintlabs.kafka.error;

public enum ErrorCode {
    GENERAL_SYSTEM_ERROR,
    CONFIGURATION_NOT_FOUND,
    INSTANCE_FIELD_NOT_FOUND,
    VALUE_CONVERTING_NOT_FOUND,
    VALUE_CONVERTING_KEY_NOT_FOUND;

    private static final String ERROR_PREFIX = "FINT_FLYT_MAPPING_SERVICE_";

    public String getCode() {
        return ERROR_PREFIX + name();
    }

}
