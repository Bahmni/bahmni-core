package org.bahmni.module.bahmnicore.web.v1_0.controller.condition;

import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.Patient;
import org.openmrs.api.ConditionService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniConditionConceptSaveService;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.emrapi.conditionslist.ConditionListConstants;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.openmrs.util.LocaleUtility.getDefaultLocale;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmni/terminologies")
public class BahmniConditionController {


    ConditionService conditionService;

    BahmniConditionConceptSaveService bahmniConditionConceptSaveService;

    @Autowired
    public BahmniConditionController(ConditionService conditionService, BahmniConditionConceptSaveService bahmniConditionConceptSaveService) {
        this.conditionService = conditionService;
        this.bahmniConditionConceptSaveService = bahmniConditionConceptSaveService;

    }

    /**
     * Method to convert clinical status of core condition objects into status of condition objects of the emrapi module
     *
     * @param clinicalStatus - a ConditionClinicalStatus object
     * @return convertedStatus
     */
    private Condition.Status convertClinicalStatus(ConditionClinicalStatus clinicalStatus) {
        Condition.Status convertedStatus = Condition.Status.ACTIVE;

        if (clinicalStatus == ConditionClinicalStatus.ACTIVE) {
            convertedStatus = Condition.Status.ACTIVE;
        } else if (clinicalStatus == ConditionClinicalStatus.INACTIVE) {
            convertedStatus = Condition.Status.INACTIVE;
        } else if (clinicalStatus == ConditionClinicalStatus.HISTORY_OF) {
            convertedStatus = Condition.Status.HISTORY_OF;
        }

        return convertedStatus;
    }

    /**
     * Method to convert an org.openmrs.Concept object into an
     * org.openmrs.module.emrapi.conditionslist.contract.Concept object
     *
     * @param coreConcept - a Condition.Status object
     * @return concept
     */
    private org.openmrs.module.emrapi.conditionslist.contract.Concept convertCoreConceptToEmrapiConcept(Concept coreConcept) {
        ConceptName fullySpecifiedName = coreConcept.getFullySpecifiedName(Context.getLocale());
        if (fullySpecifiedName == null) {
            fullySpecifiedName = coreConcept.getFullySpecifiedName(getDefaultLocale());
        }
        if (fullySpecifiedName == null) {
            fullySpecifiedName = coreConcept.getFullySpecifiedName(new Locale("en"));
        }
        org.openmrs.module.emrapi.conditionslist.contract.Concept concept =
                new org.openmrs.module.emrapi.conditionslist.contract.Concept(coreConcept.getUuid(),
                        fullySpecifiedName.getName());
        ConceptName shortName = coreConcept.getShortNameInLocale(Context.getLocale());

        if (shortName != null) {
            concept.setShortName(shortName.getName());
        }
        return concept;
    }

    /**
     * Method to convert an org.openmrs.Condition object into an org.openmrs.module.emrapi.conditionslist.contract.Condition
     * object
     *
     * @param coreCondition -a core condition object
     * @return contractCondition
     */
    private org.openmrs.module.emrapi.conditionslist.contract.Condition convertCoreConditionToEmrapiContractCondition(org.openmrs.Condition coreCondition) {
        org.openmrs.module.emrapi.conditionslist.contract.Concept concept = new org.openmrs.module.emrapi.conditionslist.contract.Concept();
        if (coreCondition.getCondition().getCoded() != null) {
            concept = convertCoreConceptToEmrapiConcept(coreCondition.getCondition().getCoded());
        }
        org.openmrs.module.emrapi.conditionslist.contract.Condition contractCondition = new org.openmrs.module.emrapi.conditionslist.contract.Condition();
        CodedOrFreeText codedOrFreeText = coreCondition.getCondition();

        contractCondition.setUuid(coreCondition.getUuid());
        contractCondition.setAdditionalDetail(coreCondition.getAdditionalDetail());
        contractCondition.setStatus(convertClinicalStatus(coreCondition.getClinicalStatus()));
        contractCondition.setConcept(concept);
        contractCondition.setPatientUuid(coreCondition.getPatient().getUuid());
        contractCondition.setConditionNonCoded(codedOrFreeText.getNonCoded());
        contractCondition.setOnSetDate(coreCondition.getOnsetDate());
        contractCondition.setVoided(coreCondition.getVoided());
        contractCondition.setVoidReason(coreCondition.getVoidReason());
        contractCondition.setEndDate(coreCondition.getEndDate());
        contractCondition.setCreator(coreCondition.getCreator().getUuid());
        contractCondition.setDateCreated(coreCondition.getDateCreated());
        if (coreCondition.getPreviousVersion() != null) {
            contractCondition.setPreviousConditionUuid(convertCoreConditionToEmrapiCondition(coreCondition.getPreviousVersion()).getUuid());
        }

        return contractCondition;
    }

