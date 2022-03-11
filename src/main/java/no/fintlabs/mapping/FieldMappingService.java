package no.fintlabs.mapping;

import no.fintlabs.model.configuration.ConfigurationField;
import no.fintlabs.model.configuration.Property;
import no.fintlabs.model.configuration.ValueSource;
import no.fintlabs.model.instance.InstanceField;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: 28/01/2022 Naming
@Service
public class FieldMappingService {

    public Map<String, String> mapFields(List<ConfigurationField> configurationFields, Map<String, InstanceField> instanceFields) {
        return configurationFields.stream()
                .map(configField -> mapFieldEntry(configField, instanceFields))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private AbstractMap.SimpleEntry<String, String> mapFieldEntry(ConfigurationField configField, Map<String, InstanceField> instanceFields) {
        return new AbstractMap.SimpleEntry<>(
                configField.getField(),
                mapField(configField, instanceFields)
        );
    }

    private String mapField(ConfigurationField configField, Map<String, InstanceField> instanceFields) {
        switch (configField.getValueBuildStrategy()) {
            case COMBINE_STRING_VALUE:
                return mapFieldWithCombineStringValueStrategy(configField, instanceFields);
            case FIXED_ARCHIVE_CODE_VALUE:
                return mapFieldWithFixedValueStrategy(configField);
            default:
                throw new IllegalArgumentException("Strategy not supported: " + configField.getValueBuildStrategy());
        }
    }

    private String mapFieldWithFixedValueStrategy(ConfigurationField configField) {
        return configField.getValueBuilder().getValue();
    }

    private String mapFieldWithCombineStringValueStrategy(ConfigurationField configField, Map<String, InstanceField> instanceFields) {
        String input = configField.getValueBuilder().getValue();
        List<String> args = getArgsSortedByOrder(instanceFields, configField);
        return String.format(input, args.toArray());
    }

    private List<String> getArgsSortedByOrder(Map<String, InstanceField> instanceFields, ConfigurationField titleConfiguration) {
        return titleConfiguration.getValueBuilder().getProperties().stream()
                .sorted(Comparator.comparingInt(Property::getOrder))
                .map(property -> this.getValue(instanceFields, property.getKey(), property.getSource()))
                .collect(Collectors.toList());
    }

    private String getValue(Map<String, InstanceField> instanceFields, String key, ValueSource source) {
        if (!instanceFields.containsKey(key)) {
            throw new NoSuchFieldException(key);
        }
        return instanceFields.get(key).getValue();
    }
}
