package no.novari.flyt.mapping.kafka

import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowErrorHandlerConfiguration
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowErrorHandlerFactory
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService
import no.novari.flyt.mapping.InstanceProcessingService
import no.novari.flyt.mapping.kafka.configuration.KafkaConsumerProperties
import no.novari.flyt.mapping.kafka.error.InstanceErrorEventProducerService
import no.novari.flyt.mapping.model.instance.InstanceObject
import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.topic.name.EventTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ListenerExecutionFailedException

@Configuration
class InstanceRegisteredEventConsumerConfiguration(
    private val consumerProperties: KafkaConsumerProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun instanceRegisteredEventConsumer(
        factoryService: InstanceFlowListenerFactoryService,
        errorHandlerFactory: InstanceFlowErrorHandlerFactory,
        errorEventProducerService: InstanceErrorEventProducerService,
        processingService: InstanceProcessingService,
    ): ConcurrentMessageListenerContainer<String, InstanceObject> {
        return createConsumer(
            factoryService,
            errorHandlerFactory,
            errorEventProducerService,
            processingService,
            "instance-registered",
        )
    }

    @Bean
    fun instanceRequestedForRetryEventConsumer(
        factoryService: InstanceFlowListenerFactoryService,
        errorHandlerFactory: InstanceFlowErrorHandlerFactory,
        errorEventProducerService: InstanceErrorEventProducerService,
        processingService: InstanceProcessingService,
    ): ConcurrentMessageListenerContainer<String, InstanceObject> {
        return createConsumer(
            factoryService,
            errorHandlerFactory,
            errorEventProducerService,
            processingService,
            "instance-requested-for-retry",
        )
    }

    private fun createConsumer(
        factoryService: InstanceFlowListenerFactoryService,
        errorHandlerFactory: InstanceFlowErrorHandlerFactory,
        errorEventProducerService: InstanceErrorEventProducerService,
        processingService: InstanceProcessingService,
        eventName: String,
    ): ConcurrentMessageListenerContainer<String, InstanceObject> {
        val errorHandlerConfig = createErrorHandlerConfig(errorEventProducerService)

        return factoryService
            .createRecordListenerContainerFactory(
                InstanceObject::class.java,
                processingService::process,
                ListenerConfiguration
                    .stepBuilder()
                    .groupIdApplicationDefault()
                    .maxPollRecordsKafkaDefault()
                    .maxPollIntervalKafkaDefault()
                    .continueFromPreviousOffsetOnAssignment()
                    .build(),
                errorHandlerFactory.createErrorHandler(errorHandlerConfig),
            ).createContainer(
                EventTopicNameParameters
                    .builder()
                    .topicNamePrefixParameters(
                        TopicNamePrefixParameters
                            .stepBuilder()
                            .orgIdApplicationDefault()
                            .domainContextApplicationDefault()
                            .build(),
                    ).eventName(eventName)
                    .build(),
            )
    }

    private fun createErrorHandlerConfig(
        errorEventProducerService: InstanceErrorEventProducerService,
    ): InstanceFlowErrorHandlerConfiguration<InstanceObject> {
        val backoff = consumerProperties.instanceProcessingBackoff
        return InstanceFlowErrorHandlerConfiguration
            .stepBuilder<InstanceObject>()
            .retryWithExponentialInterval(
                backoff.initialInterval,
                backoff.multiplier,
                backoff.maxInterval,
                backoff.maxRetries,
            ).useDefaultRetryClassification()
            .restartRetryOnExceptionChange()
            .recoverFailedRecords(
                object : InstanceFlowErrorHandlerConfiguration.InstanceFlowRecoverer<InstanceObject> {
                    override fun recover(
                        instanceFlowConsumerRecord: InstanceFlowConsumerRecord<InstanceObject>,
                        exception: Exception,
                    ) {
                        val unwrappedException = unwrapException(exception)
                        log.debug("{} handled using publishGeneralSystemErrorEvent", unwrappedException.javaClass)
                        errorEventProducerService.publishGeneralSystemErrorEvent(
                            instanceFlowConsumerRecord.instanceFlowHeaders,
                        )
                    }

                    override fun recoverForMissingInstanceFlowHeaders(
                        consumerRecord: ConsumerRecord<String, InstanceObject>,
                        exception: Exception,
                    ) {
                        log.warn("Missing headers on record, skipping", exception)
                    }
                },
            ).skipRecordOnRecoveryFailure()
            .build()
    }

    private fun unwrapException(exception: Exception): Exception {
        var current: Throwable = exception
        while (
            current is ListenerExecutionFailedException &&
            current.cause != null &&
            current.cause !== current
        ) {
            current = current.cause!!
        }

        return current as? Exception ?: exception
    }
}
