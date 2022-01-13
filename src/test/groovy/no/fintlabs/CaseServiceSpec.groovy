package no.fintlabs

import no.fintlabs.model.configuration.Field
import no.fintlabs.model.configuration.IntegrationConfiguration
import no.fintlabs.model.configuration.Property
import no.fintlabs.model.configuration.ValueBuildStrategy
import no.fintlabs.model.configuration.ValueBuilder
import no.fintlabs.model.configuration.ValueSource
import no.fintlabs.model.instance.InstanceField
import no.fintlabs.model.instance.Instance
import spock.lang.Specification

class CaseServiceSpec extends Specification {

    def caseService

    void setup() {
        caseService = new CaseService()


    }

    def "... should return a SakResource"() {

        given:
        def configuration = new IntegrationConfiguration()
        def instance = new Instance()


        instance.getFields().put("title", new InstanceField("Tittel", "Test titel"))
        configuration.getCaseConfiguration().getFields().push(new Field(ValueBuildStrategy.COMBINE_STRING_VALUE, "tittel", new ValueBuilder("%s", Collections.singletonList(new Property(ValueSource.FORM, "title", 0)))))

        when:
        def sak = caseService.createSak(configuration, instance)

        then:
        sak
        sak.getTittel() == "Test"

    }
}
