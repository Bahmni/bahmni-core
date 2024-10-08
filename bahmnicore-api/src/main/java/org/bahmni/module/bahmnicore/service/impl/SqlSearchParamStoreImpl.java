package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.service.SqlSearchParamStore;
import org.openmrs.Concept;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class SqlSearchParamStoreImpl implements SqlSearchParamStore {
    public static final String BAHMNI_PRIMARY_IDENTIFIER_TYPE = "bahmni.primaryIdentifierType";
    public static final String ADT_NOTES_CONCEPT = "Adt Notes";
    public static final String VISIT_DIAGNOSES_CONCEPT = "Visit Diagnoses";
    public static final String CODED_DIAGNOSIS_CONCEPT = "Coded Diagnosis";
    public static final String NON_CODED_DIAGNOSIS_CONCEPT = "Non-coded Diagnosis";
    public static final String DIAGNOSIS_CERTAINTY_CONCEPT = "Diagnosis Certainty";
    public static final String DIAGNOSIS_ORDER_CONCEPT = "Diagnosis order";
    public static final String ADMISSION_ENCOUNTER_TYPE = "ADMISSION";
    public static final String DISPOSITION_CONCEPT = "Disposition";
    private AdministrationService administrationService;
    private boolean initialized = false;
    private Map<String, Object> parameters = new HashMap<>();

    public void setAdministrationService(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    private Map<String, String> conceptNameFieldMap = new HashMap<String, String>() {{
        put(ADT_NOTES_CONCEPT, "adt_notes_concept_id");
        put(VISIT_DIAGNOSES_CONCEPT, "visit_diagnoses_concept_id");
        put(CODED_DIAGNOSIS_CONCEPT, "coded_diagnosis_concept_id");
        put(NON_CODED_DIAGNOSIS_CONCEPT, "non_coded_diagnosis_concept_id");
        put(DIAGNOSIS_CERTAINTY_CONCEPT, "diagnosis_certainty_concept_id");
        put(DIAGNOSIS_ORDER_CONCEPT, "diagnosis_order_concept_id");
        put(DISPOSITION_CONCEPT, "disposition_concept_id");
    }};

    @Override
    public synchronized void initQueryParamStore() {
        if (this.initialized) return;
        parameters.clear();
        String primaryIdentifierProperty = administrationService.getGlobalProperty(BAHMNI_PRIMARY_IDENTIFIER_TYPE);
        Optional.ofNullable(Context.getPatientService().getPatientIdentifierTypeByUuid(primaryIdentifierProperty))
                .ifPresent(identifierType -> parameters.put("patient_identifier_type_id", identifierType.getPatientIdentifierTypeId()));
        Optional.ofNullable(Context.getEncounterService().getEncounterType(ADMISSION_ENCOUNTER_TYPE))
                .ifPresent(encounterType -> parameters.put("admission_encounter_type_id", encounterType.getEncounterTypeId()));
        Context.getConceptService().getConceptsByName(ADT_NOTES_CONCEPT, Locale.ENGLISH, true)
                .stream()
                .findFirst()
                .ifPresent(concept -> parameters.put(conceptNameFieldMap.get(ADT_NOTES_CONCEPT), concept.getConceptId()));
        Context.getConceptService().getConceptsByName(DISPOSITION_CONCEPT, Locale.ENGLISH, true)
                .stream()
                .findFirst()
                .ifPresent(concept -> parameters.put(conceptNameFieldMap.get(DISPOSITION_CONCEPT), concept.getConceptId()));
        Optional<Concept> visitDiagnosisConcept = Context.getConceptService().getConceptsByName(VISIT_DIAGNOSES_CONCEPT, Locale.ENGLISH, true).stream().findFirst();
        if (visitDiagnosisConcept.isPresent()) {
            parameters.put(conceptNameFieldMap.get(VISIT_DIAGNOSES_CONCEPT), visitDiagnosisConcept.get().getConceptId());
            //noinspection DuplicatedCode
            visitDiagnosisConcept.get().getSetMembers().stream().filter(member -> {
                return matchMemberByName(member, CODED_DIAGNOSIS_CONCEPT);
            }).findFirst().ifPresent(concept -> parameters.put(conceptNameFieldMap.get(CODED_DIAGNOSIS_CONCEPT), concept.getConceptId()));

            visitDiagnosisConcept.get().getSetMembers().stream().filter(member -> {
                return matchMemberByName(member, NON_CODED_DIAGNOSIS_CONCEPT);
            }).findFirst().ifPresent(concept -> parameters.put(conceptNameFieldMap.get(NON_CODED_DIAGNOSIS_CONCEPT), concept.getConceptId()));
            //noinspection DuplicatedCode
            visitDiagnosisConcept.get().getSetMembers().stream().filter(member -> {
                return matchMemberByName(member, DIAGNOSIS_CERTAINTY_CONCEPT);
            }).findFirst().ifPresent(concept -> parameters.put(conceptNameFieldMap.get(DIAGNOSIS_CERTAINTY_CONCEPT), concept.getConceptId()));

            visitDiagnosisConcept.get().getSetMembers().stream().filter(member -> {
                return matchMemberByName(member, DIAGNOSIS_ORDER_CONCEPT);
            }).findFirst().ifPresent(concept -> parameters.put(conceptNameFieldMap.get(DIAGNOSIS_ORDER_CONCEPT), concept.getConceptId()));
        }
        this.initialized = true;
    }

    private boolean matchMemberByName(Concept member, String name) {
        return member.getNames().stream().anyMatch(conceptName -> conceptName.getName().equals(name)
                && conceptName.getConceptNameType().equals(ConceptNameType.FULLY_SPECIFIED)
                && conceptName.getLocale().equals(Locale.ENGLISH));
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public Map<String, Object> getSearchableParameters() {
        return this.parameters;
    }
}
