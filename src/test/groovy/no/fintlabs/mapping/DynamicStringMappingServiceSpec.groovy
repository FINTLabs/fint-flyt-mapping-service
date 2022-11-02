package no.fintlabs.mapping

import no.fintlabs.InstanceFieldNotFoundException
import no.fintlabs.model.instance.Instance
import no.fintlabs.model.instance.InstanceField
import spock.lang.Specification

class DynamicStringMappingServiceSpec extends Specification {

    DynamicStringMappingService dynamicStringMappingService

    def setup() {
        dynamicStringMappingService = new DynamicStringMappingService()
    }

    def 'should return string with values from instance fields if all instance fields are found'() {
        given:
        Instance instance = Instance
                .builder()
                .fieldPerKey(Map.of(
                        "tittel", InstanceField.builder().value("Tittel som ikke skal brukes").build(),
                        "fornavn", InstanceField.builder().value("Ola").build(),
                        "etter-navn", InstanceField.builder().value("Nordmann").build(),
                        "dato", InstanceField.builder().value("24.12.2022").build(),
                        "person nr1 fødselsdato", InstanceField.builder().value("01.01.2000").build()
                ))
                .build()

        when:
        String result = dynamicStringMappingService.toMappedInstanceFieldValue(
                instance,
                "Søknad VGS \$if{fornavn}\$if{etter-navn} \$if{person nr1 fødselsdato} for dato \$if{dato} ettellerannet"
        )

        then:
        result == "Søknad VGS OlaNordmann 01.01.2000 for dato 24.12.2022 ettellerannet"
    }

    def 'should throw exception if an instance field cannot be found'() {
        given:
        Instance instance = Instance
                .builder()
                .fieldPerKey(Map.of(
                        "tittel", InstanceField.builder().value("Tittel som ikke skal brukes").build(),
                        "fornavn", InstanceField.builder().value("Ola").build()
                ))
                .build()

        when:
        dynamicStringMappingService.toMappedInstanceFieldValue(
                instance,
                "Søknad VGS \$if{etternavn}"
        )

        then:
        def e = thrown(InstanceFieldNotFoundException)
        e.getMessage() == "Could not find instance field with key='etternavn'"
    }

    def 'should return blank string if an instance field value is null'() {
        given:
        Instance instance = Instance
                .builder()
                .fieldPerKey(Map.of(
                        "tittel", InstanceField.builder().value("Tittel som ikke skal brukes").build(),
                        "fornavn", InstanceField.builder().value(null).build()
                ))
                .build()

        when:
        String result = dynamicStringMappingService.toMappedInstanceFieldValue(
                instance,
                "Søknad VGS \$if{fornavn}"
        )

        then:
        result == "Søknad VGS "
    }

}
