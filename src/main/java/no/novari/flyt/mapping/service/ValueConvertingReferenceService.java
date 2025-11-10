package no.novari.flyt.mapping.service;

import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValueConvertingReferenceService {

    private static final Pattern curlyBracketsWrapper = Pattern.compile("\\{[^}]+}");
    private static final Pattern valueConvertingReferencePattern = Pattern.compile("\\$vc" + curlyBracketsWrapper);

    public Long getFirstValueConverterId(String mappingString) {
        Matcher matcher = valueConvertingReferencePattern.matcher(mappingString);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Mapping string contains no valid value converting reference");
        }
        String matchedReference = matcher.group(0);
        return getValueConvertingId(matchedReference);
    }

    private Long getValueConvertingId(String valueConvertingReference) {
        return Long.parseLong(
                valueConvertingReference
                        .replace("$vc{", "").replace("}", "")
        );
    }

}
