package org.bahmni.module.admin.csv.patientmatchingalgorithm;

import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.patientmatchingalgorithm.exception.CannotMatchPatientException;
import org.openmrs.Patient;

import java.util.List;

public abstract class PatientMatchingAlgorithm {
    public String valueFor(String keyToSearch, List<KeyValue> patientAttributes) {
        for (KeyValue patientAttributeKeyValue : patientAttributes) {
            if (patientAttributeKeyValue.getKey().equalsIgnoreCase(keyToSearch)) {
                return patientAttributeKeyValue.getValue();
            }
        }
        return null;
    }

    public abstract Patient run(List<Patient> patientList, List<KeyValue> patientAttributes) throws CannotMatchPatientException;
}
