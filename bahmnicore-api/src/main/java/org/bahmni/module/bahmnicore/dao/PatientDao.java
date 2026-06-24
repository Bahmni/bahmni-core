package org.bahmni.module.bahmnicore.dao;

import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.RelationshipType;

import java.util.List;
import java.util.function.Supplier;

public interface PatientDao {

    /***
     * Please do not use this method, use the getPatients(PatientSearchParameters ...) instead.
     * @param identifier Patient Identifier
     * @param name Patient Name
     * @param customAttribute Person Attribute
     * @param addressFieldName Person Address Field
     * @param addressFieldValue Person Address Field Value
     * @param length Size of result
     * @param offset Offset for result
     * @param patientAttributes List of person attributes
     * @param programAttribute Program Attribute
     * @param programAttributeField Program Attribute Field
     * @param addressSearchResultFields Search Result Fields from Address
     * @param patientSearchResultFields Search Result Fields from Patient
     * @param loginLocationUuid Login Location UUID
     * @param filterPatientsByLocation Enable Filtering By Location
     * @param filterOnAllIdentifiers Enable filtering on All Identifiers
     * @return List of Patients
     */
    @Deprecated
    public List<PatientResponse> getPatients(String identifier, String name, String customAttribute,
                                             String addressFieldName, String addressFieldValue, Integer length, Integer offset,
                                             String[] patientAttributes, String programAttribute, String programAttributeField,
                                             String[] addressSearchResultFields, String[] patientSearchResultFields, String loginLocationUuid, Boolean filterPatientsByLocation, Boolean filterOnAllIdentifiers);

    List<PatientResponse> getPatientsUsingLuceneSearch(String identifier, String name, String customAttribute,
                                                       String addressFieldName, String addressFieldValue, Integer length,
                                                       Integer offset, String[] customAttributeFields, String programAttributeFieldValue,
                                                       String programAttributeFieldName, String[] addressSearchResultFields,
                                                       String[] patientSearchResultFields, String loginLocationUuid, Boolean filterPatientsByLocation, Boolean filterOnAllIdentifiers);

    public Patient getPatient(String identifier);

    public List<Patient> getPatients(String partialIdentifier, boolean shouldMatchExactPatientId);

    public List<RelationshipType> getByAIsToB(String aIsToB);

    public List<PatientResponse> getPatients(PatientSearchParameters searchParameters, Supplier<Location> visitLocation, Supplier<List<String>> configuredAddressFields);

    public List<String> getConfiguredPatientAddressFields();
}
