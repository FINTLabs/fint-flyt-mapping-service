package no.novari.flyt.mapping.intgrconf

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.web.client.RestClient

@Configuration
class IntgrConfClientConfiguration {
    // Calls are made from Kafka listener threads without an HTTP request, so the request-bound
    // default manager cannot be used.
    @Bean
    fun intgrConfAuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
        restClientBuilder: RestClient.Builder,
    ): OAuth2AuthorizedClientManager {
        // The default token client has no timeouts. Building it from Boot's builder applies spring.http.client.*.
        val tokenResponseClient = RestClientClientCredentialsTokenResponseClient()
        tokenResponseClient.setRestClient(
            restClientBuilder
                .clone()
                .messageConverters { converters ->
                    converters.clear()
                    converters.add(FormHttpMessageConverter())
                    converters.add(OAuth2AccessTokenResponseHttpMessageConverter())
                }.defaultStatusHandler(OAuth2ErrorResponseErrorHandler())
                .build(),
        )
        val manager =
            AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService)
        manager.setAuthorizedClientProvider(
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials { it.accessTokenResponseClient(tokenResponseClient) }
                .build(),
        )
        return manager
    }

    @Bean
    fun intgrConfRestClient(
        restClientBuilder: RestClient.Builder,
        @Qualifier("intgrConfAuthorizedClientManager") authorizedClientManager: OAuth2AuthorizedClientManager,
        authorizedClientService: OAuth2AuthorizedClientService,
        properties: IntgrConfClientProperties,
    ): RestClient {
        val interceptor = OAuth2ClientHttpRequestInterceptor(authorizedClientManager)
        interceptor.setClientRegistrationIdResolver { properties.clientRegistrationId }
        // Removes a rejected token (401/403) from the cache, so the Kafka retry fetches a new one.
        interceptor.setAuthorizationFailureHandler(
            OAuth2ClientHttpRequestInterceptor.authorizationFailureHandler(authorizedClientService),
        )
        return restClientBuilder
            .baseUrl("${properties.baseUrl}/api/intern-klient")
            .requestInterceptor(interceptor)
            .build()
    }
}
