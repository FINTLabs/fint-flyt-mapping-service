package no.fintlabs.mapping

import no.fintlabs.model.configuration.*
import no.fintlabs.model.instance.InstanceField
import spock.lang.Specification

class FieldMappingServiceSpec extends Specification {

    FieldMappingService fieldMappingService

    void setup() {
        fieldMappingService = new FieldMappingService();
    }

    def "should map field with combine string value strategy and a static value"() {

        given:
        def configurationFields = List.of(
                new Field(
                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                        "title",
                        new ValueBuilder(
                                "Søknad om TT-kort",
                                List.of()
                        )
                )
        )
        def instanceFields = Map.of("title", new InstanceField("Tittel", "Test tittel"))

        when:
        def caseFields = fieldMappingService.mapFields(configurationFields, instanceFields)

        then:
        caseFields.get("title") == "Søknad om TT-kort"
    }

    def "should map field with combine string value strategy and a single property"() {

        given:
        def configurationFields = List.of(
                new Field(
                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                        "title",
                        new ValueBuilder(
                                "%s",
                                Collections.singletonList(new Property(ValueSource.FORM, "title", 0))
                        )
                )
        )
        def instanceFields = Map.of("title", new InstanceField("Tittel", "Test tittel"))

        when:
        def caseFields = fieldMappingService.mapFields(configurationFields, instanceFields)

        then:
        caseFields.get("title") == "Test tittel"
    }

    def "should map field with combine string value strategy and text with multiple properties"() {

        given:
        def configurationFields = List.of(
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
        )
        def instanceFields = Map.of(
                "Title-part-one", new InstanceField("Tittel", "Tittel-del-1"),
                "Title-part-two", new InstanceField("Tittel", "Tittel-del-2")
        )
        when:
        def caseFields = fieldMappingService.mapFields(configurationFields, instanceFields)

        then:
        caseFields.get("title") == "Tittel: Tittel-del-1 Tittel-del-2"
    }

    def "should map field with combine string value strategy and the correct order of properties"() {

        given:
        def configurationFields = List.of(
                new Field(
                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                        "title",
                        new ValueBuilder(
                                "%s %s %s %s",
                                List.of(
                                        new Property(ValueSource.FORM, "four", 3),
                                        new Property(ValueSource.FORM, "one", 0),
                                        new Property(ValueSource.FORM, "three", 2),
                                        new Property(ValueSource.FORM, "two", 1)
                                )
                        )
                )
        )
        def instanceFields = Map.of(
                "one", new InstanceField("Tittel", "del1"),
                "two", new InstanceField("Tittel", "del2"),
                "three", new InstanceField("Tittel", "del3"),
                "four", new InstanceField("Tittel", "del4")
        )
        when:
        def caseFields = fieldMappingService.mapFields(configurationFields, instanceFields)

        then:
        caseFields.get("title") == "del1 del2 del3 del4"
    }

    def "should map field with fixed string value strategy"() {

        given:
        def configurationFields = List.of(
                new Field(
                        ValueBuildStrategy.FIXED_ARCHIVE_CODE_VALUE,
                        "title",
                        new ValueBuilder(
                                "1234#FiXedValue",
                                List.of()
                        )
                )
        )
        def instanceFields = Map.of()

        when:
        def caseFields = fieldMappingService.mapFields(configurationFields, instanceFields)

        then:
        caseFields.get("title") == "1234#FiXedValue"
    }

    def "should map multiple fields"() {

        given:
        def configurationFields = List.of(
                new Field(
                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                        "Tittel",
                        new ValueBuilder("Søknad om: %s", new Property(ValueSource.FORM, "felt1", 0),)
                ),
                new Field(
                        ValueBuildStrategy.FIXED_ARCHIVE_CODE_VALUE,
                        "OffentligTittel",
                        new ValueBuilder("Dette er en undertittel")
                )
        )
        def instanceFields = Map.of("felt1", new InstanceField("felt1", "TT-kort"))

        when:
        def caseFields = fieldMappingService.mapFields(configurationFields, instanceFields)

        then:
        caseFields.get("Tittel") == "Søknad om: TT-kort"
        caseFields.get("OffentligTittel") == "Dette er en undertittel"
    }
}
