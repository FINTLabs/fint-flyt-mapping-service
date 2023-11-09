package no.fintlabs;

import no.fintlabs.exception.ConfigurationNotFoundException;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.InstanceMappedEventProducerService;
import no.fintlabs.kafka.configuration.ActiveConfigurationIdRequestProducerService;
import no.fintlabs.model.instance.InstanceObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Disabled
class InstanceProcessingServiceTest {

    @InjectMocks
    private InstanceProcessingService instanceProcessingService;

    @Mock
    private InstanceMappedEventProducerService instanceMappedEventProducerService;

    @Mock
    private ActiveConfigurationIdRequestProducerService activeConfigurationIdRequestProducerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldProcessSuccessfully() {
        Long integrationId = 123L;
        Long configurationId = 456L;
        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);
        when(instanceFlowHeaders.toBuilder()).thenReturn(instanceFlowHeaders.toBuilder().integrationId(integrationId));

        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord = mock(InstanceFlowConsumerRecord.class);
        when(flytConsumerRecord.getInstanceFlowHeaders()).thenReturn(instanceFlowHeaders);

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.of(configurationId));
//        when(configurationMappingRequestProducerService.get(configurationId)).thenReturn(Optional.of(new ObjectMapping()));
//        when(instanceMappingService.toMappedInstanceObject(any(), any())).thenReturn(new Object());

        instanceProcessingService.process(flytConsumerRecord);

        verify(instanceMappedEventProducerService).publish(any(InstanceFlowHeaders.class), any());
    }

    @Test
    void shouldThrowConfigurationNotFoundExceptionForIntegrationId() {
        Long integrationId = 123L;
        InstanceFlowHeaders instanceFlowHeaders = mock(InstanceFlowHeaders.class);
        when(instanceFlowHeaders.getIntegrationId()).thenReturn(integrationId);

        InstanceFlowConsumerRecord<InstanceObject> flytConsumerRecord = mock(InstanceFlowConsumerRecord.class);
        when(flytConsumerRecord.getInstanceFlowHeaders()).thenReturn(instanceFlowHeaders);

        when(activeConfigurationIdRequestProducerService.get(integrationId)).thenReturn(Optional.empty());

        assertThrows(ConfigurationNotFoundException.class, () -> instanceProcessingService.process(flytConsumerRecord));
    }

}
