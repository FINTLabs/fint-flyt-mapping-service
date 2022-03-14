package no.fintlabs.validation

import no.fintlabs.mapping.fields.MappingField
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Collectors

class MappedFieldsValidationServiceSpec extends Specification {

    private static class TestMappingField implements MappingField {

        private final String fieldKey
        private final boolean required;

        TestMappingField(String fieldKey, boolean required) {
            this.fieldKey = fieldKey
            this.required = required
        }

        @Override
        String getFieldKey() {
            return fieldKey
        }

        @Override
        boolean isRequired() {
            return required
        }
    }

    @Shared
    MappedFieldsValidationService mappedFieldsValidationService

    def setup() {
        this.mappedFieldsValidationService = new MappedFieldsValidationService()
    }

    def 'given that all fields are required and present, validation should not return any errors'() {
        given:
        MappingField[] caseMappingFields = [
                new TestMappingField("caseField1Key", true),
                new TestMappingField("caseField2Key", true)
        ]
        Map<String, String> caseValuesByFieldKey = [
                "caseField1Key": "caseField1Value",
                "caseField2Key": "caseField2Value"
        ]
        MappingField[] recordMappingFields = [
                new TestMappingField("recordField1Key", true),
                new TestMappingField("recordField2Key", true)
        ]
        Map<String, String> recordValuesByFieldKey = [
                "recordField1Key": "recordField1Value",
                "recordField2Key": "recordField2Value"
        ]
        MappingField[] documentMappingFields = [
                new TestMappingField("documentField1Key", true),
                new TestMappingField("documentField2Key", true)
        ]
        Map<String, String> documentValuesByFieldKey = [
                "documentField1Key": "documentField1Value",
                "documentField2Key": "documentField2Value"
        ]
        when:
        Optional<MappedFieldsValidationService.Error> error = this.mappedFieldsValidationService.validate(
                caseMappingFields, caseValuesByFieldKey,
                recordMappingFields, recordValuesByFieldKey,
                documentMappingFields, documentValuesByFieldKey
        )
        then:
        error.isEmpty()
    }

    def 'given required and not required fields where the required fields are present, validation should not return any errors'() {
        given:
        MappingField[] caseMappingFields = [
                new TestMappingField("caseField1Key", true),
                new TestMappingField("caseField2Key", true)
        ]
        Map<String, String> caseValuesByFieldKey = [
                "caseField1Key": "caseField1Value",
                "caseField2Key": "caseField2Value"
        ]
        MappingField[] recordMappingFields = [
                new TestMappingField("recordField1Key", true),
                new TestMappingField("recordField2Key", false)
        ]
        Map<String, String> recordValuesByFieldKey = [
                "recordField1Key": "recordField1Value",
        ]
        MappingField[] documentMappingFields = [
                new TestMappingField("documentField1Key", false),
                new TestMappingField("documentField2Key", true)
        ]
        Map<String, String> documentValuesByFieldKey = [
                "documentField2Key": "documentField2Value"
        ]
        when:
        Optional<MappedFieldsValidationService.Error> error = this.mappedFieldsValidationService.validate(
                caseMappingFields, caseValuesByFieldKey,
                recordMappingFields, recordValuesByFieldKey,
                documentMappingFields, documentValuesByFieldKey
        )
        then:
        error.isEmpty()
    }

    def 'given that a required field is not present, validation should return an error'() {
        given:
        MappingField[] caseMappingFields = [
                new TestMappingField("caseField1Key", true),
                new TestMappingField("caseField2Key", true)
        ]
        Map<String, String> caseValuesByFieldKey = [
                "caseField1Key": "caseField1Value",
        ]
        MappingField[] recordMappingFields = [
                new TestMappingField("recordField1Key", true),
                new TestMappingField("recordField2Key", true)
        ]
        Map<String, String> recordValuesByFieldKey = [
                "recordField1Key": "recordField1Value",
                "recordField2Key": "recordField2Value"
        ]
        MappingField[] documentMappingFields = [
                new TestMappingField("documentField1Key", true),
                new TestMappingField("documentField2Key", true)
        ]
        Map<String, String> documentValuesByFieldKey = Map.of()
        when:
        Optional<MappedFieldsValidationService.Error> error = this.mappedFieldsValidationService.validate(
                caseMappingFields, caseValuesByFieldKey,
                recordMappingFields, recordValuesByFieldKey,
                documentMappingFields, documentValuesByFieldKey
        )
        then:
        error.isPresent()

        error.get().missingCaseFields.size() == 1
        error.get().missingCaseFields.get(0).fieldKey == "caseField2Key"

        error.get().missingRecordFields.size() == 0

        error.get().missingDocumentFields.size() == 2
        error.get().missingDocumentFields.stream().map(MappingField::getFieldKey).collect(Collectors.toList())
                .containsAll("documentField1Key", "documentField2Key")
    }

}
