package no.fintlabs.model.instance;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Instance {
    private String formId;
    private List<Document> documents;
    private String id;
    private Map<String, InstanceField> fields;
    private String uri;
}