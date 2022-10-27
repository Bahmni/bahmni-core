package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.patient.response.PatientConfigResponse;
import org.openmrs.Patient;
import org.openmrs.RelationshipType;
import org.openmrs.annotation.Authorized;

import java.util.List;

public interface BahmniPatientService {
    public PatientConfigResponse getConfig();

    @Authorized({"Get Patients"})
    public List<Patient> get(String partialIdentifier, boolean shouldMatchExactPatientId);

    public List<RelationshipType> getByAIsToB(String aIsToB);
}
