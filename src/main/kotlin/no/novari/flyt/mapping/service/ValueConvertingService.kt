package no.novari.flyt.mapping.service

import no.novari.flyt.mapping.exception.ValueConvertingKeyNotFoundException
import no.novari.flyt.mapping.exception.ValueConvertingNotFoundException
import no.novari.flyt.mapping.intgrconf.IntgrConfClient
import no.novari.flyt.mapping.model.instance.InstanceObject
import org.springframework.stereotype.Service

@Service
class ValueConvertingService(
    private val intgrConfClient: IntgrConfClient,
    private val instanceReferenceService: InstanceReferenceService,
    private val valueConvertingReferenceService: ValueConvertingReferenceService,
) {
    fun convertValue(
        mappingString: String,
        instanceValuePerKey: Map<String, String?>,
        selectedCollectionObjectsPerKey: Array<InstanceObject>,
    ): String {
        val instanceValue =
            instanceReferenceService.getFirstInstanceValue(
                mappingString,
                instanceValuePerKey,
                selectedCollectionObjectsPerKey,
            )

        val valueConvertingId = valueConvertingReferenceService.getFirstValueConverterId(mappingString)
        val valueConverting =
            intgrConfClient.getValueConverting(valueConvertingId)
                ?: throw ValueConvertingNotFoundException(valueConvertingId)

        val convertingMap = valueConverting.convertingMap
        if (!convertingMap.containsKey(instanceValue)) {
            throw ValueConvertingKeyNotFoundException(valueConvertingId, instanceValue)
        }

        return convertingMap.getValue(instanceValue)
    }
}
