package no.fintlabs;

import no.fintlabs.model.configuration.Field;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import no.fintlabs.model.configuration.Property;
import no.fintlabs.model.configuration.ValueBuilder;
import no.fintlabs.model.instance.Instance;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class InstanceFieldsValidator {

    public void validate(IntegrationConfiguration integrationConfiguration, Instance instance) {
        Set<String> instanceFieldKeys = instance.getFields().keySet();
        List<String> missingFieldKeys = Stream.of(
                        integrationConfiguration.getCaseConfiguration().getFields(),
                        integrationConfiguration.getRecordConfiguration().getFields(),
                        integrationConfiguration.getDocumentConfiguration().getFields()
                )
                .flatMap(Collection::stream)
                .distinct()
                .map(Field::getValueBuilder)
                .map(ValueBuilder::getProperties)
                .flatMap(Collection::stream)
                .map(Property::getKey)
                .filter(key -> !instanceFieldKeys.contains(key))
                .collect(Collectors.toList());
        if (!missingFieldKeys.isEmpty()) {
            throw new MissingFieldsValidationException("Instance", missingFieldKeys);
        }
    }

}
