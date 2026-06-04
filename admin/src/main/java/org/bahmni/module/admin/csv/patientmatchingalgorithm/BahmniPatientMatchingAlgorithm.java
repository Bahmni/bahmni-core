package org.bahmni.module.admin.csv.patientmatchingalgorithm;

import org.bahmni.csv.KeyValue;
import org.openmrs.Patient;

import java.util.List;

public class BahmniPatientMatchingAlgorithm extends PatientMatchingAlgorithm {
    @Override
    public Patient run(List<Patient> patientList, List<KeyValue> patientAttributes) {
        return patientList.size() > 0 ? patientList.get(0) : null;
    }
}
