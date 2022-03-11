package no.fintlabs.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.fintlabs.mapping.fields.CaseMappingField;
import no.fintlabs.mapping.fields.DocumentMappingField;
import no.fintlabs.mapping.fields.MappingField;
import no.fintlabs.mapping.fields.RecordMappingField;
import no.fintlabs.model.configuration.ConfigurationField;
import no.fintlabs.model.configuration.Property;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

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
            Map<String, String> caseValuesByFieldKey,
            Map<String, String> recordValuesByFieldKey,
            Map<String, String> documentValuesByFieldKey
    ) {
        List<MappingField> missingCaseMappingFields = findMissingFields(CaseMappingField.values(), caseValuesByFieldKey);
        List<MappingField> missingRecordMappingFields = findMissingFields(RecordMappingField.values(), recordValuesByFieldKey);
        List<MappingField> missingDocumentMappingFields = findMissingFields(DocumentMappingField.values(), documentValuesByFieldKey);

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

    private Map<ConfigurationField, List<String>> findMissingInstanceFieldsPerConfigurationField(List<ConfigurationField> configurationFields, Set<String> instanceFieldKeys) {
        return configurationFields
                .stream()
                .map(field -> new AbstractMap.SimpleEntry<>(field, this.findMissingProperties(field, instanceFieldKeys)))
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private List<String> findMissingProperties(ConfigurationField configurationField, Set<String> instanceFieldKeys) {
        return configurationField.getValueBuilder().getProperties()
                .stream()
                .map(Property::getKey)
                .filter(key -> !instanceFieldKeys.contains(key))
                .collect(Collectors.toList());
    }

}
