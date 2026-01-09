package no.novari.flyt.mapping.kafka.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "novari.flyt.mapping-service.kafka.consumer")
public class KafkaConsumerProperties {

    private BackoffProperties instanceProcessingBackoff = new BackoffProperties();

    @Getter
    @Setter
    public static class BackoffProperties {
        private Duration initialInterval = Duration.ofSeconds(2);
        private double multiplier = 2.0;
        private Duration maxInterval = Duration.ofMinutes(1);
        private int maxRetries = 5;
    }
}
