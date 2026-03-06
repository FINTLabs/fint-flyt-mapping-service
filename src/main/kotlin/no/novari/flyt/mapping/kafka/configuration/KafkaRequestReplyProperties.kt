package no.novari.flyt.mapping.kafka.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "novari.flyt.mapping-service.kafka.request-reply")
class KafkaRequestReplyProperties {
    var replyTimeout: Duration = Duration.ofSeconds(5)
    var replyRetentionTime: Duration = Duration.ofMinutes(5)
}
