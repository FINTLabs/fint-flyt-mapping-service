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
public class ConfigurationElement {
    private String key;
    private Collection<ConfigurationElement> elements;
    private Collection<FieldConfiguration> fieldConfigurations;
    private Collection<CollectionFieldConfiguration> collectionFieldConfigurations;
}
