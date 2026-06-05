package org.bahmni.test.builder;

import org.openmrs.Concept;
import org.openmrs.Obs;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class DiagnosisBuilder {
    public static final String VISIT_DIAGNOSES = "Visit Diagnoses";
    public static final String BAHMNI_INITIAL_DIAGNOSIS = "Bahmni Initial Diagnosis";
    public static final String BAHMNI_DIAGNOSIS_REVISED = "Bahmni Diagnosis Revised";

    private Concept confirmedConcept = new ConceptBuilder().withName("Confirmed", Locale.getDefault()).withClass("Misc").withDataType("N/A").build();

    private Concept codedDiagnosisConcept = new ConceptBuilder().withName("Coded Diagnosis", Locale.getDefault()).withClass("Question").withDataType("Coded").build();
    private Concept certaintyConcept = new ConceptBuilder().withName("Diagnosis Certainty", Locale.getDefault()).withClass("Question").withDataType("Coded").build();
    private Concept visitDiagnosesConcept = new ConceptBuilder().withName(VISIT_DIAGNOSES, Locale.getDefault()).withDataType("N/A").build();

    private String diagnosisObsUUID = UUID.randomUUID().toString();
    protected Obs visitDiagnosesObs;


    public DiagnosisBuilder() {
        visitDiagnosesObs = new Obs();
    }

    public DiagnosisBuilder withUuid(String uuid) {
        this.diagnosisObsUUID = uuid;
        return this;
    }

    public DiagnosisBuilder withDefaults() {
        Concept malariaConcept = new ConceptBuilder().withName("Malaria", Locale.getDefault()).withClass("Diagnosis").build();

        Obs codedDiagnosisObs = new ObsBuilder().withConcept(codedDiagnosisConcept).withValue(malariaConcept).build();
        Obs certaintyObs = new ObsBuilder().withConcept(certaintyConcept).withValue(confirmedConcept).build();

        visitDiagnosesObs = new ObsBuilder().withValue("").withUUID(diagnosisObsUUID).withConcept(visitDiagnosesConcept).withGroupMembers(codedDiagnosisObs, certaintyObs).build();

        return this;
    }


    public DiagnosisBuilder withFirstObs(String firstVisitDiagnosisObsUuid) {
        Obs bahmniInitialObs = new ObsBuilder().withConcept(BAHMNI_INITIAL_DIAGNOSIS, Locale.getDefault()).withValue(firstVisitDiagnosisObsUuid).build();

        addChildObs(bahmniInitialObs, visitDiagnosesObs);
        return this;
    }

    public DiagnosisBuilder withChildObs(Obs childObs){
        addChildObs(childObs,visitDiagnosesObs);
        return this;
    }

    private Obs addChildObs(Obs childObs, Obs parentObs) {
        Set<Obs> groupMembers = parentObs.getGroupMembers(true);
        if (groupMembers == null)
            groupMembers = new HashSet<>();

        groupMembers.add(childObs);

        parentObs.setGroupMembers(groupMembers);
        return parentObs;
    }

    public Obs build() {
        return visitDiagnosesObs;
    }

}
