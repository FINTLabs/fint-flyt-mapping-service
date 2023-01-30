package no.fintlabs.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElementCollectionMapping {

    @Builder.Default
    private Collection<ElementMapping> elementMappings = new ArrayList<>();

    @Builder.Default
    private Collection<ElementsFromCollectionMapping> elementsFromCollectionMappings = new ArrayList<>();

}
