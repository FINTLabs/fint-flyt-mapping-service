package no.fintlabs.integration.error.mappers

import no.fintlabs.integration.error.ErrorCode
import no.fintlabs.kafka.event.error.Error
import no.fintlabs.kafka.event.error.ErrorCollection
import no.fintlabs.mapping.fields.CaseMappingField
import no.fintlabs.mapping.fields.DocumentMappingField
import no.fintlabs.mapping.fields.RecordMappingField
import no.fintlabs.validation.MappedFieldsValidationService
import no.fintlabs.validation.exceptions.MissingMappingFieldsValidationException
import spock.lang.Specification

class MissingMappingFieldsErrorMapperSpec extends Specification {

    MissingMappingFieldsErrorMapper missingMappingFieldsErrorMapper

    def setup() {
        missingMappingFieldsErrorMapper = new MissingMappingFieldsErrorMapper()
    }

    def 'should map to error collection'() {
        given:
        MissingMappingFieldsValidationException exception = new MissingMappingFieldsValidationException(
                new MappedFieldsValidationService.Error(
                        List.of(CaseMappingField.ADMINISTRATIV_ENHET, CaseMappingField.JOURNALENHET),
                        List.of(RecordMappingField.JOURNALPOSTTYPE),
                        List.of(DocumentMappingField.SKJERMINGSHJEMMEL)
                )
        )

        when:
        ErrorCollection errorCollection = missingMappingFieldsErrorMapper.map(exception)

        then:
        errorCollection.getErrors().size() == 4
        errorCollection.getErrors().containsAll(
                Error.builder()
                        .errorCode(ErrorCode.MISSING_MAPPING_FIELD_FOR_CASE.getCode())
                        .args(Map.of("mappingField", CaseMappingField.ADMINISTRATIV_ENHET.getFieldKey()))
                        .build(),
                Error.builder()
                        .errorCode(ErrorCode.MISSING_MAPPING_FIELD_FOR_CASE.getCode())
                        .args(Map.of("mappingField", CaseMappingField.JOURNALENHET.getFieldKey()))
                        .build(),
                Error.builder()
                        .errorCode(ErrorCode.MISSING_MAPPING_FIELD_FOR_RECORD.getCode())
                        .args(Map.of("mappingField", RecordMappingField.JOURNALPOSTTYPE.getFieldKey()))
                        .build(),
                Error.builder()
                        .errorCode(ErrorCode.MISSING_MAPPING_FIELD_FOR_DOCUMENT.getCode())
                        .args(Map.of("mappingField", DocumentMappingField.SKJERMINGSHJEMMEL.getFieldKey()))
                        .build(),
        )

    }

}
