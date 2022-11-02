package no.fintlabs.mapping;

import no.fintlabs.InstanceFieldNotFoundException;
import no.fintlabs.model.instance.Instance;
import no.fintlabs.model.instance.InstanceField;
import no.fintlabs.model.mappedinstance.MappedInstanceField;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DynamicStringMappingService extends DynamicInstanceFieldMapper {

    private static final Pattern ifReferencePattern = Pattern.compile("\\$if\\{[^}]+}");

    @Override
    protected MappedInstanceField.Type getMappedInstanceFieldType() {
        return MappedInstanceField.Type.STRING;
    }

    @Override
    protected String toMappedInstanceFieldValue(Instance instance, String fieldConfigurationValue) {
        Matcher matcher = ifReferencePattern.matcher(fieldConfigurationValue);
        return matcher.replaceAll(matchResult -> getInstanceFieldValue(instance, matchResult.group()));
    }

    private String getInstanceFieldValue(Instance instance, String ifReference) {
        String instanceFieldKey = ifReference.replace("$if{", "").replace("}", "");
        InstanceField instanceField = Optional.ofNullable(instance.getFieldPerKey().get(instanceFieldKey))
                .orElseThrow(() -> new InstanceFieldNotFoundException(instanceFieldKey));
        return Optional.ofNullable(instanceField.getValue())
                .orElse("");
    }

}
