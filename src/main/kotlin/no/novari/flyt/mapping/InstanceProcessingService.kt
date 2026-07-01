package no.novari.flyt.mapping

import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord
import no.novari.flyt.mapping.exception.InstanceFieldNotFoundException
import no.novari.flyt.mapping.exception.ValueConvertingKeyNotFoundException
import no.novari.flyt.mapping.exception.ValueConvertingNotFoundException
import no.novari.flyt.mapping.kafka.InstanceMappedEventProducerService
import no.novari.flyt.mapping.kafka.configuration.ActiveConfigurationIdRequestProducerService
import no.novari.flyt.mapping.kafka.configuration.ConfigurationMappingRequestProducerService
import no.novari.flyt.mapping.kafka.error.InstanceErrorEventProducerService
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
    private val instanceErrorEventProducerService: InstanceErrorEventProducerService,
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
                instanceErrorEventProducerService.publishMissingValueConvertingErrorEvent(
                    flytConsumerRecord.instanceFlowHeaders,
                    exception.valueConvertingId,
                )
                return
            } catch (exception: ValueConvertingKeyNotFoundException) {
                instanceErrorEventProducerService.publishMissingValueConvertingKeyErrorEvent(
                    flytConsumerRecord.instanceFlowHeaders,
                    exception.valueConvertingId,
                    exception.valueConvertingKey,
                )
                return
            } catch (exception: InstanceFieldNotFoundException) {
                instanceErrorEventProducerService.publishInstanceFieldNotFoundErrorEvent(
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
        val integrationId =
            record.instanceFlowHeaders.integrationId
                ?: run {
                    instanceErrorEventProducerService.publishConfigurationNotFoundErrorEvent(
                        record.instanceFlowHeaders,
                    )
                    return null
                }
        return activeConfigurationIdRequestProducerService.get(integrationId) ?: run {
            instanceErrorEventProducerService.publishConfigurationNotFoundErrorEvent(record.instanceFlowHeaders)
            null
        }
    }

    private fun getObjectMappingOrPublishError(
        record: InstanceFlowConsumerRecord<InstanceObject>,
        configurationId: Long,
    ): ObjectMapping? {
        return configurationMappingRequestProducerService.get(configurationId) ?: run {
            instanceErrorEventProducerService.publishConfigurationNotFoundErrorEvent(record.instanceFlowHeaders)
            null
        }
    }
}
