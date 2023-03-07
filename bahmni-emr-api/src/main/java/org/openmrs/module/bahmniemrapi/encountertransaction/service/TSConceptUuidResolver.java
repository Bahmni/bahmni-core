package org.openmrs.module.bahmniemrapi.encountertransaction.service;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class TSConceptUuidResolver {
    public static final String CONCEPT_CLASS_DIAGNOSIS = "Diagnosis";
    public static final String CONCEPT_DATATYPE_NA = "N/A";
    public static final String DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT = "Unclassified";
    public static final String GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID = "bahmni.diagnosisSetForNewDiagnosisConcepts";
    private static final String TERMINOLOGY_SERVER_CODED_ANSWER_DELIMITER = "/";

    public AdministrationService adminService;
    private ConceptService conceptService;
    private EmrApiProperties emrApiProperties;
    private TerminologyLookupService terminologyLookupService;
    private FhirConceptSourceService conceptSourceService;
    private static Logger logger = LogManager.getLogger(TSConceptUuidResolver.class);

    @Autowired
    public TSConceptUuidResolver(@Qualifier("adminService") AdministrationService administrationService, ConceptService conceptService,
                                 EmrApiProperties emrApiProperties, @Qualifier("fhirTsServices") TerminologyLookupService terminologyLookupService,
                                 FhirConceptSourceService conceptSourceService) {
        this.adminService = administrationService;
        this.conceptService = conceptService;
        this.emrApiProperties = emrApiProperties;
        this.terminologyLookupService = terminologyLookupService;
        this.conceptSourceService = conceptSourceService;
    }


    protected void resolveConceptUuid(org.openmrs.module.emrapi.conditionslist.contract.Concept codedAnswer, String conceptClass, String codedConceptUuid) {
        String codedAnswerUuidWithSystem = codedAnswer.getUuid();
        String updatedConceptUuid = getUpdatedConceptUuid(codedAnswerUuidWithSystem, conceptClass, codedConceptUuid);
        codedAnswer.setUuid(updatedConceptUuid);
    }



    public void resolveConceptUuid(EncounterTransaction.Concept codedAnswer, String conceptClass, String codedConceptUuid) {
        String codedAnswerUuidWithSystem = codedAnswer.getUuid();
        String updatedConceptUuid = getUpdatedConceptUuid(codedAnswerUuidWithSystem, conceptClass, codedConceptUuid);
        codedAnswer.setUuid(updatedConceptUuid);
    }

    private String getUpdatedConceptUuid(String codedAnswerUuidWithSystem, String conceptClass, String codedConceptUuid) {
        int conceptCodeIndex = codedAnswerUuidWithSystem.lastIndexOf(TERMINOLOGY_SERVER_CODED_ANSWER_DELIMITER);
        boolean isConceptFromTerminologyServer = conceptCodeIndex > -1 ? true : false;
        if (isConceptFromTerminologyServer) {
            String diagnosisConceptSystem = codedAnswerUuidWithSystem.substring(0, conceptCodeIndex);
            String diagnosisConceptReferenceTermCode = codedAnswerUuidWithSystem.substring(conceptCodeIndex + 1);
            Optional<ConceptSource> conceptSourceByUrl = conceptSourceService.getConceptSourceByUrl(diagnosisConceptSystem);
            ConceptSource conceptSource = conceptSourceByUrl.isPresent() ? conceptSourceByUrl.get() : null;
            if (conceptSource == null) {
                logger.error("Concept Source " + diagnosisConceptSystem + " not found");
                throw new APIException("Concept Source " + diagnosisConceptSystem + " not found");
            }
            Concept existingDiagnosisAnswerConcept = conceptService.getConceptByMapping(diagnosisConceptReferenceTermCode, conceptSource.getName());
            if (existingDiagnosisAnswerConcept == null) {
                Concept newDiagnosisAnswerConcept = createNewConcept(diagnosisConceptReferenceTermCode, conceptSource, conceptClass);

                if(CONCEPT_CLASS_DIAGNOSIS.equalsIgnoreCase(conceptClass)) {
                   codedConceptUuid   = adminService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID);
                }
                addNewConceptToConceptSet(newDiagnosisAnswerConcept, codedConceptUuid);
                return newDiagnosisAnswerConcept.getUuid();
            } else {
                ConceptName answerConceptNameInUserLocale = existingDiagnosisAnswerConcept.getFullySpecifiedName(Context.getLocale());
                if (answerConceptNameInUserLocale == null)
                    updateExistingConcept(existingDiagnosisAnswerConcept, diagnosisConceptReferenceTermCode, conceptClass);
                return existingDiagnosisAnswerConcept.getUuid();
            }
        } else {
            return codedAnswerUuidWithSystem;
        }
    }

    private Concept createNewConcept(String conceptReferenceTermCode, ConceptSource conceptSource, String conceptClass) {
        Concept concept = getConcept(conceptReferenceTermCode, conceptClass);
        addConceptMap(concept, conceptSource, conceptReferenceTermCode);
        conceptService.saveConcept(concept);
        return concept;
    }

    private void updateExistingConcept(Concept existingDiagnosisAnswerConcept, String conceptReferenceTermCode, String conceptClass) {
        Concept conceptInUserLocale = getConcept(conceptReferenceTermCode, conceptClass);
        conceptInUserLocale.getNames(Context.getLocale()).stream().forEach(conceptName -> existingDiagnosisAnswerConcept.addName(conceptName));
        conceptService.saveConcept(existingDiagnosisAnswerConcept);
    }


    private Concept getConcept(String referenceCode, String conceptClass) {
        Concept concept = terminologyLookupService.getConcept(referenceCode, Context.getLocale().getLanguage());
        ConceptClass diagnosisConceptClass = conceptService.getConceptClassByName(conceptClass);
        concept.setConceptClass(diagnosisConceptClass);

        ConceptDatatype diagnosisConceptDataType = conceptService.getConceptDatatypeByName(CONCEPT_DATATYPE_NA);
        concept.setDatatype(diagnosisConceptDataType);

        return concept;
    }

    private void addConceptMap(Concept concept, ConceptSource conceptSource, String conceptReferenceTermCode) {
        ConceptMap conceptMap = getConceptMap(concept.getName().getName(), conceptReferenceTermCode, conceptSource);
        Collection<ConceptMap> conceptMappings = concept.getConceptMappings();
        conceptMappings.add(conceptMap);
        concept.setConceptMappings(conceptMappings);
    }

    private ConceptMap getConceptMap(String name, String conceptReferenceTermCode, ConceptSource conceptSource) {
        ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTerm(conceptSource, conceptReferenceTermCode, name);
        ConceptMapType sameAsConceptMapType = conceptService.getConceptMapTypeByUuid(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
        ConceptMap conceptMap = new ConceptMap(conceptReferenceTerm, sameAsConceptMapType);
        return conceptMap;
    }

    private void addNewConceptToConceptSet(Concept diagnosisConcept, String conceptSetUuid) {
        Concept diagnosisConceptSet = null;
        if (StringUtils.isNotBlank(conceptSetUuid)) {
            diagnosisConceptSet = conceptService.getConceptByUuid(conceptSetUuid);
        } else {
            Collection<Concept> diagnosisSets = emrApiProperties.getDiagnosisSets();
            Optional<Concept> optionalConcept = diagnosisSets.stream().filter(c -> c.getName().getName().equals(DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT) && !c.getRetired()).findFirst();
            if (optionalConcept.isPresent()) {
                diagnosisConceptSet = optionalConcept.get();
            }
        }
        if (diagnosisConceptSet == null) {
            if(StringUtils.isNotBlank(conceptSetUuid)) {
                logger.error("Concept Set with uuid " + conceptSetUuid + " not found");
                throw new APIException("Concept Set with uuid " + conceptSetUuid + " not found");
            } else {
                logger.error("Concept Set " + DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT + " not found");
                throw new APIException("Concept Set " + DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT + " not found");
            }
        }

        diagnosisConceptSet.addSetMember(diagnosisConcept);
        conceptService.saveConcept(diagnosisConceptSet);
    }
}
