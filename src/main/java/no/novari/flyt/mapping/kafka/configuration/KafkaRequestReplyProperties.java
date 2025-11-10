package no.novari.flyt.mapping.kafka.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fint.flyt.mapping-service.kafka.request-reply")
public class KafkaRequestReplyProperties {

    private Duration replyTimeout;
    private Duration replyRetentionTime;
}
