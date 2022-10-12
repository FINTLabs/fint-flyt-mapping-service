package no.fintlabs.mapping

import no.fintlabs.model.configuration.Configuration
import no.fintlabs.model.configuration.ConfigurationElement
import no.fintlabs.model.configuration.FieldCollectionConfiguration
import no.fintlabs.model.configuration.FieldConfiguration
import no.fintlabs.model.instance.Instance
import no.fintlabs.model.instance.InstanceField
import no.fintlabs.model.mappedinstance.MappedInstance
import no.fintlabs.model.mappedinstance.MappedInstanceElement
import no.fintlabs.model.mappedinstance.MappedInstanceField
import no.fintlabs.model.mappedinstance.MappedInstanceFieldCollection
import spock.lang.Specification

class InstanceMappingIntegrationSpec extends Specification {

    InstanceMappingService instanceMappingService

    def setup() {
        instanceMappingService = new InstanceMappingService(new DynamicStringMappingService())
    }

    def 'Should map to same structure as configuration'() {
        given:
        Instance instance = Instance
                .builder()
                .fieldPerKey(Map.of("fieldKey1", InstanceField.builder().key("fieldKey1").value("abc").build(),
                        "fieldKey2", InstanceField.builder().key("fieldKey2").value("http://www.example.com").build(),
                        "fieldKey3", InstanceField.builder().key("fieldKey3").value("true").build(),))
                .build()

        Configuration configuration = Configuration
                .builder()
                .elements(List.of(ConfigurationElement
                        .builder()
                        .key("element1")
                        .fieldConfigurations(List.of(FieldConfiguration
                                .builder()
                                .key("field11")
                                .type(FieldConfiguration.Type.STRING)
                                .value("staticString")
                                .build(),
                                FieldConfiguration
                                        .builder()
                                        .key("field12")
                                        .type(FieldConfiguration.Type.DYNAMIC_STRING)
                                        .value("dynamicString")
                                        .build()))
                        .fieldCollectionConfigurations(List.of(FieldCollectionConfiguration
                                .builder()
                                .key("fieldCollection11")
                                .type(FieldCollectionConfiguration.Type.URL)
                                .values(List.of("http://www.example.com", "www.fintlabs.no"))
                                .build()))
                        .elements(List.of(ConfigurationElement
                                .builder()
                                .key("element11")
                                .fieldConfigurations(List.of(FieldConfiguration
                                        .builder()
                                        .key("field111")
                                        .type(FieldConfiguration.Type.BOOLEAN)
                                        .value("true")
                                        .build()))
                                .fieldCollectionConfigurations(Collections.emptyList())
                                .elements(Collections.emptyList())
                                .build()))
                        .build(),
                        ConfigurationElement
                                .builder()
                                .key("element2")
                                .fieldConfigurations(List.of(FieldConfiguration
                                        .builder()
                                        .key("field21")
                                        .type(FieldConfiguration.Type.STRING)
                                        .value("")
                                        .build()))
                                .fieldCollectionConfigurations(Collections.emptyList())
                                .elements(Collections.emptyList())
                                .build()))
                .build()

        when:
        MappedInstance mappedInstance = instanceMappingService.map(instance, configuration)

        then:
        mappedInstance == MappedInstance
                .builder()
                .elements(List.of(MappedInstanceElement
                        .builder()
                        .key("element1")
                        .fields(List.of(MappedInstanceField
                                .builder()
                                .key("field11")
                                .type(MappedInstanceField.Type.STRING)
                                .value("staticString")
                                .build(),
                                MappedInstanceField
                                        .builder()
                                        .key("field12")
                                        .type(MappedInstanceField.Type.STRING)
                                        .value("dynamicString")
                                        .build()))
                        .fieldCollections(List.of(MappedInstanceFieldCollection
                                .builder()
                                .key("fieldCollection11")
                                .type(MappedInstanceFieldCollection.Type.URL)
                                .values(List.of("http://www.example.com", "www.fintlabs.no"))
                                .build()))
                        .elements(List.of(MappedInstanceElement
                                .builder()
                                .key("element11")
                                .fields(List.of(MappedInstanceField
                                        .builder()
                                        .key("field111")
                                        .type(MappedInstanceField.Type.BOOLEAN)
                                        .value("true")
                                        .build()))
                                .fieldCollections(Collections.emptyList())
                                .elements(Collections.emptyList())
                                .build()))
                        .build(),
                        MappedInstanceElement
                                .builder()
                                .key("element2")
                                .fields(List.of(MappedInstanceField
                                        .builder()
                                        .key("field21")
                                        .type(MappedInstanceField.Type.STRING)
                                        .value("")
                                        .build()))
                                .fieldCollections(Collections.emptyList())
                                .elements(Collections.emptyList())
                                .build()))
                .build()
    }
}