    /**
     * Method to convert an org.openmrs.Condition object into an org.openmrs.module.emrapi.conditionslist.Condition
     * object
     *
     * @param coreCondition - an org.openmrs.Condition object
     * @return cListCondition
     */
    private org.openmrs.module.emrapi.conditionslist.Condition convertCoreConditionToEmrapiCondition(org.openmrs.Condition coreCondition) {
        org.openmrs.module.emrapi.conditionslist.Condition cListCondition = new org.openmrs.module.emrapi.conditionslist.Condition();
        Concept concept;

        if (coreCondition.getCondition().getCoded() != null) {
            concept = Context.getConceptService()
                    .getConceptByUuid(coreCondition.getCondition().getCoded().getUuid());

            if (coreCondition.getCondition().getSpecificName() == null) {
                coreCondition.getCondition().setSpecificName(coreCondition.getCondition().getCoded().getName(Context.getLocale()));
            }
        } else {
            concept = new Concept();
        }

        cListCondition.setUuid(coreCondition.getUuid());
        cListCondition.setConcept(concept);
        cListCondition.setAdditionalDetail(coreCondition.getAdditionalDetail());
        cListCondition.setPatient(coreCondition.getPatient());
        cListCondition.setConditionNonCoded(coreCondition.getCondition().getNonCoded());
        cListCondition.setOnsetDate(coreCondition.getOnsetDate());
        cListCondition.setVoided(coreCondition.getVoided());
        cListCondition.setVoidReason(coreCondition.getVoidReason());
        cListCondition.setEndDate(coreCondition.getEndDate());
        cListCondition.setCreator(coreCondition.getCreator());
        cListCondition.setDateCreated(coreCondition.getDateCreated());
        cListCondition.setStatus(convertClinicalStatus(coreCondition.getClinicalStatus()));
        if (coreCondition.getPreviousVersion() != null) {
            cListCondition.setPreviousCondition(convertCoreConditionToEmrapiCondition(coreCondition.getPreviousVersion()));
        }

        return cListCondition;
    }

    /**
     * Method to convert an org.openmrs.module.emrapi.conditionslist.contract.Condition object into an org.openmrs.Condition
     * object
     *
     * @param condition - an org.openmrs.module.emrapi.conditionslist.contract.Condition object
     * @return cond
     */
    private org.openmrs.Condition convertEmrapiContractConditionToCoreCondition(org.openmrs.module.emrapi.conditionslist.contract.Condition condition) {
        bahmniConditionConceptSaveService.update(condition);
        Concept concept = Context.getConceptService().getConceptByUuid(condition.getConcept().getUuid());
        Patient patient = Context.getPatientService().getPatientByUuid(condition.getPatientUuid());
        String nonCodedConditionConcept = Context.getAdministrationService().getGlobalProperty(
                ConditionListConstants.GLOBAL_PROPERTY_NON_CODED_UUID);

        org.openmrs.Condition openmrsCondition = new org.openmrs.Condition();
        CodedOrFreeText codedOrFreeText;

        if (!isEmpty(condition.getConditionNonCoded())) {
            concept = Context.getConceptService().getConceptByUuid(nonCodedConditionConcept);
        }
        if (condition.getUuid() != null) {
            openmrsCondition.setUuid(condition.getUuid());
        }

        if (concept != null) {
            ConceptName fullySpecifiedName = getName(concept.getNames(), condition.getConcept().getName());
            codedOrFreeText = new CodedOrFreeText(concept, fullySpecifiedName, condition.getConditionNonCoded());
        } else {
            codedOrFreeText = new CodedOrFreeText(null, null, condition.getConditionNonCoded());
        }

        openmrsCondition.setAdditionalDetail(condition.getAdditionalDetail());
        openmrsCondition.setClinicalStatus(convertConditionListStatus(condition.getStatus()));
        openmrsCondition.setCondition(codedOrFreeText);
        openmrsCondition.setPatient(patient);
        openmrsCondition.setOnsetDate(condition.getOnSetDate());
        openmrsCondition.setEndDate(condition.getEndDate());
        openmrsCondition.setVoided(condition.getVoided());
        openmrsCondition.setVoidReason(condition.getVoidReason());

        return openmrsCondition;
    }

    /**
     * Method to convert Condition.Status object of the emrapi module into ConditionClinicalStatus object of the core module
     *
     * @param status - a Condition.Status object
     * @return convertedStatus
     */
    private ConditionClinicalStatus convertConditionListStatus(Condition.Status status) {
        ConditionClinicalStatus convertedStatus = ConditionClinicalStatus.ACTIVE;

        if (status == Condition.Status.ACTIVE) {
            convertedStatus = ConditionClinicalStatus.ACTIVE;
        } else if (status == Condition.Status.INACTIVE) {
            convertedStatus = ConditionClinicalStatus.INACTIVE;
        } else if (status == Condition.Status.HISTORY_OF) {
            convertedStatus = ConditionClinicalStatus.HISTORY_OF;
        }

        return convertedStatus;
    }

    /**
     * Method to return a concept name
     *
     * @param names - a collection of concept name objects
     * @param name  - name of a concept
     * @return null or currentName
     */
    private ConceptName getName(Collection<ConceptName> names, String name) {
        if (name == null) {
            return null;
        }

        for (ConceptName currentName : names) {
            if (name.equalsIgnoreCase(currentName.getName())) {
                return currentName;
            }
        }

        return null;
    }


    /**
     * Saves a condition.
     *
     * @param conditions - a list of conditions to be saved
     */
    @RequestMapping(method = RequestMethod.POST, value = "/condition")
    @ResponseBody
    public List<org.openmrs.module.emrapi.conditionslist.contract.Condition> save(@RequestBody org.openmrs.module.emrapi.conditionslist.contract.Condition[] conditions) {

        List<org.openmrs.module.emrapi.conditionslist.contract.Condition> savedConditions = new ArrayList<>();

        for (org.openmrs.module.emrapi.conditionslist.contract.Condition condition : conditions) {
            org.openmrs.Condition savedCondition = conditionService.saveCondition(
                    convertEmrapiContractConditionToCoreCondition(condition));
            savedConditions.add(convertCoreConditionToEmrapiContractCondition(savedCondition));
        }
        return savedConditions;
    }
}
