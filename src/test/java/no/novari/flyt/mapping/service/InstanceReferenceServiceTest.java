package no.novari.flyt.mapping.service;

import no.novari.flyt.mapping.exception.InstanceFieldNotFoundException;
import no.novari.flyt.mapping.model.instance.InstanceObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstanceReferenceServiceTest {

    private InstanceReferenceService instanceReferenceService;

    @BeforeEach
    void setup() {
        instanceReferenceService = new InstanceReferenceService();
    }

    @Test
    void shouldReturnStringWithValuesFromInstanceFieldsIfAllInstanceFieldsAreFound() {
        String result = instanceReferenceService.replaceIfReferencesWithInstanceValues(
                "Søknad VGS $if{fornavn}$if{etter-navn} $if{person nr1 fødselsdato} for dato $if{dato} ettellerannet",
                Map.of(
                        "tittel", "Tittel som ikke skal brukes",
                        "fornavn", "Ola",
                        "etter-navn", "Nordmann",
                        "dato", "24.12.2022",
                        "person nr1 fødselsdato", "01.01.2000"
                ),
                new InstanceObject[]{}
        );

        assertEquals("Søknad VGS OlaNordmann 01.01.2000 for dato 24.12.2022 ettellerannet", result);
    }

    @Test
    void shouldThrowExceptionIfAnInstanceFieldCannotBeFound() {
        Exception exception = assertThrows(InstanceFieldNotFoundException.class, () ->
                instanceReferenceService.replaceIfReferencesWithInstanceValues(
                        "Søknad VGS $if{etternavn}",
                        Map.of(
                                "tittel", "Tittel som ikke skal brukes",
                                "fornavn", "Ola"
                        ),
                        new InstanceObject[]{}
                )
        );

        assertEquals("Could not find instance field with key='etternavn'", exception.getMessage());
    }

    @Test
    void shouldReturnBlankStringIfAnInstanceFieldValueIsNull() {
        Map<String, String> instanceValuePerKey = new HashMap<>();
        instanceValuePerKey.put("tittel", "Tittel som ikke skal brukes");
        instanceValuePerKey.put("fornavn", null);

        String result = instanceReferenceService.replaceIfReferencesWithInstanceValues(
                "Søknad VGS $if{fornavn}",
                instanceValuePerKey,
                new InstanceObject[]{}
        );

        assertEquals("Søknad VGS ", result);
    }

    @Test
    void shouldReturnStringWithValuesFromIteratedCollectionObjects() {
        Map<String, String> instanceValuePerKey = new HashMap<>();
        instanceValuePerKey.put("tittel", "Tittel her");
        instanceValuePerKey.put("fornavn", null);

        String result = instanceReferenceService.replaceIfReferencesWithInstanceValues(
                "Søknad VGS - $icf{0}{navn} - $if{tittel} - $icf{0}{adresselinje} - $icf{1}{organisasjon}",
                instanceValuePerKey,
                new InstanceObject[]{
                        InstanceObject
                                .builder()
                                .valuePerKey(Map.of(
                                        "navn", "Navn Navnesen",
                                        "adresselinje", "Gate 1, 0000, By, Land"
                                ))
                                .build(),
                        InstanceObject
                                .builder()
                                .valuePerKey(Map.of(
                                        "organisasjon", "Fintlabs"
                                ))
                                .build(),
                }
        );

        assertEquals("Søknad VGS - Navn Navnesen - Tittel her - Gate 1, 0000, By, Land - Fintlabs", result);
    }

    @Test
    void shouldThrowExceptionIfAnInstanceCollectionObjectFieldCannotBeFound() {
        Map<String, String> instanceValuePerKey = new HashMap<>();
        instanceValuePerKey.put("tittel", "Tittel her");
        instanceValuePerKey.put("fornavn", null);

        Exception exception = assertThrows(InstanceFieldNotFoundException.class, () ->
                instanceReferenceService.replaceIfReferencesWithInstanceValues(
                        "Søknad VGS - $icf{0}{navn} - $if{tittel} - $icf{0}{adresselinje} - $icf{1}{organisasjon}",
                        instanceValuePerKey,
                        new InstanceObject[]{
                                InstanceObject
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "navn", "Navn Navnesen"
                                        ))
                                        .build(),
                                InstanceObject
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "organisasjon", "Fintlabs"
                                        ))
                                        .build(),
                        }
                )
        );

        assertEquals("Could not find instance field with key='adresselinje'", exception.getMessage());
    }
}
