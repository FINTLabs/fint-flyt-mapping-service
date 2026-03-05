package no.novari.flyt.mapping.kafka.error

enum class ErrorCode {
    GENERAL_SYSTEM_ERROR,
    CONFIGURATION_NOT_FOUND,
    INSTANCE_FIELD_NOT_FOUND,
    VALUE_CONVERTING_NOT_FOUND,
    VALUE_CONVERTING_KEY_NOT_FOUND,
    ;

    fun getCode(): String = ERROR_PREFIX + name

    companion object {
        private const val ERROR_PREFIX = "FINT_FLYT_MAPPING_SERVICE_"
    }
}
