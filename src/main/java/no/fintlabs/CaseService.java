package no.fintlabs;

import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.model.configuration.Field;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import no.fintlabs.model.configuration.Property;
import no.fintlabs.model.configuration.ValueSource;
import no.fintlabs.model.instance.Instance;
import no.fintlabs.model.instance.InstanceField;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CaseService {

    public SakResource createSak(IntegrationConfiguration integrationConfiguration, Instance instance) {

        SakResource sakResource = new SakResource();

        Map<String, String> caseValuesByFieldKey = mapCaseFields(
                integrationConfiguration.getCaseConfiguration().getFields(),
                instance.getFields()
        );
        sakResource.setTittel(caseValuesByFieldKey.get("title"));
        return sakResource;
    }

    private Map<String, String> mapCaseFields(List<Field> configurationFields, Map<String, InstanceField> instanceFields) {
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
            default:
                throw new IllegalArgumentException("Strategy not supported: " + configField.getValueBuildStrategy());
        }
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

    private String getValue(Map<String, InstanceField> instance, String key, ValueSource source) {
        // TODO: 13/01/2022 Handle none existing
        return instance.get(key).getValue();
    }
}
