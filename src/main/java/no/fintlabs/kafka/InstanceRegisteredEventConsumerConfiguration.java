package no.fintlabs.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.InstanceProcessingService;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.model.instance.InstanceObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
public class InstanceRegisteredEventConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, InstanceObject> instanceRegisteredEventConsumer(
            InstanceFlowEventConsumerFactoryService instanceFlowEventConsumerFactoryService,
            InstanceProcessingService instanceProcessingService
    ) {
        return instanceFlowEventConsumerFactoryService.createRecordFactory(
                InstanceObject.class,
                instanceProcessingService::process
        ).createContainer(
                EventTopicNameParameters.builder()
                        .eventName("instance-registered")
                        .build()
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, InstanceObject> instanceRequestedForRetryEventConsumer(
            InstanceFlowEventConsumerFactoryService instanceFlowEventConsumerFactoryService,
            InstanceProcessingService instanceProcessingService
    ) {
        return instanceFlowEventConsumerFactoryService.createRecordFactory(
                InstanceObject.class,
                instanceProcessingService::process
        ).createContainer(
                EventTopicNameParameters.builder()
                        .eventName("instance-requested-for-retry")
                        .build()
        );
    }

}
