package no.fintlabs.model.mappedinstance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MappedInstanceField {

    public enum Type {
        STRING, URL, BOOLEAN
    }

    private String key;
    private Type type;
    private String value;

}
