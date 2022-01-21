package no.fintlabs

import no.fintlabs.model.configuration.*
import no.fintlabs.model.instance.Instance
import no.fintlabs.model.instance.InstanceField
import spock.lang.Shared
import spock.lang.Specification

class InstanceFieldsValidatorSpec extends Specification {

    @Shared
    InstanceFieldsValidator instanceFieldValidator

    def setup() {
        this.instanceFieldValidator = new InstanceFieldsValidator()
    }

    def 'given an instance containing all required fields, validation should not throw exception'() {
        given:
        IntegrationConfiguration integrationConfiguration = Stub(IntegrationConfiguration.class) {
            getCaseConfiguration() >> Stub(CaseConfiguration.class) {
                getFields() >> [
                        new Field(
                                ValueBuildStrategy.COMBINE_STRING_VALUE,
                                "Tittel",
                                new ValueBuilder("Søknad om: %s", new Property(ValueSource.FORM, "caseconfiguration-field-1", 0))
                        )
                ]
            }
            getDocumentConfiguration() >> Stub(DocumentConfiguration.class) {
                getFields() >> [new Field(
                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                        "Offentlig tittel",
                        new ValueBuilder("%s", new Property(ValueSource.FORM, "documentconfiguration-field-1", 0))
                )]
            }
            getRecordConfiguration() >> Stub(RecordConfiguration.class) {
                getFields() >> [new Field(
                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                        "Skjerming",
                        new ValueBuilder("Skjerming: %s", new Property(ValueSource.FORM, "recordconfiguration-field-1", 0))
                )]
            }
        }
        Instance instance = Stub() {
            getFields() >> ["caseconfiguration-field-1"    : new InstanceField("SkjemaTittel", "Value1"),
                            "documentconfiguration-field-1": new InstanceField("SkjemaTittel2", "Value2"),
                            "recordconfiguration-field-1"  : new InstanceField("SkjemaSkjerming", "Value3"),
            ]
        }
        when:
        this.instanceFieldValidator.validate(integrationConfiguration, instance)
        then:
        noExceptionThrown()
    }

    def 'given an instance that does not contain a required field, validation should not throw exception'() {
        given:
        IntegrationConfiguration integrationConfiguration = Stub(IntegrationConfiguration.class) {
            getCaseConfiguration() >> Stub(CaseConfiguration.class) {
                getFields() >> [
                        new Field(
                                ValueBuildStrategy.COMBINE_STRING_VALUE,
                                "Tittel",
                                new ValueBuilder("Søknad om: %s", new Property(ValueSource.FORM, "caseconfiguration-field-1", 0))
                        )
                ]
            }
            getDocumentConfiguration() >> Stub(DocumentConfiguration.class) {
                getFields() >> [new Field(
                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                        "Offentlig tittel",
                        new ValueBuilder("%s", new Property(ValueSource.FORM, "documentconfiguration-field-1", 0))
                )]
            }
            getRecordConfiguration() >> Stub(RecordConfiguration.class) {
                getFields() >> [new Field(
                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                        "Skjerming",
                        new ValueBuilder("Skjerming: %s", new Property(ValueSource.FORM, "recordconfiguration-field-1", 0))
                )]
            }
        }
        Instance instance = Stub() {
            getFields() >> ["documentconfiguration-field-1": new InstanceField("SkjemaTittel2", "Value2")]
        }

        when:
        this.instanceFieldValidator.validate(integrationConfiguration, instance)

        then:
        def e = thrown(MissingFieldsValidationException)
        e.getMissingFields().size() == 2
        e.getMissingFields().containsAll(["caseconfiguration-field-1", "recordconfiguration-field-1"])
    }
}
