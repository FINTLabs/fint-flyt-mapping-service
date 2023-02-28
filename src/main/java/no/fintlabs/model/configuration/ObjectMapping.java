package no.fintlabs.model.configuration;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class ObjectMapping {

    public ObjectMapping(
            Map<String, ValueMapping> valueMappingPerKey,
            Map<String, CollectionMapping<ValueMapping>> valueCollectionMappingPerKey,
            Map<String, ObjectMapping> objectMappingPerKey,
            Map<String, CollectionMapping<ObjectMapping>> objectCollectionMappingPerKey
    ) {
        this.valueMappingPerKey = Optional.ofNullable(valueMappingPerKey).orElse(new HashMap<>());
        this.valueCollectionMappingPerKey = Optional.ofNullable(valueCollectionMappingPerKey).orElse(new HashMap<>());
        this.objectMappingPerKey = Optional.ofNullable(objectMappingPerKey).orElse(new HashMap<>());
        this.objectCollectionMappingPerKey = Optional.ofNullable(objectCollectionMappingPerKey).orElse(new HashMap<>());
    }

    private final Map<String, ValueMapping> valueMappingPerKey;
    private final Map<String, CollectionMapping<ValueMapping>> valueCollectionMappingPerKey;
    private final Map<String, ObjectMapping> objectMappingPerKey;
    private final Map<String, CollectionMapping<ObjectMapping>> objectCollectionMappingPerKey;

}
