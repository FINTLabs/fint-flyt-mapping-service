package no.fintlabs.model.mappedinstance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MappedInstanceElement {
    private String key;
    private Collection<MappedInstanceElement> elements;
    private Collection<MappedInstanceField> fields;
    private Collection<MappedInstanceCollectionField> collectionFields;
}
