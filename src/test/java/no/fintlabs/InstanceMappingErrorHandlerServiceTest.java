package no.fintlabs;

import no.fintlabs.exception.ConfigurationNotFoundException;
import no.fintlabs.exception.InstanceFieldNotFoundException;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.error.InstanceMappingErrorEventProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.UUID;

import static org.mockito.Mockito.*;

class InstanceMappingErrorHandlerServiceTest {

    @InjectMocks
    private InstanceMappingErrorHandlerService instanceMappingErrorHandlerService;

    @Mock
    private InstanceMappingErrorEventProducerService instanceMappingErrorEventProducerService;

    @Mock
    private ConsumerRecord<?, ?> consumerRecord;

    InstanceFlowHeaders instanceFlowHeaders;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        instanceFlowHeaders = InstanceFlowHeaders
                .builder()
                .sourceApplicationId(1L)
                .correlationId(UUID.randomUUID())
                .build();
    }

    @Test
    void shouldHandleConfigurationNotFoundException() {
        Throwable cause = new ConfigurationNotFoundException("Some error message");

        instanceMappingErrorHandlerService.handleInstanceFlowRecord(cause, instanceFlowHeaders, consumerRecord);

        verify(instanceMappingErrorEventProducerService).publishConfigurationNotFoundErrorEvent(instanceFlowHeaders);
    }

    @Test
    void shouldHandleInstanceFieldNotFoundException() {
        String instanceFieldKey = "fieldKey";
        Throwable cause = new InstanceFieldNotFoundException(instanceFieldKey);

        instanceMappingErrorHandlerService.handleInstanceFlowRecord(cause, instanceFlowHeaders, consumerRecord);

        verify(instanceMappingErrorEventProducerService).publishInstanceFieldNotFoundErrorEvent(instanceFlowHeaders, instanceFieldKey);
    }

    @Test
    void shouldHandleRuntimeException() {
        Throwable cause = new RuntimeException("Some error message");

        instanceMappingErrorHandlerService.handleInstanceFlowRecord(cause, instanceFlowHeaders, consumerRecord);

        verify(instanceMappingErrorEventProducerService).publishGeneralSystemErrorEvent(instanceFlowHeaders);
    }
}
