package no.fintlabs.mapping

import no.fintlabs.InstanceFieldNotFoundException
import no.fintlabs.model.instance.InstanceElement
import spock.lang.Specification

class InstanceReferenceServiceSpec extends Specification {

    InstanceReferenceService instanceReferenceService

    def setup() {
        instanceReferenceService = new InstanceReferenceService()
    }

    def 'should return string with values from instance fields if all instance fields are found'() {
        when:
        String result = instanceReferenceService.replaceIfReferencesWithInstanceValues(
                "Søknad VGS \$if{fornavn}\$if{etter-navn} \$if{person nr1 fødselsdato} for dato \$if{dato} ettellerannet",
                Map.of(
                        "tittel", "Tittel som ikke skal brukes",
                        "fornavn", "Ola",
                        "etter-navn", "Nordmann",
                        "dato", "24.12.2022",
                        "person nr1 fødselsdato", "01.01.2000"
                ),
                new InstanceElement[]{}
        )

        then:
        result == "Søknad VGS OlaNordmann 01.01.2000 for dato 24.12.2022 ettellerannet"
    }

    def 'should throw exception if an instance field cannot be found'() {
        when:
        instanceReferenceService.replaceIfReferencesWithInstanceValues(
                "Søknad VGS \$if{etternavn}",
                Map.of(
                        "tittel", "Tittel som ikke skal brukes",
                        "fornavn", "Ola"
                ),
                new InstanceElement[]{}
        )

        then:
        def e = thrown(InstanceFieldNotFoundException)
        e.getMessage() == "Could not find instance field with key='etternavn'"
    }

    def 'should return blank string if an instance field value is null'() {
        given:
        Map<String, String> instanceValuePerKey = new HashMap<>()
        instanceValuePerKey.put("tittel", "Tittel som ikke skal brukes")
        instanceValuePerKey.put("fornavn", null)

        when:
        String result = instanceReferenceService.replaceIfReferencesWithInstanceValues(
                "Søknad VGS \$if{fornavn}",
                instanceValuePerKey,
                new InstanceElement[]{}
        )

        then:
        result == "Søknad VGS "
    }

    def 'should return string with values from iterated collection elements'() {
        given:
        Map<String, String> instanceValuePerKey = new HashMap<>()
        instanceValuePerKey.put("tittel", "Tittel her")
        instanceValuePerKey.put("fornavn", null)

        when:
        String result = instanceReferenceService.replaceIfReferencesWithInstanceValues(
                "Søknad VGS - \$icf{0}{navn} - \$if{tittel} - \$icf{0}{adresselinje} - \$icf{1}{organisasjon}",
                instanceValuePerKey,
                new InstanceElement[]{
                        InstanceElement
                                .builder()
                                .valuePerKey(Map.of(
                                        "navn", "Navn Navnesen",
                                        "adresselinje", "Gate 1, 0000, By, Land"
                                ))
                                .build(),
                        InstanceElement
                                .builder()
                                .valuePerKey(Map.of(
                                        "organisasjon", "Fintlabs",
                                ))
                                .build(),
                }
        )

        then:
        result == "Søknad VGS - Navn Navnesen - Tittel her - Gate 1, 0000, By, Land - Fintlabs"
    }

}
