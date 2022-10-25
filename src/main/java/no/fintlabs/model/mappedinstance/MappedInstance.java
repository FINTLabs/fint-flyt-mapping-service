package no.fintlabs.model.mappedinstance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fintlabs.model.instance.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MappedInstance {
    private Collection<MappedInstanceElement> elements = new ArrayList<>();
    private List<Document> documents;
}
