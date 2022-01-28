package no.fintlabs;

import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.mapping.CaseMappingService;
import no.fintlabs.mapping.DocumentMappingService;
import no.fintlabs.mapping.FieldMappingService;
import no.fintlabs.mapping.RecordMappingService;
import no.fintlabs.model.configuration.CaseCreationStrategy;
import no.fintlabs.model.configuration.IntegrationConfiguration;
import no.fintlabs.model.instance.Instance;
import no.fintlabs.validation.InstanceFieldsValidationService;
import no.fintlabs.validation.MappedFieldsValidationService;
import no.fintlabs.validation.exceptions.MissingInstanceFieldsValidationException;
import no.fintlabs.validation.exceptions.MissingMappingFieldsValidationException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

// TODO: 28/01/2022 Name and placement?
@Service
public class CaseService {

    private final InstanceFieldsValidationService instanceFieldsValidationService;
    private final MappedFieldsValidationService mappedFieldsValidationService;

    private final FieldMappingService fieldMappingService;
    private final CaseMappingService caseMappingService;
    private final RecordMappingService recordMappingService;
    private final DocumentMappingService documentMappingService;

    public CaseService(
            InstanceFieldsValidationService instanceFieldsValidationService,
            MappedFieldsValidationService mappedFieldsValidationService,
            FieldMappingService fieldMappingService,
            CaseMappingService caseMappingService,
            RecordMappingService recordMappingService,
            DocumentMappingService documentMappingService
    ) {
        this.instanceFieldsValidationService = instanceFieldsValidationService;
        this.mappedFieldsValidationService = mappedFieldsValidationService;
        this.fieldMappingService = fieldMappingService;
        this.caseMappingService = caseMappingService;
        this.recordMappingService = recordMappingService;
        this.documentMappingService = documentMappingService;
    }

    public SakResource createOrUpdateCase(IntegrationConfiguration integrationConfiguration, Instance instance) {
        this.instanceFieldsValidationService.validate(integrationConfiguration, instance)
                .ifPresent(error -> {
                    throw new MissingInstanceFieldsValidationException(error);
                });

        Map<String, String> caseValuesByFieldKey = fieldMappingService.mapFields(integrationConfiguration.getCaseConfiguration().getFields(), instance.getFields());
        Map<String, String> recordValuesByFieldKey = fieldMappingService.mapFields(integrationConfiguration.getRecordConfiguration().getFields(), instance.getFields());
        Map<String, String> documentValuesByFieldKey = fieldMappingService.mapFields(integrationConfiguration.getDocumentConfiguration().getFields(), instance.getFields());

        this.mappedFieldsValidationService.validate(
                caseValuesByFieldKey,
                recordValuesByFieldKey,
                documentValuesByFieldKey
        ).ifPresent(error -> {
            throw new MissingMappingFieldsValidationException(error);
        });

        DokumentbeskrivelseResource dokumentbeskrivelseResource = this.documentMappingService.map(documentValuesByFieldKey);
        JournalpostResource journalpostResource = this.recordMappingService.map(recordValuesByFieldKey);
        journalpostResource.getDokumentbeskrivelse().add(dokumentbeskrivelseResource);

        SakResource sak = this.createOrFindExisingCase(
                integrationConfiguration.getCaseConfiguration().getCaseCreationStrategy(),
                caseValuesByFieldKey
        );

        sak.getJournalpost().add(journalpostResource);
        return sak;
    }

    private SakResource createOrFindExisingCase(CaseCreationStrategy caseCreationStrategy, Map<String, String> caseValuesByFieldKey) {
        switch (caseCreationStrategy) {
            case NEW:
                return this.caseMappingService.map(caseValuesByFieldKey);
            case EXISTING:
                return this.findExistingCaseWithFilter(null, null)
                        .orElse(this.caseMappingService.map(caseValuesByFieldKey));
            case COLLECTION:
                return this.findExistingCaseWithCaseNumber(null, null);
            default:
                throw new RuntimeException();
        }
    }

    private Optional<SakResource> findExistingCaseWithFilter(IntegrationConfiguration integrationConfiguration, Instance instance) {
        // TODO: 27/01/2022 Fail if multiple matching cases exist
        // TODO: 26/01/2022 Implement
        return Optional.empty();
    }

    private SakResource findExistingCaseWithCaseNumber(IntegrationConfiguration integrationConfiguration, Instance instance) {
        // TODO: 27/01/2022 Fail if no or multiple matching cases exist
        // TODO: 26/01/2022 Implement
        return new SakResource();
    }

}
