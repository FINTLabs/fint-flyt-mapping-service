package no.fintlabs.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldConfiguration {

    public enum Type {
        STRING, URL, BOOLEAN, DYNAMIC_STRING
    }

    private String key;
    private Type type;
    private String value;
}
