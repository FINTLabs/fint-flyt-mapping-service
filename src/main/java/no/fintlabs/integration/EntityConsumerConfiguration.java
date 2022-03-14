package no.fintlabs.integration;

import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheManager;
import no.fintlabs.kafka.entity.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.FintKafkaEntityConsumerFactory;
import no.fintlabs.links.ResourceLinkUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class EntityConsumerConfiguration {

    @Value("${fint.org-id}")
    private String orgId;

    private final FintCacheManager fintCacheManager;
    private final FintKafkaEntityConsumerFactory entityConsumerFactory;

    public EntityConsumerConfiguration(FintCacheManager fintCacheManager, FintKafkaEntityConsumerFactory entityConsumerFactory) {
        this.fintCacheManager = fintCacheManager;
        this.entityConsumerFactory = entityConsumerFactory;
    }

    private <T extends FintLinks> ConcurrentMessageListenerContainer<String, T> createCacheConsumer(
            String resourceReference,
            Class<T> resourceClass
    ) {
        FintCache<String, T> cache = fintCacheManager.createCache(
                resourceReference,
                String.class,
                resourceClass
        );
        return entityConsumerFactory.createConsumer(
                EntityTopicNameParameters.builder()
                        .orgId(orgId)
                        .domainContext("skjema")
                        .resource(resourceReference)
                        .build(),
                resourceClass,
                consumerRecord -> cache.put(
                        ResourceLinkUtil.getSelfLinks(consumerRecord.value()),
                        consumerRecord.value()
                ),
                new CommonLoggingErrorHandler()
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceEntityConsumer() {
        return createCacheConsumer("arkiv.noark.klassifikasjonssystem", KlassifikasjonssystemResource.class);
    }

}
