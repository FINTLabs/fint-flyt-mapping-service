package no.novari.flyt.mapping.service;

import no.novari.flyt.mapping.kafka.configuration.ValueConvertingRequestProducerService;
import no.novari.flyt.mapping.model.configuration.CollectionMapping;
import no.novari.flyt.mapping.model.configuration.FromCollectionMapping;
import no.novari.flyt.mapping.model.configuration.ObjectMapping;
import no.novari.flyt.mapping.model.configuration.ValueMapping;
import no.novari.flyt.mapping.model.instance.InstanceObject;
import no.novari.flyt.mapping.model.valueconverting.ValueConverting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class InstanceMappingServiceIntegrationTest {

    private ValueConvertingRequestProducerService valueConvertingRequestProducerService;
    private InstanceMappingService instanceMappingService;

    @BeforeEach
    void setup() {
        valueConvertingRequestProducerService = mock(ValueConvertingRequestProducerService.class);
        InstanceReferenceService instanceReferenceService = new InstanceReferenceService();
        instanceMappingService = new InstanceMappingService(
                instanceReferenceService,
                new ValueMappingService(
                        new InstanceReferenceService(),
                        new ValueConvertingService(
                                valueConvertingRequestProducerService,
                                instanceReferenceService,
                                new ValueConvertingReferenceService()
                        )
                )
        );
    }

    @Test
    void shouldMapInstanceBasedOnMappingFromConfiguration() {
        ObjectMapping objectMapping = ObjectMapping
                .builder()
                .valueMappingPerKey(Map.of(
                                "kombinert tittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("Livsmotto: $if{tittel1}, $if{tittel2}").build(),
                                "ferdigstilt", ValueMapping.builder().type(ValueMapping.Type.BOOLEAN).mappingString("true").build()
                        )
                )
                .objectMappingPerKey(Map.of(
                        "adresse",
                        ObjectMapping
                                .builder()
                                .valueMappingPerKey(Map.of(
                                        "mottakernavn", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("$if{person2.navn}").build(),
                                        "by", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("$if{person2.by}").build(),
                                        "byKortnavn", ValueMapping.builder().type(ValueMapping.Type.VALUE_CONVERTING).mappingString("$vc{0}$if{person2.by}").build()
                                ))
                                .build()
                ))
                .objectCollectionMappingPerKey(Map.of(
                        "parter",
                        CollectionMapping
                                .<ObjectMapping>builder()
                                .elementMappings(List.of(
                                        ObjectMapping
                                                .builder()
                                                .valueMappingPerKey(Map.of(
                                                        "navn", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("$if{person1.navn}").build(),
                                                        "publikasjon", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("Ole Brumm - Ukjent").build(),
                                                        "vedleggTittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("Filopplastning_128920").build()
                                                ))
                                                .objectCollectionMappingPerKey(Map.of(
                                                        "priser", CollectionMapping.<ObjectMapping>builder().build()
                                                ))
                                                .build()
                                ))
                                .fromCollectionMappings(List.of(
                                        FromCollectionMapping
                                                .<ObjectMapping>builder()
                                                .instanceCollectionReferencesOrdered(List.of(
                                                        "$if{saksparter}",
                                                        "$icf{0}{publikasjoner}",
                                                        "$if{dokumenter}"
                                                ))
                                                .elementMapping(
                                                        ObjectMapping
                                                                .builder()
                                                                .valueMappingPerKey(Map.of(
                                                                        "navn", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("$icf{0}{navn}").build(),
                                                                        "publikasjonTittelFørsteBokstav", ValueMapping.builder().type(ValueMapping.Type.VALUE_CONVERTING).mappingString("$vc{2}$icf{1}{tittel}").build(),
                                                                        "publikasjon", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("$icf{1}{tittel} - $icf{1}{utgiver}").build(),
                                                                        "vedleggTittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("$icf{2}{tittel}").build()
                                                                ))
                                                                .valueCollectionMappingPerKey(Map.of(
                                                                        "venner",
                                                                        CollectionMapping
                                                                                .<ValueMapping>builder()
                                                                                .elementMappings(List.of(
                                                                                        ValueMapping
                                                                                                .builder()
                                                                                                .type(ValueMapping.Type.STRING)
                                                                                                .mappingString("Ole Brum")
                                                                                                .build()
                                                                                ))
                                                                                .fromCollectionMappings(List.of(
                                                                                        FromCollectionMapping
                                                                                                .<ValueMapping>builder()
                                                                                                .instanceCollectionReferencesOrdered(List.of(
                                                                                                        "$if{saksparter}"
                                                                                                ))
                                                                                                .elementMapping(
                                                                                                        ValueMapping
                                                                                                                .builder()
                                                                                                                .type(ValueMapping.Type.DYNAMIC_STRING)
                                                                                                                .mappingString("$icf{3}{navn}")
                                                                                                                .build()
                                                                                                )
                                                                                                .build()
                                                                                ))
                                                                                .build()
                                                                ))
                                                                .objectCollectionMappingPerKey(Map.of(
                                                                        "priser",
                                                                        CollectionMapping
                                                                                .<ObjectMapping>builder()
                                                                                .elementMappings(List.of(
                                                                                        ObjectMapping
                                                                                                .builder()
                                                                                                .valueMappingPerKey(Map.of(
                                                                                                        "pristittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("pris-$icf{1}{tittel}-$icf{2}{tittel}").build()
                                                                                                ))
                                                                                                .build()
                                                                                ))
                                                                                .build(),
                                                                        "dokumenter",
                                                                        CollectionMapping
                                                                                .<ObjectMapping>builder()
                                                                                .fromCollectionMappings(List.of(
                                                                                        FromCollectionMapping
                                                                                                .<ObjectMapping>builder()
                                                                                                .instanceCollectionReferencesOrdered(List.of(
                                                                                                        "$if{dokumenter}"
                                                                                                ))
                                                                                                .elementMapping(
                                                                                                        ObjectMapping.builder()
                                                                                                                .valueMappingPerKey(Map.of(
                                                                                                                        "tittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("$icf{3}{tittel}").build()
                                                                                                                ))
                                                                                                                .build()
                                                                                                )
                                                                                                .build()
                                                                                ))
                                                                                .build()
                                                                ))
                                                                .build()
                                                )
                                                .build()
                                ))
                                .build()
                ))
                .build();

        InstanceObject instance = InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "tittel1", "Hei på deg",
                        "tittel2", "her er jeg",
                        "person1.navn", "Arne Arnesen",
                        "person2.navn", "Navn Navnesen",
                        "person2.by", "Oslo"
                ))
                .objectCollectionPerKey(Map.of(
                        "saksparter", List.of(
                                InstanceObject
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "navn", "Nora Noradottir"
                                        ))
                                        .objectCollectionPerKey(Map.of(
                                                "publikasjoner", List.of(
                                                        InstanceObject
                                                                .builder()
                                                                .valuePerKey(Map.of(
                                                                        "tittel", "Min barnebok",
                                                                        "utgiver", "Bokprodusenten"
                                                                ))
                                                                .build(),
                                                        InstanceObject
                                                                .builder()
                                                                .valuePerKey(Map.of(
                                                                        "tittel", "Ludde",
                                                                        "utgiver", "Alletiders"
                                                                ))
                                                                .build()
                                                )
                                        ))
                                        .build(),
                                InstanceObject
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "navn", "Eirik Eiriksson"
                                        ))
                                        .objectCollectionPerKey(Map.of(
                                                "publikasjoner", List.of(
                                                        InstanceObject
                                                                .builder()
                                                                .valuePerKey(Map.of(
                                                                        "tittel", "Den lille mulvarpen",
                                                                        "utgiver", "ABC"
                                                                ))
                                                                .build()
                                                )
                                        ))
                                        .build()
                        ),
                        "dokumenter", List.of(
                                InstanceObject
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "tittel", "Dokument1"
                                        ))
                                        .build(),
                                InstanceObject
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "tittel", "Dokument2"
                                        ))
                                        .build()
                        )
                ))
                .build();



        // Given
        ValueConverting valueConverting1 = ValueConverting
                .builder()
                .convertingMap(Map.of(
                        "Trondheim", "TRD",
                        "Oslo", "OSL"
                ))
                .build();

        when(valueConvertingRequestProducerService.get(0L)).thenReturn(Optional.of(valueConverting1));

        // When
        Optional<ValueConverting> result1 = valueConvertingRequestProducerService.get(0L);

        // Then
        verify(valueConvertingRequestProducerService, times(1)).get(0L);
        assertEquals(Optional.of(valueConverting1), result1);


        // Given
        ValueConverting valueConverting2 = ValueConverting
                .builder()
                .convertingMap(Map.of(
                        "Min barnebok", "M",
                        "Ludde", "L",
                        "Den lille mulvarpen", "D"
                ))
                .build();

        when(valueConvertingRequestProducerService.get(2L)).thenReturn(Optional.of(valueConverting2));

        // When
        for (int i = 0; i < 6; i++) {
            Optional<ValueConverting> result2 = valueConvertingRequestProducerService.get(2L);

            // Then
            verify(valueConvertingRequestProducerService, times(i + 1)).get(2L);
            assertEquals(Optional.of(valueConverting2), result2);
        }

        Map<String, ?> mappedInstance = instanceMappingService.toMappedInstanceObject(
                objectMapping,
                instance
        );

        Map<String, ?> expectedInstance = Map.of(
                "kombinert tittel", "Livsmotto: Hei på deg, her er jeg",
                "ferdigstilt", true,
                "adresse",
                Map.of(
                        "mottakernavn", "Navn Navnesen",
                        "by", "Oslo",
                        "byKortnavn", "OSL"
                ),
                "parter",
                List.<Object>of(
                        Map.of(
                                "navn", "Arne Arnesen",
                                "publikasjon", "Ole Brumm - Ukjent",
                                "vedleggTittel", "Filopplastning_128920",
                                "priser", Collections.emptyList()
                        ),
                        Map.of(
                                "navn", "Nora Noradottir",
                                "publikasjonTittelFørsteBokstav", "M",
                                "publikasjon", "Min barnebok - Bokprodusenten",
                                "vedleggTittel", "Dokument1",
                                "priser", List.of(Map.of("pristittel", "pris-Min barnebok-Dokument1")),
                                "dokumenter", List.of(Map.of("tittel", "Dokument1"), Map.of("tittel", "Dokument2")),
                                "venner", List.of("Ole Brum", "Nora Noradottir", "Eirik Eiriksson")
                        ),
                        Map.of(
                                "navn", "Nora Noradottir",
                                "publikasjonTittelFørsteBokstav", "M",
                                "publikasjon", "Min barnebok - Bokprodusenten",
                                "vedleggTittel", "Dokument2",
                                "priser", List.of(Map.of("pristittel", "pris-Min barnebok-Dokument2")),
                                "dokumenter", List.of(Map.of("tittel", "Dokument1"), Map.of("tittel", "Dokument2")),
                                "venner", List.of("Ole Brum", "Nora Noradottir", "Eirik Eiriksson")
                        ),
                        Map.of(
                                "navn", "Nora Noradottir",
                                "publikasjonTittelFørsteBokstav", "L",
                                "publikasjon", "Ludde - Alletiders",
                                "vedleggTittel", "Dokument1",
                                "priser", List.of(Map.of("pristittel", "pris-Ludde-Dokument1")),
                                "dokumenter", List.of(Map.of("tittel", "Dokument1"), Map.of("tittel", "Dokument2")),
                                "venner", List.of("Ole Brum", "Nora Noradottir", "Eirik Eiriksson")
                        ),
                        Map.of(
                                "navn", "Nora Noradottir",
                                "publikasjonTittelFørsteBokstav", "L",
                                "publikasjon", "Ludde - Alletiders",
                                "vedleggTittel", "Dokument2",
                                "priser", List.of(Map.of("pristittel", "pris-Ludde-Dokument2")),
                                "dokumenter", List.of(Map.of("tittel", "Dokument1"), Map.of("tittel", "Dokument2")),
                                "venner", List.of("Ole Brum", "Nora Noradottir", "Eirik Eiriksson")
                        ),
                        Map.of(
                                "navn", "Eirik Eiriksson",
                                "publikasjonTittelFørsteBokstav", "D",
                                "publikasjon", "Den lille mulvarpen - ABC",
                                "vedleggTittel", "Dokument1",
                                "priser", List.of(Map.of("pristittel", "pris-Den lille mulvarpen-Dokument1")),
                                "dokumenter", List.of(Map.of("tittel", "Dokument1"), Map.of("tittel", "Dokument2")),
                                "venner", List.of("Ole Brum", "Nora Noradottir", "Eirik Eiriksson")
                        ),
                        Map.of(
                                "navn", "Eirik Eiriksson",
                                "publikasjonTittelFørsteBokstav", "D",
                                "publikasjon", "Den lille mulvarpen - ABC",
                                "vedleggTittel", "Dokument2",
                                "priser", List.of(Map.of("pristittel", "pris-Den lille mulvarpen-Dokument2")),
                                "dokumenter", List.of(Map.of("tittel", "Dokument1"), Map.of("tittel", "Dokument2")),
                                "venner", List.of("Ole Brum", "Nora Noradottir", "Eirik Eiriksson")
                        )
                )
        );


        assertEquals(mappedInstance, expectedInstance);

    }

}
