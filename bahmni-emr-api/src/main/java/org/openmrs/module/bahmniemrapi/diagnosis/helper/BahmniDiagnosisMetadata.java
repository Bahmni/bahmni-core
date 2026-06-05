package org.openmrs.module.bahmniemrapi.diagnosis.helper;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Component
public class BahmniDiagnosisMetadata {

    private static final String BAHMNI_DIAGNOSIS_STATUS = "Bahmni Diagnosis Status";
    private static final String BAHMNI_DIAGNOSIS_REVISED = "Bahmni Diagnosis Revised";
    private static final String BAHMNI_INITIAL_DIAGNOSIS = "Bahmni Initial Diagnosis";

    private ObsService obsService;

    private ConceptService conceptService;

    private EmrApiProperties emrApiProperties;

    private EncounterTransactionMapper encounterTransactionMapper;

    private Concept getConceptsByNameAndLocale(String name, Locale locale) {
        List<Concept> conceptList = conceptService.getConceptsByName(name, locale, false);
        return conceptList.isEmpty() ? null : conceptList.get(0);
    }

    public Concept getBahmniInitialDiagnosisConcept() {
        return getConceptsByNameAndLocale(BAHMNI_INITIAL_DIAGNOSIS, Locale.ENGLISH);
    }

    public Concept getBahmniDiagnosisRevisedConcept() {
        return getConceptsByNameAndLocale(BAHMNI_DIAGNOSIS_REVISED, Locale.ENGLISH);
    }

    public Concept getBahmniDiagnosisStatusConcept() {
        return getConceptsByNameAndLocale(BAHMNI_DIAGNOSIS_STATUS, Locale.ENGLISH);
    }

    @Autowired
    public BahmniDiagnosisMetadata(ObsService obsService, ConceptService conceptService,
                                   EmrApiProperties emrApiProperties,
                                   EncounterTransactionMapper encounterTransactionMapper) {
        this.obsService = obsService;
        this.conceptService = conceptService;
        this.emrApiProperties = emrApiProperties;
        this.encounterTransactionMapper = encounterTransactionMapper;
    }

    public String findInitialDiagnosisUuid(Obs visitDiagnosisObs) {
        for (Obs obs : visitDiagnosisObs.getGroupMembers()) {
            if (obs.getConcept().getName().getName().equals(BAHMNI_INITIAL_DIAGNOSIS)) {
                return obs.getValueText();
            }
        }
        return null;
    }

    public List<BahmniDiagnosisRequest> map(List<EncounterTransaction.Diagnosis> diagnoses, boolean includeAll) {
        List<BahmniDiagnosisRequest> bahmniDiagnoses = new ArrayList<>();
        for (EncounterTransaction.Diagnosis diagnosis : diagnoses) {
            bahmniDiagnoses.add(mapBahmniDiagnosis(diagnosis, null, true, includeAll, diagnosisSchemaContainsStatus(), true));
        }
        return bahmniDiagnoses;
    }

