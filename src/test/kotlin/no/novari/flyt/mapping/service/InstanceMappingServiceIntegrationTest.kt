package no.novari.flyt.mapping.service

import no.novari.flyt.mapping.kafka.configuration.ValueConvertingRequestProducerService
import no.novari.flyt.mapping.model.configuration.CollectionMapping
import no.novari.flyt.mapping.model.configuration.FromCollectionMapping
import no.novari.flyt.mapping.model.configuration.ObjectMapping
import no.novari.flyt.mapping.model.configuration.ValueMapping
import no.novari.flyt.mapping.model.instance.InstanceObject
import no.novari.flyt.mapping.model.valueconverting.ValueConverting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Collections

class InstanceMappingServiceIntegrationTest {
    private lateinit var valueConvertingRequestProducerService: ValueConvertingRequestProducerService
    private lateinit var instanceMappingService: InstanceMappingService

    @BeforeEach
    fun setup() {
        valueConvertingRequestProducerService = mock(ValueConvertingRequestProducerService::class.java)
        val instanceReferenceService = InstanceReferenceService()
        instanceMappingService =
            InstanceMappingService(
                instanceReferenceService,
                ValueMappingService(
                    InstanceReferenceService(),
                    ValueConvertingService(
                        valueConvertingRequestProducerService,
                        instanceReferenceService,
                        ValueConvertingReferenceService(),
                    ),
                ),
            )
    }

