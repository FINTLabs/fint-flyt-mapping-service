package no.fintlabs.model.configuration;

import lombok.Data;

@Data
public class Metadata {
    private String id;
    private String name;
    private String description;
    private int version;
}