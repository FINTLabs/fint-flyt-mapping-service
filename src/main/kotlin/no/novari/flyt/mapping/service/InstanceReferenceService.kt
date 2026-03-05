package no.novari.flyt.mapping.service

import no.novari.flyt.mapping.exception.InstanceFieldNotFoundException
import no.novari.flyt.mapping.model.instance.InstanceObject
import org.springframework.stereotype.Service

@Service
class InstanceReferenceService {
    private data class CollectionFieldKey(
        val collectionIndex: Int,
        val collectionFieldKey: String,
    )

    fun replaceIfReferencesWithInstanceValues(
        mappingString: String,
        instanceValuePerKey: Map<String, String?>,
        selectedCollectionObjectsPerKey: Array<InstanceObject>,
    ): String {
        val matcher = REFERENCE_PATTERN.matcher(mappingString)
        return matcher.replaceAll { matchResult ->
            val matchedReference = matchResult.group()
            if (INSTANCE_FIELD_REFERENCE_PATTERN.matcher(matchedReference).matches()) {
                getInstanceValue(matchedReference, instanceValuePerKey)
            } else {
                getCollectionFieldValue(matchedReference, selectedCollectionObjectsPerKey)
            }
        }
    }

    fun getFirstInstanceValue(
        mappingString: String,
        instanceValuePerKey: Map<String, String?>,
        selectedCollectionObjectsPerKey: Array<InstanceObject>,
    ): String {
        val matcher = REFERENCE_PATTERN.matcher(mappingString)
        if (!matcher.find()) {
            throw IllegalArgumentException("Mapping string contains no valid instance field reference")
        }

        val matchedReference = matcher.group(0)
        return if (INSTANCE_FIELD_REFERENCE_PATTERN.matcher(matchedReference).matches()) {
            getInstanceValue(matchedReference, instanceValuePerKey)
        } else {
            getCollectionFieldValue(matchedReference, selectedCollectionObjectsPerKey)
        }
    }

    fun getInstanceObjectCollection(
        collectionReference: String,
        objectCollectionPerKey: Map<String, Collection<InstanceObject>>,
        selectedCollectionObjectsByCollectionIndex: Array<InstanceObject>,
    ): Collection<InstanceObject> {
        return if (INSTANCE_FIELD_REFERENCE_PATTERN.matcher(collectionReference).matches()) {
            getCollectionFromInstance(collectionReference, objectCollectionPerKey)
        } else {
            getCollectionFromSelectedCollectionObject(collectionReference, selectedCollectionObjectsByCollectionIndex)
        }
    }

    private fun getInstanceValue(
        ifReference: String,
        instanceValuePerKey: Map<String, String?>,
    ): String {
        val instanceValueKey = getInstanceFieldKey(ifReference)
        if (!instanceValuePerKey.containsKey(instanceValueKey)) {
            throw InstanceFieldNotFoundException(instanceValueKey)
        }
        return instanceValuePerKey[instanceValueKey] ?: ""
    }

    private fun getInstanceFieldKey(ifReference: String): String {
        return ifReference
            .replace("${'$'}if{", "")
            .replace("}", "")
    }

    private fun getCollectionFieldValue(
        icfReference: String,
        selectedCollectionObjectsPerCollectionIndex: Array<InstanceObject>,
    ): String {
        val collectionFieldKey = getCollectionFieldKey(icfReference)
        val valuePerKeyForCollectionObject =
            selectedCollectionObjectsPerCollectionIndex[collectionFieldKey.collectionIndex].valuePerKey

        if (!valuePerKeyForCollectionObject.containsKey(collectionFieldKey.collectionFieldKey)) {
            throw InstanceFieldNotFoundException(collectionFieldKey.collectionFieldKey)
        }

        return valuePerKeyForCollectionObject[collectionFieldKey.collectionFieldKey] ?: ""
    }

    private fun getCollectionFieldKey(icfReference: String): CollectionFieldKey {
        val collectionIndexAndCollectionObjectValueReference = icfReference.split("}{")
        val collectionIndex =
            collectionIndexAndCollectionObjectValueReference[0]
                .replace("${'$'}icf{", "")
                .toInt()

        val collectionObjectValueReference =
            collectionIndexAndCollectionObjectValueReference[1]
                .replace("}", "")

        return CollectionFieldKey(
            collectionIndex = collectionIndex,
            collectionFieldKey = collectionObjectValueReference,
        )
    }

    private fun getCollectionFromInstance(
        collectionReference: String,
        objectCollectionPerKey: Map<String, Collection<InstanceObject>>,
    ): Collection<InstanceObject> {
        val collectionKey = getInstanceFieldKey(collectionReference)
        if (!objectCollectionPerKey.containsKey(collectionKey)) {
            throw InstanceFieldNotFoundException(collectionKey)
        }
        return objectCollectionPerKey[collectionKey] ?: emptyList()
    }

    private fun getCollectionFromSelectedCollectionObject(
        collectionReference: String,
        selectedCollectionObjectsByCollectionIndex: Array<InstanceObject>,
    ): Collection<InstanceObject> {
        val collectionFieldKey = getCollectionFieldKey(collectionReference)
        val objectCollectionPerKey =
            selectedCollectionObjectsByCollectionIndex[collectionFieldKey.collectionIndex].objectCollectionPerKey

        if (!objectCollectionPerKey.containsKey(collectionFieldKey.collectionFieldKey)) {
            throw InstanceFieldNotFoundException(
                "${collectionFieldKey.collectionIndex}.${collectionFieldKey.collectionFieldKey}",
            )
        }

        return objectCollectionPerKey[collectionFieldKey.collectionFieldKey] ?: emptyList()
    }

    companion object {
        private const val CURLY_BRACKETS_WRAPPER = """[{][^}]+}"""
        private val INSTANCE_FIELD_REFERENCE_PATTERN =
            Regex("""[$]if$CURLY_BRACKETS_WRAPPER""").toPattern()
        private val INSTANCE_COLLECTION_FIELD_REFERENCE_PATTERN =
            Regex("""[$]icf$CURLY_BRACKETS_WRAPPER$CURLY_BRACKETS_WRAPPER""").toPattern()
        private val REFERENCE_PATTERN =
            Regex(
                "${INSTANCE_FIELD_REFERENCE_PATTERN.pattern()}|${INSTANCE_COLLECTION_FIELD_REFERENCE_PATTERN.pattern()}",
            ).toPattern()
    }
}
