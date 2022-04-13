package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.integration.error.ErrorEventProducerService;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InstanceProcessingErrorHandlerService extends CommonLoggingErrorHandler {

    // TODO: 28/03/2022 Inlcude skjema headers in outgoing error events

    private final ErrorEventProducerService errorEventProducerService;

    public InstanceProcessingErrorHandlerService(ErrorEventProducerService errorEventProducerService) {
        this.errorEventProducerService = errorEventProducerService;
    }

//    @Override
//    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer, MessageListenerContainer container, boolean batchListener) {
//        super.handleOtherException(thrownException, consumer, container, batchListener);
//        sendErrorEvent(thrownException);
//    }
//
//    @Override
//    public void handleRecord(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer, MessageListenerContainer container) {
//        super.handleRecord(thrownException, record, consumer, container);
//        sendErrorEvent(thrownException);
//    }
//
//    @Override
//    public void handleBatch(Exception thrownException, ConsumerRecords<?, ?> data, Consumer<?, ?> consumer, MessageListenerContainer container, Runnable invokeListener) {
//        super.handleBatch(thrownException, data, consumer, container, invokeListener);
//        sendErrorEvent(thrownException);
//    }
//
//    private void sendErrorEvent(Exception thrownException) {
//        if (thrownException instanceof MissingInstanceFieldsValidationException) {
//            errorEventProducerService.sendMissingInstanceFieldsErrorEvent(
//                    (MissingInstanceFieldsValidationException) thrownException
//            );
//        } else if (thrownException instanceof MissingMappingFieldsValidationException) {
//            errorEventProducerService.sendMissingMappingFieldsErrorEvent(
//                    (MissingMappingFieldsValidationException) thrownException
//            );
//        } else if (thrownException instanceof NoSuchIntegrationConfigurationException) {
//            errorEventProducerService.sendNoConfigurationForIntegrationErrorEvent();
//        } else {
//            errorEventProducerService.sendGeneralSystemErrorEvent();
//        }
//    }

}
