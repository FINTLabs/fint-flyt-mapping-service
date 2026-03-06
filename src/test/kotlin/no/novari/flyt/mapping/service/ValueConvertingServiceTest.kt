package no.novari.flyt.mapping.service

import no.novari.flyt.mapping.exception.ValueConvertingKeyNotFoundException
import no.novari.flyt.mapping.exception.ValueConvertingNotFoundException
import no.novari.flyt.mapping.kafka.configuration.ValueConvertingRequestProducerService
import no.novari.flyt.mapping.model.instance.InstanceObject
import no.novari.flyt.mapping.model.valueconverting.ValueConverting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ValueConvertingServiceTest {
    private val valueConvertingRequestProducerService = mock(ValueConvertingRequestProducerService::class.java)
    private val instanceReferenceService = mock(InstanceReferenceService::class.java)
    private val valueConvertingReferenceService = mock(ValueConvertingReferenceService::class.java)
    private val service =
        ValueConvertingService(
            valueConvertingRequestProducerService,
            instanceReferenceService,
            valueConvertingReferenceService,
        )

    @Test
    fun testConvertValue_ValidInput() {
        val mappingString = "mapping string"
        val instanceValuePerKey = mutableMapOf<String, String?>()
        val selectedCollectionObjectsPerKey = emptyArray<InstanceObject>()
        val instanceValue = "instance value"
        val valueConvertingId = 123L

        val convertingMap = mutableMapOf<String, String>()
        convertingMap[instanceValue] = "converted value"
        val valueConverting =
            ValueConverting
                .builder()
                .convertingMap(convertingMap)
                .build()

        `when`(
            instanceReferenceService.getFirstInstanceValue(
                mappingString,
                instanceValuePerKey,
                selectedCollectionObjectsPerKey,
            ),
        ).thenReturn(instanceValue)
        `when`(valueConvertingReferenceService.getFirstValueConverterId(mappingString)).thenReturn(valueConvertingId)
        `when`(valueConvertingRequestProducerService.get(valueConvertingId)).thenReturn(valueConverting)

        val result = service.convertValue(mappingString, instanceValuePerKey, selectedCollectionObjectsPerKey)

        assertEquals("converted value", result)
    }

    @Test
    fun testConvertValue_MissingValueConverting() {
        val mappingString = "mapping string"
        val instanceValuePerKey = mutableMapOf<String, String?>()
        val selectedCollectionObjectsPerKey = emptyArray<InstanceObject>()
        val instanceValue = "instance value"
        val valueConvertingId = 123L

        `when`(
            instanceReferenceService.getFirstInstanceValue(
                mappingString,
                instanceValuePerKey,
                selectedCollectionObjectsPerKey,
            ),
        ).thenReturn(instanceValue)
        `when`(valueConvertingReferenceService.getFirstValueConverterId(mappingString)).thenReturn(valueConvertingId)
        `when`(valueConvertingRequestProducerService.get(valueConvertingId)).thenReturn(null)

        val exception =
            assertThrows(ValueConvertingNotFoundException::class.java) {
                service.convertValue(mappingString, instanceValuePerKey, selectedCollectionObjectsPerKey)
            }

        val expectedMessage = "Could not find value converter with id=$valueConvertingId"
        val actualMessage = exception.message

        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    fun testConvertValue_MissingInstanceValueInConvertingMap() {
        val mappingString = "mapping string"
        val instanceValuePerKey = mutableMapOf<String, String?>()
        val selectedCollectionObjectsPerKey = emptyArray<InstanceObject>()
        val instanceValue = "instance value"
        val valueConvertingId = 123L
        val valueConverting =
            ValueConverting
                .builder()
                .convertingMap(mutableMapOf())
                .build()

        `when`(
            instanceReferenceService.getFirstInstanceValue(
                mappingString,
                instanceValuePerKey,
                selectedCollectionObjectsPerKey,
            ),
        ).thenReturn(instanceValue)
        `when`(valueConvertingReferenceService.getFirstValueConverterId(mappingString)).thenReturn(valueConvertingId)
        `when`(valueConvertingRequestProducerService.get(valueConvertingId)).thenReturn(valueConverting)

        val exception =
            assertThrows(ValueConvertingKeyNotFoundException::class.java) {
                service.convertValue(mappingString, instanceValuePerKey, selectedCollectionObjectsPerKey)
            }

        val expectedMessage =
            "Value converting map does not contain key=$instanceValue in value converter with id=$valueConvertingId"
        val actualMessage = exception.message

        assertEquals(expectedMessage, actualMessage)
    }
}
