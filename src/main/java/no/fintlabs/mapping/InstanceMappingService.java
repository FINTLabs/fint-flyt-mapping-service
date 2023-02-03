package no.fintlabs.mapping;

import no.fintlabs.model.configuration.ElementCollectionMapping;
import no.fintlabs.model.configuration.ElementMapping;
import no.fintlabs.model.configuration.ValueMapping;
import no.fintlabs.model.instance.InstanceElement;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class InstanceMappingService {

    private final InstanceReferenceService instanceReferenceService;
    private final ValueMappingService valueMappingService;


    public InstanceMappingService(InstanceReferenceService instanceReferenceService, ValueMappingService valueMappingService) {
        this.instanceReferenceService = instanceReferenceService;
        this.valueMappingService = valueMappingService;
    }

    public Map<String, ?> toMappedInstanceElement(
            ElementMapping elementMapping,
            InstanceElement instance
    ) {
        return toMappedInstanceElement(elementMapping, instance, new InstanceElement[]{});
    }

    private Map<String, ?> toMappedInstanceElement(
            ElementMapping elementMapping,
            InstanceElement instance,
            InstanceElement[] selectedCollectionElementsByCollectionIndex
    ) {
        return Stream.of(
                        toValuePerKey(
                                elementMapping.getValueMappingPerKey(),
                                instance,
                                selectedCollectionElementsByCollectionIndex
                        ),
                        toMappedInstanceElementPerKey(
                                elementMapping.getElementMappingPerKey(),
                                instance,
                                selectedCollectionElementsByCollectionIndex
                        ),
                        toMappedInstanceElementCollectionPerKey(
                                elementMapping.getElementCollectionMappingPerKey(),
                                instance,
                                selectedCollectionElementsByCollectionIndex
                        )
                )
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, String> toValuePerKey(
            Map<String, ValueMapping> valueMappingPerKey,
            InstanceElement instance,
            InstanceElement[] selectedCollectionElementsByCollectionIndex
    ) {
        return valueMappingPerKey
                .keySet()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> valueMappingService.toValue(
                                valueMappingPerKey.get(key),
                                instance.getValuePerKey(),
                                selectedCollectionElementsByCollectionIndex
                        )
                ));
    }

    private Map<String, Map<String, ?>> toMappedInstanceElementPerKey(
            Map<String, ElementMapping> elementMappingPerKey,
            InstanceElement instance,
            InstanceElement[] selectedCollectionElementsByCollectionIndex
    ) {
        return elementMappingPerKey
                .keySet()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> toMappedInstanceElement(
                                elementMappingPerKey.get(key),
                                instance,
                                selectedCollectionElementsByCollectionIndex
                        )
                ));
    }

    private Map<String, Collection<Map<String, ?>>> toMappedInstanceElementCollectionPerKey(
            Map<String, ElementCollectionMapping> elementCollectionMappingPerKey,
            InstanceElement instance,
            InstanceElement[] selectedCollectionElementsByCollectionIndex
    ) {
        return elementCollectionMappingPerKey
                .keySet()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> toMappedInstanceElements(
                                elementCollectionMappingPerKey.get(key),
                                instance,
                                selectedCollectionElementsByCollectionIndex
                        )
                ));
    }

    private Collection<Map<String, ?>> toMappedInstanceElements(
            ElementCollectionMapping elementCollectionMapping,
            InstanceElement instance,
            InstanceElement[] selectedCollectionElementsByCollectionIndex
    ) {
        return Stream.concat(
                        elementCollectionMapping.getElementMappings()
                                .stream()
                                .map(elementMapping -> toMappedInstanceElement(elementMapping, instance)),
                        elementCollectionMapping.getElementsFromCollectionMappings()
                                .stream()
                                .map(elementsFromCollectionMapping -> toMappedInstanceElements(
                                        elementsFromCollectionMapping.getElementMapping(),
                                        instance,
                                        elementsFromCollectionMapping.getInstanceCollectionReferencesOrdered().toArray(new String[0]),
                                        selectedCollectionElementsByCollectionIndex
                                ))
                                .flatMap(Collection::stream)
                )
                .collect(Collectors.toList());
    }

    private Collection<Map<String, ?>> toMappedInstanceElements(
            ElementMapping elementMapping,
            InstanceElement instance,
            String[] instanceCollectionReferencesByCollectionIndex,
            InstanceElement[] selectedCollectionElementsByCollectionIndex
    ) {
        if (selectedCollectionElementsByCollectionIndex.length == instanceCollectionReferencesByCollectionIndex.length) {
            return List.of(toMappedInstanceElement(elementMapping, instance, selectedCollectionElementsByCollectionIndex));
        }

        int nextCollectionIndex = selectedCollectionElementsByCollectionIndex.length;

        Collection<InstanceElement> nextCollection = instanceReferenceService.getInstanceElementCollection(
                instanceCollectionReferencesByCollectionIndex[nextCollectionIndex],
                instance.getElementCollectionPerKey(),
                selectedCollectionElementsByCollectionIndex
        );

        return nextCollection
                .stream()
                .map(instanceElement -> {
                    InstanceElement[] newselectedCollectionElementsByCollectionIndex =
                            Arrays.copyOf(selectedCollectionElementsByCollectionIndex, nextCollectionIndex + 1);
                    newselectedCollectionElementsByCollectionIndex[nextCollectionIndex] = instanceElement;
                    return toMappedInstanceElements(
                            elementMapping,
                            instance,
                            instanceCollectionReferencesByCollectionIndex,
                            newselectedCollectionElementsByCollectionIndex
                    );
                })
                .flatMap(Collection::stream)
                .toList();
    }
}
