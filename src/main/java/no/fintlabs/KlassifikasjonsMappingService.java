package no.fintlabs;

import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fintlabs.kafka.consumer.cache.FintCache;
import no.fintlabs.kafka.consumer.cache.FintCacheManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KlassifikasjonsMappingService {

    private final FintCache<String, KlassifikasjonssystemResource> klassifikasjonssystemCache;

    public KlassifikasjonsMappingService(FintCacheManager fintCacheManager) {
        klassifikasjonssystemCache = fintCacheManager.getCache("arkiv.noark.klassifikasjonssystem", String.class, KlassifikasjonssystemResource.class);
    }

    public List<KlasseResource> getKlasseResources(Map<String, String> caseValuesByFieldKey) {
        // TODO: 19/01/2022 Exception/validation handeling
        KlasseResource primarKlasseResource = this.getKlasseResource(
                caseValuesByFieldKey,
                "primarordningsprinsipp",
                "primarklasse",
                1
        );
        KlasseResource sekundarKlasseResource = this.getKlasseResource(
                caseValuesByFieldKey,
                "sekundarordningsprinsipp",
                "sekundarklasse",
                2
        );
        return List.of(primarKlasseResource, sekundarKlasseResource);
    }

    private KlasseResource getKlasseResource(Map<String, String> caseValuesByFieldKey, String ordningsprinsippFieldKey, String klasseFieldKey, int order) {
        String klassifikasjonssystemHref = caseValuesByFieldKey.get(ordningsprinsippFieldKey);
        if (StringUtils.isBlank(klassifikasjonssystemHref)) {
            return null;
        }

        KlassifikasjonssystemResource klassifikasjonssystem = klassifikasjonssystemCache.getOptional(klassifikasjonssystemHref)
                .orElseThrow();

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
}
