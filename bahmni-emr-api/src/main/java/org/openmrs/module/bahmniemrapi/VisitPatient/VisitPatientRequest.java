package org.openmrs.module.bahmniemrapi.VisitPatient;

public class VisitPatientRequest {
    private String patient;
    private String visitType;
    private String location;

    public VisitPatientRequest() {
    }

    public VisitPatientRequest(String patient, String visitType, String location) {
        this.patient = patient;
        this.visitType = visitType;
        this.location = location;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
