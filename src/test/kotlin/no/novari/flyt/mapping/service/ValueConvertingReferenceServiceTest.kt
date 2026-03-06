package no.novari.flyt.mapping.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ValueConvertingReferenceServiceTest {
    private val service = ValueConvertingReferenceService()

    @Test
    fun testGetFirstValueConverterId_ValidInput() {
        val mappingString = "something \$vc{123} something"

        val result = service.getFirstValueConverterId(mappingString)

        assertEquals(123L, result)
    }

    @Test
    fun testGetFirstValueConverterId_NoValueConvertingReference() {
        val mappingString = "something {123} something"

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                service.getFirstValueConverterId(mappingString)
            }

        val expectedMessage = "Mapping string contains no valid value converting reference"
        val actualMessage = exception.message

        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    fun testGetFirstValueConverterId_InvalidNumber() {
        val mappingString = "something \$vc{abc} something"

        val exception =
            assertThrows(NumberFormatException::class.java) {
                service.getFirstValueConverterId(mappingString)
            }

        val expectedMessage = "For input string: \"abc\""
        val actualMessage = exception.message

        assertEquals(expectedMessage, actualMessage)
    }
}
