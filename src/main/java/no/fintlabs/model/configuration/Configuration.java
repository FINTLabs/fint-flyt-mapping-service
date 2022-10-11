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
public class Configuration {
    private Long integrationId;
    private Long integrationMetadataId;
    private Integer version;
    private boolean completed;
    private String comment;
    private Collection<ConfigurationElement> elements = new ArrayList<>();
}
