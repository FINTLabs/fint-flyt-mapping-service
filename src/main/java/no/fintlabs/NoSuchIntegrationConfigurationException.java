package no.fintlabs;

public class NoSuchIntegrationConfigurationException extends RuntimeException {

    public NoSuchIntegrationConfigurationException(String integrationId) {
        super("No configuration for integration with id=" + integrationId);
    }
}
