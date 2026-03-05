package no.novari.flyt.mapping.service

import no.novari.flyt.mapping.model.configuration.ValueMapping
import no.novari.flyt.mapping.model.instance.InstanceObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class ValueMappingServiceTest {
    @InjectMocks
    private lateinit var valueMappingService: ValueMappingService

    @Mock
    private lateinit var instanceReferenceService: InstanceReferenceService

    @Mock
    private lateinit var valueConvertingService: ValueConvertingService

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testToValue_StringType() {
        val valueMapping =
            ValueMapping
                .builder()
                .type(ValueMapping.Type.STRING)
                .mappingString("test_string")
                .build()
        assertEquals("test_string", valueMappingService.toValue(valueMapping, emptyMap(), emptyArray<InstanceObject>()))
    }

    @Test
    fun testToValue_BooleanType() {
        val valueMapping =
            ValueMapping
                .builder()
                .type(ValueMapping.Type.BOOLEAN)
                .mappingString("true")
                .build()
        assertEquals(true, valueMappingService.toValue(valueMapping, emptyMap(), emptyArray<InstanceObject>()))
    }

    @Test
    fun testToValue_FileType() {
        val valueMapping =
            ValueMapping
                .builder()
                .type(ValueMapping.Type.FILE)
                .mappingString("file_key")
                .build()
        `when`(instanceReferenceService.replaceIfReferencesWithInstanceValues(eq("file_key"), any(), any()))
            .thenReturn("test_value")

        assertEquals("test_value", valueMappingService.toValue(valueMapping, emptyMap(), emptyArray<InstanceObject>()))
    }

    @Test
    fun testToValue_ValueConvertingType() {
        val valueMapping =
            ValueMapping
                .builder()
                .type(
                    ValueMapping.Type.VALUE_CONVERTING,
                ).mappingString("value_key")
                .build()
        `when`(valueConvertingService.convertValue(eq("value_key"), any(), any())).thenReturn("test_value")

        assertEquals("test_value", valueMappingService.toValue(valueMapping, emptyMap(), emptyArray<InstanceObject>()))
    }

    @Test
    fun testToValue_UrlType() {
        val valueMapping =
            ValueMapping
                .builder()
                .type(
                    ValueMapping.Type.URL,
                ).mappingString("https://example.com")
                .build()
        assertEquals(
            "https://example.com",
            valueMappingService.toValue(valueMapping, emptyMap(), emptyArray<InstanceObject>()),
        )
    }

    @Test
    fun testToValue_DynamicStringType() {
        val valueMapping =
            ValueMapping
                .builder()
                .type(
                    ValueMapping.Type.DYNAMIC_STRING,
                ).mappingString("dynamic_key")
                .build()
        `when`(instanceReferenceService.replaceIfReferencesWithInstanceValues(eq("dynamic_key"), any(), any()))
            .thenReturn("dynamic_value")

        assertEquals(
            "dynamic_value",
            valueMappingService.toValue(valueMapping, emptyMap(), emptyArray<InstanceObject>()),
        )
    }
}
