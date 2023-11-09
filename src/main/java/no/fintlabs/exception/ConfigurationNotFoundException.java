package no.fintlabs.exception;

public class ConfigurationNotFoundException extends RuntimeException {

    public static ConfigurationNotFoundException fromIntegrationId(Long integrationId) {
        return new ConfigurationNotFoundException("Could not find active configuration id for integration with id=" + integrationId);
    }

    public static ConfigurationNotFoundException fromConfigurationId(Long configurationId) {
        return new ConfigurationNotFoundException("Could not find configuration with id=" + configurationId);
    }

    public ConfigurationNotFoundException(String message) {
        super(message);
    }

}
