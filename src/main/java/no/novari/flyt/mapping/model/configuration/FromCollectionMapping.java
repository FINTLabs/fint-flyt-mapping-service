package no.novari.flyt.mapping.model.configuration;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class FromCollectionMapping<T> {

    public FromCollectionMapping(List<String> instanceCollectionReferencesOrdered, T elementMapping) {
        this.instanceCollectionReferencesOrdered = Optional.ofNullable(instanceCollectionReferencesOrdered).orElse(new ArrayList<>());
        this.elementMapping = elementMapping;
    }

    private final List<@NotBlank String> instanceCollectionReferencesOrdered;

    private final T elementMapping;

}
