package no.novari.flyt.mapping.intgrconf

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "novari.flyt.mapping-service.intgr-conf-client")
class IntgrConfClientProperties {
    lateinit var baseUrl: String
    var clientRegistrationId: String = "intgr-conf-service"
}
