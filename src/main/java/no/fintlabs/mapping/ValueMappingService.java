package no.fintlabs.mapping;


import no.fintlabs.model.configuration.ValueMapping;
import no.fintlabs.model.instance.InstanceObject;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ValueMappingService {

    private final InstanceReferenceService instanceReferenceService;
    private final ValueConvertingService valueConvertingService;

    public ValueMappingService(
            InstanceReferenceService instanceReferenceService,
            ValueConvertingService valueConvertingService
    ) {
        this.instanceReferenceService = instanceReferenceService;
        this.valueConvertingService = valueConvertingService;
    }

    public String toValue(
            ValueMapping valueMapping,
            Map<String, String> instanceValuePerKey,
            InstanceObject[] selectedCollectionObjectsPerKey
    ) {
        return valueMapping.getMappingString() == null
                ? null
                : switch (valueMapping.getType()) {
            case STRING, URL, BOOLEAN -> valueMapping.getMappingString();
            case FILE, DYNAMIC_STRING -> instanceReferenceService.replaceIfReferencesWithInstanceValues(
                    valueMapping.getMappingString(),
                    instanceValuePerKey,
                    selectedCollectionObjectsPerKey
            );
            case VALUE_CONVERTING -> valueConvertingService.convertValue(
                    valueMapping.getMappingString(),
                    instanceValuePerKey,
                    selectedCollectionObjectsPerKey
            );
        };
    }

}
