package no.fintlabs.integration.error.mappers;

import no.fintlabs.integration.error.ErrorCode;
import no.fintlabs.kafka.event.error.Error;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.mapping.fields.MappingField;
import no.fintlabs.validation.exceptions.MissingMappingFieldsValidationException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MissingMappingFieldsErrorMapper {

    public ErrorCollection map(MissingMappingFieldsValidationException e) {
        Collection<Error> missingMappingFieldsForCase = mapToErrors(
                ErrorCode.MISSING_MAPPING_FIELD_FOR_CASE,
                e.getValidationError().getMissingCaseFields()
        );
        Collection<Error> missingMappingFieldsForRecord = mapToErrors(
                ErrorCode.MISSING_MAPPING_FIELD_FOR_RECORD,
                e.getValidationError().getMissingRecordFields()
        );
        Collection<Error> missingMappingFieldsForDocument = mapToErrors(
                ErrorCode.MISSING_MAPPING_FIELD_FOR_DOCUMENT,
                e.getValidationError().getMissingDocumentFields()
        );

        return new ErrorCollection(
                Stream.of(missingMappingFieldsForCase, missingMappingFieldsForRecord, missingMappingFieldsForDocument)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );
    }

    private Collection<Error> mapToErrors(ErrorCode errorCode, List<MappingField> missingMappingFields) {
        return missingMappingFields
                .stream()
                .map(MappingField::getFieldKey)
                .map(missingMappingField -> Error
                        .builder()
                        .errorCode(errorCode.getCode())
                        .args(Map.of(
                                "mappingField", missingMappingField
                        ))
                        .build()
                )
                .collect(Collectors.toList());
    }
}
