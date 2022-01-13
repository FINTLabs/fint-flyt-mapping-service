package no.fintlabs

import no.fintlabs.model.configuration.*
import no.fintlabs.model.instance.Instance
import no.fintlabs.model.instance.InstanceField
import spock.lang.Specification

import java.util.function.ToDoubleBiFunction

class CaseServiceSpec extends Specification {

    def caseService

    void setup() {
        caseService = new CaseService()
    }

    def "should map field with combine string value strategy and a static value"() {

        given:

        def configuration = IntegrationConfiguration.builder()
                .caseConfiguration(CaseConfiguration.builder()
                        .fields(List.of(
                                new Field(
                                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                                        "title",
                                        new ValueBuilder(
                                                "Søknad om TT-kort",
                                                List.of()
                                        )
                                )
                        ))
                        .build()
                )
                .build()

        def instance = Instance.builder().fields(Map.of("title", new InstanceField("Tittel", "Test tittel"))).build()

        when:
        def sak = caseService.createSak(configuration, instance)

        then:
        sak
        sak.getTittel() == "Søknad om TT-kort"
    }

    def "should map field with combine string value strategy and a single property"() {

        given:
        def configuration = IntegrationConfiguration.builder()
                .caseConfiguration(CaseConfiguration.builder()
                        .fields(List.of(
                                new Field(
                                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                                        "title",
                                        new ValueBuilder(
                                                "%s",
                                                Collections.singletonList(new Property(ValueSource.FORM, "title", 0))
                                        )
                                )
                        ))
                        .build()
                )
                .build()

        def instance = Instance.builder()
                .fields(Map.of("title", new InstanceField("Tittel", "Test tittel")))
                .build()

        when:
        def sak = caseService.createSak(configuration, instance)

        then:
        sak
        sak.getTittel() == "Test tittel"
    }

    def "should map field with combine string value strategy and text with multiple properties"() {

        given:
        def configuration = IntegrationConfiguration.builder()
                .caseConfiguration(CaseConfiguration.builder()
                        .fields(List.of(
                                new Field(
                                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                                        "title",
                                        new ValueBuilder(
                                                "Tittel: %s %s",
                                                List.of(
                                                        new Property(ValueSource.FORM, "Title-part-one", 0),
                                                        new Property(ValueSource.FORM, "Title-part-two", 1)
                                                )
                                        )
                                )
                        ))
                        .build()
                )
                .build()

        def instance = Instance.builder()
                .fields(Map.of(
                        "Title-part-one", new InstanceField("Tittel", "Tittel-del-1"),
                        "Title-part-two", new InstanceField("Tittel", "Tittel-del-2")
                ))
                .build()

        when:
        def sak = caseService.createSak(configuration, instance)

        then:
        sak
        sak.getTittel() == "Tittel: Tittel-del-1 Tittel-del-2"
    }

    // Todo: Check order of elements in string format

}
