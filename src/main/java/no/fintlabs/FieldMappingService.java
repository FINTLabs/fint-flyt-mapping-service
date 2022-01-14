package no.fintlabs;

import no.fintlabs.model.configuration.Field;
import no.fintlabs.model.configuration.Property;
import no.fintlabs.model.configuration.ValueSource;
import no.fintlabs.model.instance.InstanceField;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FieldMappingService {

    public static class NoSuchFieldException extends RuntimeException {
        public NoSuchFieldException(String fieldKey) {
            super("Instance fields does not contain field with key=" + fieldKey);
        }
    }

    public Map<String, String> mapCaseFields(List<Field> configurationFields, Map<String, InstanceField> instanceFields) {
        return configurationFields.stream()
                .map(configField -> mapFieldEntry(configField, instanceFields))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private AbstractMap.SimpleEntry<String, String> mapFieldEntry(Field configField, Map<String, InstanceField> instanceFields) {
        return new AbstractMap.SimpleEntry<>(
                configField.getField(),
                mapField(configField, instanceFields)
        );
    }

    private String mapField(Field configField, Map<String, InstanceField> instanceFields) {
        switch (configField.getValueBuildStrategy()) {
            case COMBINE_STRING_VALUE:
                return mapFieldWithCombineStringValueStrategy(configField, instanceFields);
            case FIXED_ARCHIVE_CODE_VALUE:
                return mapFieldWithFixedValueStrategy(configField);
            default:
                throw new IllegalArgumentException("Strategy not supported: " + configField.getValueBuildStrategy());
        }
    }

    private String mapFieldWithFixedValueStrategy(Field configField) {
        return configField.getValueBuilder().getValue();
    }

    private String mapFieldWithCombineStringValueStrategy(Field configField, Map<String, InstanceField> instanceFields) {
        String input = configField.getValueBuilder().getValue();
        List<String> args = getSortedArgs(instanceFields, configField);
        return String.format(input, args.toArray());
    }

    private List<String> getSortedArgs(Map<String, InstanceField> instanceFields, Field titleConfiguration) {
        return titleConfiguration.getValueBuilder().getProperties().stream()
                .sorted(Comparator.comparingInt(Property::getOrder))
                .map(f -> this.getValue(instanceFields, f.getKey(), f.getSource()))
                .collect(Collectors.toList());
    }

    private String getValue(Map<String, InstanceField> instanceFields, String key, ValueSource source) {

        if (!instanceFields.containsKey(key)) {
            throw new NoSuchFieldException(key);
        }

        return instanceFields.get(key).getValue();
    }
}
