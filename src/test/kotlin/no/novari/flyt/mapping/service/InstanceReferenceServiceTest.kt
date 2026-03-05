package no.novari.flyt.mapping.service

import no.novari.flyt.mapping.exception.InstanceFieldNotFoundException
import no.novari.flyt.mapping.model.instance.InstanceObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InstanceReferenceServiceTest {
    private lateinit var instanceReferenceService: InstanceReferenceService

    @BeforeEach
    fun setup() {
        instanceReferenceService = InstanceReferenceService()
    }

    @Test
    fun shouldReturnStringWithValuesFromInstanceFieldsIfAllInstanceFieldsAreFound() {
        val result =
            instanceReferenceService.replaceIfReferencesWithInstanceValues(
                "Søknad VGS \$if{fornavn}\$if{etter-navn} \$if{person nr1 fødselsdato} for dato \$if{dato} ettellerannet",
                mapOf(
                    "tittel" to "Tittel som ikke skal brukes",
                    "fornavn" to "Ola",
                    "etter-navn" to "Nordmann",
                    "dato" to "24.12.2022",
                    "person nr1 fødselsdato" to "01.01.2000",
                ),
                emptyArray(),
            )

        assertEquals("Søknad VGS OlaNordmann 01.01.2000 for dato 24.12.2022 ettellerannet", result)
    }

    @Test
    fun shouldThrowExceptionIfAnInstanceFieldCannotBeFound() {
        val exception =
            assertThrows(InstanceFieldNotFoundException::class.java) {
                instanceReferenceService.replaceIfReferencesWithInstanceValues(
                    "Søknad VGS \$if{etternavn}",
                    mapOf(
                        "tittel" to "Tittel som ikke skal brukes",
                        "fornavn" to "Ola",
                    ),
                    emptyArray(),
                )
            }

        assertEquals("Could not find instance field with key='etternavn'", exception.message)
    }

    @Test
    fun shouldReturnBlankStringIfAnInstanceFieldValueIsNull() {
        val instanceValuePerKey =
            mutableMapOf(
                "tittel" to "Tittel som ikke skal brukes",
                "fornavn" to null,
            )

        val result =
            instanceReferenceService.replaceIfReferencesWithInstanceValues(
                "Søknad VGS \$if{fornavn}",
                instanceValuePerKey,
                emptyArray(),
            )

        assertEquals("Søknad VGS ", result)
    }

    @Test
    fun shouldReturnStringWithValuesFromIteratedCollectionObjects() {
        val instanceValuePerKey =
            mutableMapOf(
                "tittel" to "Tittel her",
                "fornavn" to null,
            )

        val result =
            instanceReferenceService.replaceIfReferencesWithInstanceValues(
                "Søknad VGS - \$icf{0}{navn} - \$if{tittel} - \$icf{0}{adresselinje} - \$icf{1}{organisasjon}",
                instanceValuePerKey,
                arrayOf(
                    InstanceObject
                        .builder()
                        .valuePerKey(
                            mapOf(
                                "navn" to "Navn Navnesen",
                                "adresselinje" to "Gate 1, 0000, By, Land",
                            ),
                        ).build(),
                    InstanceObject
                        .builder()
                        .valuePerKey(mapOf("organisasjon" to "Fintlabs"))
                        .build(),
                ),
            )

        assertEquals("Søknad VGS - Navn Navnesen - Tittel her - Gate 1, 0000, By, Land - Fintlabs", result)
    }

    @Test
    fun shouldThrowExceptionIfAnInstanceCollectionObjectFieldCannotBeFound() {
        val instanceValuePerKey =
            mutableMapOf(
                "tittel" to "Tittel her",
                "fornavn" to null,
            )

        val exception =
            assertThrows(InstanceFieldNotFoundException::class.java) {
                instanceReferenceService.replaceIfReferencesWithInstanceValues(
                    "Søknad VGS - \$icf{0}{navn} - \$if{tittel} - \$icf{0}{adresselinje} - \$icf{1}{organisasjon}",
                    instanceValuePerKey,
                    arrayOf(
                        InstanceObject
                            .builder()
                            .valuePerKey(mapOf("navn" to "Navn Navnesen"))
                            .build(),
                        InstanceObject
                            .builder()
                            .valuePerKey(mapOf("organisasjon" to "Fintlabs"))
                            .build(),
                    ),
                )
            }

        assertEquals("Could not find instance field with key='adresselinje'", exception.message)
    }
}
