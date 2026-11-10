package no.novari.flyt.mapping.intgrconf

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient

@Configuration
class IntgrConfClientConfiguration {
    // Calls are made from Kafka listener threads without an HTTP request, so the request-bound
    // default manager cannot be used.
    @Bean
    fun intgrConfAuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager =
        AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientService,
        )

    @Bean
    fun intgrConfRestClient(
        restClientBuilder: RestClient.Builder,
        @Qualifier("intgrConfAuthorizedClientManager") authorizedClientManager: OAuth2AuthorizedClientManager,
        properties: IntgrConfClientProperties,
    ): RestClient {
        val interceptor = OAuth2ClientHttpRequestInterceptor(authorizedClientManager)
        interceptor.setClientRegistrationIdResolver { properties.clientRegistrationId }
        return restClientBuilder
            .baseUrl("${properties.baseUrl}/api/intern-klient")
            .requestInterceptor(interceptor)
            .build()
    }
}
