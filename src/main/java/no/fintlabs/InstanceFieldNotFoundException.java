package no.fintlabs;

public class InstanceFieldNotFoundException extends RuntimeException {

    public InstanceFieldNotFoundException(String instanceFieldKey) {
        super("Could not find instance field with key='" + instanceFieldKey + "'");
    }

}
