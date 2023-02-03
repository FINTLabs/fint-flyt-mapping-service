package no.fintlabs.mapping


import no.fintlabs.model.configuration.ElementCollectionMapping
import no.fintlabs.model.configuration.ElementMapping
import no.fintlabs.model.configuration.ElementsFromCollectionMapping
import no.fintlabs.model.configuration.ValueMapping
import no.fintlabs.model.instance.InstanceElement
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
        ElementMapping elementMapping = ElementMapping
                .builder()
                .valueMappingPerKey(Map.of(
                        "kombinert tittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("Livsmotto: \$if{tittel1}, \$if{tittel2}").build())
                )
                .elementMappingPerKey(Map.of(
                        "adresse",
                        ElementMapping
                                .builder()
                                .valueMappingPerKey(Map.of(
                                        "mottakernavn", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$if{person2.navn}").build(),
                                        "by", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$if{person2.by}").build(),
                                ))
                                .build()
                ))
                .elementCollectionMappingPerKey(Map.of(
                        "parter",
                        ElementCollectionMapping
                                .builder()
                                .elementMappings(List.of(
                                        ElementMapping
                                                .builder()
                                                .valueMappingPerKey(Map.of(
                                                        "navn", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$if{person1.navn}").build(),
                                                        "publikasjon", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("Ole Brumm - Ukjent").build(),
                                                        "vedleggTittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("Filopplastning_128920").build()
                                                ))
                                                .build()
                                ))
                                .elementsFromCollectionMappings(List.of(
                                        ElementsFromCollectionMapping
                                                .builder()
                                                .instanceCollectionReferencesOrdered(List.of(
                                                        "\$if{saksparter}",
                                                        "\$icf{0}{publikasjoner}",
                                                        "\$if{dokumenter}"
                                                ))
                                                .elementMapping(
                                                        ElementMapping
                                                                .builder()
                                                                .valueMappingPerKey(Map.of(
                                                                        "navn", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$icf{0}{navn}").build(),
                                                                        "publikasjon", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$icf{1}{tittel} - \$icf{1}{utgiver}").build(),
                                                                        "vedleggTittel", ValueMapping.builder().type(ValueMapping.Type.DYNAMIC_STRING).mappingString("\$icf{2}{tittel}").build()
                                                                ))
                                                                .build()
                                                )
                                                .build()
                                ))
                                .build()
                ))
                .build()

        InstanceElement instance = InstanceElement
                .builder()
                .valuePerKey(Map.of(
                        "tittel1", "Hei på deg",
                        "tittel2", "her er jeg",
                        "person1.navn", "Arne Arnesen",
                        "person2.navn", "Navn Navnesen",
                        "person2.by", "Oslo",
                ))
                .elementCollectionPerKey(Map.of(
                        "saksparter", List.of(
                        InstanceElement
                                .builder()
                                .valuePerKey(Map.of(
                                        "navn", "Nora Noradottir"
                                ))
                                .elementCollectionPerKey(Map.of(
                                        "publikasjoner", List.of(
                                        InstanceElement
                                                .builder()
                                                .valuePerKey(Map.of(
                                                        "tittel", "Min barnebok",
                                                        "utgiver", "Bokprodusenten"
                                                ))
                                                .build(),
                                        InstanceElement
                                                .builder()
                                                .valuePerKey(Map.of(
                                                        "tittel", "Ludde",
                                                        "utgiver", "Alletiders"
                                                ))
                                                .build(),
                                )
                                ))
                                .build(),
                        InstanceElement
                                .builder()
                                .valuePerKey(Map.of(
                                        "navn", "Eirik Eiriksson"
                                ))
                                .elementCollectionPerKey(Map.of(
                                        "publikasjoner", List.of(
                                        InstanceElement
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
                        InstanceElement
                                .builder()
                                .valuePerKey(Map.of(
                                        "tittel", "Dokument1"
                                ))
                                .build(),
                        InstanceElement
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
                                "vedleggTittel", "Filopplastning_128920"
                        ),
                        Map.of(
                                "navn", "Nora Noradottir",
                                "publikasjon", "Min barnebok - Bokprodusenten",
                                "vedleggTittel", "Dokument1"
                        ),
                        Map.of(
                                "navn", "Nora Noradottir",
                                "publikasjon", "Min barnebok - Bokprodusenten",
                                "vedleggTittel", "Dokument2"
                        ),
                        Map.of(
                                "navn", "Nora Noradottir",
                                "publikasjon", "Ludde - Alletiders",
                                "vedleggTittel", "Dokument1"
                        ),
                        Map.of(
                                "navn", "Nora Noradottir",
                                "publikasjon", "Ludde - Alletiders",
                                "vedleggTittel", "Dokument2"
                        ),
                        Map.of(
                                "navn", "Eirik Eiriksson",
                                "publikasjon", "Den lille mulvarpen - ABC",
                                "vedleggTittel", "Dokument1"
                        ),
                        Map.of(
                                "navn", "Eirik Eiriksson",
                                "publikasjon", "Den lille mulvarpen - ABC",
                                "vedleggTittel", "Dokument2"
                        )
                )
        )
    }

}
