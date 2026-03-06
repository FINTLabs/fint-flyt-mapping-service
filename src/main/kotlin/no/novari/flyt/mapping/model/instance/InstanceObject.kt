package no.novari.flyt.mapping.model.instance

data class InstanceObject(
    val valuePerKey: Map<String, String?> = emptyMap(),
    val objectCollectionPerKey: Map<String, Collection<InstanceObject>> = emptyMap(),
) {
    class Builder {
        private var valuePerKey: Map<String, String?>? = null
        private var objectCollectionPerKey: Map<String, Collection<InstanceObject>>? = null

        fun valuePerKey(valuePerKey: Map<String, String?>) = apply { this.valuePerKey = valuePerKey }

        fun objectCollectionPerKey(objectCollectionPerKey: Map<String, Collection<InstanceObject>>) =
            apply { this.objectCollectionPerKey = objectCollectionPerKey }

        fun build(): InstanceObject {
            return InstanceObject(
                valuePerKey = valuePerKey ?: emptyMap(),
                objectCollectionPerKey = objectCollectionPerKey ?: emptyMap(),
            )
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
