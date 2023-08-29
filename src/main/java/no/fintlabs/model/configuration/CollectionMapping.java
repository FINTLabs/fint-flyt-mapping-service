package no.fintlabs.model.configuration;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class CollectionMapping<T> {

    public CollectionMapping(
            Collection<T> elementMappings,
            Collection<FromCollectionMapping<T>> fromCollectionMappings
    ) {
        this.elementMappings = Optional.ofNullable(elementMappings).orElse(new ArrayList<>());
        this.fromCollectionMappings = Optional.ofNullable(fromCollectionMappings).orElse(new ArrayList<>());
    }

    private final Collection<T> elementMappings;

    private final Collection<FromCollectionMapping<T>> fromCollectionMappings;

    @Override
    public String toString() {
        return "Sensitive data omitted";
    }

}
