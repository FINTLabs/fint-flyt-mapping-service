package no.novari.flyt.mapping

import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord
import no.novari.flyt.mapping.exception.InstanceFieldNotFoundException
import no.novari.flyt.mapping.exception.ValueConvertingKeyNotFoundException
import no.novari.flyt.mapping.exception.ValueConvertingNotFoundException
import no.novari.flyt.mapping.kafka.InstanceMappedEventProducerService
import no.novari.flyt.mapping.kafka.configuration.ActiveConfigurationIdRequestProducerService
import no.novari.flyt.mapping.kafka.configuration.ConfigurationMappingRequestProducerService
import no.novari.flyt.mapping.kafka.error.InstanceMappingErrorEventProducerService
import no.novari.flyt.mapping.model.configuration.ObjectMapping
import no.novari.flyt.mapping.model.instance.InstanceObject
import no.novari.flyt.mapping.service.InstanceMappingService
import org.springframework.stereotype.Service

@Service
class InstanceProcessingService(
    private val configurationMappingRequestProducerService: ConfigurationMappingRequestProducerService,
    private val instanceMappedEventProducerService: InstanceMappedEventProducerService,
    private val activeConfigurationIdRequestProducerService: ActiveConfigurationIdRequestProducerService,
    private val instanceMappingService: InstanceMappingService,
    private val instanceMappingErrorEventProducerService: InstanceMappingErrorEventProducerService,
) {
    fun process(flytConsumerRecord: InstanceFlowConsumerRecord<InstanceObject>) {
        val configurationId = getConfigurationIdOrPublishError(flytConsumerRecord) ?: return
        val objectMapping = getObjectMappingOrPublishError(flytConsumerRecord, configurationId) ?: return

        val mappedInstance =
            try {
                instanceMappingService.toMappedInstanceObject(
                    objectMapping,
                    flytConsumerRecord.consumerRecord.value(),
                )
            } catch (exception: ValueConvertingNotFoundException) {
                instanceMappingErrorEventProducerService.publishMissingValueConvertingErrorEvent(
                    flytConsumerRecord.instanceFlowHeaders,
                    exception.valueConvertingId,
                )
                return
            } catch (exception: ValueConvertingKeyNotFoundException) {
                instanceMappingErrorEventProducerService.publishMissingValueConvertingKeyErrorEvent(
                    flytConsumerRecord.instanceFlowHeaders,
                    exception.valueConvertingId,
                    exception.valueConvertingKey,
                )
                return
            } catch (exception: InstanceFieldNotFoundException) {
                instanceMappingErrorEventProducerService.publishInstanceFieldNotFoundErrorEvent(
                    flytConsumerRecord.instanceFlowHeaders,
                    exception.instanceFieldKey,
                )
                return
            }

        val instanceFlowHeaders =
            flytConsumerRecord.instanceFlowHeaders
                .toBuilder()
                .configurationId(configurationId)
                .build()

        instanceMappedEventProducerService.publish(instanceFlowHeaders, mappedInstance)
    }

    private fun getConfigurationIdOrPublishError(record: InstanceFlowConsumerRecord<InstanceObject>): Long? {
        return activeConfigurationIdRequestProducerService.get(
            record.instanceFlowHeaders.integrationId,
        ) ?: run {
            instanceMappingErrorEventProducerService.publishConfigurationNotFoundErrorEvent(record.instanceFlowHeaders)
            null
        }
    }

    private fun getObjectMappingOrPublishError(
        record: InstanceFlowConsumerRecord<InstanceObject>,
        configurationId: Long,
    ): ObjectMapping? {
        return configurationMappingRequestProducerService.get(configurationId) ?: run {
            instanceMappingErrorEventProducerService.publishConfigurationNotFoundErrorEvent(record.instanceFlowHeaders)
            null
        }
    }
}
