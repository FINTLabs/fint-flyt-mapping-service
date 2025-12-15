package no.novari.flyt.mapping.service;


import no.novari.flyt.mapping.model.configuration.ValueMapping;
import no.novari.flyt.mapping.model.instance.InstanceObject;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

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

    public Object toValue(
            ValueMapping valueMapping,
            Map<String, String> instanceValuePerKey,
            InstanceObject[] selectedCollectionObjectsPerKey
    ) {
        return Objects.isNull(valueMapping.getMappingString()) ? null : switch (valueMapping.getType()) {
            case BOOLEAN -> Boolean.parseBoolean(valueMapping.getMappingString());
            case STRING, URL -> valueMapping.getMappingString();
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
