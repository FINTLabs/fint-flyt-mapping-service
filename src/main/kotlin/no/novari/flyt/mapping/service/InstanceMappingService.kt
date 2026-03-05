package no.novari.flyt.mapping.service

import no.novari.flyt.mapping.model.configuration.CollectionMapping
import no.novari.flyt.mapping.model.configuration.ObjectMapping
import no.novari.flyt.mapping.model.configuration.ValueMapping
import no.novari.flyt.mapping.model.instance.InstanceObject
import org.springframework.stereotype.Service

@Service
class InstanceMappingService(
    private val instanceReferenceService: InstanceReferenceService,
    private val valueMappingService: ValueMappingService,
) {
    fun toMappedInstanceObject(
        objectMapping: ObjectMapping,
        instance: InstanceObject,
    ): Map<String, Any?> {
        return toMappedInstanceObject(objectMapping, instance, emptyArray())
    }

    private fun toMappedInstanceObject(
        objectMapping: ObjectMapping,
        instance: InstanceObject,
        selectedCollectionObjectsByCollectionIndex: Array<InstanceObject>,
    ): Map<String, Any?> {
        return buildMap {
            putAll(
                toMappedInstanceValuePerKey(
                    objectMapping.valueMappingPerKey,
                    instance,
                    selectedCollectionObjectsByCollectionIndex,
                ),
            )
            putAll(
                toMappedInstanceObjectPerKey(
                    objectMapping.objectMappingPerKey,
                    instance,
                    selectedCollectionObjectsByCollectionIndex,
                ),
            )
            putAll(
                toMappedInstanceElementCollectionPerKey(
                    ::toMappedInstanceValue,
                    objectMapping.valueCollectionMappingPerKey,
                    instance,
                    selectedCollectionObjectsByCollectionIndex,
                ),
            )
            putAll(
                toMappedInstanceElementCollectionPerKey(
                    { nestedObjectMapping, instanceObject, selectedObjects ->
                        toMappedInstanceObject(nestedObjectMapping, instanceObject, selectedObjects)
                    },
                    objectMapping.objectCollectionMappingPerKey,
                    instance,
                    selectedCollectionObjectsByCollectionIndex,
                ),
            )
        }
    }

    private fun toMappedInstanceValuePerKey(
        valueMappingPerKey: Map<String, ValueMapping>,
        instance: InstanceObject,
        selectedCollectionObjectsByCollectionIndex: Array<InstanceObject>,
    ): Map<String, Any?> {
        return valueMappingPerKey.mapValues { (_, valueMapping) ->
            toMappedInstanceValue(valueMapping, instance, selectedCollectionObjectsByCollectionIndex)
        }
    }

    private fun toMappedInstanceValue(
        valueMapping: ValueMapping,
        instance: InstanceObject,
        selectedCollectionObjectsByCollectionIndex: Array<InstanceObject>,
    ): Any? {
        return valueMappingService.toValue(
            valueMapping,
            instance.valuePerKey,
            selectedCollectionObjectsByCollectionIndex,
        )
    }

    private fun toMappedInstanceObjectPerKey(
        objectMappingPerKey: Map<String, ObjectMapping>,
        instance: InstanceObject,
        selectedCollectionObjectsByCollectionIndex: Array<InstanceObject>,
    ): Map<String, Map<String, Any?>> {
        return objectMappingPerKey.mapValues { (_, objectMapping) ->
            toMappedInstanceObject(objectMapping, instance, selectedCollectionObjectsByCollectionIndex)
        }
    }

    private fun <T, R> toMappedInstanceElementCollectionPerKey(
        elementMappingFunction: (T, InstanceObject, Array<InstanceObject>) -> R,
        collectionMappingPerKey: Map<String, CollectionMapping<T>>,
        instance: InstanceObject,
        selectedCollectionObjectsByCollectionIndex: Array<InstanceObject>,
    ): Map<String, Collection<R>> {
        return collectionMappingPerKey.mapValues { (_, collectionMapping) ->
            toMappedInstanceElements(
                elementMappingFunction,
                collectionMapping,
                instance,
                selectedCollectionObjectsByCollectionIndex,
            )
        }
    }

    private fun <T, R> toMappedInstanceElements(
        elementMappingFunction: (T, InstanceObject, Array<InstanceObject>) -> R,
        elementCollectionMapping: CollectionMapping<T>,
        instance: InstanceObject,
        selectedCollectionObjectsByCollectionIndex: Array<InstanceObject>,
    ): Collection<R> {
        return buildList {
            addAll(
                elementCollectionMapping.elementMappings.map { elementMapping ->
                    elementMappingFunction(elementMapping, instance, selectedCollectionObjectsByCollectionIndex)
                },
            )

            elementCollectionMapping.fromCollectionMappings.forEach { fromCollectionMapping ->
                addAll(
                    toMappedInstanceElements(
                        elementMappingFunction,
                        fromCollectionMapping.elementMapping,
                        instance,
                        fromCollectionMapping.instanceCollectionReferencesOrdered.toTypedArray(),
                        0,
                        selectedCollectionObjectsByCollectionIndex,
                    ),
                )
            }
        }
    }

    private fun <T, R> toMappedInstanceElements(
        elementMappingFunction: (T, InstanceObject, Array<InstanceObject>) -> R,
        elementMapping: T,
        instance: InstanceObject,
        instanceCollectionReferencesByCollectionIndex: Array<String>,
        nextCollectionIndex: Int,
        selectedCollectionObjectsByCollectionIndex: Array<InstanceObject>,
    ): Collection<R> {
        if (nextCollectionIndex == instanceCollectionReferencesByCollectionIndex.size) {
            return listOf(
                elementMappingFunction(
                    elementMapping,
                    instance,
                    selectedCollectionObjectsByCollectionIndex,
                ),
            )
        }

        val nextCollection =
            instanceReferenceService.getInstanceObjectCollection(
                instanceCollectionReferencesByCollectionIndex[nextCollectionIndex],
                instance.objectCollectionPerKey,
                selectedCollectionObjectsByCollectionIndex,
            )

        return nextCollection.flatMap { instanceObject ->
            val newSelectedCollectionObjectsByCollectionIndex =
                selectedCollectionObjectsByCollectionIndex + instanceObject

            toMappedInstanceElements(
                elementMappingFunction,
                elementMapping,
                instance,
                instanceCollectionReferencesByCollectionIndex,
                nextCollectionIndex + 1,
                newSelectedCollectionObjectsByCollectionIndex,
            )
        }
    }
}
