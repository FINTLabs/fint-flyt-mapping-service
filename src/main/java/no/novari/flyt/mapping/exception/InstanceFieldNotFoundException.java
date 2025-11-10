package no.novari.flyt.mapping.exception;

import lombok.Getter;

@Getter
public class InstanceFieldNotFoundException extends RuntimeException {

    private final String instanceFieldKey;

    public InstanceFieldNotFoundException(String instanceFieldKey) {
        super("Could not find instance field with key='" + instanceFieldKey + "'");
        this.instanceFieldKey = instanceFieldKey;
    }

}
