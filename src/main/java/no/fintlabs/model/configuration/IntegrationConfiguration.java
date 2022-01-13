package no.fintlabs.model.configuration;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IntegrationConfiguration {
    private String documentId;

    private LocalDateTime documentCreatedDate;

    private String id;
    private String name;
    private String description;
    private int version;

    private RecordConfiguration recordConfiguration;
    private DocumentConfiguration documentConfiguration;
    private CaseConfiguration caseConfiguration;
    private ApplicantConfiguration applicantConfiguration;

    public boolean isSameAs(String otherId) {
        return id.equals(otherId);
    }
}