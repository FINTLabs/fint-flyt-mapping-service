package no.fintlabs.integration;

import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheManager;
import no.fintlabs.flyt.kafka.entity.FlytEntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.links.ResourceLinkUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class EntityConsumerConfiguration {

    private final FintCacheManager fintCacheManager;
    private final FlytEntityConsumerFactoryService flytEntityConsumerFactoryService;

    public EntityConsumerConfiguration(FintCacheManager fintCacheManager, FlytEntityConsumerFactoryService flytEntityConsumerFactoryService) {
        this.fintCacheManager = fintCacheManager;
        this.flytEntityConsumerFactoryService = flytEntityConsumerFactoryService;
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
        return flytEntityConsumerFactoryService.createFactory(
                resourceClass,
                consumerRecord -> cache.put(
                        ResourceLinkUtil.getSelfLinks(consumerRecord.value()),
                        consumerRecord.value()
                ),
                new CommonLoggingErrorHandler()
        ).createContainer(
                EntityTopicNameParameters.builder()
                        .resource(resourceReference)
                        .build()
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceEntityConsumer() {
        return createCacheConsumer("arkiv.noark.klassifikasjonssystem", KlassifikasjonssystemResource.class);
    }

}
