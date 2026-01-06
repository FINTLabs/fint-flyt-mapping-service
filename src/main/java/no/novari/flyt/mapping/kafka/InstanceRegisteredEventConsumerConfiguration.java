package no.novari.flyt.mapping.kafka;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord;
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowErrorHandlerConfiguration;
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowErrorHandlerFactory;
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService;
import no.novari.flyt.mapping.InstanceProcessingService;
import no.novari.flyt.mapping.exception.InstanceFieldNotFoundException;
import no.novari.flyt.mapping.exception.ValueConvertingKeyNotFoundException;
import no.novari.flyt.mapping.exception.ValueConvertingNotFoundException;
import no.novari.flyt.mapping.kafka.configuration.KafkaConsumerProperties;
import no.novari.flyt.mapping.kafka.error.InstanceMappingErrorEventProducerService;
import no.novari.flyt.mapping.model.instance.InstanceObject;
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.topic.name.EventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ListenerExecutionFailedException;

import java.util.List;

@Slf4j
@Configuration
public class InstanceRegisteredEventConsumerConfiguration {

    private final KafkaConsumerProperties consumerProperties;

    public InstanceRegisteredEventConsumerConfiguration(KafkaConsumerProperties consumerProperties) {
        this.consumerProperties = consumerProperties;
    }

    private ConcurrentMessageListenerContainer<String, InstanceObject> createConsumer(
            InstanceFlowListenerFactoryService factoryService,
            InstanceFlowErrorHandlerFactory errorHandlerFactory,
            InstanceMappingErrorEventProducerService errorEventProducerService,
            InstanceProcessingService processingService,
            String eventName
    ) {
        InstanceFlowErrorHandlerConfiguration<InstanceObject> errorHandlerConfig =
                createErrorHandlerConfig(errorEventProducerService);

        return factoryService.createRecordListenerContainerFactory(
                InstanceObject.class,
                processingService::process,
                ListenerConfiguration.stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build(),
                errorHandlerFactory.createErrorHandler(errorHandlerConfig)
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

    private InstanceFlowErrorHandlerConfiguration<InstanceObject> createErrorHandlerConfig(
            InstanceMappingErrorEventProducerService errorEventProducerService
    ) {
        KafkaConsumerProperties.BackoffProperties backoff = consumerProperties.getInstanceProcessingBackoff();
        return InstanceFlowErrorHandlerConfiguration.<InstanceObject>stepBuilder()
                .retryWithExponentialInterval(
                        backoff.getInitialInterval(),
                        backoff.getMultiplier(),
                        backoff.getMaxInterval(),
                        backoff.getMaxRetries()
                )
                .excludeExceptionsFromRetry(List.of(
                        ValueConvertingNotFoundException.class,
                        ValueConvertingKeyNotFoundException.class,
                        InstanceFieldNotFoundException.class
                ))
                .restartRetryOnExceptionChange()
                .recoverFailedRecords(new InstanceFlowErrorHandlerConfiguration.InstanceFlowRecoverer<>() {
                    @Override
                    public void recover(
                            InstanceFlowConsumerRecord<InstanceObject> record,
                            Exception exception
                    ) {
                        Exception unwrappedException = unwrapException(exception);
                        switch (unwrappedException) {
                            case ValueConvertingNotFoundException e: {
                                log.debug("ValueConvertingNotFoundException handled using publishMissingValueConvertingErrorEvent");
                                errorEventProducerService.publishMissingValueConvertingErrorEvent(
                                        record.getInstanceFlowHeaders(),
                                        e.getValueConvertingId()
                                );
                                return;
                            }
                            case ValueConvertingKeyNotFoundException e: {
                                log.debug("ValueConvertingKeyNotFoundException handled using publishMissingValueConvertingKeyErrorEvent");
                                errorEventProducerService.publishMissingValueConvertingKeyErrorEvent(
                                        record.getInstanceFlowHeaders(),
                                        e.getValueConvertingId(),
                                        e.getValueConvertingKey()
                                );
                                return;
                            }
                            case InstanceFieldNotFoundException e: {
                                log.debug("InstanceFieldNotFoundException handled using publishInstanceFieldNotFoundErrorEvent");
                                errorEventProducerService.publishInstanceFieldNotFoundErrorEvent(
                                        record.getInstanceFlowHeaders(),
                                        e.getInstanceFieldKey()
                                );
                                return;
                            }
                            default:
                                log.debug("{} handled using publishInstanceFieldNotFoundErrorEvent", unwrappedException.getClass());
                                errorEventProducerService.publishGeneralSystemErrorEvent(
                                        record.getInstanceFlowHeaders()
                                );
                        }
                    }

                    @Override
                    public void recoverForMissingInstanceFlowHeaders(
                            ConsumerRecord<String, InstanceObject> record,
                            Exception exception
                    ) {
                        log.warn("Missing headers on record, skipping", exception);
                    }
                })
                .skipRecordOnRecoveryFailure()
                .build();
    }

    private static Exception unwrapException(Exception exception) {
        Throwable current = exception;
        while (current instanceof ListenerExecutionFailedException
                && current.getCause() != null
                && current.getCause() != current) {
            current = current.getCause();
        }
        return current instanceof Exception ? (Exception) current : exception;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, InstanceObject> instanceRegisteredEventConsumer(
            InstanceFlowListenerFactoryService factoryService,
            InstanceFlowErrorHandlerFactory errorHandlerFactory,
            InstanceMappingErrorEventProducerService errorEventProducerService,
            InstanceProcessingService processingService
    ) {
        return createConsumer(
                factoryService,
                errorHandlerFactory,
                errorEventProducerService,
                processingService,
                "instance-registered"
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, InstanceObject> instanceRequestedForRetryEventConsumer(
            InstanceFlowListenerFactoryService factoryService,
            InstanceFlowErrorHandlerFactory errorHandlerFactory,
            InstanceMappingErrorEventProducerService errorEventProducerService,
            InstanceProcessingService processingService
    ) {
        return createConsumer(
                factoryService,
                errorHandlerFactory,
                errorEventProducerService,
                processingService,
                "instance-requested-for-retry"
        );
    }

}
