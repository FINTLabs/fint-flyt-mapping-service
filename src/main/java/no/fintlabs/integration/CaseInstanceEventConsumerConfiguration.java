package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.InstanceProcessingService;
import no.fintlabs.kafka.event.EventTopicNameParameters;
import no.fintlabs.kafka.event.FintKafkaEventConsumerFactory;
import no.fintlabs.model.instance.Instance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
public class CaseInstanceEventConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, Instance> caseInstanceConsumer(
            @Value("${fint.org-id}") String orgId,
            FintKafkaEventConsumerFactory fintKafkaEventConsumerFactory,
            InstanceProcessingService instanceProcessingService
    ) {
        // TODO: 05/01/2022 :
        //  1. Get skjemaId from instance
        //  2. Request configuration by skjemaId from SkjemaConfigurationRequestService
        //  3. Map instance with configuration
        //  4. Produce result to kafka through CaseEventProducerService
        return fintKafkaEventConsumerFactory.createConsumer(
                EventTopicNameParameters.builder()
                        .orgId(orgId)
                        .domainContext("skjema")
                        .eventName("new-instance")
                        .build(),
                Instance.class,
                consumerRecord -> instanceProcessingService.process(consumerRecord.value()),
                new CommonLoggingErrorHandler()
        );
    }

}
