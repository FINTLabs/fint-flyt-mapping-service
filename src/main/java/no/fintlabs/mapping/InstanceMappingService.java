package no.fintlabs.mapping;

import no.fintlabs.model.configuration.CollectionMapping;
import no.fintlabs.model.configuration.ObjectMapping;
import no.fintlabs.model.configuration.ValueMapping;
import no.fintlabs.model.instance.InstanceObject;
import org.apache.commons.lang3.function.TriFunction;
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
                        toMappedInstanceElementCollectionPerKey(
                                this::toValue,
                                objectMapping.getValueCollectionMappingPerKey(),
                                instance,
                                selectedCollectionObjectsByCollectionIndex
                        ),
                        toMappedInstanceElementCollectionPerKey(
                                this::toMappedInstanceObject,
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
                        key -> toValue(
                                valueMappingPerKey.get(key),
                                instance,
                                selectedCollectionObjectsByCollectionIndex
                        )
                ));
    }

    private String toValue(
            ValueMapping valueMapping,
            InstanceObject instance,
            InstanceObject[] selectedCollectionObjectsByCollectionIndex
    ) {
        return valueMappingService.toValue(
                valueMapping,
                instance.getValuePerKey(),
                selectedCollectionObjectsByCollectionIndex
        );
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

    private <T, R> Map<String, Collection<R>> toMappedInstanceElementCollectionPerKey(
            TriFunction<T, InstanceObject, InstanceObject[], R> elementMappingFunction,
            Map<String, CollectionMapping<T>> collectionMappingPerKey,
            InstanceObject instance,
            InstanceObject[] selectedCollectionObjectsByCollectionIndex
    ) {
        return collectionMappingPerKey
                .keySet()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> toMappedInstanceElements(
                                elementMappingFunction,
                                collectionMappingPerKey.get(key),
                                instance,
                                selectedCollectionObjectsByCollectionIndex
                        )
                ));
    }

    private <T, R> Collection<R> toMappedInstanceElements(
            TriFunction<T, InstanceObject, InstanceObject[], R> elementMappingFunction,
            CollectionMapping<T> elementCollectionMapping,
            InstanceObject instance,
            InstanceObject[] selectedCollectionObjectsByCollectionIndex
    ) {
        return Stream.concat(
                        elementCollectionMapping.getElementMappings()
                                .stream()
                                .map(objectMapping -> elementMappingFunction.apply(objectMapping, instance, selectedCollectionObjectsByCollectionIndex)),
                        elementCollectionMapping.getFromCollectionMappings()
                                .stream()
                                .map(fromCollectionMapping -> toMappedInstanceElements(
                                        elementMappingFunction,
                                        fromCollectionMapping.getElementMapping(),
                                        instance,
                                        fromCollectionMapping.getInstanceCollectionReferencesOrdered().toArray(new String[0]),
                                        0,
                                        selectedCollectionObjectsByCollectionIndex
                                ))
                                .flatMap(Collection::stream)
                )
                .collect(Collectors.toList());
    }

    private <T, R> Collection<R> toMappedInstanceElements(
            TriFunction<T, InstanceObject, InstanceObject[], R> elementMappingFunction,
            T elementMapping,
            InstanceObject instance,
            String[] instanceCollectionReferencesByCollectionIndex,
            int nextCollectionIndex,
            InstanceObject[] selectedCollectionObjectsByCollectionIndex
    ) {
        if (nextCollectionIndex == instanceCollectionReferencesByCollectionIndex.length) {
            return List.of(elementMappingFunction.apply(elementMapping, instance, selectedCollectionObjectsByCollectionIndex));
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
                    return toMappedInstanceElements(
                            elementMappingFunction,
                            elementMapping,
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
