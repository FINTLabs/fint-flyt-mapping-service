package no.novari.flyt.mapping.exception

class InstanceFieldNotFoundException(
    val instanceFieldKey: String,
) : RuntimeException("Could not find instance field with key='$instanceFieldKey'")
