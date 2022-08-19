package no.fintlabs.validation

import no.fintlabs.model.configuration.*
import no.fintlabs.model.instance.Instance
import no.fintlabs.model.instance.InstanceField
import spock.lang.Shared
import spock.lang.Specification

class InstanceFieldsValidationServiceSpec extends Specification {

    @Shared
    InstanceFieldsValidationService instanceFieldValidator

    def setup() {
        this.instanceFieldValidator = new InstanceFieldsValidationService()
    }

    def 'given an instance containing all required fields, validation should not throw exception'() {
        given:
        IntegrationConfiguration integrationConfiguration = Stub(IntegrationConfiguration.class) {
            getCaseConfiguration() >> Stub(CaseConfiguration.class) {
                getFields() >> [
                        new ConfigurationField(
                                ValueBuildStrategy.COMBINE_STRING_VALUE,
                                "Tittel",
                                new ValueBuilder("Søknad om: %s", new Property(ValueSource.FORM, "caseconfiguration-field-1", 0))
                        )
                ]
            }
            getDocumentConfiguration() >> Stub(DocumentConfiguration.class) {
                getFields() >> [new ConfigurationField(
                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                        "Offentlig tittel",
                        new ValueBuilder("%s", new Property(ValueSource.FORM, "documentconfiguration-field-1", 0))
                )]
            }
            getRecordConfiguration() >> Stub(RecordConfiguration.class) {
                getFields() >> [new ConfigurationField(
                        ValueBuildStrategy.COMBINE_STRING_VALUE,
                        "Skjerming",
                        new ValueBuilder("Skjerming: %s", new Property(ValueSource.FORM, "recordconfiguration-field-1", 0))
                )]
            }
        }
        Instance instance = Stub() {
            getFieldPerKey() >> ["caseconfiguration-field-1"    : new InstanceField("SkjemaTittel", "Value1"),
                            "documentconfiguration-field-1": new InstanceField("SkjemaTittel2", "Value2"),
                            "recordconfiguration-field-1"  : new InstanceField("SkjemaSkjerming", "Value3"),
            ]
        }
        when:
        Optional<InstanceFieldsValidationService.Error> result = this.instanceFieldValidator.validate(integrationConfiguration, instance)
        then:
        result.isEmpty()
    }

    def 'given an instance that does not contain a required field, validation should throw exception'() {
        given:
        ConfigurationField caseField = new ConfigurationField(
                ValueBuildStrategy.COMBINE_STRING_VALUE,
                "Tittel",
                new ValueBuilder("Søknad om: %s", new Property(ValueSource.FORM, "caseconfiguration-field-1", 0))
        )
        ConfigurationField recordField = new ConfigurationField(
                ValueBuildStrategy.COMBINE_STRING_VALUE,
                "Skjerming",
                new ValueBuilder("Skjerming: %s", new Property(ValueSource.FORM, "recordconfiguration-field-1", 0))
        )
        ConfigurationField documentField = new ConfigurationField(
                ValueBuildStrategy.COMBINE_STRING_VALUE,
                "Offentlig tittel",
                new ValueBuilder("%s", new Property(ValueSource.FORM, "documentconfiguration-field-1", 0))
        )

        IntegrationConfiguration integrationConfiguration = Stub(IntegrationConfiguration.class) {
            getCaseConfiguration() >> Stub(CaseConfiguration.class) {
                getFields() >> [caseField]
            }
            getRecordConfiguration() >> Stub(RecordConfiguration.class) {
                getFields() >> [recordField]
            }
            getDocumentConfiguration() >> Stub(DocumentConfiguration.class) {
                getFields() >> [documentField]
            }
        }
        Instance instance = Stub() {
            getFieldPerKey() >> ["documentconfiguration-field-1": new InstanceField("SkjemaTittel2", "Value2")]
        }

        when:
        Optional<InstanceFieldsValidationService.Error> optionalError = this.instanceFieldValidator.validate(integrationConfiguration, instance)

        then:
        optionalError.isPresent()
        def error = optionalError.get()

        error.getMissingInstanceFieldsPerCaseConfigurationField().size() == 1
        error.getMissingInstanceFieldsPerCaseConfigurationField().get(caseField) == ["caseconfiguration-field-1"]

        error.getMissingInstanceFieldsPerRecordConfigurationField().size() == 1
        error.getMissingInstanceFieldsPerRecordConfigurationField().get(recordField) == ["recordconfiguration-field-1"]

        error.getMissingInstanceFieldsPerDocumentConfigurationField().size() == 0
    }
}
