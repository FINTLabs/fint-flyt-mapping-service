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
    private lateinit var intgrConfClient: IntgrConfClient

    @Mock
    private lateinit var instanceMappingService: InstanceMappingService

    @Mock
    private lateinit var instanceErrorEventProducerService: InstanceErrorEventProducerService

    @Test
    fun shouldProcessSuccessfully() {
        val integrationId = 123L
        val configurationId = 456L
        val instanceFlowHeaders =
            InstanceFlowHeaders(
                sourceApplicationId = 1L,
                correlationId = UUID.fromString("1d4fb1f1-ab87-4bc1-979a-b5a97295d7d2"),
                integrationId = integrationId,
            )

        val objectMapping = mock(ObjectMapping::class.java)

        val instance = mock(InstanceObject::class.java)

        val mappedInstance = emptyMap<String, Any>()

        `when`(intgrConfClient.getActiveConfigurationId(integrationId)).thenReturn(configurationId)
        `when`(intgrConfClient.getMapping(configurationId)).thenReturn(objectMapping)
        doReturn(mappedInstance)
            .`when`(instanceMappingService)
            .toMappedInstanceObject(objectMapping, instance)

        assertDoesNotThrow { instanceProcessingService.process(instance, instanceFlowHeaders) }

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

        `when`(intgrConfClient.getActiveConfigurationId(integrationId)).thenReturn(null)

        assertDoesNotThrow { instanceProcessingService.process(InstanceObject(), instanceFlowHeaders) }

        verify(instanceErrorEventProducerService)
            .publishConfigurationNotFoundErrorEvent(instanceFlowHeaders)
    }

    @Test
    fun givenNoConfigurationShouldPublishConfigurationNotFoundErrorEvent() {
        val integrationId = 123L
        val configurationId = 456L
        val instanceFlowHeaders = mock(InstanceFlowHeaders::class.java)
        `when`(instanceFlowHeaders.integrationId).thenReturn(integrationId)

        `when`(intgrConfClient.getActiveConfigurationId(integrationId)).thenReturn(configurationId)
        `when`(intgrConfClient.getMapping(configurationId)).thenReturn(null)

        assertDoesNotThrow { instanceProcessingService.process(InstanceObject(), instanceFlowHeaders) }

        verify(instanceErrorEventProducerService)
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

        `when`(intgrConfClient.getActiveConfigurationId(integrationId)).thenReturn(configurationId)
        `when`(intgrConfClient.getMapping(configurationId)).thenReturn(objectMapping)
        `when`(instanceMappingService.toMappedInstanceObject(objectMapping, instanceObject))
            .thenThrow(ValueConvertingNotFoundException(678L))

        instanceProcessingService.process(instanceObject, instanceFlowHeaders)

        verify(instanceErrorEventProducerService)
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

        `when`(intgrConfClient.getActiveConfigurationId(integrationId)).thenReturn(configurationId)
        `when`(intgrConfClient.getMapping(configurationId)).thenReturn(objectMapping)
        `when`(instanceMappingService.toMappedInstanceObject(objectMapping, instanceObject))
            .thenThrow(ValueConvertingKeyNotFoundException(678L, "testKey"))

        instanceProcessingService.process(instanceObject, instanceFlowHeaders)

        verify(instanceErrorEventProducerService)
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

        `when`(intgrConfClient.getActiveConfigurationId(integrationId)).thenReturn(configurationId)
        `when`(intgrConfClient.getMapping(configurationId)).thenReturn(objectMapping)
        `when`(instanceMappingService.toMappedInstanceObject(objectMapping, instanceObject))
            .thenThrow(InstanceFieldNotFoundException("testKey"))

        instanceProcessingService.process(instanceObject, instanceFlowHeaders)

        verify(instanceErrorEventProducerService)
            .publishInstanceFieldNotFoundErrorEvent(instanceFlowHeaders, "testKey")
        verify(instanceMappedEventProducerService, never()).publish(any(), any())
    }

    @Test
    fun givenRuntimeExceptionShouldPublishGeneralSystemErrorEvent() {
        val integrationId = 123L

        val instanceFlowHeaders = mock(InstanceFlowHeaders::class.java)
        `when`(instanceFlowHeaders.integrationId).thenReturn(integrationId)

        `when`(intgrConfClient.getActiveConfigurationId(integrationId)).thenThrow(RuntimeException())

        assertThrows(RuntimeException::class.java) {
            instanceProcessingService.process(InstanceObject(), instanceFlowHeaders)
        }

        verify(instanceErrorEventProducerService, never())
            .publishGeneralSystemErrorEvent(instanceFlowHeaders)
    }
}
