package org.bahmni.module.bahmnicore.dao;

import org.bahmni.module.bahmnicommons.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.RelationshipType;

import java.util.List;
import java.util.function.Supplier;

public interface PatientDao {

    public Patient getPatient(String identifier);

    public List<Patient> getPatients(String partialIdentifier, boolean shouldMatchExactPatientId);

    public List<RelationshipType> getByAIsToB(String aIsToB);

    public List<PatientResponse> getPatients(PatientSearchParameters searchParameters, Supplier<Location> visitLocation, Supplier<List<String>> configuredAddressFields);

    public List<String> getConfiguredPatientAddressFields();
}
