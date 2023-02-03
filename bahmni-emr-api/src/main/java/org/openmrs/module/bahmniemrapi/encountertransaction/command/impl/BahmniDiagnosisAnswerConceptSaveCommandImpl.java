package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.apache.commons.lang.StringUtils;
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
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPreSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class BahmniDiagnosisAnswerConceptSaveCommandImpl implements EncounterDataPreSaveCommand {

    private AdministrationService adminService;

    private ConceptService conceptService;

    private EmrApiProperties emrApiProperties;

    private TerminologyLookupService terminologyLookupService;

    private FhirConceptSourceService conceptSourceService;

    public static final String CONCEPT_CLASS_DIAGNOSIS = "Diagnosis";

    public static final String CONCEPT_DATATYPE_NA = "N/A";

    public static final String DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT = "Unclassified";

    public static final String GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID = "bahmni.diagnosisSetForNewDiagnosisConcepts";

    private static final String TERMINOLOGY_SERVER_CODED_ANSWER_DELIMITER = "/";

    @Autowired
    public BahmniDiagnosisAnswerConceptSaveCommandImpl(@Qualifier("adminService") AdministrationService administrationService, ConceptService conceptService,
                                                       EmrApiProperties emrApiProperties, TerminologyLookupService terminologyLookupService,
                                                       FhirConceptSourceService conceptSourceService) {
        this.adminService = administrationService;
        this.conceptService = conceptService;
        this.emrApiProperties = emrApiProperties;
        this.terminologyLookupService = terminologyLookupService;
        this.conceptSourceService = conceptSourceService;
    }

    @Override
    public BahmniEncounterTransaction update(BahmniEncounterTransaction bahmniEncounterTransaction) {
        List<BahmniDiagnosisRequest> bahmniDiagnoses = bahmniEncounterTransaction.getBahmniDiagnoses();
        bahmniDiagnoses.stream().forEach(this::updateDiagnosisAnswerConceptUuid);
        return bahmniEncounterTransaction;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private void updateDiagnosisAnswerConceptUuid(BahmniDiagnosisRequest bahmniDiagnosis) {
        EncounterTransaction.Concept codedAnswer = bahmniDiagnosis.getCodedAnswer();
        if(codedAnswer != null) {
            String codedAnswerUuidWithSystem = codedAnswer.getUuid();
            int conceptCodeIndex = codedAnswerUuidWithSystem.lastIndexOf(TERMINOLOGY_SERVER_CODED_ANSWER_DELIMITER);
            boolean isConceptFromTerminologyServer = conceptCodeIndex > -1 ? true : false;
            if(isConceptFromTerminologyServer) {
                String diagnosisConceptSystem = codedAnswerUuidWithSystem.substring(0, conceptCodeIndex);
                String diagnosisConceptReferenceTermCode = codedAnswerUuidWithSystem.substring(conceptCodeIndex + 1);
                updateDiagnosisAnswerConceptUuid(codedAnswer, diagnosisConceptReferenceTermCode, diagnosisConceptSystem);
            }
        }
    }

    private void updateDiagnosisAnswerConceptUuid(EncounterTransaction.Concept codedAnswer, String diagnosisConceptReferenceTermCode, String diagnosisConceptSystem) {
        Optional<ConceptSource> conceptSourceByUrl = conceptSourceService.getConceptSourceByUrl(diagnosisConceptSystem);
        ConceptSource conceptSource = conceptSourceByUrl.isPresent() ? conceptSourceByUrl.get() : null;
        if(conceptSource == null)
            throw new APIException("Concept Source " + diagnosisConceptSystem + " not found");
        Concept existingDiagnosisAnswerConcept = conceptService.getConceptByMapping(diagnosisConceptReferenceTermCode, conceptSource.getName());
        if(existingDiagnosisAnswerConcept == null) {
            Concept newDiagnosisAnswerConcept = createNewDiagnosisConcept(diagnosisConceptReferenceTermCode, conceptSource);
            codedAnswer.setUuid(newDiagnosisAnswerConcept.getUuid());
            addNewDiagnosisConceptToDiagnosisSet(newDiagnosisAnswerConcept);
        } else {
            ConceptName answerConceptNameInUserLocale = existingDiagnosisAnswerConcept.getFullySpecifiedName(Context.getLocale());
            if(answerConceptNameInUserLocale == null)
                updateExistingConcept(existingDiagnosisAnswerConcept, diagnosisConceptReferenceTermCode);
            codedAnswer.setUuid(existingDiagnosisAnswerConcept.getUuid());
        }
    }

    private Concept createNewDiagnosisConcept(String conceptReferenceTermCode, ConceptSource conceptSource) {
        Concept concept = getConcept(conceptReferenceTermCode);
        addConceptMap(concept, conceptSource, conceptReferenceTermCode);
        conceptService.saveConcept(concept);
        return concept;
    }

    private void updateExistingConcept(Concept existingDiagnosisAnswerConcept, String conceptReferenceTermCode) {
        Concept conceptInUserLocale = getConcept(conceptReferenceTermCode);
        conceptInUserLocale.getNames(Context.getLocale()).stream().forEach(conceptName -> existingDiagnosisAnswerConcept.addName(conceptName));
        conceptService.saveConcept(existingDiagnosisAnswerConcept);
    }


    private Concept getConcept(String referenceCode) {
        Concept concept = terminologyLookupService.getConcept(referenceCode, Context.getLocale().getLanguage());
        ConceptClass diagnosisConceptClass = conceptService.getConceptClassByName(CONCEPT_CLASS_DIAGNOSIS);
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

    private void addNewDiagnosisConceptToDiagnosisSet(Concept diagnosisConcept) {
        String diagnosisSetForNewConcepts = adminService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID);
        Concept diagnosisConceptSet = null;
        if(StringUtils.isNotBlank(diagnosisSetForNewConcepts)) {
            diagnosisConceptSet = conceptService.getConceptByUuid(diagnosisSetForNewConcepts);
        } else {
            Collection<Concept> diagnosisSets = emrApiProperties.getDiagnosisSets();
            Optional<Concept> optionalConcept = diagnosisSets.stream().filter(c -> c.getName().getName().equals(DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT) && !c.getRetired()).findFirst();
            if(optionalConcept.isPresent()) {
                diagnosisConceptSet = optionalConcept.get();
            }
        }
        if(diagnosisConceptSet == null)
            throw new APIException("Concept Set " + DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT + " not found");

        diagnosisConceptSet.addSetMember(diagnosisConcept);
        conceptService.saveConcept(diagnosisConceptSet);
    }
}
