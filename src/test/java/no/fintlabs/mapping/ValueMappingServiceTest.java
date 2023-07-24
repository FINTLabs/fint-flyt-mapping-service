package no.fintlabs.mapping;

import no.fintlabs.model.configuration.ValueMapping;
import no.fintlabs.model.instance.InstanceObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ValueMappingServiceTest {
    @InjectMocks
    private ValueMappingService valueMappingService;

    @Mock
    private InstanceReferenceService instanceReferenceService;

    @Mock
    private ValueConvertingService valueConvertingService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testToValue_StringType() {
        ValueMapping valueMapping = ValueMapping.builder().type(ValueMapping.Type.STRING).mappingString("test_string").build();
        assertEquals("test_string", valueMappingService.toValue(valueMapping, Map.of(), new InstanceObject[0]));
    }

    @Test
    void testToValue_BooleanType() {
        ValueMapping valueMapping = ValueMapping.builder().type(ValueMapping.Type.BOOLEAN).mappingString("true").build();
        assertEquals(true, valueMappingService.toValue(valueMapping, Map.of(), new InstanceObject[0]));
    }

    @Test
    void testToValue_FileType() {
        ValueMapping valueMapping = ValueMapping.builder().type(ValueMapping.Type.FILE).mappingString("file_key").build();
        when(instanceReferenceService.replaceIfReferencesWithInstanceValues(eq("file_key"), any(), any())).thenReturn("test_value");

        assertEquals("test_value", valueMappingService.toValue(valueMapping, Map.of(), new InstanceObject[0]));
    }

    @Test
    void testToValue_ValueConvertingType() {
        ValueMapping valueMapping = ValueMapping.builder().type(ValueMapping.Type.VALUE_CONVERTING).mappingString("value_key").build();
        when(valueConvertingService.convertValue(eq("value_key"), any(), any())).thenReturn("test_value");

        assertEquals("test_value", valueMappingService.toValue(valueMapping, Map.of(), new InstanceObject[0]));
    }

    @Test
    void testToValue_UrlType() {
        ValueMapping valueMapping = ValueMapping.builder().type(ValueMapping.Type.URL).mappingString("https://example.com").build();
        assertEquals("https://example.com", valueMappingService.toValue(valueMapping, Map.of(), new InstanceObject[0]));
    }

    @Test
    void testToValue_DynamicStringType() {
        ValueMapping valueMapping = ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("dynamic_key").build();
        when(instanceReferenceService.replaceIfReferencesWithInstanceValues(eq("dynamic_key"), any(), any())).thenReturn("dynamic_value");

        assertEquals("dynamic_value", valueMappingService.toValue(valueMapping, Map.of(), new InstanceObject[0]));
    }
}
