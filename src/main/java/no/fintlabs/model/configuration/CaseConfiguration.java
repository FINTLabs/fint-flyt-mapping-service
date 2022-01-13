package no.fintlabs.model.configuration;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CaseConfiguration {
    private CaseCreationStrategy caseCreationStrategy;
    private List<Field> fields = new ArrayList<>();
}