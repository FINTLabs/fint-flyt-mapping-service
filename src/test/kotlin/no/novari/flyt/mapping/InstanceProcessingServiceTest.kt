package no.novari.flyt.mapping

import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
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
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class InstanceProcessingServiceTest {
    @InjectMocks
    private lateinit var instanceProcessingService: InstanceProcessingService

    @Mock
    private lateinit var instanceMappedEventProducerService: InstanceMappedEventProducerService

    @Mock
    private lateinit var activeConfigurationIdRequestProducerService: ActiveConfigurationIdRequestProducerService

    @Mock
    private lateinit var configurationMappingRequestProducerService: ConfigurationMappingRequestProducerService

    @Mock
    private lateinit var instanceMappingService: InstanceMappingService

    @Mock
    private lateinit var instanceMappingErrorEventProducerService: InstanceMappingErrorEventProducerService

    @Test
    fun shouldProcessSuccessfully() {
        val integrationId = 123L
        val configurationId = 456L
        val instanceFlowHeaders =
            InstanceFlowHeaders
                .builder()
                .sourceApplicationId(1L)
                .correlationId(UUID.fromString("1d4fb1f1-ab87-4bc1-979a-b5a97295d7d2"))
                .integrationId(integrationId)
                .build()

        val objectMapping = mock(ObjectMapping::class.java)

        val instance = mock(InstanceObject::class.java)
        val consumerRecord = newConsumerRecord(instance)
        val flytConsumerRecord = newFlytConsumerRecord(instanceFlowHeaders, consumerRecord)

        val mappedInstance = emptyMap<String, Any>()

        `when`(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(configurationId)
        `when`(configurationMappingRequestProducerService.get(configurationId)).thenReturn(objectMapping)
        doReturn(mappedInstance)
            .`when`(instanceMappingService)
            .toMappedInstanceObject(objectMapping, instance)

        assertDoesNotThrow { instanceProcessingService.process(flytConsumerRecord) }

        val instanceFlowHeadersCaptor = argumentCaptor<InstanceFlowHeaders>()
        val mappedInstanceCaptor = argumentCaptor<Any>()
        verify(instanceMappedEventProducerService).publish(
            instanceFlowHeadersCaptor.capture(),
            mappedInstanceCaptor.capture(),
        )

        assertEquals(1L, instanceFlowHeadersCaptor.firstValue.sourceApplicationId)
        assertEquals(
            UUID.fromString("1d4fb1f1-ab87-4bc1-979a-b5a97295d7d2"),
            instanceFlowHeadersCaptor.firstValue.correlationId,
        )
        assertEquals(integrationId, instanceFlowHeadersCaptor.firstValue.integrationId)
        assertEquals(configurationId, instanceFlowHeadersCaptor.firstValue.configurationId)
        assertSame(mappedInstance, mappedInstanceCaptor.firstValue)
    }

    @Test
    fun givenNoActiveConfigurationIdShouldPublishConfigurationNotFoundErrorEvent() {
        val integrationId = 123L
        val instanceFlowHeaders = mock(InstanceFlowHeaders::class.java)
        `when`(instanceFlowHeaders.integrationId).thenReturn(integrationId)

        val flytConsumerRecord = newFlytConsumerRecord(instanceFlowHeaders, newConsumerRecord(null))

        `when`(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(null)

        assertDoesNotThrow { instanceProcessingService.process(flytConsumerRecord) }

        verify(instanceMappingErrorEventProducerService)
            .publishConfigurationNotFoundErrorEvent(instanceFlowHeaders)
    }

    @Test
    fun givenNoConfigurationShouldPublishConfigurationNotFoundErrorEvent() {
        val integrationId = 123L
        val configurationId = 456L
        val instanceFlowHeaders = mock(InstanceFlowHeaders::class.java)
        `when`(instanceFlowHeaders.integrationId).thenReturn(integrationId)

        val flytConsumerRecord = newFlytConsumerRecord(instanceFlowHeaders, newConsumerRecord(null))

        `when`(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(configurationId)
        `when`(configurationMappingRequestProducerService.get(configurationId)).thenReturn(null)

        assertDoesNotThrow { instanceProcessingService.process(flytConsumerRecord) }

        verify(instanceMappingErrorEventProducerService)
            .publishConfigurationNotFoundErrorEvent(instanceFlowHeaders)
    }

    @Test
    fun givenValueConvertingNotFoundExceptionShouldPublishMissingValueConvertingErrorEvent() {
        val integrationId = 123L
        val instanceFlowHeaders = mock(InstanceFlowHeaders::class.java)
        `when`(instanceFlowHeaders.integrationId).thenReturn(integrationId)
        val configurationId = 456L
        val objectMapping = mock(ObjectMapping::class.java)

        val instanceObject = mock(InstanceObject::class.java)
        val consumerRecord = newConsumerRecord(instanceObject)
        val flytConsumerRecord = newFlytConsumerRecord(instanceFlowHeaders, consumerRecord)

        `when`(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(configurationId)
        `when`(configurationMappingRequestProducerService.get(configurationId)).thenReturn(objectMapping)
        `when`(instanceMappingService.toMappedInstanceObject(objectMapping, instanceObject))
            .thenThrow(ValueConvertingNotFoundException(678L))

        instanceProcessingService.process(flytConsumerRecord)

        verify(instanceMappingErrorEventProducerService)
            .publishMissingValueConvertingErrorEvent(instanceFlowHeaders, 678L)
        verify(instanceMappedEventProducerService, never()).publish(any(), any())
    }

    @Test
    fun givenValueConvertingKeyNotFoundExceptionShouldPublishMissingValueConvertingKeyErrorEvent() {
        val integrationId = 123L
        val instanceFlowHeaders = mock(InstanceFlowHeaders::class.java)
        `when`(instanceFlowHeaders.integrationId).thenReturn(integrationId)
        val configurationId = 456L
        val objectMapping = mock(ObjectMapping::class.java)

        val instanceObject = mock(InstanceObject::class.java)
        val consumerRecord = newConsumerRecord(instanceObject)
        val flytConsumerRecord = newFlytConsumerRecord(instanceFlowHeaders, consumerRecord)

        `when`(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(configurationId)
        `when`(configurationMappingRequestProducerService.get(configurationId)).thenReturn(objectMapping)
        `when`(instanceMappingService.toMappedInstanceObject(objectMapping, instanceObject))
            .thenThrow(ValueConvertingKeyNotFoundException(678L, "testKey"))

        instanceProcessingService.process(flytConsumerRecord)

        verify(instanceMappingErrorEventProducerService)
            .publishMissingValueConvertingKeyErrorEvent(instanceFlowHeaders, 678L, "testKey")
        verify(instanceMappedEventProducerService, never()).publish(any(), any())
    }

    @Test
    fun givenInstanceFieldNotFoundExceptionShouldPublishInstanceFieldNotFoundErrorEvent() {
        val integrationId = 123L
        val instanceFlowHeaders = mock(InstanceFlowHeaders::class.java)
        `when`(instanceFlowHeaders.integrationId).thenReturn(integrationId)
        val configurationId = 456L
        val objectMapping = mock(ObjectMapping::class.java)

        val instanceObject = mock(InstanceObject::class.java)
        val consumerRecord = newConsumerRecord(instanceObject)
        val flytConsumerRecord = newFlytConsumerRecord(instanceFlowHeaders, consumerRecord)

        `when`(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(configurationId)
        `when`(configurationMappingRequestProducerService.get(configurationId)).thenReturn(objectMapping)
        `when`(instanceMappingService.toMappedInstanceObject(objectMapping, instanceObject))
            .thenThrow(InstanceFieldNotFoundException("testKey"))

        instanceProcessingService.process(flytConsumerRecord)

        verify(instanceMappingErrorEventProducerService)
            .publishInstanceFieldNotFoundErrorEvent(instanceFlowHeaders, "testKey")
        verify(instanceMappedEventProducerService, never()).publish(any(), any())
    }

    @Test
    fun givenRuntimeExceptionShouldPublishGeneralSystemErrorEvent() {
        val integrationId = 123L

        val instanceFlowHeaders = mock(InstanceFlowHeaders::class.java)
        `when`(instanceFlowHeaders.integrationId).thenReturn(integrationId)
        val flytConsumerRecord = newFlytConsumerRecord(instanceFlowHeaders, newConsumerRecord(null))

        `when`(activeConfigurationIdRequestProducerService.get(integrationId)).thenThrow(RuntimeException())

        assertThrows(RuntimeException::class.java) {
            instanceProcessingService.process(flytConsumerRecord)
        }

        verify(instanceMappingErrorEventProducerService, never())
            .publishGeneralSystemErrorEvent(instanceFlowHeaders)
    }

    private fun newConsumerRecord(instanceObject: InstanceObject?): ConsumerRecord<String, InstanceObject> {
        return ConsumerRecord("test-topic", 0, 0L, "test-key", instanceObject)
    }

    private fun newFlytConsumerRecord(
        instanceFlowHeaders: InstanceFlowHeaders,
        consumerRecord: ConsumerRecord<String, InstanceObject>,
    ): InstanceFlowConsumerRecord<InstanceObject> {
        return InstanceFlowConsumerRecord
            .builder<InstanceObject>()
            .instanceFlowHeaders(instanceFlowHeaders)
            .consumerRecord(consumerRecord)
            .build()
    }
}
