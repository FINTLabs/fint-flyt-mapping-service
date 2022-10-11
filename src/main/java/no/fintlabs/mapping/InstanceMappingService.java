package no.fintlabs.mapping;

import no.fintlabs.model.configuration.Configuration;
import no.fintlabs.model.configuration.ConfigurationElement;
import no.fintlabs.model.configuration.FieldCollectionConfiguration;
import no.fintlabs.model.configuration.FieldConfiguration;
import no.fintlabs.model.instance.Instance;
import no.fintlabs.model.mappedinstance.MappedInstance;
import no.fintlabs.model.mappedinstance.MappedInstanceElement;
import no.fintlabs.model.mappedinstance.MappedInstanceField;
import no.fintlabs.model.mappedinstance.MappedInstanceFieldCollection;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class InstanceMappingService {

    private final DynamicStringMappingService dynamicStringMappingService;

    public InstanceMappingService(DynamicStringMappingService dynamicStringMappingService) {
        this.dynamicStringMappingService = dynamicStringMappingService;
    }

    public MappedInstance map(Instance instance, Configuration configuration) {
        return MappedInstance
                .builder()
                .elements(toMappedInstanceElements(instance, configuration.getElements()))
                .build();
    }

    private Collection<MappedInstanceElement> toMappedInstanceElements(Instance instance, Collection<ConfigurationElement> configurationElements) {
        return configurationElements
                .stream()
                .map(element -> toMappedInstanceElement(instance, element))
                .toList();
    }

    private MappedInstanceElement toMappedInstanceElement(Instance instance, ConfigurationElement configurationElement) {
        return MappedInstanceElement
                .builder()
                .key(configurationElement.getKey())
                .elements(toMappedInstanceElements(instance, configurationElement.getElements()))
                .fields(toMappedInstanceFields(instance, configurationElement.getFieldConfigurations()))
                .fieldCollections(toMappedInstanceFieldCollections(configurationElement.getFieldCollectionConfigurations()))
                .build();
    }

    private Collection<MappedInstanceField> toMappedInstanceFields(Instance instance, Collection<FieldConfiguration> fieldConfigurations) {
        return fieldConfigurations
                .stream()
                .map(fieldConfiguration -> toMappedInstanceField(instance, fieldConfiguration))
                .toList();
    }

    private MappedInstanceField toMappedInstanceField(Instance instance, FieldConfiguration fieldConfiguration) {
        return switch (fieldConfiguration.getType()) {
            case STRING, URL, BOOLEAN -> this.toMappedInstanceFieldForStaticField(fieldConfiguration);
            case DYNAMIC_STRING -> dynamicStringMappingService.toInstanceField(instance, fieldConfiguration);
        };
    }

    private MappedInstanceField toMappedInstanceFieldForStaticField(FieldConfiguration fieldConfiguration) {
        return MappedInstanceField
                .builder()
                .key(fieldConfiguration.getKey())
                .type(MappedInstanceField.Type.valueOf(fieldConfiguration.getType().name()))
                .value(fieldConfiguration.getValue())
                .build();
    }

    private Collection<MappedInstanceFieldCollection> toMappedInstanceFieldCollections(Collection<FieldCollectionConfiguration> fieldCollectionConfigurations) {
        return fieldCollectionConfigurations
                .stream()
                .map(this::toMappedInstanceFieldCollection)
                .toList();
    }

    private MappedInstanceFieldCollection toMappedInstanceFieldCollection(FieldCollectionConfiguration fieldCollectionConfiguration) {
        return MappedInstanceFieldCollection
                .builder()
                .key(fieldCollectionConfiguration.getKey())
                .type(MappedInstanceFieldCollection.Type.valueOf(fieldCollectionConfiguration.getType().name()))
                .values(fieldCollectionConfiguration.getValues())
                .build();
    }

}
