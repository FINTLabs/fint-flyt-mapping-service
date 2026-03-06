package no.novari.flyt.mapping.model.configuration

data class CollectionMapping<T>(
    val elementMappings: Collection<T> = emptyList(),
    val fromCollectionMappings: Collection<FromCollectionMapping<T>> = emptyList(),
) {
    class Builder<T> {
        private var elementMappings: Collection<T>? = null
        private var fromCollectionMappings: Collection<FromCollectionMapping<T>>? = null

        fun elementMappings(elementMappings: Collection<T>) = apply { this.elementMappings = elementMappings }

        fun fromCollectionMappings(fromCollectionMappings: Collection<FromCollectionMapping<T>>) =
            apply { this.fromCollectionMappings = fromCollectionMappings }

        fun build(): CollectionMapping<T> {
            return CollectionMapping(
                elementMappings = elementMappings ?: emptyList(),
                fromCollectionMappings = fromCollectionMappings ?: emptyList(),
            )
        }
    }

    companion object {
        @JvmStatic
        fun <T> builder(): Builder<T> = Builder()
    }
}
