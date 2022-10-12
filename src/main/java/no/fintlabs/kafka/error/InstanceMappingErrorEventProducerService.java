package no.fintlabs.kafka.error;

import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducer;
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.error.Error;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicService;
import org.springframework.stereotype.Service;

@Service
public class InstanceMappingErrorEventProducerService {

    private final InstanceFlowErrorEventProducer instanceFlowErrorEventProducer;
    private final ErrorEventTopicNameParameters errorEventTopicNameParameters;

    public InstanceMappingErrorEventProducerService(
            InstanceFlowErrorEventProducer instanceFlowErrorEventProducer,
            ErrorEventTopicService errorEventTopicService
    ) {
        this.instanceFlowErrorEventProducer = instanceFlowErrorEventProducer;
        this.errorEventTopicNameParameters = ErrorEventTopicNameParameters.builder()
                .errorEventName("instance-mapping-error")
                .build();

        errorEventTopicService.ensureTopic(errorEventTopicNameParameters, 0);
    }

    public void publishConfigurationNotFoundErrorEvent(InstanceFlowHeaders instanceFlowHeaders) {
        publishErrorEventWithASingleErrorCode(instanceFlowHeaders, ErrorCode.CONFIGURATION_NOT_FOUND);
    }

    public void publishGeneralSystemErrorEvent(InstanceFlowHeaders instanceFlowHeaders) {
        publishErrorEventWithASingleErrorCode(instanceFlowHeaders, ErrorCode.GENERAL_SYSTEM_ERROR);
    }

    private void publishErrorEventWithASingleErrorCode(InstanceFlowHeaders instanceFlowHeaders, ErrorCode errorCode) {
        instanceFlowErrorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(errorEventTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(new ErrorCollection(Error.builder().errorCode(errorCode.getCode()).build()))
                        .build()
        );
    }

}