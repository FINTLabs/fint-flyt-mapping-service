package no.novari.flyt.mapping.intgrconf

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import java.time.Instant

class IntgrConfClientTest {
    private val baseUrl = "http://fint-flyt-intgr-conf-service:8080/afk-no/api/intern-klient"
    private val restClientBuilder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(restClientBuilder).build()
    private val client =
        IntgrConfClient(
            IntgrConfClientConfiguration().intgrConfRestClient(
                restClientBuilder,
                authorizedClientManager(),
                mock(),
                IntgrConfClientProperties().apply { baseUrl = "http://fint-flyt-intgr-conf-service:8080/afk-no" },
            ),
        )

    @AfterEach
    fun verify() {
        server.verify()
    }

    @Test
    fun `getActiveConfigurationId sends bearer token and returns id`() {
        server
            .expect(requestTo("$baseUrl/integrasjoner/1/aktiv-konfigurasjon-id"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
            .andRespond(withSuccess("2", MediaType.APPLICATION_JSON))

        assertThat(client.getActiveConfigurationId(1)).isEqualTo(2L)
    }

    @Test
    fun `getMapping returns mapping`() {
        server
            .expect(requestTo("$baseUrl/konfigurasjoner/2/mapping"))
            .andRespond(
                withSuccess(
                    """{"valueMappingPerKey":{"tittel":{"type":"STRING","mappingString":"Sak"}}}""",
                    MediaType.APPLICATION_JSON,
                ),
            )

        val mapping = client.getMapping(2)

        assertThat(mapping?.valueMappingPerKey?.get("tittel")?.mappingString).isEqualTo("Sak")
    }

    @Test
    fun `getValueConverting returns value converting`() {
        server
            .expect(requestTo("$baseUrl/value-convertings/3"))
            .andRespond(withSuccess("""{"id":3,"convertingMap":{"a":"b"}}""", MediaType.APPLICATION_JSON))

        val valueConverting = client.getValueConverting(3)

        assertThat(valueConverting?.convertingMap).containsEntry("a", "b")
    }

    @Test
    fun `404 returns null like the Kafka reply did`() {
        server
            .expect(requestTo("$baseUrl/integrasjoner/1/aktiv-konfigurasjon-id"))
            .andRespond(
                withStatus(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""{"status":404,"error":"Not Found"}"""),
            )

        assertThat(client.getActiveConfigurationId(1)).isNull()
    }

    @Test
    fun `server error is thrown so the Kafka consumer retries`() {
        server
            .expect(requestTo("$baseUrl/value-convertings/3"))
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE))

        assertThrows<HttpServerErrorException> { client.getValueConverting(3) }
    }

    private fun authorizedClientManager(): OAuth2AuthorizedClientManager {
        val registration =
            ClientRegistration
                .withRegistrationId("intgr-conf-service")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientId("client")
                .tokenUri("http://idp/token")
                .build()
        val token =
            OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
            )
        val authorizedClient = OAuth2AuthorizedClient(registration, "anonymousUser", token)
        return mock { on { authorize(any()) } doReturn authorizedClient }
    }
}
