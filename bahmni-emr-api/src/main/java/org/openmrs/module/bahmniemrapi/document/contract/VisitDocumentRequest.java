package org.openmrs.module.bahmniemrapi.document.contract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VisitDocumentRequest {
    private String patientUuid;
    private String visitUuid;
    private String visitTypeUuid;
    private Date visitStartDate;
    private Date visitEndDate;
    private String encounterTypeUuid;
    private Date encounterDateTime;
    private List<Document> documents = new ArrayList<>();
    private String providerUuid;
    private String locationUuid;
    private String visitLocationUuid;

    public VisitDocumentRequest() {
    }

    public VisitDocumentRequest(String patientUUID, String visitUuid, String visitTypeUUID, Date visitStartDate,
                                Date visitEndDate, String encounterTypeUUID, Date encounterDateTime,
                                List<Document> documents, String providerUuid, String locationUuid, String visitLocationUuid) {
        this.patientUuid = patientUUID;
        this.visitUuid = visitUuid;
        this.visitTypeUuid = visitTypeUUID;
        this.visitStartDate = visitStartDate;
        this.visitEndDate = visitEndDate;
        this.encounterTypeUuid = encounterTypeUUID;
        this.encounterDateTime = encounterDateTime;
        this.providerUuid = providerUuid;
        this.documents = documents;
        this.locationUuid = locationUuid;
        this.visitLocationUuid = visitLocationUuid;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getVisitTypeUuid() {
        return visitTypeUuid;
    }

    public void setVisitTypeUuid(String visitTypeUuid) {
        this.visitTypeUuid = visitTypeUuid;
    }

    public Date getVisitStartDate() {
        return visitStartDate;
    }

    public void setVisitStartDate(Date visitStartDate) {
        this.visitStartDate = visitStartDate;
    }

    public Date getVisitEndDate() {
        return visitEndDate;
    }

    public void setVisitEndDate(Date visitEndDate) {
        this.visitEndDate = visitEndDate;
    }

    public String getEncounterTypeUuid() {
        return encounterTypeUuid;
    }

    public void setEncounterTypeUuid(String encounterTypeUuid) {
        this.encounterTypeUuid = encounterTypeUuid;
    }

    public Date getEncounterDateTime() {
        return encounterDateTime;
    }

    public void setEncounterDateTime(Date encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public String getProviderUuid() {
        return providerUuid;
    }

    public void setProviderUuid(String providerUuid) {
        this.providerUuid = providerUuid;
    }

    public String getLocationUuid() {
        return locationUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

    public String getVisitLocationUuid() {
        return visitLocationUuid;
    }

    public void setVisitLocationUuid(String visitLocationUuid) {
        this.visitLocationUuid = visitLocationUuid;
    }

}
