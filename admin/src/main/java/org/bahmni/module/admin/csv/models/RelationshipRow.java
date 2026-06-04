package org.bahmni.module.admin.csv.models;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVHeader;

public class RelationshipRow extends CSVEntity {

    @CSVHeader(name = "Registration_Number")
    private String patientIdentifier;

    @CSVHeader(name = "Relationship_Type")
    private String relationshipType;

    @CSVHeader(name = "Related_To_Registration_Number")
    private String patientRelationIdentifier;

    @CSVHeader(name = "Provider_Name")
    private String providerName;

    @CSVHeader(name = "Relationship_StartDate")
    private String startDate;

    @CSVHeader(name = "Relationship_EndDate")
    private String endDate;

    public RelationshipRow(String patientIdentifier, String patientRelationIdentifier, String providerName, String relationshipType, String startDate, String endDate) {
        this.patientIdentifier = patientIdentifier;
        this.patientRelationIdentifier = patientRelationIdentifier;
        this.providerName = providerName;
        this.relationshipType = relationshipType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public RelationshipRow() {
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getPatientRelationIdentifier() {
        return patientRelationIdentifier;
    }

    public void setPatientRelationIdentifier(String patientRelationIdentifier) {
        this.patientRelationIdentifier = patientRelationIdentifier;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
