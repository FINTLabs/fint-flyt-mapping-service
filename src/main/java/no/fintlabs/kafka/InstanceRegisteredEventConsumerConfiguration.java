package no.fintlabs.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.InstanceMappingErrorHandlerService;
import no.fintlabs.InstanceProcessingService;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.model.instance.InstanceElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
public class InstanceRegisteredEventConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, InstanceElement> instanceRegisteredEventConsumer(
            InstanceFlowEventConsumerFactoryService instanceFlowEventConsumerFactoryService,
            InstanceProcessingService instanceProcessingService,
            InstanceMappingErrorHandlerService instanceMappingErrorHandlerService
    ) {
        return instanceFlowEventConsumerFactoryService.createFactory(
                InstanceElement.class,
                instanceProcessingService::process,
                instanceMappingErrorHandlerService,
                false
        ).createContainer(
                EventTopicNameParameters.builder()
                        .eventName("instance-registered")
                        .build()
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, InstanceElement> instanceRequestedForRetryEventConsumer(
            InstanceFlowEventConsumerFactoryService instanceFlowEventConsumerFactoryService,
            InstanceProcessingService instanceProcessingService,
            InstanceMappingErrorHandlerService instanceMappingErrorHandlerService
    ) {
        return instanceFlowEventConsumerFactoryService.createFactory(
                InstanceElement.class,
                instanceProcessingService::process,
                instanceMappingErrorHandlerService,
                false
        ).createContainer(
                EventTopicNameParameters.builder()
                        .eventName("instance-requested-for-retry")
                        .build()
        );
    }

}
