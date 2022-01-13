package no.fintlabs.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fintlabs.model.instance.Instance;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Field {
    private ValueBuildStrategy valueBuildStrategy;
    private String field;
    private ValueBuilder valueBuilder;

    public String getCalculatedValue(Instance instance) {
        if (valueBuildStrategy.equals(ValueBuildStrategy.FIXED_ARCHIVE_CODE_VALUE)) {
            return valueBuilder.getValue();
        }

        if (valueBuildStrategy.equals(ValueBuildStrategy.COMBINE_STRING_VALUE)) {
            List<String> args = new ArrayList<>();
            valueBuilder.getProperties()
                    .stream()
                    .sorted((o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()))
                    .forEach(property -> {
                        args.add(instance.getFields().get(property.getKey()).getValue());
                    });
            return String.format(valueBuilder.getValue(), args);
        }

        throw new RuntimeException("Value build strategy unknown");

    }
}