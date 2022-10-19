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
public class MappedInstanceCollectionField {

    public enum Type {
        STRING, URL
    }

    private String key;
    private Type type;
    private Collection<String> values;

}
