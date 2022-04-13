package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.InstanceProcessingErrorHandlerService;
import no.fintlabs.InstanceProcessingService;
import no.fintlabs.flyt.kafka.event.FlytEventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.model.instance.Instance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
public class CaseInstanceEventConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, Instance> caseInstanceConsumer(
            FlytEventConsumerFactoryService flytEventConsumerFactoryService,
            InstanceProcessingService instanceProcessingService,
            InstanceProcessingErrorHandlerService instanceProcessingErrorHandlerService
    ) {
        return flytEventConsumerFactoryService.createInstanceFlowFactory(
                Instance.class,
                instanceProcessingService::process,
                instanceProcessingErrorHandlerService,
                false
        ).createContainer(
                EventTopicNameParameters.builder()
                        .eventName("new-instance")
                        .build()
        );
    }

}
