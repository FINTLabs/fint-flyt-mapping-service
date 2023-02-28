package no.fintlabs.mapping

import no.fintlabs.model.configuration.CollectionMapping
import no.fintlabs.model.configuration.FromCollectionMapping
import no.fintlabs.model.configuration.ObjectMapping
import no.fintlabs.model.configuration.ValueMapping
import no.fintlabs.model.instance.InstanceObject
import spock.lang.Specification

class InstanceMappingServiceIntegrationSpec extends Specification {

    InstanceMappingService instanceMappingService

    def setup() {
        instanceMappingService = new InstanceMappingService(
                new InstanceReferenceService(),
                new ValueMappingService(new InstanceReferenceService())
        )
    }

    def 'should map instance based on mapping from configuration'() {
        given:
        ObjectMapping elementMapping = ObjectMapping
                .builder()
                .valueMappingPerKey(Map.of(
                        "kombinert tittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("Livsmotto: \$if{tittel1}, \$if{tittel2}").build())
                )
                .objectMappingPerKey(Map.of(
                        "adresse",
                        ObjectMapping
                                .builder()
                                .valueMappingPerKey(Map.of(
                                        "mottakernavn", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$if{person2.navn}").build(),
                                        "by", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$if{person2.by}").build(),
                                ))
                                .build()
                ))
                .objectCollectionMappingPerKey(Map.of(
                        "parter",
                        CollectionMapping
                                .<ObjectMapping> builder()
                                .elementMappings(List.of(
                                        ObjectMapping
                                                .builder()
                                                .valueMappingPerKey(Map.of(
                                                        "navn", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$if{person1.navn}").build(),
                                                        "publikasjon", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("Ole Brumm - Ukjent").build(),
                                                        "vedleggTittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("Filopplastning_128920").build()
                                                ))
                                                .objectCollectionMappingPerKey(Map.of(
                                                        "priser", CollectionMapping.<ObjectMapping> builder().build()
                                                ))
                                                .build()
                                ))
                                .fromCollectionMappings(List.of(
                                        FromCollectionMapping
                                                .<ObjectMapping> builder()
                                                .instanceCollectionReferencesOrdered(List.of(
                                                        "\$if{saksparter}",
                                                        "\$icf{0}{publikasjoner}",
                                                        "\$if{dokumenter}"
                                                ))
                                                .elementMapping(
                                                        ObjectMapping
                                                                .builder()
                                                                .valueMappingPerKey(Map.of(
                                                                        "navn", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$icf{0}{navn}").build(),
                                                                        "publikasjon", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$icf{1}{tittel} - \$icf{1}{utgiver}").build(),
                                                                        "vedleggTittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$icf{2}{tittel}").build()
                                                                ))
                                                                .objectCollectionMappingPerKey(Map.of(
                                                                        "priser",
                                                                        CollectionMapping
                                                                                .<ObjectMapping> builder()
                                                                                .elementMappings(List.of(
                                                                                        ObjectMapping
                                                                                                .builder()
                                                                                                .valueMappingPerKey(Map.of(
                                                                                                        "pristittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("pris-\$icf{1}{tittel}-\$icf{2}{tittel}").build()
                                                                                                ))
                                                                                                .build()
                                                                                ))
                                                                                .build(),
                                                                        "dokumenter",
                                                                        CollectionMapping
                                                                                .<ObjectMapping> builder()
                                                                                .fromCollectionMappings(List.of(
                                                                                        FromCollectionMapping
                                                                                                .<ObjectMapping> builder()
                                                                                                .instanceCollectionReferencesOrdered(List.of(
                                                                                                        "\$if{dokumenter}"
                                                                                                ))
                                                                                                .elementMapping(
                                                                                                        ObjectMapping.builder()
                                                                                                                .valueMappingPerKey(Map.of(
                                                                                                                        "tittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$icf{3}{tittel}").build()
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
                .build()

        InstanceObject instance = InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "tittel1", "Hei på deg",
                        "tittel2", "her er jeg",
                        "person1.navn", "Arne Arnesen",
                        "person2.navn", "Navn Navnesen",
                        "person2.by", "Oslo",
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
                                                .build(),
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
                .build()

        when:
        Map<String, ?> mappedInstance = instanceMappingService.toMappedInstanceElement(
                elementMapping,
                instance
        )

        then:

        mappedInstance == Map.of(
                "kombinert tittel", "Livsmotto: Hei på deg, her er jeg",
                "adresse",
                Map.of(
                        "mottakernavn", "Navn Navnesen",
                        "by", "Oslo"
                ),
                "parter",
                List.<Object> of(
                        Map.of(
                                "navn", "Arne Arnesen",
                                "publikasjon", "Ole Brumm - Ukjent",
                                "vedleggTittel", "Filopplastning_128920",
                                "priser", []
                        ),
                        Map.of(
                                "navn", "Nora Noradottir",
                                "publikasjon", "Min barnebok - Bokprodusenten",
                                "vedleggTittel", "Dokument1",
                                "priser", [Map.of("pristittel", "pris-Min barnebok-Dokument1")],
                                "dokumenter", [Map.of("tittel", "Dokument1"), Map.of("tittel", "Dokument2")]
                        ),
                        Map.of(
                                "navn", "Nora Noradottir",
                                "publikasjon", "Min barnebok - Bokprodusenten",
                                "vedleggTittel", "Dokument2",
                                "priser", [Map.of("pristittel", "pris-Min barnebok-Dokument2")],
                                "dokumenter", [Map.of("tittel", "Dokument1"), Map.of("tittel", "Dokument2")]
                        ),
                        Map.of(
                                "navn", "Nora Noradottir",
                                "publikasjon", "Ludde - Alletiders",
                                "vedleggTittel", "Dokument1",
                                "priser", [Map.of("pristittel", "pris-Ludde-Dokument1")],
                                "dokumenter", [Map.of("tittel", "Dokument1"), Map.of("tittel", "Dokument2")]
                        ),
                        Map.of(
                                "navn", "Nora Noradottir",
                                "publikasjon", "Ludde - Alletiders",
                                "vedleggTittel", "Dokument2",
                                "priser", [Map.of("pristittel", "pris-Ludde-Dokument2")],
                                "dokumenter", [Map.of("tittel", "Dokument1"), Map.of("tittel", "Dokument2")]
                        ),
                        Map.of(
                                "navn", "Eirik Eiriksson",
                                "publikasjon", "Den lille mulvarpen - ABC",
                                "vedleggTittel", "Dokument1",
                                "priser", [Map.of("pristittel", "pris-Den lille mulvarpen-Dokument1")],
                                "dokumenter", [Map.of("tittel", "Dokument1"), Map.of("tittel", "Dokument2")]
                        ),
                        Map.of(
                                "navn", "Eirik Eiriksson",
                                "publikasjon", "Den lille mulvarpen - ABC",
                                "vedleggTittel", "Dokument2",
                                "priser", [Map.of("pristittel", "pris-Den lille mulvarpen-Dokument2")],
                                "dokumenter", [Map.of("tittel", "Dokument1"), Map.of("tittel", "Dokument2")]
                        )
                )
        )
    }

}
