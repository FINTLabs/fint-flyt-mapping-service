package no.fintlabs.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.fintlabs.mapping.fields.MappingField;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MappedFieldsValidationService {

    @Data
    @AllArgsConstructor
    public static class Error {
        private final List<MappingField> missingCaseFields;
        private final List<MappingField> missingRecordFields;
        private final List<MappingField> missingDocumentFields;
    }

    public Optional<Error> validate(
            MappingField[] caseMappingFields, Map<String, String> caseValuesByFieldKey,
            MappingField[] recordMappingFields, Map<String, String> recordValuesByFieldKey,
            MappingField[] documentMappingFields, Map<String, String> documentValuesByFieldKey
    ) {
        List<MappingField> missingCaseMappingFields = findMissingFields(caseMappingFields, caseValuesByFieldKey);
        List<MappingField> missingRecordMappingFields = findMissingFields(recordMappingFields, recordValuesByFieldKey);
        List<MappingField> missingDocumentMappingFields = findMissingFields(documentMappingFields, documentValuesByFieldKey);

        return Stream.of(
                missingCaseMappingFields,
                missingRecordMappingFields,
                missingDocumentMappingFields
        ).anyMatch(mappingFields -> !mappingFields.isEmpty())
                ? Optional.of(new Error(missingCaseMappingFields, missingRecordMappingFields, missingDocumentMappingFields))
                : Optional.empty();
    }

    public List<MappingField> findMissingFields(MappingField[] expectedMappingFields, Map<String, String> valuesByFieldKey) {
        return Arrays.stream(expectedMappingFields)
                .filter(MappingField::isRequired)
                .filter(key -> !valuesByFieldKey.containsKey(key.getFieldKey()))
                .collect(Collectors.toList());
    }

}
