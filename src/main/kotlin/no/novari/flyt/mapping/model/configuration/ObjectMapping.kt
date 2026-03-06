package no.novari.flyt.mapping.model.configuration

data class ObjectMapping(
    val valueMappingPerKey: Map<String, ValueMapping> = emptyMap(),
    val valueCollectionMappingPerKey: Map<String, CollectionMapping<ValueMapping>> = emptyMap(),
    val objectMappingPerKey: Map<String, ObjectMapping> = emptyMap(),
    val objectCollectionMappingPerKey: Map<String, CollectionMapping<ObjectMapping>> = emptyMap(),
) {
    class Builder {
        private var valueMappingPerKey: Map<String, ValueMapping>? = null
        private var valueCollectionMappingPerKey: Map<String, CollectionMapping<ValueMapping>>? = null
        private var objectMappingPerKey: Map<String, ObjectMapping>? = null
        private var objectCollectionMappingPerKey: Map<String, CollectionMapping<ObjectMapping>>? = null

        fun valueMappingPerKey(valueMappingPerKey: Map<String, ValueMapping>) =
            apply { this.valueMappingPerKey = valueMappingPerKey }

        fun valueCollectionMappingPerKey(valueCollectionMappingPerKey: Map<String, CollectionMapping<ValueMapping>>) =
            apply { this.valueCollectionMappingPerKey = valueCollectionMappingPerKey }

        fun objectMappingPerKey(objectMappingPerKey: Map<String, ObjectMapping>) =
            apply { this.objectMappingPerKey = objectMappingPerKey }

        fun objectCollectionMappingPerKey(
            objectCollectionMappingPerKey: Map<String, CollectionMapping<ObjectMapping>>,
        ) = apply { this.objectCollectionMappingPerKey = objectCollectionMappingPerKey }

        fun build(): ObjectMapping {
            return ObjectMapping(
                valueMappingPerKey = valueMappingPerKey ?: emptyMap(),
                valueCollectionMappingPerKey = valueCollectionMappingPerKey ?: emptyMap(),
                objectMappingPerKey = objectMappingPerKey ?: emptyMap(),
                objectCollectionMappingPerKey = objectCollectionMappingPerKey ?: emptyMap(),
            )
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
