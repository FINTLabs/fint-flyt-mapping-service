package no.novari.flyt.mapping.kafka;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService;
import no.novari.flyt.mapping.InstanceProcessingService;
import no.novari.flyt.mapping.model.instance.InstanceObject;
import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import no.novari.kafka.consuming.ErrorHandlerFactory;
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.topic.name.EventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
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
                                .useDefaultRetryClassification()
                                .restartRetryOnExceptionChange()
                                .skipFailedRecords()
                                .build()
                )
        ).createContainer(EventTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
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
