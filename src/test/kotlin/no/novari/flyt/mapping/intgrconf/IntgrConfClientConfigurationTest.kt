package no.novari.flyt.mapping.intgrconf

import com.sun.net.httpserver.HttpServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import java.net.InetSocketAddress
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

class IntgrConfClientConfigurationTest {
    private val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
    private val tokenRequests = AtomicInteger()
    private val clientRegistrationRepository by lazy { InMemoryClientRegistrationRepository(registration()) }
    private val authorizedClientService by lazy { InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository) }

    @AfterEach
    fun stop() {
        server.stop(0)
    }

    @Test
    fun `token is fetched with client_credentials`() {
        tokenEndpoint(delay = Duration.ZERO)

        val authorizedClient = authorize()

        assertThat(authorizedClient?.accessToken?.tokenValue).isEqualTo("token")
    }

    @Test
    fun `token request uses the read timeout of the RestClient builder`() {
        tokenEndpoint(delay = Duration.ofSeconds(3))

        val elapsed = measureTimeMillis { assertThrows<Exception> { authorize() } }

        assertThat(elapsed).isLessThan(2_000)
    }

    @Test
    fun `rejected token is removed so the next call fetches a new one`() {
        server.createContext("/api/intern-klient/integrasjoner/1/aktiv-konfigurasjon-id") { exchange ->
            exchange.sendResponseHeaders(401, -1)
            exchange.close()
        }
        tokenEndpoint(delay = Duration.ZERO)
        val restClient =
            IntgrConfClientConfiguration().intgrConfRestClient(
                RestClient.builder(),
                manager(),
                authorizedClientService,
                IntgrConfClientProperties().apply { baseUrl = "http://localhost:${server.address.port}" },
            )

        repeat(2) {
            assertThrows<HttpClientErrorException.Unauthorized> {
                restClient
                    .get()
                    .uri("/integrasjoner/1/aktiv-konfigurasjon-id")
                    .retrieve()
                    .toBodilessEntity()
            }
        }

        assertThat(tokenRequests.get()).isEqualTo(2)
    }

    private fun tokenEndpoint(delay: Duration) {
        server.createContext("/token") { exchange ->
            tokenRequests.incrementAndGet()
            Thread.sleep(delay.toMillis())
            val body = """{"access_token":"token","token_type":"Bearer","expires_in":300}""".toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
    }

    private fun manager() =
        IntgrConfClientConfiguration().intgrConfAuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientService,
            RestClient.builder().requestFactory(
                JdkClientHttpRequestFactory().apply { setReadTimeout(Duration.ofMillis(300)) },
            ),
        )

    private fun authorize() =
        manager()
            .authorize(
                OAuth2AuthorizeRequest
                    .withClientRegistrationId("intgr-conf-service")
                    .principal(
                        AnonymousAuthenticationToken(
                            "anonymous",
                            "anonymousUser",
                            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"),
                        ),
                    ).build(),
            )

    private fun registration() =
        ClientRegistration
            .withRegistrationId("intgr-conf-service")
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .clientId("client")
            .clientSecret("secret")
            .tokenUri("http://localhost:${server.address.port}/token")
            .build()
}
