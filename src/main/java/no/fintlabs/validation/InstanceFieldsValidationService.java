package no.fintlabs.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.fintlabs.model.configuration.Field;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import no.fintlabs.model.configuration.Property;
import no.fintlabs.model.instance.Instance;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Service
public class InstanceFieldsValidationService {

    @Data
    @AllArgsConstructor
    public static class Error {
        Map<Field, List<String>> missingInstanceFieldsPerCaseConfigurationField;
        Map<Field, List<String>> missingInstanceFieldsPerRecordConfigurationField;
        Map<Field, List<String>> missingInstanceFieldsPerDocumentConfigurationField;
    }

    public Optional<Error> validate(IntegrationConfiguration integrationConfiguration, Instance instance) {
        Set<String> instanceFieldKeys = instance.getFields().keySet();

        Map<Field, List<String>> missingInstanceFieldsPerCaseConfigurationField =
                this.findMissingInstanceFieldsPerConfigurationField(integrationConfiguration.getCaseConfiguration().getFields(), instanceFieldKeys);
        Map<Field, List<String>> missingInstanceFieldsPerRecordConfigurationField =
                this.findMissingInstanceFieldsPerConfigurationField(integrationConfiguration.getRecordConfiguration().getFields(), instanceFieldKeys);
        Map<Field, List<String>> missingInstanceFieldsPerDocumentConfigurationField =
                this.findMissingInstanceFieldsPerConfigurationField(integrationConfiguration.getDocumentConfiguration().getFields(), instanceFieldKeys);

        return Stream.of(
                missingInstanceFieldsPerCaseConfigurationField,
                missingInstanceFieldsPerRecordConfigurationField,
                missingInstanceFieldsPerDocumentConfigurationField
        ).anyMatch(map -> !map.isEmpty())
                ? Optional.of(new Error(
                        missingInstanceFieldsPerCaseConfigurationField,
                        missingInstanceFieldsPerRecordConfigurationField,
                        missingInstanceFieldsPerDocumentConfigurationField
                ))
                : Optional.empty();
    }


    private Map<Field, List<String>> findMissingInstanceFieldsPerConfigurationField(List<Field> configurationFields, Set<String> instanceFieldKeys) {
        return configurationFields
                .stream()
                .map(field -> new AbstractMap.SimpleEntry<>(field, this.findMissingProperties(field, instanceFieldKeys)))
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private List<String> findMissingProperties(Field configurationField, Set<String> instanceFieldKeys) {
        return configurationField.getValueBuilder().getProperties()
                .stream()
                .map(Property::getKey)
                .filter(key -> !instanceFieldKeys.contains(key))
                .collect(Collectors.toList());
    }

}
