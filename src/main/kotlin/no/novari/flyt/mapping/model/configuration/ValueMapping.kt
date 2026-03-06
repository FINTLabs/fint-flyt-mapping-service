package no.novari.flyt.mapping.model.configuration

data class ValueMapping(
    val type: Type,
    val mappingString: String? = null,
) {
    enum class Type {
        STRING,
        URL,
        BOOLEAN,
        DYNAMIC_STRING,
        FILE,
        VALUE_CONVERTING,
    }

    class Builder {
        private var type: Type? = null
        private var mappingString: String? = null

        fun type(type: Type) = apply { this.type = type }

        fun mappingString(mappingString: String?) = apply { this.mappingString = mappingString }

        fun build(): ValueMapping {
            return ValueMapping(
                type = requireNotNull(type) { "type is required" },
                mappingString = mappingString,
            )
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
