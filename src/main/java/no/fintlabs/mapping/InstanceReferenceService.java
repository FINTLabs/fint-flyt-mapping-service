package no.fintlabs.mapping;

import lombok.Data;
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

    @Data
    private static class CollectionFieldKey {
        private final int collectionIndex;
        private final String collectionFieldKey;
    }

    public String replaceIfReferencesWithInstanceValues(
            String mappingString,
            Map<String, String> instanceValuePerKey,
            InstanceObject[] selectedCollectionElementsPerKey
    ) {
        Matcher matcher = referencePattern.matcher(mappingString);
        return matcher.replaceAll(matchResult -> {
            String matchedReference = matchResult.group();
            return instanceFieldReferencePattern.matcher(matchedReference).matches()
                    ? getInstanceValue(matchedReference, instanceValuePerKey)
                    : getCollectionFieldValue(matchedReference, selectedCollectionElementsPerKey);
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

    private String getCollectionFieldValue(String icfReference, InstanceObject[] selectedCollectionElementsPerCollectionIndex) {
        CollectionFieldKey collectionIndexAndFieldReference = getCollectionFieldKey(icfReference);
        Map<String, String> valuePerKeyForCollectionElement = selectedCollectionElementsPerCollectionIndex[collectionIndexAndFieldReference.getCollectionIndex()].getValuePerKey();
        if (!valuePerKeyForCollectionElement.containsKey(collectionIndexAndFieldReference.getCollectionFieldKey())) {
            throw new InstanceFieldNotFoundException(
                    collectionIndexAndFieldReference.getCollectionFieldKey()
            );
        }
        return Optional.ofNullable(
                valuePerKeyForCollectionElement.get(collectionIndexAndFieldReference.getCollectionFieldKey())
        ).orElse("");
    }

    private CollectionFieldKey getCollectionFieldKey(String icfReference) {
        String[] collectionIndexAndAollectionElementValueReference = icfReference.split("}\\{");
        int collectionIndex = Integer.parseInt(
                collectionIndexAndAollectionElementValueReference[0]
                        .replace("$icf{", "")
        );
        String collectionElementValueReference = collectionIndexAndAollectionElementValueReference[1]
                .replace("}", "");
        return new CollectionFieldKey(collectionIndex, collectionElementValueReference);
    }

    public Collection<InstanceObject> getInstanceElementCollection(
            String collectionReference,
            Map<String, Collection<InstanceObject>> elementCollectionPerKey,
            InstanceObject[] selectedCollectionElementsByCollectionIndex
    ) {
        return instanceFieldReferencePattern.matcher(collectionReference).matches()
                ? getCollectionFromInstance(collectionReference, elementCollectionPerKey)
                : getCollectionFromSelectedCollectionElement(collectionReference, selectedCollectionElementsByCollectionIndex);
    }

    private Collection<InstanceObject> getCollectionFromInstance(
            String collectionReference,
            Map<String, Collection<InstanceObject>> elementCollectionPerKey
    ) {
        String collectionKey = getInstanceFieldKey(collectionReference);
        if (!elementCollectionPerKey.containsKey(collectionKey)) {
            throw new InstanceFieldNotFoundException(collectionKey);
        }
        return Optional.ofNullable(elementCollectionPerKey.get(collectionKey)).orElse(Collections.emptyList());
    }

    private Collection<InstanceObject> getCollectionFromSelectedCollectionElement(
            String collectionReference,
            InstanceObject[] selectedCollectionElementsByCollectionIndex
    ) {
        CollectionFieldKey collectionFieldKey = getCollectionFieldKey(collectionReference);
        Map<String, Collection<InstanceObject>> elementCollectionPerKey =
                selectedCollectionElementsByCollectionIndex[collectionFieldKey.getCollectionIndex()]
                        .getObjectCollectionPerKey();
        if (!elementCollectionPerKey.containsKey(collectionFieldKey.getCollectionFieldKey())) {
            throw new InstanceFieldNotFoundException(collectionFieldKey.getCollectionIndex() + "." + collectionFieldKey.getCollectionFieldKey());
        }
        return Optional.ofNullable(elementCollectionPerKey.get(collectionFieldKey.getCollectionFieldKey())).orElse(Collections.emptyList());
    }

}