    public BahmniDiagnosisRequest mapBahmniDiagnosis(EncounterTransaction.Diagnosis diagnosis,
                                                     EncounterTransaction.Diagnosis latestDiagnosis,
                                                     boolean mapFirstDiagnosis, boolean includeAll,
                                                     boolean diagnosisSchemaContainsStatus,
                                                     boolean includeRevisedDiagnosis) {
        BahmniDiagnosisRequest bahmniDiagnosis = mapBasicDiagnosis(diagnosis);

        Obs diagnosisObsGroup = obsService.getObsByUuid(diagnosis.getExistingObs());
        HashMap<String, Obs> requiredObs = getRequiredObs(diagnosisObsGroup);
        Obs revisedObs = requiredObs.get(BAHMNI_DIAGNOSIS_REVISED);
        if (revisedObs.getValueAsBoolean() && !includeRevisedDiagnosis)
            return null;
        if (diagnosisSchemaContainsStatus) {
            Obs statusObs = requiredObs.get(BAHMNI_DIAGNOSIS_STATUS);
            if (statusObs != null) {
                Concept statusConcept = statusObs.getValueCoded();
                if (statusConcept != null) {
                    bahmniDiagnosis.setDiagnosisStatusConcept(new EncounterTransaction.Concept(statusConcept.getUuid(), statusConcept.getName().getName()));
                }
            }
        }

        if (mapFirstDiagnosis) {
            Obs initialObs = requiredObs.get(BAHMNI_INITIAL_DIAGNOSIS);
            Obs initialDiagnosisObsGroup = obsService.getObsByUuid(initialObs.getValueText());

            if(initialDiagnosisObsGroup.getVoided()){
                initialDiagnosisObsGroup = diagnosisObsGroup;
                initialObs.setValueText(diagnosisObsGroup.getUuid());
                obsService.saveObs(initialObs,"Initial obs got voided");
            }
            EncounterTransaction encounterTransactionWithInitialDiagnosis = encounterTransactionMapper.map(initialDiagnosisObsGroup.getEncounter(), includeAll);
            EncounterTransaction.Diagnosis initialDiagnosis = findInitialDiagnosis(encounterTransactionWithInitialDiagnosis, initialDiagnosisObsGroup);
            bahmniDiagnosis.setFirstDiagnosis(mapBahmniDiagnosis(initialDiagnosis, null, false, includeAll, diagnosisSchemaContainsStatus, true));
        }

        if (latestDiagnosis != null) {
            bahmniDiagnosis.setLatestDiagnosis(mapBahmniDiagnosis(latestDiagnosis, null, false, includeAll, diagnosisSchemaContainsStatus, true));
        }

        bahmniDiagnosis.setRevised(revisedObs.getValueAsBoolean());
        bahmniDiagnosis.setComments(diagnosisObsGroup.getComment());

        bahmniDiagnosis.setEncounterUuid(diagnosisObsGroup.getEncounter().getUuid());
        bahmniDiagnosis.setCreatorName(diagnosisObsGroup.getCreator().getPersonName().toString());
        return bahmniDiagnosis;
    }

    public boolean diagnosisSchemaContainsStatus() {
        Concept diagnosisSetConcept = getDiagnosisSetConcept();
        return diagnosisSetConcept.getSetMembers().contains(getBahmniDiagnosisStatusConcept());
    }

    public Obs findInitialDiagnosis(Obs diagnosisObsGroup) {
        return findObs(diagnosisObsGroup, BAHMNI_INITIAL_DIAGNOSIS);
    }

    public Diagnosis buildDiagnosisFromObsGroup(Obs diagnosisObsGroup, Collection<Concept> nonDiagnosisConcepts, Collection<Concept> nonDiagnosisConceptSets) {
        if (diagnosisObsGroup == null)
            return null;

        Diagnosis diagnosis = emrApiProperties.getDiagnosisMetadata().toDiagnosis(diagnosisObsGroup);

        Set<Concept> filter = new HashSet<>();
        filter.addAll(nonDiagnosisConcepts);
        for (Concept conceptSet : nonDiagnosisConceptSets) {
            filter.addAll(conceptSet.getSetMembers());
        }

        if (!filter.contains(diagnosis.getDiagnosis().getCodedAnswer())) {
            return diagnosis;
        }
        return null;
    }

    public Concept getDiagnosisSetConcept() {
        return emrApiProperties.getDiagnosisMetadata().getDiagnosisSetConcept();
    }

    public boolean isDiagnosis(Obs obs) {
        return obs.getConcept().equals(getDiagnosisSetConcept());
    }

    public Concept getCodedDiagnosisConcept() {
        return emrApiProperties.getDiagnosisMetadata().getCodedDiagnosisConcept();
    }

    public Concept getNonCodedDiagnosisConcept() {
        return emrApiProperties.getDiagnosisMetadata().getNonCodedDiagnosisConcept();
    }

    public Obs findMatchingDiagnosis(Collection<Obs> observations, BahmniDiagnosis bahmniDiagnosis) {
        List<Obs> matchingObs = observations.stream()
                .filter(obs -> isDiagnosis(obs))
                .filter(obs -> isDiagnosisNotRevised(obs))
                .filter(obs -> isDiagnosisMatching(obs, bahmniDiagnosis)).collect(toList());
        if (matchingObs.size() > 1) throw new RuntimeException("The same diagnosis cannot be saved more than once");
        return matchingObs.isEmpty()? null: matchingObs.get(0);
    }

