package no.novari.flyt.mapping.kafka.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "novari.flyt.mapping-service.kafka.consumer")
class KafkaConsumerProperties {
    var instanceProcessingBackoff: BackoffProperties = BackoffProperties()

    class BackoffProperties {
        var initialInterval: Duration = Duration.ofSeconds(2)
        var multiplier: Double = 2.0
        var maxInterval: Duration = Duration.ofMinutes(1)
        var maxRetries: Int = 5
    }
}
