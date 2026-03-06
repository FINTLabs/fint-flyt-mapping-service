package no.novari.flyt.mapping.model.valueconverting

data class ValueConverting(
    val id: Long? = null,
    val fromApplicationId: Long? = null,
    val fromTypeId: String? = null,
    val toApplicationId: String? = null,
    val toTypeId: String? = null,
    val convertingMap: Map<String, String> = emptyMap(),
) {
    class Builder {
        private var id: Long? = null
        private var fromApplicationId: Long? = null
        private var fromTypeId: String? = null
        private var toApplicationId: String? = null
        private var toTypeId: String? = null
        private var convertingMap: Map<String, String>? = null

        fun id(id: Long?) = apply { this.id = id }

        fun fromApplicationId(fromApplicationId: Long?) = apply { this.fromApplicationId = fromApplicationId }

        fun fromTypeId(fromTypeId: String?) = apply { this.fromTypeId = fromTypeId }

        fun toApplicationId(toApplicationId: String?) = apply { this.toApplicationId = toApplicationId }

        fun toTypeId(toTypeId: String?) = apply { this.toTypeId = toTypeId }

        fun convertingMap(convertingMap: Map<String, String>) = apply { this.convertingMap = convertingMap }

        fun build(): ValueConverting {
            return ValueConverting(
                id = id,
                fromApplicationId = fromApplicationId,
                fromTypeId = fromTypeId,
                toApplicationId = toApplicationId,
                toTypeId = toTypeId,
                convertingMap = convertingMap ?: emptyMap(),
            )
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
