package no.novari.flyt.mapping.model.configuration

import jakarta.validation.constraints.NotBlank

data class FromCollectionMapping<T>(
    val instanceCollectionReferencesOrdered: List<@NotBlank String> = emptyList(),
    val elementMapping: T,
) {
    class Builder<T> {
        private var instanceCollectionReferencesOrdered: List<String>? = null
        private var elementMapping: T? = null

        fun instanceCollectionReferencesOrdered(instanceCollectionReferencesOrdered: List<String>) =
            apply { this.instanceCollectionReferencesOrdered = instanceCollectionReferencesOrdered }

        fun elementMapping(elementMapping: T) = apply { this.elementMapping = elementMapping }

        fun build(): FromCollectionMapping<T> {
            return FromCollectionMapping(
                instanceCollectionReferencesOrdered = instanceCollectionReferencesOrdered ?: emptyList(),
                elementMapping = requireNotNull(elementMapping) { "elementMapping is required" },
            )
        }
    }

    companion object {
        @JvmStatic
        fun <T> builder(): Builder<T> = Builder()
    }
}
