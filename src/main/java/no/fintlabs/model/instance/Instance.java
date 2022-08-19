package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Instance {
    private String sourceApplicationInstanceUri;
    private Map<String, InstanceField> fieldPerKey;
    private List<Document> documents;
}
