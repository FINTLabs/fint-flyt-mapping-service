package no.novari.flyt.mapping.service

import org.springframework.stereotype.Service

@Service
class ValueConvertingReferenceService {
    fun getFirstValueConverterId(mappingString: String): Long {
        val matcher = VALUE_CONVERTING_REFERENCE_PATTERN.matcher(mappingString)
        if (!matcher.find()) {
            throw IllegalArgumentException("Mapping string contains no valid value converting reference")
        }

        val matchedReference = matcher.group(0)
        return getValueConvertingId(matchedReference)
    }

    private fun getValueConvertingId(valueConvertingReference: String): Long {
        return valueConvertingReference
            .replace("${'$'}vc{", "")
            .replace("}", "")
            .toLong()
    }

    companion object {
        private val VALUE_CONVERTING_REFERENCE_PATTERN = Regex("""${'$'}vc[{][^}]+}""").toPattern()
    }
}
