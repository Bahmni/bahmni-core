package org.bahmni.datamigration.request.patient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.bahmni.datamigration.DataScrub.scrubData;
//
//attributeType: "cd7b242c-9790-11e2-99c1-005056b562c5"
//        name: "caste"
//        value: "bar"
//        1: {attributeType:ce85ffc2-9790-11e2-99c1-005056b562c5, name:class, value:OBC}
//        attributeType: "ce85ffc2-9790-11e2-99c1-005056b562c5"
//        name: "class"
//        value: "OBC"
//        2: {attributeType:cd7be7fe-9790-11e2-99c1-005056b562c5, name:education, value:Uneducated}
//        attributeType: "cd7be7fe-9790-11e2-99c1-005056b562c5"
//        name: "education"
//        value: "Uneducated"
//        3: {attributeType:cd7c99ba-9790-11e2-99c1-005056b562c5, name:occupation, value:Student}
//        attributeType: "cd7c99ba-9790-11e2-99c1-005056b562c5"
//        name: "occupation"
//        value: "Student"
//        4: {attributeType:cd7d5878-9790-11e2-99c1-005056b562c5, name:primaryContact, value:23432}
//        attributeType: "cd7d5878-9790-11e2-99c1-005056b562c5"
//        name: "primaryContact"
//        value: "23432"
//        5: {attributeType:cd7e34e6-9790-11e2-99c1-005056b562c5, name:secondaryContact, value:34324}
//        attributeType: "cd7e34e6-9790-11e2-99c1-005056b562c5"
//        name: "secondaryContact"
//        value: "34324"
//        6: {attributeType:cd7faff6-9790-11e2-99c1-005056b562c5, name:primaryRelative, value:sfgfdg}
//        attributeType: "cd7faff6-9790-11e2-99c1-005056b562c5"
//        name: "primaryRelative"
//        value: "sfgfdg"

public class PatientRequest {
    private List<Name> names = new ArrayList<Name>();
    private LinkedHashMap age;
    private String birthdate;
    private String gender;
    private String identifier;
    private CenterId centerID;
    private List<PatientAddress> addresses = new ArrayList<PatientAddress>();
    private List<PatientAttribute> attributes = new ArrayList<PatientAttribute>();
    private String dateOfRegistration;
    private String balance;

    public void setAge(LinkedHashMap<Object, Object> age) {
        this.age = age;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setCenterID(CenterId centerID) {
        this.centerID = centerID;
    }

    public void setName(String givenName, String familyName) {
        Name name = new Name();
        name.setGivenName(givenName);
        name.setFamilyName(familyName);
        names.add(name);
    }

    public void addPatientAttribute(PatientAttribute patientAttribute) {
        attributes.add(patientAttribute);
    }

    public List<Name> getNames() {
        return names;
    }

    public void setNames(List<Name> names) {
        this.names = names;
    }

    public LinkedHashMap getAge() {
        return age;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getGender() {
        return gender;
    }

    public String getIdentifier() {
        return identifier;
    }

    public CenterId getCenterID() {
        return centerID;
    }

    public List<PatientAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<PatientAddress> addresses) {
        this.addresses = addresses;
    }

    public List<PatientAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PatientAttribute> attributes) {
        this.attributes = attributes;
    }

    public void addPatientAddress(PatientAddress patientAddress) {
        addresses.add(patientAddress);
    }

    public String getDateOfRegistration() {
        return dateOfRegistration;
    }

    public void setDateOfRegistration(String dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = scrubData(balance);
    }
}