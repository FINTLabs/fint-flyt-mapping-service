package no.fintlabs.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElementMapping {

    @Builder.Default
    private Map<String, ValueMapping> valueMappingPerKey = new HashMap<>();

    @Builder.Default
    private Map<String, ElementMapping> elementMappingPerKey = new HashMap<>();

    @Builder.Default
    private Map<String, ElementCollectionMapping> elementCollectionMappingPerKey = new HashMap<>();

}
