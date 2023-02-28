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

    public Map<String, ?> toMappedInstanceObject(
            ObjectMapping objectMapping,
            InstanceObject instance
    ) {
        return toMappedInstanceObject(objectMapping, instance, new InstanceObject[]{});
    }

    private Map<String, ?> toMappedInstanceObject(
            ObjectMapping objectMapping,
            InstanceObject instance,
            InstanceObject[] selectedCollectionObjectsByCollectionIndex
    ) {
        return Stream.of(
                        toValuePerKey(
                                objectMapping.getValueMappingPerKey(),
                                instance,
                                selectedCollectionObjectsByCollectionIndex
                        ),
                        toMappedInstanceObjectPerKey(
                                objectMapping.getObjectMappingPerKey(),
                                instance,
                                selectedCollectionObjectsByCollectionIndex
                        ),
                        toMappedInstanceObjectCollectionPerKey(
                                objectMapping.getObjectCollectionMappingPerKey(),
                                instance,
                                selectedCollectionObjectsByCollectionIndex
                        )
                )
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, String> toValuePerKey(
            Map<String, ValueMapping> valueMappingPerKey,
            InstanceObject instance,
            InstanceObject[] selectedCollectionObjectsByCollectionIndex
    ) {
        return valueMappingPerKey
                .keySet()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> valueMappingService.toValue(
                                valueMappingPerKey.get(key),
                                instance.getValuePerKey(),
                                selectedCollectionObjectsByCollectionIndex
                        )
                ));
    }

    private Map<String, Map<String, ?>> toMappedInstanceObjectPerKey(
            Map<String, ObjectMapping> objectMappingPerKey,
            InstanceObject instance,
            InstanceObject[] selectedCollectionObjectsByCollectionIndex
    ) {
        return objectMappingPerKey
                .keySet()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> toMappedInstanceObject(
                                objectMappingPerKey.get(key),
                                instance,
                                selectedCollectionObjectsByCollectionIndex
                        )
                ));
    }

    private Map<String, Collection<Map<String, ?>>> toMappedInstanceObjectCollectionPerKey(
            Map<String, CollectionMapping<ObjectMapping>> objectCollectionMappingPerKey,
            InstanceObject instance,
            InstanceObject[] selectedCollectionObjectsByCollectionIndex
    ) {
        return objectCollectionMappingPerKey
                .keySet()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> toMappedInstanceObjects(
                                objectCollectionMappingPerKey.get(key),
                                instance,
                                selectedCollectionObjectsByCollectionIndex
                        )
                ));
    }

    private Collection<Map<String, ?>> toMappedInstanceObjects(
            CollectionMapping<ObjectMapping> objectCollectionMapping,
            InstanceObject instance,
            InstanceObject[] selectedCollectionObjectsByCollectionIndex
    ) {
        return Stream.concat(
                        objectCollectionMapping.getElementMappings()
                                .stream()
                                .map(objectMapping -> toMappedInstanceObject(objectMapping, instance, selectedCollectionObjectsByCollectionIndex)),
                        objectCollectionMapping.getFromCollectionMappings()
                                .stream()
                                .map(objectsFromCollectionMapping -> toMappedInstanceObjects(
                                        objectsFromCollectionMapping.getElementMapping(),
                                        instance,
                                        objectsFromCollectionMapping.getInstanceCollectionReferencesOrdered().toArray(new String[0]),
                                        0,
                                        selectedCollectionObjectsByCollectionIndex
                                ))
                                .flatMap(Collection::stream)
                )
                .collect(Collectors.toList());
    }

    private Collection<Map<String, ?>> toMappedInstanceObjects(
            ObjectMapping objectMapping,
            InstanceObject instance,
            String[] instanceCollectionReferencesByCollectionIndex,
            int nextCollectionIndex,
            InstanceObject[] selectedCollectionObjectsByCollectionIndex
    ) {
        if (nextCollectionIndex == instanceCollectionReferencesByCollectionIndex.length) {
            return List.of(toMappedInstanceObject(objectMapping, instance, selectedCollectionObjectsByCollectionIndex));
        }

        Collection<InstanceObject> nextCollection = instanceReferenceService.getInstanceObjectCollection(
                instanceCollectionReferencesByCollectionIndex[nextCollectionIndex],
                instance.getObjectCollectionPerKey(),
                selectedCollectionObjectsByCollectionIndex
        );

        return nextCollection
                .stream()
                .map(instanceObject -> {
                    InstanceObject[] newSelectedCollectionObjectsByCollectionIndex =
                            Arrays.copyOf(selectedCollectionObjectsByCollectionIndex, selectedCollectionObjectsByCollectionIndex.length + 1);
                    newSelectedCollectionObjectsByCollectionIndex[selectedCollectionObjectsByCollectionIndex.length] = instanceObject;
                    return toMappedInstanceObjects(
                            objectMapping,
                            instance,
                            instanceCollectionReferencesByCollectionIndex,
                            nextCollectionIndex + 1,
                            newSelectedCollectionObjectsByCollectionIndex
                    );
                })
                .flatMap(Collection::stream)
                .toList();
    }
}
