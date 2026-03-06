package no.novari.flyt.mapping.service

import no.novari.flyt.mapping.model.configuration.ValueMapping
import no.novari.flyt.mapping.model.instance.InstanceObject
import org.springframework.stereotype.Service

@Service
class ValueMappingService(
    private val instanceReferenceService: InstanceReferenceService,
    private val valueConvertingService: ValueConvertingService,
) {
    fun toValue(
        valueMapping: ValueMapping,
        instanceValuePerKey: Map<String, String?>,
        selectedCollectionObjectsPerKey: Array<InstanceObject>,
    ): Any? {
        val mappingString = valueMapping.mappingString ?: return null

        return when (valueMapping.type) {
            ValueMapping.Type.BOOLEAN -> {
                mappingString.toBoolean()
            }

            ValueMapping.Type.STRING,
            ValueMapping.Type.URL,
            -> {
                mappingString
            }

            ValueMapping.Type.FILE,
            ValueMapping.Type.DYNAMIC_STRING,
            -> {
                instanceReferenceService.replaceIfReferencesWithInstanceValues(
                    mappingString,
                    instanceValuePerKey,
                    selectedCollectionObjectsPerKey,
                )
            }

            ValueMapping.Type.VALUE_CONVERTING -> {
                valueConvertingService.convertValue(
                    mappingString,
                    instanceValuePerKey,
                    selectedCollectionObjectsPerKey,
                )
            }
        }
    }
}
