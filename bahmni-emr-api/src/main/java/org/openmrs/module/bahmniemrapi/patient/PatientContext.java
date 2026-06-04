package org.openmrs.module.bahmniemrapi.patient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PatientContext {
    private Date birthdate;
    private String givenName;
    private String middleName;
    private String familyName;
    private String identifier;
    private String uuid;
    private String gender;
    private Map<String, Map<String, String>> personAttributes = new HashMap<>();
    private Map<String, Map<String, Object>> programAttributes = new HashMap<>();
    private Map<String, String> additionalPatientIdentifiers = new HashMap<>();

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthDate) {
        this.birthdate = birthDate;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Map<String, Map<String, String>> getPersonAttributes() {
        return personAttributes;
    }

    public void setPersonAttributes(Map<String, Map<String, String>> attributes) {
        this.personAttributes = attributes;
    }

    public void addPersonAttribute(String key, String description, String value) {
        HashMap<String, String> responseValue = new HashMap<>();
        responseValue.put("value", value);
        responseValue.put("description", description);
        this.personAttributes.put(key, responseValue);
    }

    public Map<String, Map<String, Object>> getProgramAttributes() {
        return programAttributes;
    }

    public void setProgramAttributes(Map<String, Map<String, Object>> programAttributes) {
        this.programAttributes = programAttributes;
    }

    public void addProgramAttribute(String key, String description, Object value) {
        HashMap<String, Object> responseValue = new HashMap<>();
        responseValue.put("value", value);
        responseValue.put("description", description);
        this.programAttributes.put(key, responseValue);
    }

    public Map<String, String> getAdditionalPatientIdentifiers() {
        return additionalPatientIdentifiers;
    }

    public void setAdditionalPatientIdentifiers(Map<String, String> additionalPatientIdentifiers) {
        this.additionalPatientIdentifiers = additionalPatientIdentifiers;
    }

    public void addAdditionalPatientIdentifier(String type, String value) {
        this.additionalPatientIdentifiers.put(type, value);
    }
}
