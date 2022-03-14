package no.fintlabs.model.configuration;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IntegrationConfiguration {
    private String documentId;

    private LocalDateTime documentCreatedDate;

    private String integrationId;
    private String name;
    private String description;
    private String sourceApplication;
    private String sourceApplicationIntegrationId;
    private String orgId;
    private String destination;
    private int version;
    private boolean isPublished;

    private RecordConfiguration recordConfiguration;
    private DocumentConfiguration documentConfiguration;
    private CaseConfiguration caseConfiguration;
    private ApplicantConfiguration applicantConfiguration;

    public boolean isSameAs(String otherId) {
        return integrationId.equals(otherId);
    }
}