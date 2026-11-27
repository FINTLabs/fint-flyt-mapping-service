package no.novari.flyt.mapping

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.mapping.exception.InstanceFieldNotFoundException
import no.novari.flyt.mapping.exception.ValueConvertingKeyNotFoundException
import no.novari.flyt.mapping.exception.ValueConvertingNotFoundException
import no.novari.flyt.mapping.intgrconf.IntgrConfClient
import no.novari.flyt.mapping.kafka.InstanceMappedEventProducerService
import no.novari.flyt.mapping.kafka.error.InstanceErrorEventProducerService
import no.novari.flyt.mapping.model.configuration.ObjectMapping
import no.novari.flyt.mapping.model.instance.InstanceObject
import no.novari.flyt.mapping.service.InstanceMappingService
import org.springframework.stereotype.Service

@Service
class InstanceProcessingService(
    private val intgrConfClient: IntgrConfClient,
    private val instanceMappedEventProducerService: InstanceMappedEventProducerService,
    private val instanceMappingService: InstanceMappingService,
    private val instanceErrorEventProducerService: InstanceErrorEventProducerService,
) {
    fun process(
        instance: InstanceObject,
        headers: InstanceFlowHeaders,
    ) {
        val configurationId = getConfigurationIdOrPublishError(headers) ?: return
        val objectMapping = getObjectMappingOrPublishError(headers, configurationId) ?: return

        val mappedInstance =
            try {
                instanceMappingService.toMappedInstanceObject(objectMapping, instance)
            } catch (exception: ValueConvertingNotFoundException) {
                instanceErrorEventProducerService.publishMissingValueConvertingErrorEvent(
                    headers,
                    exception.valueConvertingId,
                )
                return
            } catch (exception: ValueConvertingKeyNotFoundException) {
                instanceErrorEventProducerService.publishMissingValueConvertingKeyErrorEvent(
                    headers,
                    exception.valueConvertingId,
                    exception.valueConvertingKey,
                )
                return
            } catch (exception: InstanceFieldNotFoundException) {
                instanceErrorEventProducerService.publishInstanceFieldNotFoundErrorEvent(
                    headers,
                    exception.instanceFieldKey,
                )
                return
            }

        instanceMappedEventProducerService.publish(headers.copy(configurationId = configurationId), mappedInstance)
    }

    private fun getConfigurationIdOrPublishError(headers: InstanceFlowHeaders): Long? {
        val integrationId =
            headers.integrationId
                ?: run {
                    instanceErrorEventProducerService.publishConfigurationNotFoundErrorEvent(headers)
                    return null
                }
        return intgrConfClient.getActiveConfigurationId(integrationId) ?: run {
            instanceErrorEventProducerService.publishConfigurationNotFoundErrorEvent(headers)
            null
        }
    }

    private fun getObjectMappingOrPublishError(
        headers: InstanceFlowHeaders,
        configurationId: Long,
    ): ObjectMapping? {
        return intgrConfClient.getMapping(configurationId) ?: run {
            instanceErrorEventProducerService.publishConfigurationNotFoundErrorEvent(headers)
            null
        }
    }
}
