package no.fintlabs.mapping;


import no.fintlabs.model.configuration.FieldConfiguration;
import no.fintlabs.model.instance.Instance;
import no.fintlabs.model.mappedinstance.MappedInstanceField;

public abstract class DynamicInstanceFieldMapper {

    public MappedInstanceField toInstanceField(Instance instance, FieldConfiguration fieldConfiguration) {
        return MappedInstanceField
                .builder()
                .key(fieldConfiguration.getKey())
                .type(getMappedInstanceFieldType())
                .value(toMappedInstanceFieldValue(instance, fieldConfiguration.getValue()))
                .build();
    }

    protected abstract MappedInstanceField.Type getMappedInstanceFieldType();

    protected abstract String toMappedInstanceFieldValue(Instance instance, String fieldConfigurationValue);

}
