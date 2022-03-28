package no.fintlabs.integration.error.mappers;

import no.fintlabs.integration.error.ErrorCode;
import no.fintlabs.kafka.event.error.Error;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.model.configuration.ConfigurationField;
import no.fintlabs.validation.exceptions.MissingInstanceFieldsValidationException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MissingInstanceFieldsErrorMapper {

    public ErrorCollection map(MissingInstanceFieldsValidationException e) {
        Collection<Error> missingInstanceFieldsForCaseConfiguration = mapToErrors(
                ErrorCode.MISSING_INSTANCE_FIELD_FOR_CASE_CONFIGURATION_FIELD,
                e.getValidationError().getMissingInstanceFieldsPerCaseConfigurationField()
        );
        Collection<Error> missingInstanceFieldsForRecordConfiguration = mapToErrors(
                ErrorCode.MISSING_INSTANCE_FIELD_FOR_RECORD_CONFIGURATION_FIELD,
                e.getValidationError().getMissingInstanceFieldsPerRecordConfigurationField()
        );
        Collection<Error> missingInstanceFieldsForDocumentConfiguration = mapToErrors(
                ErrorCode.MISSING_INSTANCE_FIELD_FOR_DOCUMENT_CONFIGURATION_FIELD,
                e.getValidationError().getMissingInstanceFieldsPerDocumentConfigurationField()
        );

        return new ErrorCollection(
                Stream.of(
                                missingInstanceFieldsForCaseConfiguration,
                                missingInstanceFieldsForRecordConfiguration,
                                missingInstanceFieldsForDocumentConfiguration
                        )
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );
    }

    private Collection<Error> mapToErrors(ErrorCode errorCode, Map<ConfigurationField, List<String>> missingFieldsPerConfigurationField) {
        return missingFieldsPerConfigurationField
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue()
                        .stream()
                        .map(missingInstanceField -> Error
                                .builder()
                                .errorCode(errorCode.getCode())
                                .args(Map.of(
                                        "configurationField", entry.getKey().getField(),
                                        "instanceField", missingInstanceField
                                ))
                                .build()
                        )
                ).collect(Collectors.toList());
    }

}
