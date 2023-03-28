package no.fintlabs.mapping;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.InstanceFieldNotFoundException;
import no.fintlabs.model.instance.InstanceObject;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class InstanceReferenceService {

    private static final Pattern curlyBracketsWrapper = Pattern.compile("\\{[^}]+}");
    private static final Pattern instanceFieldReferencePattern = Pattern.compile("\\$if" + curlyBracketsWrapper);
    private static final Pattern instanceCollectionFieldReferencePattern = Pattern.compile("\\$icf" + curlyBracketsWrapper + curlyBracketsWrapper);
    private static final Pattern referencePattern = Pattern.compile(instanceFieldReferencePattern + "|" + instanceCollectionFieldReferencePattern);

    @Getter
    @Builder(toBuilder = true)
    @Jacksonized
    @EqualsAndHashCode
    private static class CollectionFieldKey {
        private final int collectionIndex;
        private final String collectionFieldKey;
    }

    public String replaceIfReferencesWithInstanceValues(
            String mappingString,
            Map<String, String> instanceValuePerKey,
            InstanceObject[] selectedCollectionObjectsPerKey
    ) {
        Matcher matcher = referencePattern.matcher(mappingString);
        return matcher.replaceAll(matchResult -> {
            String matchedReference = matchResult.group();
            return instanceFieldReferencePattern.matcher(matchedReference).matches()
                    ? getInstanceValue(matchedReference, instanceValuePerKey)
                    : getCollectionFieldValue(matchedReference, selectedCollectionObjectsPerKey);
        });
    }

    private String getInstanceValue(String ifReference, Map<String, String> instanceValuePerKey) {
        String instanceValueKey = getInstanceFieldKey(ifReference);
        if (!instanceValuePerKey.containsKey(instanceValueKey)) {
            throw new InstanceFieldNotFoundException(instanceValueKey);
        }
        return Optional.ofNullable(instanceValuePerKey.get(instanceValueKey)).orElse("");
    }

    private String getInstanceFieldKey(String ifReference) {
        return ifReference.replace("$if{", "").replace("}", "");
    }

    private String getCollectionFieldValue(String icfReference, InstanceObject[] selectedCollectionObjectsPerCollectionIndex) {
        CollectionFieldKey collectionIndexAndFieldReference = getCollectionFieldKey(icfReference);
        Map<String, String> valuePerKeyForCollectionObject = selectedCollectionObjectsPerCollectionIndex[collectionIndexAndFieldReference.getCollectionIndex()].getValuePerKey();
        if (!valuePerKeyForCollectionObject.containsKey(collectionIndexAndFieldReference.getCollectionFieldKey())) {
            throw new InstanceFieldNotFoundException(
                    collectionIndexAndFieldReference.getCollectionFieldKey()
            );
        }
        return Optional.ofNullable(
                valuePerKeyForCollectionObject.get(collectionIndexAndFieldReference.getCollectionFieldKey())
        ).orElse("");
    }

    private CollectionFieldKey getCollectionFieldKey(String icfReference) {
        String[] collectionIndexAndAollectionObjectValueReference = icfReference.split("}\\{");
        int collectionIndex = Integer.parseInt(
                collectionIndexAndAollectionObjectValueReference[0]
                        .replace("$icf{", "")
        );
        String collectionObjectValueReference = collectionIndexAndAollectionObjectValueReference[1]
                .replace("}", "");
        return new CollectionFieldKey(collectionIndex, collectionObjectValueReference);
    }

    public Collection<InstanceObject> getInstanceObjectCollection(
            String collectionReference,
            Map<String, Collection<InstanceObject>> objectCollectionPerKey,
            InstanceObject[] selectedCollectionObjectsByCollectionIndex
    ) {
        return instanceFieldReferencePattern.matcher(collectionReference).matches()
                ? getCollectionFromInstance(collectionReference, objectCollectionPerKey)
                : getCollectionFromSelectedCollectionObject(collectionReference, selectedCollectionObjectsByCollectionIndex);
    }

    private Collection<InstanceObject> getCollectionFromInstance(
            String collectionReference,
            Map<String, Collection<InstanceObject>> objectCollectionPerKey
    ) {
        String collectionKey = getInstanceFieldKey(collectionReference);
        if (!objectCollectionPerKey.containsKey(collectionKey)) {
            throw new InstanceFieldNotFoundException(collectionKey);
        }
        return Optional.ofNullable(objectCollectionPerKey.get(collectionKey)).orElse(Collections.emptyList());
    }

    private Collection<InstanceObject> getCollectionFromSelectedCollectionObject(
            String collectionReference,
            InstanceObject[] selectedCollectionObjectsByCollectionIndex
    ) {
        CollectionFieldKey collectionFieldKey = getCollectionFieldKey(collectionReference);
        Map<String, Collection<InstanceObject>> objectCollectionPerKey =
                selectedCollectionObjectsByCollectionIndex[collectionFieldKey.getCollectionIndex()]
                        .getObjectCollectionPerKey();
        if (!objectCollectionPerKey.containsKey(collectionFieldKey.getCollectionFieldKey())) {
            throw new InstanceFieldNotFoundException(collectionFieldKey.getCollectionIndex() + "." + collectionFieldKey.getCollectionFieldKey());
        }
        return Optional.ofNullable(objectCollectionPerKey.get(collectionFieldKey.getCollectionFieldKey())).orElse(Collections.emptyList());
    }

}
