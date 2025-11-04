package no.fintlabs.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.InstanceProcessingService;
import no.fintlabs.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService;
import no.fintlabs.kafka.consuming.ErrorHandlerConfiguration;
import no.fintlabs.kafka.consuming.ErrorHandlerFactory;
import no.fintlabs.kafka.consuming.ListenerConfiguration;
import no.fintlabs.kafka.topic.name.EventTopicNameParameters;
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters;
import no.fintlabs.model.instance.InstanceObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.util.backoff.FixedBackOff;

import java.time.Duration;

@Slf4j
@Configuration
public class InstanceRegisteredEventConsumerConfiguration {

    private ConcurrentMessageListenerContainer<String, InstanceObject> createConsumer(
            InstanceFlowListenerFactoryService instanceFlowListenerFactoryService,
            ErrorHandlerFactory errorHandlerFactory,
            InstanceProcessingService processingService,
            String eventName
    ) {
        return instanceFlowListenerFactoryService.createRecordListenerContainerFactory(
                InstanceObject.class,
                processingService::process,
                ListenerConfiguration.stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .seekToBeginningOnAssignment()
                        .build(),
                errorHandlerFactory.createErrorHandler(
                        ErrorHandlerConfiguration
                                .stepBuilder()
                                .retryWithFixedInterval(Duration.ofMillis(FixedBackOff.DEFAULT_INTERVAL), 0)
                                .skipFailedRecords()
                                .build()
                )
        ).createContainer(EventTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .builder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .eventName(eventName)
                .build()
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, InstanceObject> instanceRegisteredEventConsumer(
            InstanceFlowListenerFactoryService instanceFlowListenerFactoryService,
            ErrorHandlerFactory errorHandlerFactory,
            InstanceProcessingService processingService
    ) {
        return createConsumer(
                instanceFlowListenerFactoryService,
                errorHandlerFactory,
                processingService,
                "instance-registered");
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, InstanceObject> instanceRequestedForRetryEventConsumer(
            InstanceFlowListenerFactoryService factoryService,
            ErrorHandlerFactory errorHandlerFactory,
            InstanceProcessingService processingService
    ) {
        return createConsumer(
                factoryService,
                errorHandlerFactory,
                processingService,
                "instance-requested-for-retry");
    }

}
