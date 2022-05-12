package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.kafka.InstanceFlowErrorHandler;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.integration.error.ErrorEventProducerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InstanceProcessingErrorHandlerService extends InstanceFlowErrorHandler {

    private final ErrorEventProducerService errorEventProducerService;

    public InstanceProcessingErrorHandlerService(InstanceFlowHeadersMapper instanceFlowHeadersMapper, ErrorEventProducerService errorEventProducerService) {
        super(instanceFlowHeadersMapper);
        this.errorEventProducerService = errorEventProducerService;
    }

    @Override
    public void handleInstanceFlowRecord(Exception thrownException, InstanceFlowHeaders instanceFlowHeaders, ConsumerRecord<?, ?> consumerRecord) {
        errorEventProducerService.sendGeneralSystemErrorEvent(instanceFlowHeaders);
        log.error("Could not process instance record", thrownException);
    }

}
