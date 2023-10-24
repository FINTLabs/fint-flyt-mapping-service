package no.fintlabs.model.instance;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class InstanceObject {

    @Builder.Default
    private Map<String, String> valuePerKey = new HashMap<>();

    @Builder.Default
    private Map<String, Collection<InstanceObject>> objectCollectionPerKey = new HashMap<>();

}
