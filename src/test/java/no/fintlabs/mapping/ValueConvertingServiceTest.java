package no.fintlabs.mapping;

import no.fintlabs.exception.ValueConvertingKeyNotFoundException;
import no.fintlabs.exception.ValueConvertingNotFoundException;
import no.fintlabs.kafka.configuration.ValueConvertingRequestProducerService;
import no.fintlabs.model.instance.InstanceObject;
import no.fintlabs.model.valueconverting.ValueConverting;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValueConvertingServiceTest {

    private final ValueConvertingRequestProducerService valueConvertingRequestProducerService = mock(ValueConvertingRequestProducerService.class);
    private final InstanceReferenceService instanceReferenceService = mock(InstanceReferenceService.class);
    private final ValueConvertingReferenceService valueConvertingReferenceService = mock(ValueConvertingReferenceService.class);
    private final ValueConvertingService service = new ValueConvertingService(valueConvertingRequestProducerService, instanceReferenceService, valueConvertingReferenceService);

    @Test
    public void testConvertValue_ValidInput() {
        String mappingString = "mapping string";
        Map<String, String> instanceValuePerKey = new HashMap<>();
        InstanceObject[] selectedCollectionObjectsPerKey = new InstanceObject[0];
        String instanceValue = "instance value";
        Long valueConvertingId = 123L;
        ValueConverting valueConverting = ValueConverting
                .builder()
                .convertingMap(new HashMap<>())
                .build();
        valueConverting.getConvertingMap().put(instanceValue, "converted value");

        when(instanceReferenceService.getFirstInstanceValue(mappingString, instanceValuePerKey, selectedCollectionObjectsPerKey)).thenReturn(instanceValue);
        when(valueConvertingReferenceService.getFirstValueConverterId(mappingString)).thenReturn(valueConvertingId);
        when(valueConvertingRequestProducerService.get(valueConvertingId)).thenReturn(Optional.of(valueConverting));

        String result = service.convertValue(mappingString, instanceValuePerKey, selectedCollectionObjectsPerKey);

        assertEquals("converted value", result);
    }

    @Test
    void testConvertValue_MissingValueConverting() {
        String mappingString = "mapping string";
        Map<String, String> instanceValuePerKey = new HashMap<>();
        InstanceObject[] selectedCollectionObjectsPerKey = new InstanceObject[0];
        String instanceValue = "instance value";
        Long valueConvertingId = 123L;

        when(instanceReferenceService.getFirstInstanceValue(mappingString, instanceValuePerKey, selectedCollectionObjectsPerKey)).thenReturn(instanceValue);
        when(valueConvertingReferenceService.getFirstValueConverterId(mappingString)).thenReturn(valueConvertingId);
        when(valueConvertingRequestProducerService.get(valueConvertingId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ValueConvertingNotFoundException.class, () ->
                service.convertValue(mappingString, instanceValuePerKey, selectedCollectionObjectsPerKey)
        );

        String expectedMessage = "Could not find value converter with id=" + valueConvertingId;
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testConvertValue_MissingInstanceValueInConvertingMap() {
        String mappingString = "mapping string";
        Map<String, String> instanceValuePerKey = new HashMap<>();
        InstanceObject[] selectedCollectionObjectsPerKey = new InstanceObject[0];
        String instanceValue = "instance value";
        Long valueConvertingId = 123L;
        ValueConverting valueConverting = ValueConverting
                .builder()
                .convertingMap(new HashMap<>())
                .build();

        when(instanceReferenceService.getFirstInstanceValue(mappingString, instanceValuePerKey, selectedCollectionObjectsPerKey)).thenReturn(instanceValue);
        when(valueConvertingReferenceService.getFirstValueConverterId(mappingString)).thenReturn(valueConvertingId);
        when(valueConvertingRequestProducerService.get(valueConvertingId)).thenReturn(Optional.of(valueConverting));

        Exception exception = assertThrows(ValueConvertingKeyNotFoundException.class, () ->
                service.convertValue(mappingString, instanceValuePerKey, selectedCollectionObjectsPerKey)
        );

        String expectedMessage = "Value converting map does not contain key=" + instanceValue + " in value converter with id=" + valueConvertingId;
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
}
