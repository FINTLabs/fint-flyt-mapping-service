package no.novari.flyt.mapping.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValueConvertingReferenceServiceTest {

    private final ValueConvertingReferenceService service = new ValueConvertingReferenceService();

    @Test
    void testGetFirstValueConverterId_ValidInput() {
        String mappingString = "something $vc{123} something";

        Long result = service.getFirstValueConverterId(mappingString);

        assertEquals(123L, result);
    }

    @Test
    void testGetFirstValueConverterId_NoValueConvertingReference() {
        String mappingString = "something {123} something";

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                service.getFirstValueConverterId(mappingString)
        );

        String expectedMessage = "Mapping string contains no valid value converting reference";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testGetFirstValueConverterId_InvalidNumber() {
        String mappingString = "something $vc{abc} something";

        Exception exception = assertThrows(NumberFormatException.class, () ->
                service.getFirstValueConverterId(mappingString)
        );

        String expectedMessage = "For input string: \"abc\"";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
}
