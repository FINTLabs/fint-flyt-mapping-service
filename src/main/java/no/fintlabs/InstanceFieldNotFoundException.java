package no.fintlabs;

import lombok.Getter;

public class InstanceFieldNotFoundException extends RuntimeException {

    @Getter
    private final String instanceFieldKey;

    public InstanceFieldNotFoundException(String instanceFieldKey) {
        super("Could not find instance field with key='" + instanceFieldKey + "'");
        this.instanceFieldKey = instanceFieldKey;
    }

}
