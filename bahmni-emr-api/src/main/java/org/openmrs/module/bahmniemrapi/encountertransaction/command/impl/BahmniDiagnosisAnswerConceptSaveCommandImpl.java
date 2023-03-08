package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;


import org.apache.commons.lang.StringUtils;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.openmrs.Concept;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPreSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.TSConceptUuidResolver;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BahmniDiagnosisAnswerConceptSaveCommandImpl extends TSConceptUuidResolver implements EncounterDataPreSaveCommand {
    public static final String CONCEPT_CLASS_DIAGNOSIS = "Diagnosis";
    public static final String CONCEPT_DATATYPE_NA = "N/A";


    @Autowired
    public BahmniDiagnosisAnswerConceptSaveCommandImpl(@Qualifier("adminService") AdministrationService administrationService, ConceptService conceptService, EmrApiProperties emrApiProperties, @Qualifier("fhirTsServices") TerminologyLookupService terminologyLookupService, FhirConceptSourceService conceptSourceService) {
        super(administrationService, conceptService, emrApiProperties, terminologyLookupService, conceptSourceService);
    }

    @Override
    public BahmniEncounterTransaction update(BahmniEncounterTransaction bahmniEncounterTransaction) {
        List<BahmniDiagnosisRequest> bahmniDiagnoses = bahmniEncounterTransaction.getBahmniDiagnoses();
        bahmniDiagnoses.stream().forEach(this ::updateDiagnosisAnswerConceptUuid);
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
        String codedConceptUuid = adminService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID);
        EncounterTransaction.Concept codedAnswer = bahmniDiagnosis.getCodedAnswer();
        if (codedAnswer != null && codedAnswer.getUuid() != null) {
            Concept conceptSet;
            if (StringUtils.isNotBlank(codedConceptUuid)) {
                conceptSet = getConceptSetByUuid(codedConceptUuid);
            } else {
                conceptSet = getDefaultDiagnosisConceptSet();
            }
            resolveConceptUuid(codedAnswer, CONCEPT_CLASS_DIAGNOSIS, conceptSet, CONCEPT_DATATYPE_NA);
        }
    }

}
