package no.novari.flyt.mapping.kafka.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "novari.flyt.mapping-service.kafka.event")
class KafkaEventProperties {
    var instanceProcessingEventsRetentionTime: Duration = Duration.ofDays(4)
}
