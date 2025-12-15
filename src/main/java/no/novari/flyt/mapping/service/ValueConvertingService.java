package no.novari.flyt.mapping.service;

import no.novari.flyt.mapping.exception.ValueConvertingKeyNotFoundException;
import no.novari.flyt.mapping.exception.ValueConvertingNotFoundException;
import no.novari.flyt.mapping.kafka.configuration.ValueConvertingRequestProducerService;
import no.novari.flyt.mapping.model.instance.InstanceObject;
import no.novari.flyt.mapping.model.valueconverting.ValueConverting;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ValueConvertingService {

    private final ValueConvertingRequestProducerService valueConvertingRequestProducerService;
    private final InstanceReferenceService instanceReferenceService;
    private final ValueConvertingReferenceService valueConvertingReferenceService;

    public ValueConvertingService(
            ValueConvertingRequestProducerService valueConvertingRequestProducerService,
            InstanceReferenceService instanceReferenceService,
            ValueConvertingReferenceService valueConvertingReferenceService
    ) {
        this.valueConvertingRequestProducerService = valueConvertingRequestProducerService;
        this.instanceReferenceService = instanceReferenceService;
        this.valueConvertingReferenceService = valueConvertingReferenceService;
    }

    public String convertValue(
            String mappingString,
            Map<String, String> instanceValuePerKey,
            InstanceObject[] selectedCollectionObjectsPerKey
    ) {
        String instanceValue = instanceReferenceService.getFirstInstanceValue(
                mappingString,
                instanceValuePerKey,
                selectedCollectionObjectsPerKey
        );
        Long valueConvertingId = valueConvertingReferenceService.getFirstValueConverterId(mappingString);
        ValueConverting valueConverting = valueConvertingRequestProducerService.get(valueConvertingId)
                .orElseThrow(() -> new ValueConvertingNotFoundException(valueConvertingId));
        Map<String, String> convertingMap = valueConverting.getConvertingMap();
        if (!convertingMap.containsKey(instanceValue)) {
            throw new ValueConvertingKeyNotFoundException(valueConvertingId, instanceValue);
        }
        return convertingMap.get(instanceValue);
    }

}
