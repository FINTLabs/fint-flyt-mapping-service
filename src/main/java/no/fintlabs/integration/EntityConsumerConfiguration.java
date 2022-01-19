package no.fintlabs.integration;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fintlabs.kafka.consumer.EntityConsumerFactory;
import no.fintlabs.kafka.topic.DomainContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class EntityConsumerConfiguration {

    @Bean
    ConcurrentMessageListenerContainer<String, String> klassifikasjonssystemResourceEntityConsumer(EntityConsumerFactory entityConsumerFactory) {
        return entityConsumerFactory.createEntityConsumer(
                DomainContext.SKJEMA,
                "arkiv.noark.klassifikasjonssystem",
                KlassifikasjonssystemResource.class,
                resource -> Stream.concat(
                        Stream.of(resource.getSystemId().getIdentifikatorverdi()),
                        resource.getSelfLinks().stream().map(Link::getHref)
                ).collect(Collectors.toList()),
                true
        );
    }
}
