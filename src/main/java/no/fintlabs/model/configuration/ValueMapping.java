package no.fintlabs.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValueMapping {

    public enum Type {
        STRING, URL, BOOLEAN, DYNAMIC_STRING, FILE
    }

    private Type type;
    private String mappingString;
}