    @Test
    fun shouldMapInstanceBasedOnMappingFromConfiguration() {
        val dynamicType = ValueMapping.Type.DYNAMIC_STRING
        val objectMapping =
            ObjectMapping
                .builder()
                .valueMappingPerKey(
                    mapOf(
                        "kombinert tittel" to
                            ValueMapping
                                .builder()
                                .type(dynamicType)
                                .mappingString("Livsmotto: \$if{tittel1}, \$if{tittel2}")
                                .build(),
                        "ferdigstilt" to
                            ValueMapping
                                .builder()
                                .type(ValueMapping.Type.BOOLEAN)
                                .mappingString("true")
                                .build(),
                    ),
                ).objectMappingPerKey(
                    mapOf(
                        "adresse" to
                            ObjectMapping
                                .builder()
                                .valueMappingPerKey(
                                    mapOf(
                                        "mottakernavn" to
                                            ValueMapping
                                                .builder()
                                                .type(dynamicType)
                                                .mappingString("\$if{person2.navn}")
                                                .build(),
                                        "by" to
                                            ValueMapping
                                                .builder()
                                                .type(dynamicType)
                                                .mappingString("\$if{person2.by}")
                                                .build(),
                                        "byKortnavn" to
                                            ValueMapping
                                                .builder()
                                                .type(ValueMapping.Type.VALUE_CONVERTING)
                                                .mappingString("\$vc{0}\$if{person2.by}")
                                                .build(),
                                    ),
                                ).build(),
                    ),
                ).objectCollectionMappingPerKey(
                    mapOf(
                        "parter" to
                            CollectionMapping
                                .builder<ObjectMapping>()
                                .elementMappings(
                                    listOf(
                                        ObjectMapping
                                            .builder()
                                            .valueMappingPerKey(
                                                mapOf(
                                                    "navn" to
                                                        ValueMapping
                                                            .builder()
                                                            .type(dynamicType)
                                                            .mappingString("\$if{person1.navn}")
                                                            .build(),
                                                    "publikasjon" to
                                                        ValueMapping
                                                            .builder()
                                                            .type(dynamicType)
                                                            .mappingString("Ole Brumm - Ukjent")
                                                            .build(),
                                                    "vedleggTittel" to
                                                        ValueMapping
                                                            .builder()
                                                            .type(dynamicType)
                                                            .mappingString("Filopplastning_128920")
                                                            .build(),
                                                ),
                                            ).objectCollectionMappingPerKey(
                                                mapOf("priser" to CollectionMapping.builder<ObjectMapping>().build()),
                                            ).build(),
                                    ),
                                ).fromCollectionMappings(
                                    listOf(
                                        FromCollectionMapping
                                            .builder<ObjectMapping>()
                                            .instanceCollectionReferencesOrdered(
                                                listOf(
                                                    "\$if{saksparter}",
                                                    "\$icf{0}{publikasjoner}",
                                                    "\$if{dokumenter}",
                                                ),
                                            ).elementMapping(
                                                ObjectMapping
                                                    .builder()
                                                    .valueMappingPerKey(
                                                        mapOf(
                                                            "navn" to
                                                                ValueMapping
                                                                    .builder()
                                                                    .type(dynamicType)
                                                                    .mappingString("\$icf{0}{navn}")
                                                                    .build(),
                                                            "publikasjonTittelFørsteBokstav" to
                                                                ValueMapping
                                                                    .builder()
                                                                    .type(ValueMapping.Type.VALUE_CONVERTING)
                                                                    .mappingString("\$vc{2}\$icf{1}{tittel}")
                                                                    .build(),
                                                            "publikasjon" to
                                                                ValueMapping
                                                                    .builder()
                                                                    .type(dynamicType)
                                                                    .mappingString(
                                                                        "\$icf{1}{tittel} - \$icf{1}{utgiver}",
                                                                    ).build(),
                                                            "vedleggTittel" to
                                                                ValueMapping
                                                                    .builder()
                                                                    .type(dynamicType)
                                                                    .mappingString("\$icf{2}{tittel}")
                                                                    .build(),
                                                        ),
                                                    ).valueCollectionMappingPerKey(
                                                        mapOf(
                                                            "venner" to
                                                                CollectionMapping
                                                                    .builder<ValueMapping>()
                                                                    .elementMappings(
                                                                        listOf(
                                                                            ValueMapping
                                                                                .builder()
                                                                                .type(ValueMapping.Type.STRING)
                                                                                .mappingString("Ole Brum")
                                                                                .build(),
                                                                        ),
                                                                    ).fromCollectionMappings(
                                                                        listOf(
                                                                            FromCollectionMapping
                                                                                .builder<ValueMapping>()
                                                                                .instanceCollectionReferencesOrdered(
                                                                                    listOf("\$if{saksparter}"),
                                                                                ).elementMapping(
                                                                                    ValueMapping
                                                                                        .builder()
                                                                                        .type(
                                                                                            dynamicType,
                                                                                        ).mappingString(
                                                                                            "\$icf{3}{navn}",
                                                                                        ).build(),
                                                                                ).build(),
                                                                        ),
                                                                    ).build(),
                                                        ),
                                                    ).objectCollectionMappingPerKey(
                                                        mapOf(
                                                            "priser" to
                                                                CollectionMapping
                                                                    .builder<ObjectMapping>()
                                                                    .elementMappings(
                                                                        listOf(
                                                                            ObjectMapping
                                                                                .builder()
                                                                                .valueMappingPerKey(
                                                                                    mapOf(
                                                                                        "pristittel" to
                                                                                            ValueMapping
                                                                                                .builder()
                                                                                                .type(
                                                                                                    dynamicType,
                                                                                                ).mappingString(
                                                                                                    "pris-\$icf{1}{tittel}-\$icf{2}{tittel}",
                                                                                                ).build(),
                                                                                    ),
                                                                                ).build(),
                                                                        ),
                                                                    ).build(),
                                                            "dokumenter" to
                                                                CollectionMapping
                                                                    .builder<ObjectMapping>()
                                                                    .fromCollectionMappings(
                                                                        listOf(
                                                                            FromCollectionMapping
                                                                                .builder<ObjectMapping>()
                                                                                .instanceCollectionReferencesOrdered(
                                                                                    listOf("\$if{dokumenter}"),
                                                                                ).elementMapping(
                                                                                    ObjectMapping
                                                                                        .builder()
                                                                                        .valueMappingPerKey(
                                                                                            mapOf(
                                                                                                "tittel" to
                                                                                                    ValueMapping
                                                                                                        .builder()
                                                                                                        .type(
                                                                                                            dynamicType,
                                                                                                        ).mappingString(
                                                                                                            "\$icf{3}{tittel}",
                                                                                                        ).build(),
                                                                                            ),
                                                                                        ).build(),
                                                                                ).build(),
                                                                        ),
                                                                    ).build(),
                                                        ),
                                                    ).build(),
                                            ).build(),
                                    ),
                                ).build(),
                    ),
                ).build()

        val instance =
            InstanceObject
                .builder()
                .valuePerKey(
                    mapOf(
                        "tittel1" to "Hei på deg",
                        "tittel2" to "her er jeg",
                        "person1.navn" to "Arne Arnesen",
                        "person2.navn" to "Navn Navnesen",
                        "person2.by" to "Oslo",
                    ),
                ).objectCollectionPerKey(
                    mapOf(
                        "saksparter" to
                            listOf(
                                InstanceObject
                                    .builder()
                                    .valuePerKey(mapOf("navn" to "Nora Noradottir"))
                                    .objectCollectionPerKey(
                                        mapOf(
                                            "publikasjoner" to
                                                listOf(
                                                    InstanceObject
                                                        .builder()
                                                        .valuePerKey(
                                                            mapOf(
                                                                "tittel" to "Min barnebok",
                                                                "utgiver" to "Bokprodusenten",
                                                            ),
                                                        ).build(),
                                                    InstanceObject
                                                        .builder()
                                                        .valuePerKey(
                                                            mapOf(
                                                                "tittel" to "Ludde",
                                                                "utgiver" to "Alletiders",
                                                            ),
                                                        ).build(),
                                                ),
                                        ),
                                    ).build(),
                                InstanceObject
                                    .builder()
                                    .valuePerKey(mapOf("navn" to "Eirik Eiriksson"))
                                    .objectCollectionPerKey(
                                        mapOf(
                                            "publikasjoner" to
                                                listOf(
                                                    InstanceObject
                                                        .builder()
                                                        .valuePerKey(
                                                            mapOf(
                                                                "tittel" to "Den lille mulvarpen",
                                                                "utgiver" to "ABC",
                                                            ),
                                                        ).build(),
                                                ),
                                        ),
                                    ).build(),
                            ),
                        "dokumenter" to
                            listOf(
                                InstanceObject.builder().valuePerKey(mapOf("tittel" to "Dokument1")).build(),
                                InstanceObject.builder().valuePerKey(mapOf("tittel" to "Dokument2")).build(),
                            ),
                    ),
                ).build()

        val valueConverting1 =
            ValueConverting
                .builder()
                .convertingMap(
                    mapOf(
                        "Trondheim" to "TRD",
                        "Oslo" to "OSL",
                    ),
                ).build()

        `when`(valueConvertingRequestProducerService.get(0L)).thenReturn(valueConverting1)

        val result1 = valueConvertingRequestProducerService.get(0L)

        verify(valueConvertingRequestProducerService, times(1)).get(0L)
        assertEquals(valueConverting1, result1)

        val valueConverting2 =
            ValueConverting
                .builder()
                .convertingMap(
                    mapOf(
                        "Min barnebok" to "M",
                        "Ludde" to "L",
                        "Den lille mulvarpen" to "D",
                    ),
                ).build()

        `when`(valueConvertingRequestProducerService.get(2L)).thenReturn(valueConverting2)

        for (i in 0 until 6) {
            val result2 = valueConvertingRequestProducerService.get(2L)
            verify(valueConvertingRequestProducerService, times(i + 1)).get(2L)
            assertEquals(valueConverting2, result2)
        }

        val mappedInstance = instanceMappingService.toMappedInstanceObject(objectMapping, instance)

        val expectedInstance: Map<String, Any> =
            mapOf(
                "kombinert tittel" to "Livsmotto: Hei på deg, her er jeg",
                "ferdigstilt" to true,
                "adresse" to
                    mapOf(
                        "mottakernavn" to "Navn Navnesen",
                        "by" to "Oslo",
                        "byKortnavn" to "OSL",
                    ),
                "parter" to
                    listOf<Any>(
                        mapOf(
                            "navn" to "Arne Arnesen",
                            "publikasjon" to "Ole Brumm - Ukjent",
                            "vedleggTittel" to "Filopplastning_128920",
                            "priser" to Collections.emptyList<Any>(),
                        ),
                        mapOf(
                            "navn" to "Nora Noradottir",
                            "publikasjonTittelFørsteBokstav" to "M",
                            "publikasjon" to "Min barnebok - Bokprodusenten",
                            "vedleggTittel" to "Dokument1",
                            "priser" to listOf(mapOf("pristittel" to "pris-Min barnebok-Dokument1")),
                            "dokumenter" to listOf(mapOf("tittel" to "Dokument1"), mapOf("tittel" to "Dokument2")),
                            "venner" to listOf("Ole Brum", "Nora Noradottir", "Eirik Eiriksson"),
                        ),
                        mapOf(
                            "navn" to "Nora Noradottir",
                            "publikasjonTittelFørsteBokstav" to "M",
                            "publikasjon" to "Min barnebok - Bokprodusenten",
                            "vedleggTittel" to "Dokument2",
                            "priser" to listOf(mapOf("pristittel" to "pris-Min barnebok-Dokument2")),
                            "dokumenter" to listOf(mapOf("tittel" to "Dokument1"), mapOf("tittel" to "Dokument2")),
                            "venner" to listOf("Ole Brum", "Nora Noradottir", "Eirik Eiriksson"),
                        ),
                        mapOf(
                            "navn" to "Nora Noradottir",
                            "publikasjonTittelFørsteBokstav" to "L",
                            "publikasjon" to "Ludde - Alletiders",
                            "vedleggTittel" to "Dokument1",
                            "priser" to listOf(mapOf("pristittel" to "pris-Ludde-Dokument1")),
                            "dokumenter" to listOf(mapOf("tittel" to "Dokument1"), mapOf("tittel" to "Dokument2")),
                            "venner" to listOf("Ole Brum", "Nora Noradottir", "Eirik Eiriksson"),
                        ),
                        mapOf(
                            "navn" to "Nora Noradottir",
                            "publikasjonTittelFørsteBokstav" to "L",
                            "publikasjon" to "Ludde - Alletiders",
                            "vedleggTittel" to "Dokument2",
                            "priser" to listOf(mapOf("pristittel" to "pris-Ludde-Dokument2")),
                            "dokumenter" to listOf(mapOf("tittel" to "Dokument1"), mapOf("tittel" to "Dokument2")),
                            "venner" to listOf("Ole Brum", "Nora Noradottir", "Eirik Eiriksson"),
                        ),
                        mapOf(
                            "navn" to "Eirik Eiriksson",
                            "publikasjonTittelFørsteBokstav" to "D",
                            "publikasjon" to "Den lille mulvarpen - ABC",
                            "vedleggTittel" to "Dokument1",
                            "priser" to listOf(mapOf("pristittel" to "pris-Den lille mulvarpen-Dokument1")),
                            "dokumenter" to listOf(mapOf("tittel" to "Dokument1"), mapOf("tittel" to "Dokument2")),
                            "venner" to listOf("Ole Brum", "Nora Noradottir", "Eirik Eiriksson"),
                        ),
                        mapOf(
                            "navn" to "Eirik Eiriksson",
                            "publikasjonTittelFørsteBokstav" to "D",
                            "publikasjon" to "Den lille mulvarpen - ABC",
                            "vedleggTittel" to "Dokument2",
                            "priser" to listOf(mapOf("pristittel" to "pris-Den lille mulvarpen-Dokument2")),
                            "dokumenter" to listOf(mapOf("tittel" to "Dokument1"), mapOf("tittel" to "Dokument2")),
                            "venner" to listOf("Ole Brum", "Nora Noradottir", "Eirik Eiriksson"),
                        ),
                    ),
            )

        assertEquals(expectedInstance, mappedInstance)
    }
}
