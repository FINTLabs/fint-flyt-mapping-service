package no.fintlabs.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValueBuilder {
    private String value;
    private List<Property> properties;

    public ValueBuilder(String value, Property... properties) {
        this.value = value;
        this.properties = List.of(properties);
    }
}