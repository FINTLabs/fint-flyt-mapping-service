package no.fintlabs;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MappedFieldsValidator {

    public void validate(Map<String, String> caseValuesByFieldKey, SakField[] requiredFields) {
        List<String> missingFieldKeys = Arrays.stream(requiredFields)
                .map(SakField::getFieldKey)
                .filter(key -> !caseValuesByFieldKey.containsKey(key))
                .collect(Collectors.toList());
        if (!missingFieldKeys.isEmpty()) {
            throw new MissingFieldsValidationException("Mapped case values", missingFieldKeys);
        }
    }
}
