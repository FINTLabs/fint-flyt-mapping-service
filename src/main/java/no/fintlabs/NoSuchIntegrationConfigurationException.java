package no.fintlabs;

public class NoSuchIntegrationConfigurationException extends RuntimeException {

    public NoSuchIntegrationConfigurationException(String sourceApplicationIntegrationId) {
        super("No configuration for integration with id=" + sourceApplicationIntegrationId);
    }
}
