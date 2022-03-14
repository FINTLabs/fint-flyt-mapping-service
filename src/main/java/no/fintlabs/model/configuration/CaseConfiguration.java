package no.fintlabs.model.configuration;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CaseConfiguration {
    private CaseCreationStrategy caseCreationStrategy;
    private String caseNumber;
    private List<ConfigurationField> fields;
}