package no.fintlabs.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationConfiguration {
    private Long id;
    private LocalDateTime createdDate;
    private String name;
    private String description;
    private String sourceApplicationId;
    private String sourceApplicationIntegrationId;
    private String orgId;
    private String destination;
    private int version;
    private boolean isPublished;
    private CaseConfiguration caseConfiguration;
    private RecordConfiguration recordConfiguration;
    private DocumentConfiguration documentConfiguration;
    private ApplicantConfiguration applicantConfiguration;
}
