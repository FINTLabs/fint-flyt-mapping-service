package no.fintlabs.integration.error.mappers

import no.fintlabs.integration.error.ErrorCode
import no.fintlabs.kafka.event.error.Error
import no.fintlabs.kafka.event.error.ErrorCollection
import no.fintlabs.model.configuration.ConfigurationField
import no.fintlabs.validation.InstanceFieldsValidationService
import no.fintlabs.validation.exceptions.MissingInstanceFieldsValidationException
import spock.lang.Specification

class MissingInstanceFieldsErrorMapperSpec extends Specification {

    MissingInstanceFieldsErrorMapper missingInstanceFieldsErrorMapper

    def setup() {
        missingInstanceFieldsErrorMapper = new MissingInstanceFieldsErrorMapper()
    }

    def 'should map to error collection'() {
        given:
        MissingInstanceFieldsValidationException exception = new MissingInstanceFieldsValidationException(
                new InstanceFieldsValidationService.Error(
                        Map.of(
                                new ConfigurationField(null, "caseConfigurationField1", null),
                                List.of("instanceField1", "instanceField2"),
                                new ConfigurationField(null, "caseConfigurationField2", null),
                                List.of("instanceField1")
                        ),
                        Map.of(
                                new ConfigurationField(null, "recordConfigurationField1", null),
                                List.of("instanceField3")
                        ),
                        Map.of(
                                new ConfigurationField(null, "documentConfigurationField1", null),
                                List.of("instanceField4"),
                        )
                )
        )

        when:
        ErrorCollection errorCollection = missingInstanceFieldsErrorMapper.map(exception)

        then:
        errorCollection.getErrors().size() == 5
        errorCollection.getErrors().containsAll(
                Error.builder()
                        .errorCode(ErrorCode.MISSING_INSTANCE_FIELD_FOR_CASE_CONFIGURATION_FIELD.getCode())
                        .args(Map.of(
                                "configurationField", "caseConfigurationField1",
                                "instanceField", "instanceField1"
                        ))
                        .build(),
                Error.builder()
                        .errorCode(ErrorCode.MISSING_INSTANCE_FIELD_FOR_CASE_CONFIGURATION_FIELD.getCode())
                        .args(Map.of(
                                "configurationField", "caseConfigurationField1",
                                "instanceField", "instanceField2"
                        ))
                        .build(),
                Error.builder()
                        .errorCode(ErrorCode.MISSING_INSTANCE_FIELD_FOR_CASE_CONFIGURATION_FIELD.getCode())
                        .args(Map.of(
                                "configurationField", "caseConfigurationField2",
                                "instanceField", "instanceField1"
                        ))
                        .build(),
                Error.builder()
                        .errorCode(ErrorCode.MISSING_INSTANCE_FIELD_FOR_RECORD_CONFIGURATION_FIELD.getCode())
                        .args(Map.of(
                                "configurationField", "recordConfigurationField1",
                                "instanceField", "instanceField3"
                        ))
                        .build(),
                Error.builder()
                        .errorCode(ErrorCode.MISSING_INSTANCE_FIELD_FOR_DOCUMENT_CONFIGURATION_FIELD.getCode())
                        .args(Map.of(
                                "configurationField", "documentConfigurationField1",
                                "instanceField", "instanceField4"
                        ))
                        .build(),

        )
    }


}
