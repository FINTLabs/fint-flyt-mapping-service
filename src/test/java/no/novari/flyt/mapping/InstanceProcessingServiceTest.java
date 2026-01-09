package no.novari.flyt.mapping;

import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.novari.flyt.mapping.exception.InstanceFieldNotFoundException;
import no.novari.flyt.mapping.exception.ValueConvertingKeyNotFoundException;
import no.novari.flyt.mapping.exception.ValueConvertingNotFoundException;
import no.novari.flyt.mapping.kafka.InstanceMappedEventProducerService;
import no.novari.flyt.mapping.kafka.configuration.ActiveConfigurationIdRequestProducerService;
import no.novari.flyt.mapping.kafka.configuration.ConfigurationMappingRequestProducerService;
import no.novari.flyt.mapping.kafka.error.InstanceMappingErrorEventProducerService;
import no.novari.flyt.mapping.model.configuration.ObjectMapping;
import no.novari.flyt.mapping.model.instance.InstanceObject;
import no.novari.flyt.mapping.service.InstanceMappingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstanceProcessingServiceTest {

    @InjectMocks
    private InstanceProcessingService instanceProcessingService;

    @Mock
    private InstanceMappedEventProducerService instanceMappedEventProducerService;

    @Mock
    private ActiveConfigurationIdRequestProducerService activeConfigurationIdRequestProducerService;

    @Mock
    private ConfigurationMappingRequestProducerService configurationMappingRequestProducerService;

    @Mock
    private InstanceMappingService instanceMappingService;

    @Mock
    private InstanceMappingErrorEventProducerService instanceMappingErrorEventProducerService;

    @Test
    void shouldProcessSuccessfully() {
        Long integrationId = 123L;
        Long configurationId = 456L;
        InstanceFlowHeaders instanceFlowHeaders = InstanceFlowHeaders
                .builder()
                .sourceApplicationId(1L)
                .correlationId(UUID.fromString("1d4fb1f1-ab87-4bc1-979a-b5a97295d7d2"))
                .integrationId(integrationId)
                .build();

        ObjectMapping objectMapping = mock(ObjectMapping.class);

        InstanceObject instance = mock(InstanceObject.class);
        ConsumerRecord<String, InstanceObject> consumerRecord = newConsumerRecord(instance);
        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord =
                newFlytConsumerRecord(instanceFlowHeaders, consumerRecord);

        Map<String, Object> mappedInstance = Map.of();

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.of(configurationId));
        when(configurationMappingRequestProducerService.get(configurationId)).thenReturn(Optional.of(objectMapping));
        doReturn(mappedInstance)
                .when(instanceMappingService)
                .toMappedInstanceObject(objectMapping, instance);

        assertDoesNotThrow(() -> instanceProcessingService.process(flytConsumerRecord));

        verify(instanceMappedEventProducerService).publish(ArgumentMatchers.argThat(
                ifh -> ifh.getSourceApplicationId() == 1L
                        && ifh.getCorrelationId().equals(UUID.fromString("1d4fb1f1-ab87-4bc1-979a-b5a97295d7d2"))
                        && ifh.getIntegrationId().equals(integrationId)
                        && ifh.getConfigurationId().equals(configurationId)
        ), ArgumentMatchers.same(mappedInstance));
    }

    @Test
    void givenNoActiveConfigurationIdShouldPublishConfigurationNotFoundErrorEvent() {
        Long integrationId = 123L;
        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);

        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord =
                newFlytConsumerRecord(instanceFlowHeaders, newConsumerRecord(null));

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> instanceProcessingService.process(flytConsumerRecord));

        verify(instanceMappingErrorEventProducerService)
                .publishConfigurationNotFoundErrorEvent(instanceFlowHeaders);
    }

    @Test
    void givenNoConfigurationShouldPublishConfigurationNotFoundErrorEvent() {
        Long integrationId = 123L;
        Long configurationId = 456L;
        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);

        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord =
                newFlytConsumerRecord(instanceFlowHeaders, newConsumerRecord(null));

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.of(configurationId));
        when(configurationMappingRequestProducerService.get(configurationId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> instanceProcessingService.process(flytConsumerRecord));

        verify(instanceMappingErrorEventProducerService)
                .publishConfigurationNotFoundErrorEvent(instanceFlowHeaders);
    }

    @Test
    void givenValueConvertingNotFoundExceptionShouldPublishMissingValueConvertingErrorEvent() {
        Long integrationId = 123L;
        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);
        Long configurationId = 456L;
        ObjectMapping objectMapping = mock(ObjectMapping.class);

        InstanceObject instanceObject = mock(InstanceObject.class);
        ConsumerRecord<String, InstanceObject> consumerRecord = newConsumerRecord(instanceObject);
        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord =
                newFlytConsumerRecord(instanceFlowHeaders, consumerRecord);

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.of(configurationId));
        when(configurationMappingRequestProducerService.get(configurationId)).thenReturn(Optional.of(objectMapping));
        when(instanceMappingService.toMappedInstanceObject(objectMapping, instanceObject))
                .thenThrow(new ValueConvertingNotFoundException(678L));

        instanceProcessingService.process(flytConsumerRecord);

        verify(instanceMappingErrorEventProducerService)
                .publishMissingValueConvertingErrorEvent(instanceFlowHeaders, 678L);
        verify(instanceMappedEventProducerService, never()).publish(any(), any());
    }

    @Test
    void givenValueConvertingKeyNotFoundExceptionShouldPublishMissingValueConvertingKeyErrorEvent() {
        Long integrationId = 123L;
        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);
        Long configurationId = 456L;
        ObjectMapping objectMapping = mock(ObjectMapping.class);

        InstanceObject instanceObject = mock(InstanceObject.class);
        ConsumerRecord<String, InstanceObject> consumerRecord = newConsumerRecord(instanceObject);
        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord =
                newFlytConsumerRecord(instanceFlowHeaders, consumerRecord);

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.of(configurationId));
        when(configurationMappingRequestProducerService.get(configurationId)).thenReturn(Optional.of(objectMapping));
        when(instanceMappingService.toMappedInstanceObject(objectMapping, instanceObject))
                .thenThrow(new ValueConvertingKeyNotFoundException(678L, "testKey"));

        instanceProcessingService.process(flytConsumerRecord);

        verify(instanceMappingErrorEventProducerService)
                .publishMissingValueConvertingKeyErrorEvent(instanceFlowHeaders, 678L, "testKey");
        verify(instanceMappedEventProducerService, never()).publish(any(), any());
    }

    @Test
    void givenInstanceFieldNotFoundExceptionShouldPublishInstanceFieldNotFoundErrorEvent() {
        Long integrationId = 123L;
        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);
        Long configurationId = 456L;
        ObjectMapping objectMapping = mock(ObjectMapping.class);

        InstanceObject instanceObject = mock(InstanceObject.class);
        ConsumerRecord<String, InstanceObject> consumerRecord = newConsumerRecord(instanceObject);
        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord =
                newFlytConsumerRecord(instanceFlowHeaders, consumerRecord);

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.of(configurationId));
        when(configurationMappingRequestProducerService.get(configurationId)).thenReturn(Optional.of(objectMapping));
        when(instanceMappingService.toMappedInstanceObject(objectMapping, instanceObject))
                .thenThrow(new InstanceFieldNotFoundException("testKey"));

        instanceProcessingService.process(flytConsumerRecord);

        verify(instanceMappingErrorEventProducerService)
                .publishInstanceFieldNotFoundErrorEvent(instanceFlowHeaders, "testKey");
        verify(instanceMappedEventProducerService, never()).publish(any(), any());
    }

    @Test
    void givenRuntimeExceptionShouldPublishGeneralSystemErrorEvent() {
        Long integrationId = 123L;

        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);
        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord =
                newFlytConsumerRecord(instanceFlowHeaders, newConsumerRecord(null));

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () ->
                instanceProcessingService.process(flytConsumerRecord)
        );

        verify(instanceMappingErrorEventProducerService, never())
                .publishGeneralSystemErrorEvent(instanceFlowHeaders);
    }

    private static ConsumerRecord<String, InstanceObject> newConsumerRecord(InstanceObject instanceObject) {
        return new ConsumerRecord<>("test-topic", 0, 0L, "test-key", instanceObject);
    }

    private static InstanceFlowConsumerRecord<InstanceObject> newFlytConsumerRecord(
            InstanceFlowHeaders instanceFlowHeaders,
            ConsumerRecord<String, InstanceObject> consumerRecord
    ) {
        return InstanceFlowConsumerRecord.<InstanceObject>builder()
                .instanceFlowHeaders(instanceFlowHeaders)
                .consumerRecord(consumerRecord)
                .build();
    }

}
