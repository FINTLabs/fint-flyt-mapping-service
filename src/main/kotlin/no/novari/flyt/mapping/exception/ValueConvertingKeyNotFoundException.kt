package no.novari.flyt.mapping.exception

class ValueConvertingKeyNotFoundException(
    val valueConvertingId: Long,
    val valueConvertingKey: String,
) : RuntimeException(
        "Value converting map does not contain key=$valueConvertingKey in value converter with id=$valueConvertingId",
    )
