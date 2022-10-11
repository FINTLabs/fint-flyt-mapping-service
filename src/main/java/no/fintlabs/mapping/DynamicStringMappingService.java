package no.fintlabs.mapping;

import no.fintlabs.model.instance.Instance;
import no.fintlabs.model.mappedinstance.MappedInstanceField;
import org.springframework.stereotype.Service;

@Service
public class DynamicStringMappingService extends DynamicInstanceFieldMapper {

    @Override
    protected MappedInstanceField.Type getMappedInstanceFieldType() {
        return MappedInstanceField.Type.STRING;
    }

    @Override
    protected String toMappedInstanceFieldValue(Instance instance, String fieldConfigurationValue) {
        return fieldConfigurationValue;
    }

}
