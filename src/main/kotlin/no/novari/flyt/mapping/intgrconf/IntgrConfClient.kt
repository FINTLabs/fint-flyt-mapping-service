package no.novari.flyt.mapping.intgrconf

import no.novari.flyt.mapping.model.configuration.ObjectMapping
import no.novari.flyt.mapping.model.valueconverting.ValueConverting
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

/**
 * Client for the internal API in fint-flyt-intgr-conf-service.
 *
 * Returns `null` on 404, matching the `null` replies of the Kafka request/reply topics it replaces.
 * Other errors are thrown and retried by the error handler of the Kafka consumer.
 */
@Service
class IntgrConfClient(
    @param:Qualifier("intgrConfRestClient")
    private val restClient: RestClient,
) {
    fun getActiveConfigurationId(integrationId: Long): Long? =
        get("/integrasjoner/{integrationId}/aktiv-konfigurasjon-id", integrationId)

    fun getMapping(configurationId: Long): ObjectMapping? =
        get("/konfigurasjoner/{configurationId}/mapping", configurationId)

    fun getValueConverting(valueConvertingId: Long): ValueConverting? =
        get("/value-convertings/{valueConvertingId}", valueConvertingId)

    private inline fun <reified T : Any> get(
        uri: String,
        id: Long,
    ): T? =
        try {
            restClient
                .get()
                .uri(uri, id)
                .retrieve()
                .body<T>()
        } catch (_: HttpClientErrorException.NotFound) {
            null
        }
}
