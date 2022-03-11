package no.fintlabs.model.configuration;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApplicantConfiguration {
    private String organisationNumber;
    private String nationalIdentityNumber;
    private List<ConfigurationField> fields = new ArrayList<>();
}