    private boolean isDiagnosisNotRevised(Obs obs) {
        return !obs.getGroupMembers(false).stream()
                .anyMatch(groupMember -> {
                    return groupMember.getConcept().equals(getBahmniDiagnosisRevisedConcept())
                            && groupMember.getValueAsBoolean();});
    }

    private boolean isDiagnosisMatching(Obs obs, EncounterTransaction.Diagnosis diagnosis) {
        return obs.getGroupMembers().stream()
                .anyMatch(groupMember -> {
                            if (diagnosis.getCodedAnswer() != null &&
                                    groupMember.getConcept().equals(getCodedDiagnosisConcept())) {
                                return codedAnswersMatch(diagnosis, groupMember);
                            }
                            if (diagnosis.getFreeTextAnswer() != null &&
                                    groupMember.getConcept().equals(getNonCodedDiagnosisConcept())) {
                                return textAnswersMatch(diagnosis, groupMember);
                            }
                            return false;
                        }
                );
    }

    private boolean textAnswersMatch(EncounterTransaction.Diagnosis diagnosis, Obs obs1) {
        return obs1.getValueText().equals(diagnosis.getFreeTextAnswer());
    }

    private boolean codedAnswersMatch(EncounterTransaction.Diagnosis diagnosis, Obs obs1) {
        return obs1.getValueCoded().getUuid().equals(diagnosis.getCodedAnswer().getUuid())
                || obs1.getValueCoded().getName().equals(diagnosis.getCodedAnswer().getName());
    }

    private HashMap<String, Obs> getRequiredObs(Obs diagnosisObsGroup) {
        HashMap<String, Obs> requiredObs = new HashMap<>();
        for (Obs o : diagnosisObsGroup.getGroupMembers()) {
            Concept concept = o.getConcept();
            if (concept.hasName(BAHMNI_DIAGNOSIS_STATUS, null)) {
                requiredObs.put(BAHMNI_DIAGNOSIS_STATUS, o);
            } else if (concept.hasName(BAHMNI_DIAGNOSIS_REVISED, null)) {
                requiredObs.put(BAHMNI_DIAGNOSIS_REVISED, o);
            } else if (concept.hasName(BAHMNI_INITIAL_DIAGNOSIS, null)) {
                requiredObs.put(BAHMNI_INITIAL_DIAGNOSIS, o);
            }
        }
        return requiredObs;
    }

    private BahmniDiagnosisRequest mapBasicDiagnosis(EncounterTransaction.Diagnosis diagnosis) {
        BahmniDiagnosisRequest bahmniDiagnosis = new BahmniDiagnosisRequest();
        bahmniDiagnosis.setCertainty(diagnosis.getCertainty());
        bahmniDiagnosis.setCodedAnswer(diagnosis.getCodedAnswer());
        bahmniDiagnosis.setFreeTextAnswer(diagnosis.getFreeTextAnswer());
        bahmniDiagnosis.setOrder(diagnosis.getOrder());
        bahmniDiagnosis.setExistingObs(diagnosis.getExistingObs());
        bahmniDiagnosis.setDiagnosisDateTime(diagnosis.getDiagnosisDateTime());
        bahmniDiagnosis.setProviders(diagnosis.getProviders());
        return bahmniDiagnosis;
    }

    private EncounterTransaction.Diagnosis findInitialDiagnosis(EncounterTransaction encounterTransactionWithInitialDiagnosis, Obs initialDiagnosisObs) {
        for (EncounterTransaction.Diagnosis diagnosis : encounterTransactionWithInitialDiagnosis.getDiagnoses()) {
            if (diagnosis.getExistingObs().equals(initialDiagnosisObs.getUuid()))
                return diagnosis;
        }
        throw new AssertionError(String.format("Initial Diagnosis not found for: %s", initialDiagnosisObs.getUuid()));
    }

    private Obs findObs(Obs diagnosisObs, String conceptName) {
        for (Obs o : diagnosisObs.getGroupMembers()) {
            if (o.getConcept().hasName(conceptName, null)) {
                return o;
            }
        }
        return null;
    }
}
