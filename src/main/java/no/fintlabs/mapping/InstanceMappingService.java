package no.fintlabs.mapping;

import no.fintlabs.model.configuration.CollectionMapping;
import no.fintlabs.model.configuration.ObjectMapping;
import no.fintlabs.model.configuration.ValueMapping;
import no.fintlabs.model.instance.InstanceObject;
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
            ObjectMapping objectMapping,
            InstanceObject instance
    ) {
        return toMappedInstanceElement(objectMapping, instance, new InstanceObject[]{});
    }

    private Map<String, ?> toMappedInstanceElement(
            ObjectMapping objectMapping,
            InstanceObject instance,
            InstanceObject[] selectedCollectionElementsByCollectionIndex
    ) {
        return Stream.of(
                        toValuePerKey(
                                objectMapping.getValueMappingPerKey(),
                                instance,
                                selectedCollectionElementsByCollectionIndex
                        ),
                        toMappedInstanceElementPerKey(
                                objectMapping.getObjectMappingPerKey(),
                                instance,
                                selectedCollectionElementsByCollectionIndex
                        ),
                        toMappedInstanceElementCollectionPerKey(
                                objectMapping.getObjectCollectionMappingPerKey(),
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
            InstanceObject instance,
            InstanceObject[] selectedCollectionElementsByCollectionIndex
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
            Map<String, ObjectMapping> elementMappingPerKey,
            InstanceObject instance,
            InstanceObject[] selectedCollectionElementsByCollectionIndex
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
            Map<String, CollectionMapping<ObjectMapping>> objectCollectionMappingPerKey,
            InstanceObject instance,
            InstanceObject[] selectedCollectionElementsByCollectionIndex
    ) {
        return objectCollectionMappingPerKey
                .keySet()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> toMappedInstanceElements(
                                objectCollectionMappingPerKey.get(key),
                                instance,
                                selectedCollectionElementsByCollectionIndex
                        )
                ));
    }

    private Collection<Map<String, ?>> toMappedInstanceElements(
            CollectionMapping<ObjectMapping> objectCollectionMapping,
            InstanceObject instance,
            InstanceObject[] selectedCollectionElementsByCollectionIndex
    ) {
        return Stream.concat(
                        objectCollectionMapping.getElementMappings()
                                .stream()
                                .map(elementMapping -> toMappedInstanceElement(elementMapping, instance, selectedCollectionElementsByCollectionIndex)),
                        objectCollectionMapping.getFromCollectionMappings()
                                .stream()
                                .map(elementsFromCollectionMapping -> toMappedInstanceElements(
                                        elementsFromCollectionMapping.getElementMapping(),
                                        instance,
                                        elementsFromCollectionMapping.getInstanceCollectionReferencesOrdered().toArray(new String[0]),
                                        0,
                                        selectedCollectionElementsByCollectionIndex
                                ))
                                .flatMap(Collection::stream)
                )
                .collect(Collectors.toList());
    }

    private Collection<Map<String, ?>> toMappedInstanceElements(
            ObjectMapping objectMapping,
            InstanceObject instance,
            String[] instanceCollectionReferencesByCollectionIndex,
            int nextCollectionIndex,
            InstanceObject[] selectedCollectionElementsByCollectionIndex
    ) {
        if (nextCollectionIndex == instanceCollectionReferencesByCollectionIndex.length) {
            return List.of(toMappedInstanceElement(objectMapping, instance, selectedCollectionElementsByCollectionIndex));
        }

        Collection<InstanceObject> nextCollection = instanceReferenceService.getInstanceElementCollection(
                instanceCollectionReferencesByCollectionIndex[nextCollectionIndex],
                instance.getObjectCollectionPerKey(),
                selectedCollectionElementsByCollectionIndex
        );

        return nextCollection
                .stream()
                .map(instanceElement -> {
                    InstanceObject[] newselectedCollectionElementsByCollectionIndex =
                            Arrays.copyOf(selectedCollectionElementsByCollectionIndex, selectedCollectionElementsByCollectionIndex.length + 1);
                    newselectedCollectionElementsByCollectionIndex[selectedCollectionElementsByCollectionIndex.length] = instanceElement;
                    return toMappedInstanceElements(
                            objectMapping,
                            instance,
                            instanceCollectionReferencesByCollectionIndex,
                            nextCollectionIndex + 1,
                            newselectedCollectionElementsByCollectionIndex
                    );
                })
                .flatMap(Collection::stream)
                .toList();
    }
}
