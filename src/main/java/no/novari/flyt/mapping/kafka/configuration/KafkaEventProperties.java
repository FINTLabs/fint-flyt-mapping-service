package no.novari.flyt.mapping.kafka.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "novari.flyt.mapping-service.kafka.event")
public class KafkaEventProperties {

    private Duration instanceProcessingEventsRetentionTime;
}
