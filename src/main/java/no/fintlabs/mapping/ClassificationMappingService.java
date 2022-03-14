package no.fintlabs.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Service
@DependsOn("klassifikasjonssystemResourceEntityConsumer")
public class ClassificationMappingService {

    private final FintCache<String, KlassifikasjonssystemResource> klassifikasjonssystemCache;

    public ClassificationMappingService(FintCacheManager fintCacheManager) {
        klassifikasjonssystemCache = fintCacheManager.getCache("arkiv.noark.klassifikasjonssystem", String.class, KlassifikasjonssystemResource.class);
    }

    public List<KlasseResource> getKlasseResources(Map<String, String> caseValuesByFieldKey, List<FieldKeys> orderedFieldKeys) {
        return IntStream.range(0, orderedFieldKeys.size())
                .mapToObj(i -> this.getKlasseResource(
                        caseValuesByFieldKey,
                        orderedFieldKeys.get(i).klassifikasjonsssystemFieldKey,
                        orderedFieldKeys.get(i).klasseFieldKey,
                        i + 1
                ))
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private KlasseResource getKlasseResource(Map<String, String> caseValuesByFieldKey, String klassifikasjonssystemFieldKey, String klasseFieldKey, int order) {
        String klassifikasjonssystemHref = caseValuesByFieldKey.get(klassifikasjonssystemFieldKey);
        if (StringUtils.isBlank(klassifikasjonssystemHref)) {
            return null;
        }
        KlassifikasjonssystemResource klassifikasjonssystem = klassifikasjonssystemCache.get(klassifikasjonssystemHref);
        String klasseId = caseValuesByFieldKey.get(klasseFieldKey);
        return this.getKlasseResource(klassifikasjonssystem, klasseId, order);
    }

    private KlasseResource getKlasseResource(KlassifikasjonssystemResource klassifikasjonssystem, String klasseId, int order) {
        KlasseResource klasseResource = klassifikasjonssystem
                .getKlasse()
                .stream()
                .filter(klasse -> klasse.getKlasseId() != null && klasse.getKlasseId().equals(klasseId))
                .findFirst()
                .orElseThrow();

        KlasseResource newKlasseResource = new KlasseResource();
        newKlasseResource.setKlasseId(klasseResource.getKlasseId());
        newKlasseResource.setTittel(klasseResource.getTittel());
        newKlasseResource.setSkjerming(klasseResource.getSkjerming());
        newKlasseResource.setLinks(klasseResource.getLinks());
        newKlasseResource.setRekkefolge(order);
        return newKlasseResource;
    }

    @Data
    @AllArgsConstructor
    public static class FieldKeys {
        private final String klassifikasjonsssystemFieldKey;
        private final String klasseFieldKey;
    }
}
