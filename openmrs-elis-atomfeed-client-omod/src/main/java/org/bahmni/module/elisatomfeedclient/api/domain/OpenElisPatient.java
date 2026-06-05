package org.bahmni.module.elisatomfeedclient.api.domain;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

public class OpenElisPatient {
    private String patientIdentifier;
    private String firstName;
    private String lastName;
    private String gender;
    private String address1;
    private String address2;
    private String address3;
    private String cityVillage;
    private String countyDistrict;
    private String stateProvince;
    private String dateOfBirth;
    private String healthCenter;
    private String patientUUID;

    private List<OpenElisPatientAttribute> attributes;

    public OpenElisPatient() {
    }

    public OpenElisPatient(String patientIdentifier, String firstName, String lastName, String gender, String address1, String address2, String address3, String cityVillage, String countyDistrict, String stateProvince, String dateOfBirth, String healthCenter, String patientUUID, List<OpenElisPatientAttribute> attributes) {
        this.patientIdentifier = patientIdentifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.cityVillage = cityVillage;
        this.countyDistrict = countyDistrict;
        this.stateProvince = stateProvince;
        this.dateOfBirth = dateOfBirth;
        this.healthCenter = healthCenter;
        this.patientUUID = patientUUID;
        this.attributes = attributes;
    }

    public Date getDateOfBirthAsDate() {
        return  dateOfBirth == null ? null : DateTime.parse(dateOfBirth).toDate();
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getCityVillage() {
        return cityVillage;
    }

    public void setCityVillage(String cityVillage) {
        this.cityVillage = cityVillage;
    }

    public String getCountyDistrict() {
        return countyDistrict;
    }

    public void setCountyDistrict(String countyDistrict) {
        this.countyDistrict = countyDistrict;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getHealthCenter() {
        return healthCenter;
    }

    public void setHealthCenter(String healthCenter) {
        this.healthCenter = healthCenter;
    }

    public String getPatientUUID() {
        return patientUUID;
    }

    public void setPatientUUID(String patientUUID) {
        this.patientUUID = patientUUID;
    }

    public List<OpenElisPatientAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<OpenElisPatientAttribute> attributes) {
        this.attributes = attributes;
    }
}

