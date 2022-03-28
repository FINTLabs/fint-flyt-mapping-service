package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.InstanceProcessingErrorHandlerService;
import no.fintlabs.InstanceProcessingService;
import no.fintlabs.kafka.event.EventTopicNameParameters;
import no.fintlabs.kafka.event.FintKafkaEventConsumerFactory;
import no.fintlabs.model.instance.Instance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
public class CaseInstanceEventConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, Instance> caseInstanceConsumer(
            @Value("${fint.org-id}") String orgId,
            FintKafkaEventConsumerFactory fintKafkaEventConsumerFactory,
            InstanceProcessingService instanceProcessingService,
            InstanceProcessingErrorHandlerService instanceProcessingErrorHandlerService
    ) {
        return fintKafkaEventConsumerFactory.createConsumer(
                EventTopicNameParameters.builder()
                        .orgId(orgId)
                        .domainContext("skjema")
                        .eventName("new-instance")
                        .build(),
                Instance.class,
                // TODO: 28/03/2022 Pass skjema headers to use in mapping and to include in the outgoing producer record
                consumerRecord -> instanceProcessingService.process(consumerRecord.value()),
                instanceProcessingErrorHandlerService
        );
    }

}
