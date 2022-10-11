package no.fintlabs.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldCollectionConfiguration {

    public enum Type {
        STRING, URL, BOOLEAN
    }

    private String key;
    private Type type;
    Collection<String> values;
}
