package no.novari.flyt.mapping.exception

class ValueConvertingNotFoundException(
    val valueConvertingId: Long,
) : RuntimeException("Could not find value converter with id=$valueConvertingId")
