package no.fintlabs.model.mappedinstance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MappedInstance {
    private Collection<MappedInstanceElement> elements = new ArrayList<>();
}
